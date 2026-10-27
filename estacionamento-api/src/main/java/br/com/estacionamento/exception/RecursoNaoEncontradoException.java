package br.com.estacionamento.exception;

/**
 * Placa ou vaga que nao existe. Resulta em HTTP 404 (Not Found).
 */
public class RecursoNaoEncontradoException extends ApiException {

    public RecursoNaoEncontradoException(String codigo, String mensagem) {
        super(404, codigo, mensagem);
    }
}
