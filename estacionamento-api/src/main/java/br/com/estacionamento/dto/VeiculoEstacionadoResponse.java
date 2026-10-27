package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Veiculo que esta no estacionamento agora, com a permanencia e o valor
 * calculados ate este instante.
 */
@Schema(description = "Veiculo atualmente estacionado")
public record VeiculoEstacionadoResponse(

        @Schema(example = "ABC1D23")
        String placa,

        @Schema(description = "Vaga em que o veiculo esta.", example = "A1")
        String idVaga,

        @Schema(description = "Entrada em ISO-8601.", example = "2026-10-19T08:30:00")
        LocalDateTime horaEntrada,

        @Schema(description = "Entrada em dd/MM/yyyy HH:mm.", example = "19/10/2026 08:30")
        String horaEntradaFormatada,

        @Schema(description = "Minutos decorridos desde a entrada ate agora.", example = "195")
        long permanenciaMinutos,

        @Schema(description = "Permanencia legivel ate agora.", example = "3h15min")
        String permanenciaFormatada,

        @Schema(description = "Quanto seria cobrado se o veiculo saisse agora.", example = "14.0")
        double valorParcial) {
}
