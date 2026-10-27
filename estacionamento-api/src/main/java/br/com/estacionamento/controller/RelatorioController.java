package br.com.estacionamento.controller;

import br.com.estacionamento.dto.ConfiguracaoRequest;
import br.com.estacionamento.dto.ConfiguracaoResponse;
import br.com.estacionamento.dto.HistoricoResponse;
import br.com.estacionamento.dto.RelatorioResponse;
import br.com.estacionamento.service.EstacionamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Relatorio de ocupacao, historico e configuracao do estacionamento.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Relatorios", description = "Ocupacao, historico e configuracao")
public class RelatorioController {

    private final EstacionamentoService servico;

    public RelatorioController(EstacionamentoService servico) {
        this.servico = servico;
    }

    @GetMapping("/relatorio")
    @Operation(summary = "Relatorio de ocupacao com o mapa das vagas",
            description = "Devolve os totais (livres, ocupadas, em manutencao, taxa de ocupacao) "
                    + "e a grade de vagas agrupada por fila, pronta para desenhar o mapa.")
    public RelatorioResponse gerarRelatorio() {
        return servico.gerarRelatorio();
    }

    @GetMapping("/historico")
    @Operation(summary = "Historico de saidas e total arrecadado",
            description = "Registros do mais antigo para o mais recente. "
                    + "Lista vazia quando nenhum veiculo saiu ainda.")
    public HistoricoResponse gerarHistorico() {
        return servico.gerarHistorico();
    }

    @GetMapping("/configuracao")
    @Operation(summary = "Configuracao atual (tamanho do estacionamento e tabela de precos)")
    public ConfiguracaoResponse obterConfiguracao() {
        return servico.obterConfiguracao();
    }

    @PutMapping("/configuracao")
    @Operation(summary = "Recria o estacionamento com um novo tamanho",
            description = "ATENCAO: apaga todas as vagas, os veiculos estacionados e o historico. "
                    + "Serve para reiniciar a demonstracao sem derrubar a aplicacao.")
    public ConfiguracaoResponse reconfigurar(@Valid @RequestBody ConfiguracaoRequest requisicao) {
        return servico.reconfigurar(requisicao);
    }
}
