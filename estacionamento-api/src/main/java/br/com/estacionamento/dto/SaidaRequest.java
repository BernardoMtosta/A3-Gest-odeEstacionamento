package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo de POST /api/saidas.
 */
@Schema(description = "Dados para registrar a saida de um veiculo")
public record SaidaRequest(

        @Schema(description = "Placa do veiculo que esta estacionado.", example = "ABC1D23")
        @NotBlank(message = "A placa e obrigatoria.")
        @Size(max = 10, message = "A placa deve ter no maximo 10 caracteres.")
        String placa,

        @Schema(description = "Data e hora da saida. Aceita \"dd/MM/yyyy HH:mm\" ou ISO-8601. "
                + "Ausente ou nulo significa agora. Nao pode ser anterior a entrada.",
                example = "19/10/2026 11:45",
                nullable = true)
        String dataHora) {
}
