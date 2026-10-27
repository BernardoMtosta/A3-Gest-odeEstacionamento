package br.com.estacionamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Ponto de entrada da API.
 *
 * <p>Equivale ao antigo {@code Main.java}: antes o menu era lido do console,
 * agora cada funcionalidade virou um endpoint HTTP. Todo o estado continua
 * em memoria (matriz de vagas, HashMap de placas e LinkedList de historico),
 * portanto os dados sao perdidos quando a aplicacao e reiniciada.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class EstacionamentoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstacionamentoApiApplication.class, args);
    }
}
