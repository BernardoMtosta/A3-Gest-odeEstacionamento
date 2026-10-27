package br.com.estacionamento.domain;

import br.com.estacionamento.exception.ConflitoException;
import br.com.estacionamento.exception.RecursoNaoEncontradoException;
import br.com.estacionamento.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes da regra de negocio, sem subir o Spring.
 */
class EstacionamentoTest {

    private static final LocalDateTime ENTRADA = LocalDateTime.of(2026, 10, 19, 8, 30);

    private Estacionamento estacionamento;

    @BeforeEach
    void preparar() {
        // 1 fila com 2 vagas: A1 e A2
        estacionamento = new Estacionamento(1, 2, 5.00, 3.00);
    }

    @Test
    @DisplayName("entrada ocupa a primeira vaga livre")
    void entradaOcupaPrimeiraVagaLivre() {
        Vaga vaga = estacionamento.registrarEntrada("ABC1D23", ENTRADA);

        assertEquals("A1", vaga.getId());
        assertEquals(Vaga.Status.OCUPADA, vaga.getStatus());
        assertEquals("ABC1D23", vaga.getVeiculo().getPlaca());
    }

    @Test
    @DisplayName("placa vazia e recusada")
    void placaVaziaERecusada() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> estacionamento.registrarEntrada("  ", ENTRADA));

        assertEquals("PLACA_INVALIDA", erro.getCodigo());
    }

    @Test
    @DisplayName("mesma placa nao entra duas vezes")
    void mesmaPlacaNaoEntraDuasVezes() {
        estacionamento.registrarEntrada("ABC1D23", ENTRADA);

        ConflitoException erro = assertThrows(ConflitoException.class,
                () -> estacionamento.registrarEntrada("ABC1D23", ENTRADA));

        assertEquals("VEICULO_JA_ESTACIONADO", erro.getCodigo());
    }

    @Test
    @DisplayName("estacionamento lotado recusa a entrada")
    void estacionamentoLotadoRecusaEntrada() {
        estacionamento.registrarEntrada("AAA0A00", ENTRADA);
        estacionamento.registrarEntrada("BBB1B11", ENTRADA);

        ConflitoException erro = assertThrows(ConflitoException.class,
                () -> estacionamento.registrarEntrada("CCC2C22", ENTRADA));

        assertEquals("ESTACIONAMENTO_LOTADO", erro.getCodigo());
    }

    @Test
    @DisplayName("saida libera a vaga, cobra o valor e grava no historico")
    void saidaLiberaVagaECobra() {
        estacionamento.registrarEntrada("ABC1D23", ENTRADA);

        // 3h15min -> arredonda para 4 horas -> 5,00 + 3 x 3,00 = 14,00
        Registro registro = estacionamento.registrarSaida("ABC1D23", ENTRADA.plusMinutes(195));

        assertEquals("A1", registro.idVaga());
        assertEquals(14.00, registro.valor(), 0.001);
        assertEquals(Vaga.Status.LIVRE, estacionamento.exigirVagaPorId("A1").getStatus());
        assertNull(estacionamento.exigirVagaPorId("A1").getVeiculo());
        assertEquals(1, estacionamento.listarHistorico().size());
        assertEquals(14.00, estacionamento.totalArrecadado(), 0.001);
    }

    @Test
    @DisplayName("saida de placa que nao esta estacionada devolve 404")
    void saidaDePlacaInexistente() {
        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> estacionamento.registrarSaida("XYZ9Z99", ENTRADA));

        assertEquals("VEICULO_NAO_ENCONTRADO", erro.getCodigo());
        assertEquals(404, erro.getStatus());
    }

    @Test
    @DisplayName("saida nao pode ser anterior a entrada")
    void saidaAnteriorAEntrada() {
        estacionamento.registrarEntrada("ABC1D23", ENTRADA);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> estacionamento.registrarSaida("ABC1D23", ENTRADA.minusMinutes(10)));

        assertEquals("SAIDA_ANTES_DA_ENTRADA", erro.getCodigo());
    }

    @Test
    @DisplayName("tabela de precos: minimo de uma hora e fracao arredondada para cima")
    void tabelaDePrecos() {
        assertEquals(5.00, estacionamento.calcularValor(Duration.ofMinutes(1)), 0.001);
        assertEquals(5.00, estacionamento.calcularValor(Duration.ofMinutes(60)), 0.001);
        assertEquals(8.00, estacionamento.calcularValor(Duration.ofMinutes(61)), 0.001);
        assertEquals(11.00, estacionamento.calcularValor(Duration.ofMinutes(150)), 0.001);
    }

    @Test
    @DisplayName("manutencao alterna quando a vaga esta livre")
    void manutencaoAlterna() {
        assertEquals(Vaga.Status.MANUTENCAO, estacionamento.definirManutencao("A1", null).getStatus());
        assertEquals(Vaga.Status.LIVRE, estacionamento.definirManutencao("A1", null).getStatus());
        assertEquals(Vaga.Status.MANUTENCAO, estacionamento.definirManutencao("A1", true).getStatus());
        assertEquals(Vaga.Status.LIVRE, estacionamento.definirManutencao("A1", false).getStatus());
    }

    @Test
    @DisplayName("vaga ocupada nao entra em manutencao")
    void vagaOcupadaNaoEntraEmManutencao() {
        estacionamento.registrarEntrada("ABC1D23", ENTRADA);

        ConflitoException erro = assertThrows(ConflitoException.class,
                () -> estacionamento.definirManutencao("A1", true));

        assertEquals("VAGA_OCUPADA", erro.getCodigo());
    }

    @Test
    @DisplayName("vaga em manutencao nao recebe veiculo")
    void vagaEmManutencaoNaoRecebeVeiculo() {
        estacionamento.definirManutencao("A1", true);

        Vaga vaga = estacionamento.registrarEntrada("ABC1D23", ENTRADA);

        assertEquals("A2", vaga.getId());
    }

    @Test
    @DisplayName("id de vaga inexistente devolve 404")
    void idDeVagaInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> estacionamento.exigirVagaPorId("Z9"));
        assertThrows(RecursoNaoEncontradoException.class, () -> estacionamento.exigirVagaPorId("A9"));
        assertThrows(RecursoNaoEncontradoException.class, () -> estacionamento.exigirVagaPorId("xx"));
    }

    @Test
    @DisplayName("contagem por status reflete o estado atual")
    void contagemPorStatus() {
        estacionamento.registrarEntrada("ABC1D23", ENTRADA);

        assertEquals(1, estacionamento.contar(Vaga.Status.OCUPADA));
        assertEquals(1, estacionamento.contar(Vaga.Status.LIVRE));
        assertEquals(0, estacionamento.contar(Vaga.Status.MANUTENCAO));
        assertTrue(estacionamento.buscarVagaDaPlaca("ABC1D23").isPresent());
    }
}
