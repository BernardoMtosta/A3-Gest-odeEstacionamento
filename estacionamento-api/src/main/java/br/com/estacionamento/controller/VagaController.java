package br.com.estacionamento.controller;

import br.com.estacionamento.dto.ErroResponse;
import br.com.estacionamento.dto.ManutencaoRequest;
import br.com.estacionamento.dto.VagaResponse;
import br.com.estacionamento.service.EstacionamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta de vagas e controle de manutencao.
 */
@RestController
@RequestMapping("/api/vagas")
@Tag(name = "Vagas", description = "Consulta das vagas e controle de manutencao")
public class VagaController {

    private final EstacionamentoService servico;

    public VagaController(EstacionamentoService servico) {
        this.servico = servico;
    }

    @GetMapping
    @Operation(summary = "Lista as vagas",
            description = "Lista unica, na ordem da matriz (A1, A2, ..., B1, B2, ...). "
                    + "Para desenhar o mapa em grade use GET /api/relatorio.")
    public List<VagaResponse> listarVagas(
            @Parameter(description = "Filtro opcional por situacao",
                    schema = @Schema(allowableValues = {"LIVRE", "OCUPADA", "MANUTENCAO"}))
            @RequestParam(required = false) String status) {
        return servico.listarVagas(status);
    }

    @GetMapping("/{idVaga}")
    @Operation(summary = "Busca uma vaga pelo id (ex.: A1, B3)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga encontrada"),
            @ApiResponse(responseCode = "404", description = "Vaga nao existe (VAGA_NAO_ENCONTRADA)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public VagaResponse buscarVaga(@PathVariable String idVaga) {
        return servico.buscarVaga(idVaga);
    }

    @PatchMapping("/{idVaga}/manutencao")
    @Operation(summary = "Coloca ou tira a vaga da manutencao",
            description = "Com o corpo {\"emManutencao\": true} ou {\"emManutencao\": false} o estado e "
                    + "definido explicitamente. Sem corpo, o estado atual e alternado (LIVRE vira "
                    + "MANUTENCAO e vice-versa). Vaga ocupada nao pode ser alterada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Situacao da vaga atualizada"),
            @ApiResponse(responseCode = "404", description = "Vaga nao existe (VAGA_NAO_ENCONTRADA)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Vaga ocupada (VAGA_OCUPADA)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public VagaResponse definirManutencao(@PathVariable String idVaga,
                                          @RequestBody(required = false) ManutencaoRequest requisicao) {
        Boolean emManutencao = requisicao == null ? null : requisicao.emManutencao();
        return servico.definirManutencao(idVaga, emManutencao);
    }
}
