# Contrato da API — Estacionamento

Base: `http://localhost:8080`
Sem autenticação. Sem banco de dados: o estado vive em memória e some quando a aplicação para.
Todo corpo de requisição e resposta é `application/json; charset=UTF-8`.
Documentação navegável: `http://localhost:8080/swagger-ui.html`

## Convenções de data

| Direção | Formato |
|---|---|
| Enviado pelo front (`dataHora`) | `dd/MM/yyyy HH:mm` **ou** ISO-8601. Campo ausente ou `null` = agora |
| Devolvido pela API (`horaEntrada`, `horaSaida`) | ISO-8601, ex.: `2026-10-19T08:30:00` |
| Devolvido pela API (`...Formatada`) | `19/10/2026 08:30` |

A placa é normalizada pelo servidor: espaços e hífens são removidos e o texto vira maiúsculo. `abc-1d23` e `ABC1D23` são o mesmo veículo.

---

## Mapa dos endpoints

| Menu original | Método | Caminho |
|---|---|---|
| 1 — Entrada de veículo | `POST` | `/api/entradas` |
| 2 — Buscar veículo por placa | `GET` | `/api/veiculos/{placa}` |
| 3 — Saída de veículo | `POST` | `/api/saidas` |
| 4 — Relatório de ocupação | `GET` | `/api/relatorio` |
| 5 — Histórico | `GET` | `/api/historico` |
| 6 — Manutenção da vaga | `PATCH` | `/api/vagas/{idVaga}/manutencao` |
| — | `GET` | `/api/veiculos` |
| — | `GET` | `/api/vagas` |
| — | `GET` | `/api/vagas/{idVaga}` |
| — | `GET` | `/api/configuracao` |
| — | `PUT` | `/api/configuracao` |

---

## 1. Entrada de veículo

`POST /api/entradas` → **201 Created**

Requisição:

```json
{ "placa": "ABC1D23", "dataHora": "19/10/2026 08:30" }
```

`dataHora` pode ser omitido para usar o horário atual.

Resposta:

```json
{
  "placa": "ABC1D23",
  "idVaga": "A1",
  "horaEntrada": "2026-10-19T08:30:00",
  "horaEntradaFormatada": "19/10/2026 08:30",
  "mensagem": "Veiculo ABC1D23 estacionado na vaga A1."
}
```

Erros: `400 PLACA_INVALIDA`, `400 DATA_HORA_INVALIDA`, `409 VEICULO_JA_ESTACIONADO`, `409 ESTACIONAMENTO_LOTADO`.

---

## 2. Buscar veículo por placa

`GET /api/veiculos/ABC1D23` → **200 OK**

```json
{
  "placa": "ABC1D23",
  "idVaga": "A1",
  "horaEntrada": "2026-10-19T08:30:00",
  "horaEntradaFormatada": "19/10/2026 08:30",
  "permanenciaMinutos": 195,
  "permanenciaFormatada": "3h15min",
  "valorParcial": 14.0
}
```

`valorParcial` é quanto seria cobrado se o veículo saísse neste instante.

Erro: `404 VEICULO_NAO_ENCONTRADO`.

---

## 3. Saída de veículo

`POST /api/saidas` → **200 OK**

Requisição:

```json
{ "placa": "ABC1D23", "dataHora": "19/10/2026 11:45" }
```

Resposta:

```json
{
  "placa": "ABC1D23",
  "idVaga": "A1",
  "horaEntrada": "2026-10-19T08:30:00",
  "horaEntradaFormatada": "19/10/2026 08:30",
  "horaSaida": "2026-10-19T11:45:00",
  "horaSaidaFormatada": "19/10/2026 11:45",
  "permanenciaMinutos": 195,
  "permanenciaFormatada": "3h15min",
  "valor": 14.0,
  "mensagem": "Saida de ABC1D23 (vaga A1). Permanencia: 3h15min. Valor a pagar: R$ 14,00."
}
```

Erros: `400 DATA_HORA_INVALIDA`, `400 SAIDA_ANTES_DA_ENTRADA`, `404 VEICULO_NAO_ENCONTRADO`.

### Como o valor é calculado

Primeira hora R$ 5,00, cada hora adicional R$ 3,00. A fração de hora é arredondada para cima e cobra-se no mínimo uma hora.

| Permanência | Horas cobradas | Valor |
|---|---|---|
| 1 min | 1 | 5,00 |
| 60 min | 1 | 5,00 |
| 61 min | 2 | 8,00 |
| 3h15min | 4 | 14,00 |

Os valores vêm de `GET /api/configuracao`, não os deixe fixos no front.

---

## 4. Relatório de ocupação

`GET /api/relatorio` → **200 OK**

```json
{
  "totalVagas": 12,
  "livres": 7,
  "ocupadas": 4,
  "emManutencao": 1,
  "disponiveis": 11,
  "taxaOcupacao": 36.4,
  "filas": 3,
  "vagasPorFila": 4,
  "grade": [
    [
      { "id": "A1", "fila": "A", "numero": 1, "status": "OCUPADA",
        "veiculo": { "placa": "ABC1D23", "idVaga": "A1", "horaEntrada": "2026-10-19T08:30:00",
                     "horaEntradaFormatada": "19/10/2026 08:30", "permanenciaMinutos": 195,
                     "permanenciaFormatada": "3h15min", "valorParcial": 14.0 } },
      { "id": "A2", "fila": "A", "numero": 2, "status": "LIVRE", "veiculo": null }
    ]
  ]
}
```

