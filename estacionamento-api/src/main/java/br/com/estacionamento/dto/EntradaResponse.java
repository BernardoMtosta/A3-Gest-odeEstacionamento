package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Resposta de POST /api/entradas (HTTP 201).
 */
@Schema(description = "Confirmacao da entrada registrada")
public record EntradaResponse(

        @Schema(example = "ABC1D23")
        String placa,

        @Schema(description = "Vaga onde o veiculo foi estacionado.", example = "A1")
        String idVaga,

        @Schema(example = "2026-10-19T08:30:00")
        LocalDateTime horaEntrada,

        @Schema(example = "19/10/2026 08:30")
        String horaEntradaFormatada,

        @Schema(description = "Texto pronto para exibir na tela.",
                example = "Veiculo ABC1D23 estacionado na vaga A1.")
        String mensagem) {
}
