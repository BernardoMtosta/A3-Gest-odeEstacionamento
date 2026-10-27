package br.com.estacionamento.controller;

import br.com.estacionamento.dto.EntradaRequest;
import br.com.estacionamento.dto.EntradaResponse;
import br.com.estacionamento.dto.ErroResponse;
import br.com.estacionamento.dto.SaidaRequest;
import br.com.estacionamento.dto.SaidaResponse;
import br.com.estacionamento.dto.VeiculoEstacionadoResponse;
import br.com.estacionamento.service.EstacionamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Entradas, saidas e consulta de veiculos.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Veiculos", description = "Entrada, saida e consulta de veiculos")
public class VeiculoController {

    private final EstacionamentoService servico;

    public VeiculoController(EstacionamentoService servico) {
        this.servico = servico;
    }

    @PostMapping("/entradas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra a entrada de um veiculo",
            description = "Ocupa a primeira vaga livre encontrada, varrendo a matriz da fila A para a frente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Veiculo estacionado"),
            @ApiResponse(responseCode = "400", description = "Placa ou data invalida (PLACA_INVALIDA, DATA_HORA_INVALIDA)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Veiculo ja estacionado ou estacionamento lotado "
                    + "(VEICULO_JA_ESTACIONADO, ESTACIONAMENTO_LOTADO)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public EntradaResponse registrarEntrada(@Valid @RequestBody EntradaRequest requisicao) {
        return servico.registrarEntrada(requisicao);
    }

    @PostMapping("/saidas")
    @Operation(summary = "Registra a saida de um veiculo e calcula o valor",
            description = "Libera a vaga e grava o registro no historico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saida registrada"),
            @ApiResponse(responseCode = "400", description = "Data invalida ou saida anterior a entrada "
                    + "(DATA_HORA_INVALIDA, SAIDA_ANTES_DA_ENTRADA)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veiculo nao esta no estacionamento (VEICULO_NAO_ENCONTRADO)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public SaidaResponse registrarSaida(@Valid @RequestBody SaidaRequest requisicao) {
        return servico.registrarSaida(requisicao);
    }

    @GetMapping("/veiculos")
    @Operation(summary = "Lista os veiculos que estao no estacionamento agora")
    public List<VeiculoEstacionadoResponse> listarVeiculos() {
        return servico.listarVeiculos();
    }

    @GetMapping("/veiculos/{placa}")
    @Operation(summary = "Busca um veiculo pela placa",
            description = "Devolve a vaga, o horario de entrada, a permanencia ate agora "
                    + "e quanto seria cobrado se o veiculo saisse neste instante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veiculo encontrado"),
            @ApiResponse(responseCode = "404", description = "Veiculo nao esta no estacionamento (VEICULO_NAO_ENCONTRADO)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public VeiculoEstacionadoResponse buscarVeiculo(@PathVariable String placa) {
        return servico.buscarVeiculo(placa);
    }
}
