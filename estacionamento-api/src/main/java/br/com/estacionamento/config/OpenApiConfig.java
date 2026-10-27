package br.com.estacionamento.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Metadados do contrato publicado em /swagger-ui.html e /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI estacionamentoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API do Estacionamento")
                        .version("1.0.0")
                        .description("""
                                API REST do sistema de estacionamento.

                                Caracteristicas:
                                - Sem banco de dados: todo o estado fica em memoria (matriz de vagas,
                                  HashMap placa -> vaga e LinkedList de historico).
                                - Sem autenticacao: nenhum endpoint exige login ou token.
                                - Datas aceitas na entrada: "dd/MM/yyyy HH:mm" ou ISO-8601.
                                  Campo ausente ou nulo significa "agora".
                                - Datas devolvidas: ISO-8601 (campo base) e tambem no formato
                                  brasileiro (campos terminados em "Formatada").
                                """))
                .servers(List.of(new Server().url("http://localhost:8080").description("Ambiente local")));
    }
}