`grade` é a matriz de vagas agrupada por fila, pronta para desenhar o mapa. `veiculo` só vem preenchido quando `status` é `OCUPADA`.
`taxaOcupacao` é o percentual sobre as vagas disponíveis, ou seja, ignora as que estão em manutenção.

---

## 5. Histórico

`GET /api/historico` → **200 OK**

```json
{
  "registros": [
    {
      "placa": "ABC1D23",
      "idVaga": "A1",
      "horaEntrada": "2026-10-19T08:30:00",
      "horaEntradaFormatada": "19/10/2026 08:30",
      "horaSaida": "2026-10-19T11:45:00",
      "horaSaidaFormatada": "19/10/2026 11:45",
      "permanenciaMinutos": 195,
      "permanenciaFormatada": "3h15min",
      "valor": 14.0
    }
  ],
  "quantidade": 1,
  "totalArrecadado": 14.0
}
```

Quando nenhum veículo saiu ainda, `registros` vem como lista vazia e `quantidade` como `0`.

---

## 6. Manutenção da vaga

`PATCH /api/vagas/A1/manutencao` → **200 OK**

Com corpo, define o estado explicitamente:

```json
{ "emManutencao": true }
```

Sem corpo, alterna o estado atual: `LIVRE` vira `MANUTENCAO` e vice-versa.

Resposta:

```json
{ "id": "A1", "fila": "A", "numero": 1, "status": "MANUTENCAO", "veiculo": null }
```

Erros: `404 VAGA_NAO_ENCONTRADA`, `409 VAGA_OCUPADA`.

---

## 7. Listar vagas

`GET /api/vagas` → **200 OK** — lista única, na ordem A1, A2, …, B1, B2, …
`GET /api/vagas?status=LIVRE` — filtra por `LIVRE`, `OCUPADA` ou `MANUTENCAO`.
`GET /api/vagas/A1` — uma vaga específica.

Cada item tem o mesmo formato do objeto de vaga mostrado no relatório.

Erro do filtro: `400 STATUS_INVALIDO`.

---

## 8. Listar veículos estacionados

`GET /api/veiculos` → **200 OK** — array de veículos no mesmo formato da busca por placa.

---

## 9. Configuração

`GET /api/configuracao` → **200 OK**

```json
{
  "filas": 3,
  "vagasPorFila": 4,
  "totalVagas": 12,
  "valorPrimeiraHora": 5.0,
  "valorHoraAdicional": 3.0,
  "formatoDataHora": "dd/MM/yyyy HH:mm"
}
```

`PUT /api/configuracao` recria o estacionamento. **Apaga vagas, veículos estacionados e histórico.** Serve para reiniciar a demonstração sem derrubar a aplicação.

```json
{ "filas": 4, "vagasPorFila": 5, "valorPrimeiraHora": 6.00, "valorHoraAdicional": 4.00 }
```

Os dois campos de valor são opcionais: quando ausentes, os valores atuais são mantidos. Limites: de 1 a 26 filas, de 1 a 99 vagas por fila.

---

## Formato de erro

Todo erro usa o mesmo corpo:

```json
{
  "timestamp": "2026-10-19T11:45:00.123-03:00",
  "status": 409,
  "erro": "Conflict",
  "codigo": "VEICULO_JA_ESTACIONADO",
  "mensagem": "Veiculo ABC1D23 ja esta no estacionamento, na vaga A1.",
  "caminho": "/api/entradas",
  "campos": null
}
```

Trate o campo `codigo`, que é estável, e exiba o campo `mensagem` para o usuário. Em erros de validação, `campos` traz a lista de campos que falharam.

| Código | HTTP | Quando acontece |
|---|---|---|
| `PLACA_INVALIDA` | 400 | Placa vazia ou só com espaços |
| `DATA_HORA_INVALIDA` | 400 | `dataHora` fora dos formatos aceitos |
| `SAIDA_ANTES_DA_ENTRADA` | 400 | Saída anterior à entrada do veículo |
| `STATUS_INVALIDO` | 400 | Filtro `?status=` desconhecido |
| `VALIDACAO` | 400 | Campo obrigatório ausente ou fora dos limites |
| `CORPO_INVALIDO` | 400 | JSON mal formado ou corpo ausente |
| `PARAMETRO_INVALIDO` | 400 | Parâmetro de query ausente ou com tipo errado |
| `VEICULO_NAO_ENCONTRADO` | 404 | Placa não está estacionada |
| `VAGA_NAO_ENCONTRADA` | 404 | Id de vaga que não existe |
| `CAMINHO_NAO_ENCONTRADO` | 404 | URL que não existe nesta API |
| `METODO_NAO_PERMITIDO` | 405 | Método HTTP errado para o caminho |
| `VEICULO_JA_ESTACIONADO` | 409 | Placa já ocupa uma vaga |
| `ESTACIONAMENTO_LOTADO` | 409 | Nenhuma vaga livre |
| `VAGA_OCUPADA` | 409 | Tentou mudar manutenção de vaga ocupada |
| `ERRO_INTERNO` | 500 | Falha inesperada |

---

## Fluxo sugerido para a tela principal

1. Na abertura, chame `GET /api/configuracao` e `GET /api/relatorio`. O primeiro dá o tamanho do mapa e a tabela de preços, o segundo dá o estado de cada vaga.
2. Depois de qualquer `POST /api/entradas`, `POST /api/saidas` ou `PATCH .../manutencao`, chame `GET /api/relatorio` de novo para atualizar o mapa.
3. A tela de histórico usa apenas `GET /api/historico`.
4. Como a permanência e o `valorParcial` mudam com o tempo, atualize o relatório periodicamente se quiser mostrar esses números em tempo real.
