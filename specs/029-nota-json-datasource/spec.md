# Feature Specification: Importação de Nota de Corretagem via JSON

**Feature Branch**: `029-nota-json-datasource`

**Created**: 2026-06-11

**Status**: Draft

**Input**: Use `docs/nota2.json` como referência para gerar classes kotlin em `core/data/filestore`, crie um data source que faça o parse desse json para kotlin e retorne `BrokerageNote` do domínio. Não use os modelos de `core/domain/entity` na desserialização, mas pode fazer cópias deles em `core/data/filestore`.

## Clarifications

### Session 2026-06-11

- Q: Como o data source recebe o JSON? → A: ~~Ler o arquivo empacotado em `commonMain/resources`~~ **Revisto**: conteúdo de `docs/nota2.json` em constante Kotlin (`String`) em `core/data/filestore` (ex.: `Nota2JsonFixture`); sem file picker, sem `resources/` e sem parâmetro de conteúdo na API pública desta feature.
- Q: Qual referência canônica para esquema e mapeamento de `valor_liquido_nota`? → A: `docs/nota2.json`; `valor_liquido_nota` mapeado pass-through para `netValue` (JSON já usa positivo = débito líquido do cliente, alinhado ao domínio da feature 026).
- Q: SC-004 exige passagem no validador de domínio após parse? → A: Não — parse garante apenas mapeamento estrutural correto; `BrokerageNoteValidator` e rateio são responsabilidade do chamador (alinhado a FR-009).
- Q: Como tratar propriedades JSON extras/desconhecidas? → A: Ignorar campos desconhecidos; parse prossegue se campos obrigatórios do esquema `nota2.json` estiverem presentes e válidos.
- Q: Quais módulos Gradle podem ser alterados? → A: **Somente** `:data:filestore` (`core/data/filestore`); sem port em `:domain:usecases`, sem alterações em `:domain:entity`, `:features:*` ou `:apps:*`.
- Q: Como testar cenários de falha (US3) se a API pública só parseia a constante fixa? → A: Cenários de falha exercidos **diretamente** em `BrokerageNoteJsonMapper.parse(String)` (`internal`, `jvmTest`); `loadNote()` testado apenas no caminho de sucesso com `Nota2JsonFixture`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Carregar nota de corretagem a partir de JSON estruturado (Priority: P1)

Um fluxo de importação usa o JSON de referência definido como constante em `core/data/filestore` (formato SINACOR estruturado: metadados, resumo financeiro e lista de ativos) e obtém uma nota de corretagem completa no modelo de domínio, pronta para validação e rateio de taxas já existentes no sistema.

**Why this priority**: Sem a conversão JSON → nota de domínio, nenhum pipeline downstream (validação pré-cálculo, rateio proporcional, registro de transações) pode consumir dados importados de corretoras que exportam nesse formato.

**Independent Test**: Invocar o data source que parseia a constante JSON em `core/data/filestore` (conteúdo equivalente a `docs/nota2.json` — Nu Investimentos, nº 8827829, 10/06/2026, `valor_liquido_nota` = 12294,92) e verificar que o resultado contém metadados, resumo financeiro e todos os ativos com valores e tipos de operação corretos.

**Acceptance Scenarios**:

1. **Given** um JSON válido com `metadados`, `resumo_financeiro` e `ativos` conforme o esquema de `nota2.json`, **When** o data source processa o documento, **Then** retorna sucesso com uma nota contendo número da nota, datas de pregão e liquidação, corretora, CNPJ, valor líquido, totais financeiros, taxas rateáveis, impostos retidos e lista completa de ativos.
2. **Given** o JSON de referência com 47 linhas de ativos (COMPRA e VENDA), **When** o parse é executado, **Then** cada ativo no resultado possui ticker, especificação, tipo de operação (compra ou venda), quantidade, preço unitário e valor bruto correspondentes ao JSON de origem.
3. **Given** um ativo com `"movimentacao": "COMPRA"` no JSON, **When** convertido para o domínio, **Then** o tipo de operação é compra; **Given** `"movimentacao": "VENDA"`, **Then** o tipo é venda.

---

### User Story 2 — Isolar formato de arquivo do modelo de domínio (Priority: P1)

O sistema mantém modelos dedicados à representação do JSON (espelhando a estrutura aninhada do arquivo) separados dos tipos de domínio, convertendo explicitamente para `BrokerageNote` apenas na saída do data source.

**Why this priority**: Evita acoplar serialização ao domínio (nomes de campos em português, formatos de data string) e permite evoluir o formato de entrada sem alterar entidades de negócio.

**Independent Test**: Inspecionar que os tipos usados na leitura do JSON residem na camada de dados e que a API pública do data source expõe apenas o tipo de domínio `BrokerageNote`.

**Acceptance Scenarios**:

