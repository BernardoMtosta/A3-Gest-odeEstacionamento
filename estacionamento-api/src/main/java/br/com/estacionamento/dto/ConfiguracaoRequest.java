package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Corpo de PUT /api/configuracao. Recria o estacionamento do zero:
 * apaga vagas, veiculos estacionados e historico.
 */
@Schema(description = "Nova configuracao do estacionamento (recria tudo do zero)")
public record ConfiguracaoRequest(

        @Schema(description = "Quantidade de filas (A, B, C...). De 1 a 26.", example = "3")
        @NotNull(message = "Informe a quantidade de filas.")
        @Min(value = 1, message = "O estacionamento precisa de pelo menos 1 fila.")
        @Max(value = 26, message = "O maximo e 26 filas (letras A ate Z).")
        Integer filas,

        @Schema(description = "Quantidade de vagas em cada fila. De 1 a 99.", example = "4")
        @NotNull(message = "Informe a quantidade de vagas por fila.")
        @Min(value = 1, message = "Cada fila precisa de pelo menos 1 vaga.")
        @Max(value = 99, message = "O maximo e 99 vagas por fila.")
        Integer vagasPorFila,

        @Schema(description = "Valor da primeira hora. Nulo mantem o valor atual.",
                example = "5.00", nullable = true)
        @PositiveOrZero(message = "O valor da primeira hora nao pode ser negativo.")
        Double valorPrimeiraHora,

        @Schema(description = "Valor de cada hora adicional. Nulo mantem o valor atual.",
                example = "3.00", nullable = true)
        @PositiveOrZero(message = "O valor da hora adicional nao pode ser negativo.")
        Double valorHoraAdicional) {
}
