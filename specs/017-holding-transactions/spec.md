# Feature Specification: Transações embutidas na posição e no histórico mensal

**Feature Branch**: `017-holding-transactions`

**Created**: 2026-06-03

**Status**: Draft

**Input**: User description: "Preciso que a posição (holding) de domínio contenha a lista de transações. Ao consultar históricos mensais por período, cada registro de histórico deve devolver a posição completa com todas as transações persistidas dessa posição, sem exigir consultas adicionais só para obter movimentações."

**Refinamento (2026-06-03)**: A transação de ativo **deixa de referenciar** a posição. O grafo de leitura é unidirecional: **histórico → posição → transações**.

**Refinamento (2026-06-03)**: O caso de uso de consulta global de transações por mês de referência (`GetTransactionsUseCase`) será **descontinuado** e removido; consumidores passam a obter transações via **histórico → posição → transações** (ou **posição → transações** quando só a posição estiver no escopo).

## Clarifications

### Session 2026-06-03

- Q: Quando um registro de histórico mensal carrega a posição, quais transações devem vir em `posição.transações`? → A: **Todas** as transações persistidas da posição (sem filtro pelo mês de referência do histórico); consumidores que precisem de subconjunto (ex. saldo só do mês exibido) filtram localmente.
- Q: Como identificar a posição na escrita sem `holding` na transação? → A: Parâmetro composto **`Param(holding, transaction)`** em casos de uso de persistência (save/delete), alinhado a leituras que já recebem posição explicitamente.
- Q: Em que consultas a lista de transações deve vir preenchida na posição? → A: Em **toda** leitura de posição a partir do armazenamento (`getById`, `getAll`, histórico, etc.).
- Q: O que fazer com `GetTransactionsByHoldingUseCase`? → A: **Descontinuar**; consumidores usam posição carregada e `holding.transactions` (ex. `GetAssetHoldingUseCase`).
- Q: O que fazer com métodos de listagem em `AssetTransactionRepository`? → A: **Remover** listagens do contrato público; manter só persistência e `getById` se necessário; listagens ficam internas ao montar `AssetHolding.transactions`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Histórico mensal com movimentações da posição (Priority: P1)

O investidor consulta o **histórico de posições** para um **mês de referência** e espera que cada linha de histórico traga não só o snapshot do fim do mês (valor, quantidade, custo médio, total investido), mas também **todas as transações** já registradas para aquela posição na carteira. Assim, fluxos que calculam saldos, valorização ou detalhes de movimentação podem usar um único resultado da consulta, em vez de uma consulta global de transações por mês (hoje `GetTransactionsUseCase`) ou outras leituras em separado.

**Why this priority**: É o objetivo central da feature — fechar a lacuna entre registro de histórico e movimentações da posição.

**Independent Test**: Invocar a consulta de históricos por mês; para uma posição com N transações persistidas, verificar que cada registro de histórico devolvido expõe **histórico → posição → lista de N transações**, com dados coerentes com o armazenamento (tipo, data, valores, observações), **sem** propriedade de posição dentro de cada transação.

**Acceptance Scenarios**:

1. **Given** um mês com registros de histórico para várias posições, **When** o usuário (ou caso de uso) solicita históricos desse mês, **Then** cada registro retornado segue a cadeia **histórico contém posição; posição contém lista completa** de transações persistidas para o identificador dessa posição.
2. **Given** uma posição com transações de renda fixa, renda variável ou fundo, **When** o histórico dessa posição é carregado, **Then** cada transação na lista reflete o **tipo de produto** correto e os atributos específicos da categoria (além dos campos comuns: data, tipo compra/venda, valor total, observações).
3. **Given** uma posição **sem** transações cadastradas, **When** o histórico é carregado, **Then** a posição é retornada com **lista vazia** de transações (não nula e não omitida).

---

### User Story 2 - Consistência em outras consultas de histórico (Priority: P2)

Além da consulta por mês de referência, outras entradas do repositório de histórico (por posição e mês, por meta financeira e mês, persistência e exportação) devem manter o **mesmo contrato**: sempre que um registro de histórico expõe uma posição, essa posição deve carregar as transações persistidas da mesma forma.

**Why this priority**: Evita comportamento divergente conforme o caminho de leitura escolhido.

**Independent Test**: Repetir cenário da User Story 1 para consulta por posição+mês e por meta+mês; comparar listas de transações com a mesma posição consultada diretamente no armazenamento.

**Acceptance Scenarios**:

