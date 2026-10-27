package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Resposta de POST /api/saidas (HTTP 200), com o valor a pagar.
 */
@Schema(description = "Confirmacao da saida, com o valor a pagar")
public record SaidaResponse(

        @Schema(example = "ABC1D23")
        String placa,

        @Schema(description = "Vaga que o veiculo ocupava e acaba de liberar.", example = "A1")
        String idVaga,

        @Schema(example = "2026-10-19T08:30:00")
        LocalDateTime horaEntrada,

        @Schema(example = "19/10/2026 08:30")
        String horaEntradaFormatada,

        @Schema(example = "2026-10-19T11:45:00")
        LocalDateTime horaSaida,

        @Schema(example = "19/10/2026 11:45")
        String horaSaidaFormatada,

        @Schema(example = "195")
        long permanenciaMinutos,

        @Schema(example = "3h15min")
        String permanenciaFormatada,

        @Schema(description = "Valor cobrado: primeira hora + horas adicionais, "
                + "com a fracao de hora arredondada para cima.", example = "14.0")
        double valor,

        @Schema(description = "Texto pronto para exibir na tela.",
                example = "Saida de ABC1D23 (vaga A1). Permanencia: 3h15min. Valor a pagar: R$ 14,00.")
        String mensagem) {
}
