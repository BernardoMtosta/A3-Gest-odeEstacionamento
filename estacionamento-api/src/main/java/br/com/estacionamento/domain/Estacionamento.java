package br.com.estacionamento.domain;

import br.com.estacionamento.exception.ConflitoException;
import br.com.estacionamento.exception.RecursoNaoEncontradoException;
import br.com.estacionamento.exception.RegraDeNegocioException;
import br.com.estacionamento.util.TempoUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Regras do estacionamento. Mesma logica e mesmas estruturas de dados do
 * trabalho original; o que mudou foi a forma de responder:
 *
 * <ul>
 *   <li>antes cada metodo imprimia com {@code System.out.println};</li>
 *   <li>agora cada metodo devolve o objeto resultante ou lanca uma excecao,
 *       que a camada web traduz em status HTTP e JSON.</li>
 * </ul>
 *
 * <p>Estruturas mantidas: matriz {@code Vaga[][]} (acesso O(1)),
 * {@code HashMap} placa -> vaga (O(1) medio) e {@code LinkedList} de historico
 * (insercao no fim O(1)).</p>
 */
public class Estacionamento {

    private final Vaga[][] vagas;
    private final HashMap<String, Vaga> tabelaPlacas = new HashMap<>();
    private final LinkedList<Registro> historico = new LinkedList<>();

    private final int quantidadeFilas;
    private final int vagasPorFila;
    private final double valorPrimeiraHora;
    private final double valorHoraAdicional;

    public Estacionamento(int quantidadeFilas, int vagasPorFila,
                          double valorPrimeiraHora, double valorHoraAdicional) {
        this.quantidadeFilas = quantidadeFilas;
        this.vagasPorFila = vagasPorFila;
        this.valorPrimeiraHora = valorPrimeiraHora;
        this.valorHoraAdicional = valorHoraAdicional;
        this.vagas = new Vaga[quantidadeFilas][vagasPorFila];
        for (int fila = 0; fila < quantidadeFilas; fila++) {
            for (int numero = 0; numero < vagasPorFila; numero++) {
                vagas[fila][numero] = new Vaga(fila, numero);
            }
        }
    }

    // ------------------------------------------------------------------
    // Movimentacao de veiculos
    // ------------------------------------------------------------------

    /**
     * Estaciona o veiculo na primeira vaga livre encontrada.
     *
     * @return a vaga ocupada
     * @throws RegraDeNegocioException placa vazia
     * @throws ConflitoException placa ja estacionada ou estacionamento lotado
     */
    public Vaga registrarEntrada(String placa, LocalDateTime dataHora) {
        if (placa == null || placa.isBlank()) {
            throw new RegraDeNegocioException("PLACA_INVALIDA", "Informe uma placa valida.");
        }
        Vaga jaEstacionado = tabelaPlacas.get(placa);
        if (jaEstacionado != null) {
            throw new ConflitoException("VEICULO_JA_ESTACIONADO",
                    "Veiculo " + placa + " ja esta no estacionamento, na vaga " + jaEstacionado.getId() + ".");
        }
        for (Vaga[] fila : vagas) {
            for (Vaga vaga : fila) {
                if (vaga.getStatus() == Vaga.Status.LIVRE) {
                    vaga.ocupar(new Veiculo(placa, dataHora));
                    tabelaPlacas.put(placa, vaga);
                    return vaga;
                }
            }
        }
        throw new ConflitoException("ESTACIONAMENTO_LOTADO", "Estacionamento lotado: nenhuma vaga livre.");
    }

    /**
     * Registra a saida, libera a vaga e grava o registro no historico.
     *
     * @return o registro gravado (veiculo, vaga usada e valor cobrado)
     * @throws RecursoNaoEncontradoException placa nao esta estacionada
     * @throws RegraDeNegocioException saida anterior a entrada
     */
    public Registro registrarSaida(String placa, LocalDateTime dataHora) {
        Vaga vaga = exigirVagaDaPlaca(placa);
        Veiculo veiculo = vaga.getVeiculo();

        if (dataHora.isBefore(veiculo.getHoraEntrada())) {
            throw new RegraDeNegocioException("SAIDA_ANTES_DA_ENTRADA",
                    "A saida nao pode ser anterior a entrada ("
                            + veiculo.getHoraEntrada().format(TempoUtil.FORMATO_DATA_HORA) + ").");
        }

        veiculo.setHoraSaida(dataHora);
        double valor = calcularValor(Duration.between(veiculo.getHoraEntrada(), dataHora));
        String idVaga = vaga.getId();

        vaga.liberar();
        tabelaPlacas.remove(placa);

        Registro registro = new Registro(veiculo, idVaga, valor);
        historico.add(registro);
        return registro;
    }

    /** Vaga em que a placa esta estacionada, ou vazio se nao estiver. */
    public Optional<Vaga> buscarVagaDaPlaca(String placa) {
        return Optional.ofNullable(placa == null ? null : tabelaPlacas.get(placa));
    }