1. **Given** histórico existente para uma posição e mês, **When** a consulta é feita por posição e data de referência, **Then** o registro retornado inclui a posição com a mesma lista de transações que na consulta apenas por mês.
2. **Given** históricos filtrados por meta no mesmo mês, **When** a consulta por meta é executada, **Then** cada registro retornado inclui posição com transações completas para aquela posição.

---

### User Story 3 - Descontinuar casos de uso de leitura isolada de transações (Priority: P2)

Fluxos que hoje usam **`GetTransactionsUseCase`** (transações só por mês) ou **`GetTransactionsByHoldingUseCase`** (lista de transações por posição) passam a obter movimentações apenas via **posição carregada** (`holding.transactions`) ou **histórico → posição → transações**, eliminando APIs paralelas de leitura.

**Why this priority**: Formaliza a consolidação do grafo de leitura num único agregado (posição).

**Independent Test**: Após a migração, não existe referência a `GetTransactionsUseCase` nem `GetTransactionsByHoldingUseCase`; `GetHistoryTableDataUseCase` e gestão de transações leem `holding.transactions` da posição já carregada.

**Acceptance Scenarios**:

1. **Given** a feature concluída, **When** se audita o módulo de casos de uso, **Then** `GetTransactionsUseCase` e `GetTransactionsByHoldingUseCase` **não** estão presentes (classes e registo de injeção removidos).
2. **Given** um fluxo que antes chamava consulta global por mês, **When** precisa das transações da carteira naquele período, **Then** obtém-nas via **histórico → posição → transações** (agregando por posição conforme necessário).
3. **Given** um ecrã que precisa de transações de **uma** posição (ex.: gestão de movimentações), **When** a posição é carregada, **Then** usa **`GetAssetHoldingUseCase`** (ou posição já no estado) e lê **`holding.transactions`**, sem caso de uso dedicado que devolva só `List<AssetTransaction>`.

---

### User Story 4 - Transação sem referência à posição (Priority: P3)

O contrato de **transação de ativo** deixa de expor a posição. A ligação posição ↔ movimentação passa a ser **só no sentido posição → transações** (e, na leitura de histórico, **histórico → posição → transações**). Fluxos que hoje leem `transação.posição` passam a obter a posição pelo registro de histórico ou pela posição que agrega a lista.

**Why this priority**: Elimina referência circular e alinha o modelo ao grafo de consulta desejado.

**Independent Test**: Inspecionar o contrato público de transação — não deve existir propriedade de posição; cálculo de saldo usa `posição.transações` (ou histórico → posição → transações).

**Acceptance Scenarios**:

1. **Given** o contrato de transação de ativo no domínio, **When** um consumidor inspeciona suas propriedades, **Then** **não** existe referência embutida à posição (apenas identificador da transação, data, tipo, observações, valor e atributos por categoria).
2. **Given** um registro de histórico carregado com posição e N transações, **When** regras de domínio calculam saldo ou agregados, **Then** usam **exclusivamente** `histórico.posição.transações` (ou equivalente), sem navegar de transação de volta à posição.
3. **Given** código que instancia posição apenas com identificador e metadados (sem passar por repositório), **When** a lista de transações não é fornecida, **Then** o comportamento padrão é **lista vazia**, preservando compatibilidade com construtores existentes.

---

### Edge Cases

