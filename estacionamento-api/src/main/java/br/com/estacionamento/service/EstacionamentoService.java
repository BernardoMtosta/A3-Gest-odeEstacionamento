package br.com.estacionamento.service;

import br.com.estacionamento.config.EstacionamentoProperties;
import br.com.estacionamento.domain.Estacionamento;
import br.com.estacionamento.domain.Registro;
import br.com.estacionamento.domain.Vaga;
import br.com.estacionamento.domain.Veiculo;
import br.com.estacionamento.dto.ConfiguracaoRequest;
import br.com.estacionamento.dto.ConfiguracaoResponse;
import br.com.estacionamento.dto.EntradaRequest;
import br.com.estacionamento.dto.EntradaResponse;
import br.com.estacionamento.dto.HistoricoResponse;
import br.com.estacionamento.dto.RegistroResponse;
import br.com.estacionamento.dto.RelatorioResponse;
import br.com.estacionamento.dto.SaidaRequest;
import br.com.estacionamento.dto.SaidaResponse;
import br.com.estacionamento.dto.VagaResponse;
import br.com.estacionamento.dto.VeiculoEstacionadoResponse;
import br.com.estacionamento.exception.RegraDeNegocioException;
import br.com.estacionamento.util.TempoUtil;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Orquestra as chamadas HTTP: normaliza a entrada, chama o dominio e converte
 * o resultado em DTO.
 *
 * <p>O estado vive aqui, num unico objeto {@link Estacionamento} em memoria.
 * Como o Spring atende varias requisicoes ao mesmo tempo e as estruturas
 * usadas (HashMap, LinkedList, matriz) nao sao thread-safe, todos os metodos
 * publicos sao {@code synchronized}.</p>
 */
@Service
public class EstacionamentoService {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    private Estacionamento estacionamento;

    public EstacionamentoService(EstacionamentoProperties propriedades) {
        this.estacionamento = new Estacionamento(
                propriedades.filas(),
                propriedades.vagasPorFila(),
                propriedades.valorPrimeiraHora(),
                propriedades.valorHoraAdicional());
    }

    // ------------------------------------------------------------------
    // Entradas e saidas
    // ------------------------------------------------------------------

    /** Menu 1 do programa original: entrada de veiculo. */
    public synchronized EntradaResponse registrarEntrada(EntradaRequest requisicao) {
        String placa = normalizarPlaca(requisicao.placa());
        LocalDateTime dataHora = TempoUtil.paraDataHora(requisicao.dataHora());

        Vaga vaga = estacionamento.registrarEntrada(placa, dataHora);

        return new EntradaResponse(
                placa,
                vaga.getId(),
                dataHora,
                TempoUtil.formatar(dataHora),
                "Veiculo " + placa + " estacionado na vaga " + vaga.getId() + ".");
    }

    /** Menu 3 do programa original: saida de veiculo. */
    public synchronized SaidaResponse registrarSaida(SaidaRequest requisicao) {
        String placa = normalizarPlaca(requisicao.placa());
        LocalDateTime dataHora = TempoUtil.paraDataHora(requisicao.dataHora());

        Registro registro = estacionamento.registrarSaida(placa, dataHora);
        Veiculo veiculo = registro.veiculo();
        Duration permanencia = TempoUtil.permanenciaEntre(veiculo.getHoraEntrada(), veiculo.getHoraSaida());
        String permanenciaTexto = TempoUtil.formatarPermanencia(permanencia);

        return new SaidaResponse(
                veiculo.getPlaca(),
                registro.idVaga(),
                veiculo.getHoraEntrada(),
                TempoUtil.formatar(veiculo.getHoraEntrada()),
                veiculo.getHoraSaida(),
                TempoUtil.formatar(veiculo.getHoraSaida()),
                permanencia.toMinutes(),
                permanenciaTexto,
                registro.valor(),
                String.format(BR, "Saida de %s (vaga %s). Permanencia: %s. Valor a pagar: R$ %.2f.",
                        veiculo.getPlaca(), registro.idVaga(), permanenciaTexto, registro.valor()));
    }

    /** Menu 2 do programa original: buscar veiculo por placa. */
    public synchronized VeiculoEstacionadoResponse buscarVeiculo(String placa) {
        Vaga vaga = estacionamento.exigirVagaDaPlaca(normalizarPlaca(placa));
        return paraVeiculoResponse(vaga);
    }

    /** Lista todos os veiculos que estao no estacionamento agora. */
    public synchronized List<VeiculoEstacionadoResponse> listarVeiculos() {
        List<VeiculoEstacionadoResponse> veiculos = new ArrayList<>();
        for (Vaga vaga : estacionamento.listarVagasOcupadas()) {
            veiculos.add(paraVeiculoResponse(vaga));
        }
        return veiculos;
    }

    // ------------------------------------------------------------------
    // Vagas
    // ------------------------------------------------------------------

    /** Todas as vagas, opcionalmente filtradas por status. */
    public synchronized List<VagaResponse> listarVagas(String status) {
        Vaga.Status filtro = converterStatus(status);
        List<VagaResponse> resposta = new ArrayList<>();
        for (Vaga vaga : estacionamento.listarVagas()) {
            if (filtro == null || vaga.getStatus() == filtro) {
                resposta.add(paraVagaResponse(vaga));
            }
        }
        return resposta;
    }

    public synchronized VagaResponse buscarVaga(String idVaga) {
        return paraVagaResponse(estacionamento.exigirVagaPorId(idVaga));
    }

    /** Menu 6 do programa original: colocar ou tirar a vaga da manutencao. */
    public synchronized VagaResponse definirManutencao(String idVaga, Boolean emManutencao) {
        return paraVagaResponse(estacionamento.definirManutencao(idVaga, emManutencao));
    }

