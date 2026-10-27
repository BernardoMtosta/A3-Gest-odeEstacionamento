package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Uma vaga do mapa. O campo {@code veiculo} vem nulo quando a vaga nao esta ocupada.
 */
@Schema(description = "Vaga do estacionamento")
public record VagaResponse(

        @Schema(description = "Identificador da vaga (letra da fila + numero).", example = "A1")
        String id,

        @Schema(description = "Letra da fila.", example = "A")
        String fila,

        @Schema(description = "Numero da vaga dentro da fila, comecando em 1.", example = "1")
        int numero,

        @Schema(description = "Situacao atual da vaga.",
                allowableValues = {"LIVRE", "OCUPADA", "MANUTENCAO"}, example = "OCUPADA")
        String status,

        @Schema(description = "Veiculo estacionado, ou null se a vaga nao estiver ocupada.",
                nullable = true)
        VeiculoEstacionadoResponse veiculo) {
}
