package br.com.estacionamento.exception;

import br.com.estacionamento.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Converte qualquer excecao em um JSON unico ({@link ErroResponse}), para que o
 * front-end nunca receba uma pagina de erro HTML do servidor.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Erros previstos do dominio: 400, 404 ou 409. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResponse> tratarApiException(ApiException e, HttpServletRequest requisicao) {
        HttpStatus status = HttpStatus.valueOf(e.getStatus());
        return ResponseEntity.status(status)
                .body(montar(status, e.getCodigo(), e.getMessage(), requisicao, null));
    }

    /** Falhas das anotacoes de validacao (@NotBlank, @Min...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException e,
                                                        HttpServletRequest requisicao) {
        List<ErroResponse.CampoInvalido> campos = new ArrayList<>();
        for (FieldError erro : e.getBindingResult().getFieldErrors()) {
            campos.add(new ErroResponse.CampoInvalido(erro.getField(), erro.getDefaultMessage()));
        }
        String mensagem = campos.isEmpty()
                ? "Dados invalidos."
                : campos.get(0).mensagem();
        return ResponseEntity.badRequest()
                .body(montar(HttpStatus.BAD_REQUEST, "VALIDACAO", mensagem, requisicao, campos));
    }

    /** Corpo ausente ou JSON mal formado. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException e,
                                                            HttpServletRequest requisicao) {
        return ResponseEntity.badRequest().body(montar(HttpStatus.BAD_REQUEST, "CORPO_INVALIDO",
                "Corpo da requisicao ausente ou com JSON invalido.", requisicao, null));
    }

    /** Parametro de query obrigatorio faltando ou com tipo errado. */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResponse> tratarParametroInvalido(Exception e, HttpServletRequest requisicao) {
        return ResponseEntity.badRequest().body(montar(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                "Parametro da requisicao ausente ou invalido.", requisicao, null));
    }

    /** Caminho que nao existe na API. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> tratarCaminhoInexistente(NoResourceFoundException e,
                                                                 HttpServletRequest requisicao) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(montar(HttpStatus.NOT_FOUND,
                "CAMINHO_NAO_ENCONTRADO", "Caminho nao encontrado nesta API.", requisicao, null));
    }

    /** Metodo HTTP errado para um caminho existente (ex.: GET em /api/entradas). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException e,
                                                                 HttpServletRequest requisicao) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(montar(HttpStatus.METHOD_NOT_ALLOWED,
                "METODO_NAO_PERMITIDO",
                "Metodo " + requisicao.getMethod() + " nao e aceito neste caminho.", requisicao, null));
    }

    /** Qualquer outra falha inesperada. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception e, HttpServletRequest requisicao) {
        log.error("Erro inesperado em {} {}", requisicao.getMethod(), requisicao.getRequestURI(), e);
        return ResponseEntity.internalServerError().body(montar(HttpStatus.INTERNAL_SERVER_ERROR,
                "ERRO_INTERNO", "Erro interno no servidor.", requisicao, null));
    }

    private ErroResponse montar(HttpStatus status, String codigo, String mensagem,
                                HttpServletRequest requisicao, List<ErroResponse.CampoInvalido> campos) {
        return new ErroResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                codigo,
                mensagem,
                requisicao.getRequestURI(),
                campos);
    }
}
