# Estacionamento API

O trabalho de estacionamento convertido de programa de console para API REST com Spring Boot.

- **Sem banco de dados.** Tudo continua em memória: matriz `Vaga[][]`, `HashMap` de placas e `LinkedList` de histórico. Reiniciar a aplicação zera os dados.
- **Sem login.** Nenhum endpoint pede usuário, senha ou token.
- **CORS liberado** para qualquer origem, então o front pode rodar em outra porta.

O contrato completo está em [API.md](API.md). As interfaces TypeScript prontas para o front estão em [frontend/estacionamento-api.ts](frontend/estacionamento-api.ts).

---

## 1. Instalar o Java (esta máquina ainda não tem)

A API precisa do **JDK 17 ou superior** e do **Maven**. Abra o PowerShell e rode:

```bash
winget install --id Microsoft.OpenJDK.17 -e --accept-package-agreements --accept-source-agreements
```

```bash
winget install --id Apache.Maven -e --accept-package-agreements --accept-source-agreements
```

**Feche e abra o PowerShell de novo** para que o PATH seja atualizado. Depois confirme:

```bash
java -version; mvn -version
```

Se preferir não instalar nada pela linha de comando, a alternativa é abrir a pasta `estacionamento-api` no **IntelliJ IDEA Community**: ele baixa o JDK e já traz o Maven embutido, e aí basta rodar a classe `EstacionamentoApiApplication`. O **VS Code** com a extensão *Extension Pack for Java* faz o mesmo.

---

## 2. Rodar

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

Para gerar o `.jar` e rodar sem o Maven:

```bash
mvn clean package
```

```bash
java -jar target/estacionamento-api-1.0.0.jar
```

Rodar os testes:

```bash
mvn test
```

---

## 3. Conferir se está no ar

Abra no navegador:

| Endereço | O que é |
|---|---|
| `http://localhost:8080` | A tela de operação do pátio |
| `http://localhost:8080/swagger-ui.html` | Documentação navegável, dá para testar cada endpoint pelo navegador |
| `http://localhost:8080/v3/api-docs` | Contrato OpenAPI em JSON, serve para gerar código no front |
| `http://localhost:8080/api/relatorio` | Mapa das vagas |

O arquivo [requests.http](requests.http) tem uma sequência de chamadas de exemplo. Funciona direto no VS Code com a extensão *REST Client* ou no IntelliJ.

---

## 4. Endpoints

| Menu do programa original | Método | Caminho |
|---|---|---|
| 1 — Entrada de veículo | `POST` | `/api/entradas` |
| 2 — Buscar veículo por placa | `GET` | `/api/veiculos/{placa}` |
| 3 — Saída de veículo | `POST` | `/api/saidas` |
| 4 — Relatório de ocupação | `GET` | `/api/relatorio` |
| 5 — Histórico | `GET` | `/api/historico` |
| 6 — Manutenção da vaga | `PATCH` | `/api/vagas/{idVaga}/manutencao` |

Endpoints extras criados para o front: `GET /api/veiculos`, `GET /api/vagas`, `GET /api/vagas/{idVaga}`, `GET /api/configuracao` e `PUT /api/configuracao`.

Detalhes de cada um, com exemplos de JSON e a tabela de códigos de erro, em [API.md](API.md).

---

## 5. A tela de demonstração

O front-end fica em `src/main/resources/static/index.html` e é servido pelo próprio Spring Boot em `http://localhost:8080`. É um único arquivo, sem build, sem npm e sem framework, então não há nada para instalar.

A tela abre com a contagem de vagas livres, o mapa do pátio por fila, o formulário de entrada, a busca por placa, a lista de veículos estacionados e o histórico de saídas. Clicar numa vaga abre as ações dela: registrar saída, pôr em manutenção ou liberar.

Todos os dados vêm da API. A tela não guarda nada e não simula nada: sem o Spring Boot no ar, ela mostra um aviso de API fora do ar com um botão para tentar de novo.

Se preferir abrir a tela por outro servidor, como o Live Server do VS Code, ela procura a API em `http://localhost:8080` automaticamente. O CORS já está liberado para isso.

---

## 6. Estrutura do projeto

```
src/main/java/br/com/estacionamento/
  EstacionamentoApiApplication.java   entrada da aplicação (substitui o antigo Main)
  config/
    EstacionamentoProperties.java     tamanho do estacionamento e tabela de preços
    WebConfig.java                    CORS liberado para o front
    OpenApiConfig.java                título e descrição do Swagger
  domain/                             regra de negócio, Java puro, sem Spring
    Estacionamento.java               mesma lógica do trabalho original
    Vaga.java  Veiculo.java  Registro.java
  dto/                                corpos de requisição e resposta (records)
  service/
    EstacionamentoService.java        guarda o estado em memória e monta os DTOs
  controller/                         os endpoints HTTP
  exception/                          erros e o tratador global
  util/TempoUtil.java                 leitura e formatação de datas
```

### O que mudou em relação ao código original

| Antes | Agora |
|---|---|
| `Main` com `Scanner` e menu numérico | Controllers REST, um endpoint por opção do menu |
| `System.out.println` dentro das regras | Métodos devolvem objetos ou lançam exceção |
| Mensagem de erro impressa na tela | JSON de erro com status HTTP e código estável |
| `Vaga[][]`, `HashMap`, `LinkedList` | Iguais, sem mudança |
| Cálculo do valor e formatação da permanência | Iguais, sem mudança |
| `Registro(veiculo, valor)` | Ganhou o campo `idVaga`, para o front mostrar de qual vaga o veículo saiu |
| Vagas e preços fixos no código | Vêm do `application.yml` e podem mudar em tempo de execução |

---

## 7. Configuração

Edite `src/main/resources/application.yml`:

```yaml
estacionamento:
  filas: 3
  vagas-por-fila: 4
  valor-primeira-hora: 5.00
  valor-hora-adicional: 3.00
```

Para mudar sem reiniciar, use `PUT /api/configuracao`. Esse endpoint recria o estacionamento e **apaga vagas, veículos e histórico**, o que é prático para reiniciar uma demonstração.

---

## 8. Observações

- O estado é compartilhado por todos que chamam a API ao mesmo tempo. Como `HashMap` e `LinkedList` não são seguros para acesso concorrente, os métodos do serviço são `synchronized`.
- As respostas trazem cada data duas vezes: em ISO-8601, para o front calcular, e já formatada em `dd/MM/yyyy HH:mm`, para exibir direto na tela.
- `valorParcial` mostra quanto o veículo pagaria se saísse naquele instante, então muda a cada consulta.
