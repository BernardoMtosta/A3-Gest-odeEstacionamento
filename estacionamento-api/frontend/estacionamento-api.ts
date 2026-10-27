/**
 * Interfaces e cliente HTTP da API do Estacionamento.
 *
 * Copie este arquivo para o projeto do front-end. Ele cobre todos os endpoints
 * e não depende de nenhuma biblioteca: usa apenas `fetch`.
 *
 *   import { estacionamentoApi } from "./estacionamento-api";
 *   const relatorio = await estacionamentoApi.obterRelatorio();
 */

export const API_BASE_URL = "http://localhost:8080";

/* ------------------------------------------------------------------ */
/* Tipos                                                               */
/* ------------------------------------------------------------------ */

export type StatusVaga = "LIVRE" | "OCUPADA" | "MANUTENCAO";

/** Veículo que está no estacionamento agora. */
export interface VeiculoEstacionado {
  placa: string;
  idVaga: string;
  /** ISO-8601, ex.: "2026-10-19T08:30:00". */
  horaEntrada: string;
  /** "19/10/2026 08:30". */
  horaEntradaFormatada: string;
  permanenciaMinutos: number;
  /** "3h15min" ou "2 dia(s) e 4h10min". */
  permanenciaFormatada: string;
  /** Quanto seria cobrado se o veículo saísse agora. */
  valorParcial: number;
}

/** Uma vaga do mapa. `veiculo` só vem preenchido quando o status é OCUPADA. */
export interface Vaga {
  id: string;
  /** Letra da fila: "A", "B", "C"... */
  fila: string;
  /** Número dentro da fila, começando em 1. */
  numero: number;
  status: StatusVaga;
  veiculo: VeiculoEstacionado | null;
}

export interface EntradaRequest {
  placa: string;
  /** "dd/MM/yyyy HH:mm" ou ISO-8601. Omitido significa agora. */
  dataHora?: string | null;
}

export interface EntradaResponse {
  placa: string;
  idVaga: string;
  horaEntrada: string;
  horaEntradaFormatada: string;
  mensagem: string;
}

export interface SaidaRequest {
  placa: string;
  /** "dd/MM/yyyy HH:mm" ou ISO-8601. Omitido significa agora. */
  dataHora?: string | null;
}

export interface SaidaResponse {
  placa: string;
  idVaga: string;
  horaEntrada: string;
  horaEntradaFormatada: string;
  horaSaida: string;
  horaSaidaFormatada: string;
  permanenciaMinutos: number;
  permanenciaFormatada: string;
  valor: number;
  mensagem: string;
}

export interface Relatorio {
  totalVagas: number;
  livres: number;
  ocupadas: number;
  emManutencao: number;
  /** Total de vagas menos as que estão em manutenção. */
  disponiveis: number;
  /** Percentual sobre as vagas disponíveis. */
  taxaOcupacao: number;
  filas: number;
  vagasPorFila: number;
  /** Matriz pronta para desenhar o mapa: uma lista por fila. */
  grade: Vaga[][];
}

export interface Registro {
  placa: string;
  idVaga: string;
  horaEntrada: string;
  horaEntradaFormatada: string;
  horaSaida: string;
  horaSaidaFormatada: string;
  permanenciaMinutos: number;
  permanenciaFormatada: string;
  valor: number;
}

export interface Historico {
  registros: Registro[];
  quantidade: number;
  totalArrecadado: number;
}

export interface Configuracao {
  filas: number;
  vagasPorFila: number;
  totalVagas: number;
  valorPrimeiraHora: number;
  valorHoraAdicional: number;
  formatoDataHora: string;
}

export interface ConfiguracaoRequest {
  filas: number;
  vagasPorFila: number;
  /** Opcional: ausente mantém o valor atual. */
  valorPrimeiraHora?: number | null;
  /** Opcional: ausente mantém o valor atual. */
  valorHoraAdicional?: number | null;
}

/** Códigos de erro estáveis devolvidos pela API. */
export type CodigoErro =
  | "PLACA_INVALIDA"
  | "DATA_HORA_INVALIDA"
  | "SAIDA_ANTES_DA_ENTRADA"
  | "STATUS_INVALIDO"
  | "VALIDACAO"
  | "CORPO_INVALIDO"
  | "PARAMETRO_INVALIDO"
  | "VEICULO_NAO_ENCONTRADO"
  | "VAGA_NAO_ENCONTRADA"
  | "CAMINHO_NAO_ENCONTRADO"
  | "METODO_NAO_PERMITIDO"
  | "VEICULO_JA_ESTACIONADO"
  | "ESTACIONAMENTO_LOTADO"
  | "VAGA_OCUPADA"
  | "ERRO_INTERNO";

