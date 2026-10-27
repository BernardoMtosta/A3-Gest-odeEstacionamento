package br.com.estacionamento.exception;

/**
 * Dados invalidos ou operacao que fere uma regra do estacionamento.
 * Resulta em HTTP 400 (Bad Request).
 */
public class RegraDeNegocioException extends ApiException {

    public RegraDeNegocioException(String codigo, String mensagem) {
        super(400, codigo, mensagem);
    }
}