- **Posição sem transações**: Lista vazia; histórico mensal ainda válido.
- **Muitas transações**: Lista deve conter todas as persistidas da posição, sem truncamento silencioso.
- **Transações de categorias mistas**: Apenas transações da posição consultada; não incluir movimentações de outras posições.
- **Ordem da lista**: Transações expostas em **ordem cronológica crescente** por data (desempate estável por identificador quando datas iguais).
- **Grafo unidirecional**: Transação **não** contém posição; acesso às movimentações de uma posição no contexto de histórico é sempre **histórico → posição → transações**.
- **Código legado que usa transação.posição**: Deve ser migrado para receber posição ou histórico explicitamente, ou ler `posição.transações` com a posição já conhecida no escopo.
- **Leitura isolada de transações (`GetTransactionsUseCase`, `GetTransactionsByHoldingUseCase`)**: Ambos removidos; leitura de movimentações só via **posição** ou **histórico → posição**.
- **Leitura de posição**: `getById` / `getAll` e equivalentes DEVEM incluir lista de transações; não há variante “posição leve” sem movimentações no caminho de dados.
- **Tabela de Histórico**: Deixa de invocar caso de uso de transações; usa `result.holding.transactions` (ou `currentEntry.holding.transactions`) e aplica filtro por mês no consumidor (FR-004a).
- **Gravação de histórico**: Persistir ou atualizar snapshot mensal **não** exige enviar transações no payload; transações continuam geridas pelos fluxos de movimentação existentes.
- **Gravação de transação**: `SaveTransactionUseCase` (e delete equivalente) exigem **posição no parâmetro** junto com a transação; ViewModels que hoje montam transação com `holding` embutido passam a passar a posição separadamente.
- **Consulta sem registro de histórico**: Fora do escopo — não cria histórico; apenas enriquece leituras onde o registro já existe.
- **Filtro por data de transação vs. mês de referência**: A lista em `posição.transações` inclui **sempre** todas as transações persistidas da posição; o `referenceDate` do histórico descreve apenas o snapshot (valor/quantidade fim de mês), não restringe a lista. Filtros por mês de transação são responsabilidade do consumidor (ex. tabela de Histórico).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O modelo de **posição** no domínio DEVE expor uma propriedade de **lista de transações de ativo** associadas à posição.
- **FR-001a**: O contrato de **transação de ativo** NÃO DEVE expor referência à posição; a associação é implícita pelo agregado **posição → transações**.
- **FR-002**: A lista DEVE usar o contrato de domínio de transações **sem** propriedade de posição (campos comuns e variantes por classe de ativo: renda fixa, renda variável, fundo).
- **FR-003**: Quando não houver transações, a lista DEVE ser **vazia** (nunca ausente no modelo público).
- **FR-004**: Ao consultar históricos mensais **por mês de referência**, o sistema DEVE devolver registros na forma **histórico → posição → transações**, com a posição contendo **todas** as transações persistidas para o identificador dessa posição — **sem** filtrar pela data das transações em função do `referenceDate` do snapshot.
- **FR-004a**: Consumidores que exijam transações apenas de um mês (ex. coluna de aportes/resgates no Histórico) DEVEM aplicar filtro **no consumidor** sobre `posição.transações`; o carregamento do histórico **não** pré-filtra a lista.
- **FR-005**: Ao consultar histórico **por posição e mês** ou **por meta e mês**, o sistema DEVE aplicar a mesma regra de enriquecimento da posição (FR-004).
- **FR-006**: Os dados de cada transação na lista DEVEM ser **fiéis** ao armazenamento (identificador, data, tipo, classe de ativo, observações e atributos específicos por categoria), mapeados para o domínio — **sem** expor tipos de persistência na camada de domínio ou casos de uso.
- **FR-007**: A ordem das transações na lista DEVE ser **cronológica crescente** por data da transação.
- **FR-008**: Operações de **gravação** de registro de histórico mensal NÃO DEVEM exigir que o chamador forneça transações; a lista na posição em memória pode ser ignorada ou vazia na persistência do snapshot.
- **FR-008a**: Casos de uso de **gravação, atualização e remoção** de transação DEVEM receber a **posição e a transação** no parâmetro (ex. `Param(holding, transaction)`); a transação de domínio **não** carrega referência à posição; `AssetTransactionRepository.upsert` / `delete` / `getById` recebem a posição (ou seu identificador) explicitamente no parâmetro, não via propriedade na transação.
- **FR-009**: **Toda** leitura de posição a partir do armazenamento (por identificador, listagem, via histórico ou meta) DEVE devolver `AssetHolding` com `transações` preenchidas conforme FR-004 (lista completa persistida da posição).
- **FR-010**: Casos de uso e testes existentes que constroem posição manualmente DEVEM continuar válidos usando lista vazia como padrão até serem migrados explicitamente.
- **FR-011**: Todo consumidor que hoje acessa **posição via transação** DEVE ser atualizado para o caminho **histórico → posição → transações** ou **posição → transações**, conforme o contexto.
- **FR-012**: Os casos de uso **GetTransactionsUseCase** e **GetTransactionsByHoldingUseCase** DEVEM ser **descontinuados**: nenhum consumidor pode depender deles após esta feature.
- **FR-013**: A remoção DEVE incluir classes, parâmetros e registo em injeção dos casos de uso de leitura isolada de transações.
- **FR-013a**: O contrato público **`AssetTransactionRepository`** NÃO DEVE expor listagens (`getAllByHolding`, `getByReferenceDate`, `getByGoalAndReferenceDate`, `getAllByHoldingAndDateRange`, etc.); DEVE limitar-se a **persistência** (`upsert`, `delete`) e, se necessário, **`getById`** com posição identificada no parâmetro. Carregar listas para `AssetHolding.transactions` é responsabilidade da camada de dados ao hidratar posição (não via repositório de transações exposto a use cases).
- **FR-014**: Consumidores que hoje injetam `GetTransactionsByHoldingUseCase` (ex. `GetHistoryTableDataUseCase`, `TransactionManagementViewModel`, `TransactionViewModel`) DEVEM migrar para posição com `transactions` já carregada.

