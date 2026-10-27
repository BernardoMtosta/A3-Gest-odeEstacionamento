package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Corpo (opcional) de PATCH /api/vagas/{idVaga}/manutencao.
 */
@Schema(description = "Define o estado de manutencao da vaga")
public record ManutencaoRequest(

        @Schema(description = "true coloca em manutencao, false tira. "
                + "Se o corpo for omitido ou o campo vier nulo, o estado atual e alternado.",
                example = "true",
                nullable = true)
        Boolean emManutencao) {
}
