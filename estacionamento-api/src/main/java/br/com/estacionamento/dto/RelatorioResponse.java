package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Resposta de GET /api/relatorio. Substitui o {@code exibirRelatorio()} que
 * desenhava o mapa no console: aqui o front recebe os numeros e a grade para
 * desenhar como quiser.
 */
@Schema(description = "Resumo de ocupacao e mapa das vagas")
public record RelatorioResponse(

        @Schema(description = "Total de vagas do estacionamento.", example = "12")
        int totalVagas,

        @Schema(example = "7")
        int livres,

        @Schema(example = "4")
        int ocupadas,

        @Schema(example = "1")
        int emManutencao,

        @Schema(description = "Vagas que podem ser usadas (total menos as em manutencao).", example = "11")
        int disponiveis,

        @Schema(description = "Percentual de ocupacao sobre as vagas disponiveis.", example = "36.4")
        double taxaOcupacao,

        @Schema(description = "Quantidade de filas.", example = "3")
        int filas,

        @Schema(description = "Vagas por fila.", example = "4")
        int vagasPorFila,

        @Schema(description = "Mapa do estacionamento: uma lista por fila, na ordem A, B, C...")
        List<List<VagaResponse>> grade) {
}
