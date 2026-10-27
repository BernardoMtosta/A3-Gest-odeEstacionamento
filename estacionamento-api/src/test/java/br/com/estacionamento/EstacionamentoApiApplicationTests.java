package br.com.estacionamento;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifica que o contexto do Spring sobe: beans, controllers e configuracoes
 * estao ligados corretamente.
 */
@SpringBootTest
class EstacionamentoApiApplicationTests {

    @Test
    void contextoCarrega() {
        // sem assercoes: o teste falha se o contexto nao subir
    }
}
