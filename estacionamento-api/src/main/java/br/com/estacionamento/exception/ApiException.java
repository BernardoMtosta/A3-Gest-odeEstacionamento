package br.com.estacionamento.exception;

/**
 * Excecao base da aplicacao.
 *
 * <p>Carrega o status HTTP e um codigo de erro estavel. O front-end deve
 * decidir o que fazer olhando para o campo {@code codigo} (ex.: VAGA_OCUPADA),
 * nunca para o texto da mensagem, que pode mudar.</p>
 *
 * <p>De proposito nao depende de nenhuma classe do Spring: assim o pacote
 * {@code domain} continua sendo Java puro.</p>
 */
public class ApiException extends RuntimeException {

    private final int status;
    private final String codigo;

    public ApiException(int status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }

    public int getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }
}
