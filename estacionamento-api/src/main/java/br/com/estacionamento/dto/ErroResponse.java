package br.com.estacionamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Formato unico de erro da API. O front deve reagir ao campo {@code codigo},
 * que e estavel, e exibir o campo {@code mensagem} para o usuario.
 */
@Schema(description = "Erro retornado pela API")
public record ErroResponse(

        @Schema(example = "2026-10-19T11:45:00-03:00")
        OffsetDateTime timestamp,

        @Schema(description = "Status HTTP.", example = "409")
        int status,

        @Schema(description = "Nome do status HTTP.", example = "Conflict")
        String erro,

        @Schema(description = "Codigo estavel do erro.", example = "VEICULO_JA_ESTACIONADO")
        String codigo,

        @Schema(description = "Mensagem pronta para exibir ao usuario.",
                example = "Veiculo ABC1D23 ja esta no estacionamento, na vaga A1.")
        String mensagem,

        @Schema(description = "Caminho chamado.", example = "/api/entradas")
        String caminho,

        @Schema(description = "Preenchido apenas em erros de validacao de campos.", nullable = true)
        List<CampoInvalido> campos) {

    @Schema(description = "Campo que falhou na validacao")
    public record CampoInvalido(

            @Schema(example = "placa")
            String campo,

            @Schema(example = "A placa e obrigatoria.")
            String mensagem) {
    }
}