    // ------------------------------------------------------------------
    // Relatorio, historico e configuracao
    // ------------------------------------------------------------------

    /** Menu 4 do programa original: relatorio de ocupacao. */
    public synchronized RelatorioResponse gerarRelatorio() {
        int total = estacionamento.getTotalVagas();
        int livres = (int) estacionamento.contar(Vaga.Status.LIVRE);
        int ocupadas = (int) estacionamento.contar(Vaga.Status.OCUPADA);
        int emManutencao = (int) estacionamento.contar(Vaga.Status.MANUTENCAO);
        int disponiveis = total - emManutencao;
        double taxaOcupacao = disponiveis == 0 ? 0 : arredondar(ocupadas * 100.0 / disponiveis, 1);

        List<List<VagaResponse>> grade = new ArrayList<>();
        for (List<Vaga> fila : estacionamento.listarVagasPorFila()) {
            List<VagaResponse> linha = new ArrayList<>();
            for (Vaga vaga : fila) {
                linha.add(paraVagaResponse(vaga));
            }
            grade.add(linha);
        }

        return new RelatorioResponse(total, livres, ocupadas, emManutencao, disponiveis, taxaOcupacao,
                estacionamento.getQuantidadeFilas(), estacionamento.getVagasPorFila(), grade);
    }

    /** Menu 5 do programa original: historico. */
    public synchronized HistoricoResponse gerarHistorico() {
        List<RegistroResponse> registros = new ArrayList<>();
        for (Registro registro : estacionamento.listarHistorico()) {
            Veiculo veiculo = registro.veiculo();
            Duration permanencia = TempoUtil.permanenciaEntre(veiculo.getHoraEntrada(), veiculo.getHoraSaida());
            registros.add(new RegistroResponse(
                    veiculo.getPlaca(),
                    registro.idVaga(),
                    veiculo.getHoraEntrada(),
                    TempoUtil.formatar(veiculo.getHoraEntrada()),
                    veiculo.getHoraSaida(),
                    TempoUtil.formatar(veiculo.getHoraSaida()),
                    permanencia.toMinutes(),
                    TempoUtil.formatarPermanencia(permanencia),
                    registro.valor()));
        }
        return new HistoricoResponse(registros, registros.size(), arredondar(estacionamento.totalArrecadado(), 2));
    }

    public synchronized ConfiguracaoResponse obterConfiguracao() {
        return new ConfiguracaoResponse(
                estacionamento.getQuantidadeFilas(),
                estacionamento.getVagasPorFila(),
                estacionamento.getTotalVagas(),
                estacionamento.getValorPrimeiraHora(),
                estacionamento.getValorHoraAdicional(),
                "dd/MM/yyyy HH:mm");
    }

    /**
     * Recria o estacionamento. Apaga vagas, veiculos estacionados e historico.
     * Util para reiniciar a demonstracao sem derrubar a aplicacao.
     */
    public synchronized ConfiguracaoResponse reconfigurar(ConfiguracaoRequest requisicao) {
        double primeiraHora = requisicao.valorPrimeiraHora() != null
                ? requisicao.valorPrimeiraHora()
                : estacionamento.getValorPrimeiraHora();
        double horaAdicional = requisicao.valorHoraAdicional() != null
                ? requisicao.valorHoraAdicional()
                : estacionamento.getValorHoraAdicional();

        this.estacionamento = new Estacionamento(
                requisicao.filas(), requisicao.vagasPorFila(), primeiraHora, horaAdicional);

        return obterConfiguracao();
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /** "abc-1d23" vira "ABC1D23". */
    private String normalizarPlaca(String placa) {
        if (placa == null) {
            throw new RegraDeNegocioException("PLACA_INVALIDA", "Informe uma placa valida.");
        }
        String normalizada = placa.trim().toUpperCase(BR).replace("-", "").replace(" ", "");
        if (normalizada.isEmpty()) {
            throw new RegraDeNegocioException("PLACA_INVALIDA", "Informe uma placa valida.");
        }
        return normalizada;
    }

    private Vaga.Status converterStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return Vaga.Status.valueOf(status.trim().toUpperCase(BR));
        } catch (IllegalArgumentException e) {
            throw new RegraDeNegocioException("STATUS_INVALIDO",
                    "Status invalido: \"" + status + "\". Use LIVRE, OCUPADA ou MANUTENCAO.");
        }
    }

    private VagaResponse paraVagaResponse(Vaga vaga) {
        return new VagaResponse(
                vaga.getId(),
                vaga.getLetraFila(),
                vaga.getNumero(),
                vaga.getStatus().name(),
                vaga.getStatus() == Vaga.Status.OCUPADA ? paraVeiculoResponse(vaga) : null);
    }

    private VeiculoEstacionadoResponse paraVeiculoResponse(Vaga vaga) {
        Veiculo veiculo = vaga.getVeiculo();
        Duration permanencia = TempoUtil.permanenciaEntre(veiculo.getHoraEntrada(), LocalDateTime.now());
        return new VeiculoEstacionadoResponse(
                veiculo.getPlaca(),
                vaga.getId(),
                veiculo.getHoraEntrada(),
                TempoUtil.formatar(veiculo.getHoraEntrada()),
                permanencia.toMinutes(),
                TempoUtil.formatarPermanencia(permanencia),
                arredondar(estacionamento.calcularValor(permanencia), 2));
    }

    private double arredondar(double valor, int casas) {
        double fator = Math.pow(10, casas);
        return Math.round(valor * fator) / fator;
    }
}
