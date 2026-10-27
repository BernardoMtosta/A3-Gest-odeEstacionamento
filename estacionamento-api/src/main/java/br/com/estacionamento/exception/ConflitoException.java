package br.com.estacionamento.exception;

/**
 * Estado atual impede a operacao: veiculo ja estacionado, estacionamento
 * lotado ou vaga ocupada. Resulta em HTTP 409 (Conflict).
 */
public class ConflitoException extends ApiException {

    public ConflitoException(String codigo, String mensagem) {
        super(409, codigo, mensagem);
    }
}
