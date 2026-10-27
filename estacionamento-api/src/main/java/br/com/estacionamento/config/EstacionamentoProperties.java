package br.com.estacionamento.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Valores lidos do application.yml (prefixo {@code estacionamento}).
 * Sao apenas os defaults de inicializacao: em tempo de execucao o tamanho do
 * estacionamento pode ser trocado por PUT /api/configuracao.
 */
@ConfigurationProperties(prefix = "estacionamento")
public record EstacionamentoProperties(

        @DefaultValue("3") int filas,

        @DefaultValue("4") int vagasPorFila,

        @DefaultValue("5.00") double valorPrimeiraHora,

        @DefaultValue("3.00") double valorHoraAdicional) {
}