1. **Given** campos JSON como `numero_nota`, `data_pregao`, `taxas_rateaveis`, **When** o documento é desserializado, **Then** os tipos intermediários refletem esses nomes/estrutura sem anotar ou alterar as classes em `core/domain/entity`.
2. **Given** um JSON válido parseado com sucesso, **When** o mapeamento para domínio é aplicado, **Then** o objeto retornado usa os nomes e tipos de domínio já definidos (`noteNumber`, `tradingDate`, `settlementDate`, `ApportionableFees`, `TradeType`, etc.).

---

### User Story 3 — Falhar de forma previsível em entradas inválidas (Priority: P2)

Quando o JSON está malformado, incompleto ou contém valores incompatíveis com o esquema esperado, o data source não produz uma nota parcial nem silenciosamente ignora erros — retorna falha explícita para o chamador tratar.

**Why this priority**: Notas corrompidas gerariam custos médios e rateios incorretos se aceitas parcialmente; falha rápida protege integridade contábil.

**Independent Test**: Invocar `BrokerageNoteJsonMapper.parse(String)` com JSON sintaticamente inválido, JSON válido sem seção `ativos`, e JSON com `movimentacao` desconhecida; verificar que nenhum caso retorna `BrokerageNote`. O caminho de sucesso de `loadNote()` é validado separadamente com a constante de referência (User Story 1).

**Acceptance Scenarios**:

1. **Given** conteúdo que não é JSON válido, **When** `BrokerageNoteJsonMapper.parse` é invocado, **Then** retorna falha sem objeto de domínio.
2. **Given** JSON válido mas omitindo campo obrigatório (ex.: `metadados` ou `ativos`), **When** o parse é tentado via mapper, **Then** retorna falha descritiva.
3. **Given** um ativo com `movimentacao` diferente de `COMPRA` ou `VENDA`, **When** o mapeamento é executado via mapper, **Then** retorna falha em vez de atribuir tipo de operação incorreto.

---

### Edge Cases

- Datas em formato `dd/MM/yyyy` (ex.: `"10/06/2026"`) devem ser interpretadas corretamente como datas de pregão e liquidação.
- `valor_liquido_nota` no JSON de referência (`nota2.json`, ex.: 12294,92) MUST mapear pass-through para `netValue` no domínio (positivo = débito líquido do cliente).
- Campos monetários com valor zero (ex.: `corretagem: 0.0`, `irrf_day_trade: 0.0`) devem ser preservados como zero, não omitidos.
- Lista `ativos` vazia deve ser parseada estruturalmente, mas o consumidor downstream (validador de nota) continuará responsável por rejeitar notas sem operações — o data source não precisa duplicar essa regra.
- Quantidades inteiras no JSON (ex.: `35`) devem ser representadas como valores numéricos compatíveis com quantidade fracionável no domínio.
- Múltiplas linhas do mesmo ticker com especificações ou preços distintos devem produzir entradas separadas na lista de ativos, preservando ordem do JSON.
- Propriedades JSON desconhecidas (não presentes em `nota2.json`) MUST ser ignoradas silenciosamente; apenas ausência ou invalidez de campos obrigatórios do esquema causa falha.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST aceitar um documento JSON textual no esquema estruturado exemplificado por `docs/nota2.json` (raiz com `metadados`, `resumo_financeiro`, `ativos`).
- **FR-002**: O sistema MUST definir tipos intermediários na camada de dados (`core/data/filestore`) espelhando a hierarquia JSON, sem reutilizar classes de `core/domain/entity` na desserialização.
- **FR-003**: O sistema MUST expor um data source cuja operação principal parseia o JSON definido como constante Kotlin em `core/data/filestore` (conteúdo de `docs/nota2.json`) e retorna `Result` (ou equivalente) contendo `BrokerageNote` do domínio em caso de sucesso.
- **FR-004**: O mapeamento MUST converter `metadados.numero_nota` → número da nota; `data_pregao` / `data_liquidacao` → datas; `corretora` / `cnpj_corretora` → corretora e documento; `valor_liquido_nota` → `netValue` pass-through (sem inversão de sinal).
- **FR-005**: O mapeamento MUST converter `resumo_financeiro.volume_total_operado`, `total_compras_vista`, `total_vendas_vista`, bloco `taxas_rateaveis` (liquidação, emolumentos, transferência, corretagem, ISS, outras) e bloco `impostos_retidos` (IRRF operações, IRRF day trade) para o resumo financeiro de domínio.
- **FR-006**: Para cada item em `ativos`, o mapeamento MUST produzir ticker, especificação, tipo de operação (COMPRA→compra, VENDA→venda), quantidade, preço unitário e valor bruto total.
- **FR-007**: O mapper MUST falhar (sem retornar nota de domínio) quando o JSON for sintaticamente inválido, estruturalmente incompleto (campos obrigatórios ausentes) ou contiver enumeração de movimentação desconhecida; propriedades extras não previstas em `nota2.json` MUST ser ignoradas. `loadNote()` delega ao mapper sobre a constante fixa e propaga falha via `Result.failure`.
- **FR-008**: O conteúdo de `docs/nota2.json` MUST estar disponível como constante `String` em `core/data/filestore` (ex.: `Nota2JsonFixture`) e servir como fixture para o data source e para testes de mapeamento.
- **FR-009**: A feature MUST limitar-se à leitura e mapeamento JSON → domínio; validação de integridade da nota (volume, subtotais, duplicatas) e rateio de taxas permanecem responsabilidade dos componentes de domínio já existentes (feature 026).
- **FR-010**: Cenários de falha da User Story 3 MUST ser verificados em testes que invocam `BrokerageNoteJsonMapper.parse(String)` diretamente; `loadNote()` MUST ser coberto no caminho de sucesso com `Nota2JsonFixture` (User Story 1).

