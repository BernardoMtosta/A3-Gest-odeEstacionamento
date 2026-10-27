package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Resposta de GET /api/historico. Substitui o {@code exibirHistorico()}.
 */
@Schema(description = "Historico de saidas e total arrecadado")
public record HistoricoResponse(

        @Schema(description = "Registros do mais antigo para o mais recente.")
        List<RegistroResponse> registros,

        @Schema(description = "Quantidade de registros.", example = "2")
        int quantidade,

        @Schema(description = "Soma de todos os valores cobrados.", example = "28.0")
        double totalArrecadado) {
}