export interface CampoInvalido {
  campo: string;
  mensagem: string;
}

export interface ErroResponse {
  timestamp: string;
  status: number;
  erro: string;
  codigo: CodigoErro | string;
  mensagem: string;
  caminho: string;
  campos: CampoInvalido[] | null;
}

/** Lançado em qualquer resposta com status fora da faixa 2xx. */
export class EstacionamentoApiError extends Error {
  readonly status: number;
  readonly codigo: CodigoErro | string;
  readonly campos: CampoInvalido[] | null;

  constructor(erro: ErroResponse) {
    super(erro.mensagem);
    this.name = "EstacionamentoApiError";
    this.status = erro.status;
    this.codigo = erro.codigo;
    this.campos = erro.campos ?? null;
  }
}

/* ------------------------------------------------------------------ */
/* Cliente                                                             */
/* ------------------------------------------------------------------ */

async function requisitar<T>(caminho: string, init?: RequestInit): Promise<T> {
  const resposta = await fetch(`${API_BASE_URL}${caminho}`, {
    ...init,
    headers: {
      Accept: "application/json",
      ...(init?.body ? { "Content-Type": "application/json" } : {}),
      ...init?.headers,
    },
  });

  if (!resposta.ok) {
    let corpo: ErroResponse;
    try {
      corpo = (await resposta.json()) as ErroResponse;
    } catch {
      corpo = {
        timestamp: new Date().toISOString(),
        status: resposta.status,
        erro: resposta.statusText,
        codigo: "ERRO_INTERNO",
        mensagem: `Falha na comunicação com a API (HTTP ${resposta.status}).`,
        caminho,
        campos: null,
      };
    }
    throw new EstacionamentoApiError(corpo);
  }

  if (resposta.status === 204) {
    return undefined as T;
  }
  return (await resposta.json()) as T;
}

export const estacionamentoApi = {
  /** POST /api/entradas — registra a entrada e ocupa a primeira vaga livre. */
  registrarEntrada(dados: EntradaRequest): Promise<EntradaResponse> {
    return requisitar<EntradaResponse>("/api/entradas", {
      method: "POST",
      body: JSON.stringify(dados),
    });
  },

  /** POST /api/saidas — registra a saída, libera a vaga e devolve o valor. */
  registrarSaida(dados: SaidaRequest): Promise<SaidaResponse> {
    return requisitar<SaidaResponse>("/api/saidas", {
      method: "POST",
      body: JSON.stringify(dados),
    });
  },

  /** GET /api/veiculos/{placa} */
  buscarVeiculo(placa: string): Promise<VeiculoEstacionado> {
    return requisitar<VeiculoEstacionado>(`/api/veiculos/${encodeURIComponent(placa)}`);
  },

  /** GET /api/veiculos */
  listarVeiculos(): Promise<VeiculoEstacionado[]> {
    return requisitar<VeiculoEstacionado[]>("/api/veiculos");
  },

  /** GET /api/vagas — opcionalmente filtrando por status. */
  listarVagas(status?: StatusVaga): Promise<Vaga[]> {
    const query = status ? `?status=${status}` : "";
    return requisitar<Vaga[]>(`/api/vagas${query}`);
  },

  /** GET /api/vagas/{idVaga} */
  buscarVaga(idVaga: string): Promise<Vaga> {
    return requisitar<Vaga>(`/api/vagas/${encodeURIComponent(idVaga)}`);
  },

  /**
   * PATCH /api/vagas/{idVaga}/manutencao
   * Sem o segundo argumento, alterna o estado atual.
   */
  definirManutencao(idVaga: string, emManutencao?: boolean): Promise<Vaga> {
    return requisitar<Vaga>(`/api/vagas/${encodeURIComponent(idVaga)}/manutencao`, {
      method: "PATCH",
      body: emManutencao === undefined ? undefined : JSON.stringify({ emManutencao }),
    });
  },

  /** GET /api/relatorio — totais e mapa das vagas. */
  obterRelatorio(): Promise<Relatorio> {
    return requisitar<Relatorio>("/api/relatorio");
  },

  /** GET /api/historico */
  obterHistorico(): Promise<Historico> {
    return requisitar<Historico>("/api/historico");
  },

  /** GET /api/configuracao */
  obterConfiguracao(): Promise<Configuracao> {
    return requisitar<Configuracao>("/api/configuracao");
  },

  /** PUT /api/configuracao — recria o estacionamento e apaga tudo. */
  reconfigurar(dados: ConfiguracaoRequest): Promise<Configuracao> {
    return requisitar<Configuracao>("/api/configuracao", {
      method: "PUT",
      body: JSON.stringify(dados),
    });
  },
};
