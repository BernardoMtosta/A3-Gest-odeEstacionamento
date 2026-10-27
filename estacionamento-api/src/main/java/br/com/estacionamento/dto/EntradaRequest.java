package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo de POST /api/entradas.
 */
@Schema(description = "Dados para registrar a entrada de um veiculo")
public record EntradaRequest(

        @Schema(description = "Placa do veiculo. Espacos e hifens sao removidos e o texto vira maiusculo.",
                example = "ABC1D23")
        @NotBlank(message = "A placa e obrigatoria.")
        @Size(max = 10, message = "A placa deve ter no maximo 10 caracteres.")
        String placa,

        @Schema(description = "Data e hora da entrada. Aceita \"dd/MM/yyyy HH:mm\" ou ISO-8601. "
                + "Ausente ou nulo significa agora.",
                example = "19/10/2026 08:30",
                nullable = true)
        String dataHora) {
}
