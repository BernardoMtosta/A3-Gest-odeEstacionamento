package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta de GET e PUT /api/configuracao. Util para o front montar o mapa e
 * exibir a tabela de precos sem ter os valores chumbados no codigo.
 */
@Schema(description = "Configuracao atual do estacionamento")
public record ConfiguracaoResponse(

        @Schema(example = "3")
        int filas,

        @Schema(example = "4")
        int vagasPorFila,

        @Schema(example = "12")
        int totalVagas,

        @Schema(example = "5.00")
        double valorPrimeiraHora,

        @Schema(example = "3.00")
        double valorHoraAdicional,

        @Schema(description = "Formato de data aceito nos campos dataHora.",
                example = "dd/MM/yyyy HH:mm")
        String formatoDataHora) {
}