### Key Entities

- **Documento JSON de nota**: Representação externa com metadados (`numero_nota`, datas em string, corretora, CNPJ, `valor_liquido_nota`), resumo financeiro aninhado e array `ativos` com movimentação textual.
- **Modelos intermediários (camada de dados)**: Cópias estruturais alinhadas ao JSON — usadas apenas na desserialização; não expostas como contrato de domínio.
- **Nota de corretagem (`BrokerageNote`)**: Agregado de domínio já existente — metadados, resumo financeiro tipado e lista de ativos — retornado pelo data source após mapeamento.
- **Data source de nota JSON**: Ponto de entrada que parseia a constante JSON de referência, executa mapeamento e encapsulamento de erros.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos campos de primeiro nível presentes em `nota2.json` de referência aparecem mapeados no `BrokerageNote` resultante (metadados, 3 totais do resumo, 6 taxas rateáveis, 2 impostos retidos, e todos os ativos da lista).
- **SC-002**: Parse do JSON de referência completa em menos de 1 segundo em ambiente desktop de desenvolvimento (nota com dezenas de ativos).
- **SC-003**: 100% dos casos de JSON inválido ou incompleto nos cenários de aceite da User Story 3 resultam em falha explícita via `BrokerageNoteJsonMapper.parse`, sem objeto de domínio retornado.
- **SC-004**: Testes de mapeamento com `Nota2JsonFixture` verificam correspondência campo-a-campo entre JSON e `BrokerageNote` (tipos, contagens e valores literais); testes de falha (SC-003) usam strings JSON inline no mapper; **não** exigem aceitação por `BrokerageNoteValidator` — validação contábil permanece fora do escopo desta feature (FR-009).

## Assumptions

- O esquema JSON segue o exemplo canônico em `docs/nota2.json`, materializado como constante Kotlin em `core/data/filestore`; variações futuras de corretora exigirão extensão explícita fora deste escopo.
- Convenção de sinal de `netValue`: pass-through de `valor_liquido_nota` — o JSON de referência já expressa débito líquido como valor positivo, alinhado ao domínio (feature 026).
- Datas no JSON usam formato brasileiro `dd/MM/yyyy`.
- `movimentacao` admite apenas os literais `COMPRA` e `VENDA` (maiúsculas).
- Propriedades JSON adicionais além do esquema `nota2.json` são toleradas e ignoradas na desserialização.
- Implementação **restrita ao módulo** `:data:filestore`: DTOs, mapper, constante JSON, data source e testes; dependência existente de `:domain:entity` apenas para tipo de retorno (`BrokerageNote`) — **sem** alterar ficheiros em `:domain:usecases` ou outros módulos.
- UI de seleção de arquivo, file picker e integração em fluxos de importação na aplicação ficam fora deste escopo; o data source usa exclusivamente a constante JSON no módulo `filestore`.
- Testes automatizados: sucesso via `loadNote()` + constante; falhas via `BrokerageNoteJsonMapper.parse(String)` com JSON inline (SC-003/FR-010); mapeamento estrutural (SC-004), não integridade contábil; execução de `./gradlew` fica a critério do utilizador conforme princípio IX da constituição.

## Scope

### In scope

- **Apenas** alterações em `core/data/filestore` (Gradle `:data:filestore`).
- Modelos intermediários derivados da estrutura de `nota2.json`.
- Constante Kotlin com conteúdo de `docs/nota2.json`.
- Data source (interface + impl) que parseia a constante e mapeia para `BrokerageNote`.
- Tratamento de erros de parse/mapeamento.
- Testes unitários do mapeamento em `:data:filestore:jvmTest` (sucesso via `loadNote()`; falhas via mapper).

### Out of scope

- Alteração de **qualquer** módulo fora de `:data:filestore` (incl. `:domain:usecases`, `:domain:entity`, `:features:*`, `:apps:*`).
- Validação contábil e rateio de taxas (já cobertos pela feature 026).
- Persistência da nota importada em base de dados.
- Interface gráfica de importação e file picker de ficheiro.
- Suporte a outros formatos (PDF, XLSX, XML SINACOR bruto).