### Key Entities

- **Registro de histórico mensal**: Snapshot de fim de mês (valor, quantidade, custo médio, total investido); raiz da leitura agregada **histórico → posição → transações**.
- **Posição (holding)**: Quem detém qual ativo em qual corretora (e meta opcional); agrega **lista de transações** (único dono da relação no domínio).
- **Transação de ativo**: Movimentação de compra ou venda **sem** referência embutida à posição; variantes por classe de ativo; pertence à posição apenas pela lista agregada.
- **Consulta de históricos**: Operação que, dado um mês (e filtros opcionais), devolve lista de registros de histórico com o grafo completo; **substitui** leituras isoladas de transações.
- **Consulta de posição**: Operação que devolve posição com **transações** preenchidas; **substitui** `GetTransactionsByHoldingUseCase` em ecrãs sem histórico.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em 100% dos testes de integração/domínio que carregam histórico para posição com transações conhecidas, a lista embutida na posição tem o **mesmo tamanho** e os **mesmos identificadores** que o armazenamento.
- **SC-002**: Fluxos que hoje precisam de **duas** leituras (histórico + transações) podem obter movimentações a partir de **uma** leitura de histórico, reduzindo chamadas de repositório em pelo menos **50%** nesses fluxos.
- **SC-003**: Posições sem movimentação retornam lista vazia em **100%** dos cenários de leitura, sem erro nem valor nulo.
- **SC-004**: Nenhum consumidor de domínio ou caso de uso passa a depender de tipos da camada de persistência para representar transações — verificável por revisão de dependências entre módulos.
- **SC-005**: O contrato público de transação **não** expõe posição — verificável por revisão estática do módulo de entidades e por testes que falham se a propriedade for reintroduzida.
- **SC-006**: **Zero** referências a `GetTransactionsUseCase` ou `GetTransactionsByHoldingUseCase` no código após a feature — verificável por busca no repositório e build sem símbolos removidos.
- **SC-007**: Nenhum caso de uso em `:domain:usecases` invoca métodos de **listagem** em `AssetTransactionRepository` — verificável por revisão do contrato e dos consumidores.

## Assumptions

- O escopo cobre **leitura, modelo de domínio e refatoração** dos consumidores que usam `transação.posição`; formulários de cadastro e importação B3 continuam a persistir `holdingId` no armazenamento via parâmetro de escrita `(holding, transaction)`, não via propriedade na entidade de transação.
- **Todas** as transações da posição entram na lista ao carregar via histórico (confirmado em clarificação 2026-06-03); filtros por mês permanecem opcionais nos consumidores.
- O grafo canónico de leitura é **histórico → posição → transações**; qualquer leitura de posição no armazenamento já traz **posição → transações** (lista completa).
- Impacto em performance aceitável para carteiras típicas do produto (dezenas a centenas de transações por posição); volumes muito grandes podem ser tratados em otimização posterior, fora desta feature.
- `GetHoldingHistoriesUseCase` mantém assinatura que devolve lista de registros de histórico; o enriquecimento e a remoção de `holding` em transação são breaking changes internos migrados no mesmo feature.
- `GetTransactionsUseCase` hoje não tem consumidores ativos (apenas definição); `GetTransactionsByHoldingUseCase` tem consumidores em histórico, composeApp e asset-management — todos migrados nesta feature.
- Listagens de transações no contrato `AssetTransactionRepository` deixam de ser API pública; implementação de hidratação permanece em datasource/database (detalhe no plano).

## Sequência de implementação (artefactos)

1. **Modelo** (`AssetHolding.transactions`, remover `holding` em transação) — bloqueia compilação.
2. **Port** `AssetTransactionRepository` reduzido + **hidratação** em `:data:database`.
3. **Escrita** `Param(holding, transaction)` — antes da camada de apresentação.
4. **Leitura** via `holding.transactions`; remover `GetTransactionsByHoldingUseCase` só após migrar consumidores.
5. **Verificação** `rg` (SC-005–SC-007) conforme `quickstart.md`.
