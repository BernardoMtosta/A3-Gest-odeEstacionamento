package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Uma linha do historico (veiculo que ja saiu).
 */
@Schema(description = "Registro de um veiculo que ja saiu")
public record RegistroResponse(

        @Schema(example = "ABC1D23")
        String placa,

        @Schema(example = "A1")
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

        @Schema(example = "14.0")
        double valor) {
}