    /** Igual ao anterior, mas lanca 404 quando a placa nao esta no estacionamento. */
    public Vaga exigirVagaDaPlaca(String placa) {
        return buscarVagaDaPlaca(placa).orElseThrow(() -> new RecursoNaoEncontradoException(
                "VEICULO_NAO_ENCONTRADO", "Veiculo " + placa + " nao esta no estacionamento."));
    }

    /** Vagas ocupadas no momento, na ordem da matriz. */
    public List<Vaga> listarVagasOcupadas() {
        List<Vaga> ocupadas = new ArrayList<>();
        for (Vaga[] fila : vagas) {
            for (Vaga vaga : fila) {
                if (vaga.getStatus() == Vaga.Status.OCUPADA) {
                    ocupadas.add(vaga);
                }
            }
        }
        return ocupadas;
    }

    // ------------------------------------------------------------------
    // Vagas
    // ------------------------------------------------------------------

    /** Converte "B3" nos indices da matriz. Vazio se o id nao existir. */
    public Optional<Vaga> buscarVagaPorId(String idVaga) {
        if (idVaga == null) {
            return Optional.empty();
        }
        String id = idVaga.trim().toUpperCase();
        if (id.length() < 2) {
            return Optional.empty();
        }
        int fila = id.charAt(0) - 'A';
        int numero;
        try {
            numero = Integer.parseInt(id.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        if (fila < 0 || fila >= vagas.length || numero < 0 || numero >= vagas[fila].length) {
            return Optional.empty();
        }
        return Optional.of(vagas[fila][numero]);
    }

    public Vaga exigirVagaPorId(String idVaga) {
        return buscarVagaPorId(idVaga).orElseThrow(() -> new RecursoNaoEncontradoException(
                "VAGA_NAO_ENCONTRADA", "Vaga " + idVaga + " nao existe."));
    }

    /**
     * Coloca ou tira a vaga da manutencao.
     *
     * @param emManutencao {@code null} alterna o estado atual; {@code true} ou
     *                     {@code false} define explicitamente
     * @throws ConflitoException vaga ocupada
     */
    public Vaga definirManutencao(String idVaga, Boolean emManutencao) {
        Vaga vaga = exigirVagaPorId(idVaga);
        if (vaga.getStatus() == Vaga.Status.OCUPADA) {
            throw new ConflitoException("VAGA_OCUPADA",
                    "Vaga " + vaga.getId() + " esta ocupada; registre a saida do veiculo antes.");
        }
        if (emManutencao == null) {
            vaga.alternarManutencao();
        } else {
            vaga.definirManutencao(emManutencao);
        }
        return vaga;
    }

    /** Todas as vagas em uma lista unica, na ordem da matriz. */
    public List<Vaga> listarVagas() {
        List<Vaga> lista = new ArrayList<>();
        for (Vaga[] fila : vagas) {
            lista.addAll(List.of(fila));
        }
        return lista;
    }

    /** Vagas agrupadas por fila, preservando o formato da matriz. */
    public List<List<Vaga>> listarVagasPorFila() {
        List<List<Vaga>> grade = new ArrayList<>();
        for (Vaga[] fila : vagas) {
            grade.add(List.of(fila));
        }
        return grade;
    }

    public long contar(Vaga.Status status) {
        long total = 0;
        for (Vaga[] fila : vagas) {
            for (Vaga vaga : fila) {
                if (vaga.getStatus() == status) {
                    total++;
                }
            }
        }
        return total;
    }

    // ------------------------------------------------------------------
    // Valores e historico
    // ------------------------------------------------------------------

    /**
     * Primeira hora + horas adicionais, sempre arredondando a fracao para cima
     * e cobrando no minimo uma hora. Formula original: {@code (minutos + 59) / 60}.
     */
    public double calcularValor(Duration permanencia) {
        long minutos = permanencia.isNegative() ? 0 : permanencia.toMinutes();
        long horas = Math.max(1, (minutos + 59) / 60);
        return valorPrimeiraHora + (horas - 1) * valorHoraAdicional;
    }

    /** Copia do historico, do mais antigo para o mais recente. */
    public List<Registro> listarHistorico() {
        return new ArrayList<>(historico);
    }

    public double totalArrecadado() {
        double total = 0;
        for (Registro registro : historico) {
            total += registro.valor();
        }
        return total;
    }

    // ------------------------------------------------------------------
    // Getters de configuracao
    // ------------------------------------------------------------------

    public int getQuantidadeFilas() {
        return quantidadeFilas;
    }

    public int getVagasPorFila() {
        return vagasPorFila;
    }

    public int getTotalVagas() {
        return quantidadeFilas * vagasPorFila;
    }

    public double getValorPrimeiraHora() {
        return valorPrimeiraHora;
    }

    public double getValorHoraAdicional() {
        return valorHoraAdicional;
    }
}
