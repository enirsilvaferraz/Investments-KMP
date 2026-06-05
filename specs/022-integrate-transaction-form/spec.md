# Feature Specification: Integração do Formulário de Transações na Tela de Gestão de Ativos

**Feature Branch**: `022-integrate-transaction-form`

**Created**: 2026-06-05

**Status**: Draft

## Clarifications

### Session 2026-06-05

- Q: O que deve acontecer com a lista de transações ao mudar a `assetClass`? → A: Limpar todos os rascunhos ao trocar a `assetClass`.
- Q: O botão "Salvar" deve ser desabilitado enquanto o salvamento está em andamento? → A: Sim, desabilitar apenas durante `isSaving = true`; reabilitado ao concluir.
- Q: O que o sistema deve fazer quando o salvamento unificado falha parcialmente? → A: Tudo deve ser salvo em uma única transação de banco de dados — o fluxo de salvamento deve ser alterado para ser verdadeiramente atômico (ativo + holding + transações em uma só operação).

**Input**: Integrar o card de transações diretamente na AssetManagementScreen, removendo `TransactionFormDialog` e `TransactionManagementRouting` (redirecionando `onTransactionManagerRequest` do histórico para `AssetManagementRouting`), unificando `TransactionManagementViewModel` no `AssetManagementViewModel` e salvando asset + holding + transações em um único botão salvar.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Cadastro de ativo com transações em fluxo único (Priority: P1)

O usuário preenche os dados do ativo, da posição (corretora/titular) e adiciona uma ou mais transações — tudo na mesma tela. Ao clicar em "Salvar", o sistema persiste o ativo, a posição e todas as transações em uma única operação, sem precisar abrir um diálogo separado.

**Why this priority**: Elimina o passo extra de abrir o diálogo de transações e garante que a tela de gestão de ativos seja autossuficiente, reduzindo erros de consistência entre dados do ativo e dados das transações.

**Independent Test**: Pode ser testado abrindo a tela de novo investimento, preenchendo todos os campos, adicionando ao menos uma transação e verificando que o clique em "Salvar" persiste tudo corretamente.

**Acceptance Scenarios**:

1. **Given** o usuário abre a tela de novo investimento, **When** preenche os dados do ativo, posição e adiciona uma transação, **Then** o botão "Salvar" único está sempre habilitado e ao clicar persiste ativo + posição + transação de forma atômica.
2. **Given** o usuário abre a tela com um investimento existente, **When** adiciona novas transações e edita as existentes, **Then** ao clicar em "Salvar" as alterações em ativo, posição e todas as transações são persistidas em conjunto.
3. **Given** o usuário abre a tela e adiciona múltiplas transações sem preencher dados obrigatórios do ativo, **When** clica em "Salvar", **Then** o sistema exibe erros de validação nos campos do ativo e não persiste nenhum dado.

---

### User Story 2 — Gestão de múltiplas transações inline (Priority: P2)

O usuário pode adicionar ilimitadas transações diretamente no card de Transações da tela de gestão de ativos, sem abrir diálogos auxiliares. Cada transação pode ser removida individualmente.

**Why this priority**: Fundamental para o fluxo de entrada de dados, pois aportes e resgates são registros recorrentes.

**Independent Test**: Pode ser testado adicionando múltiplas transações (3 ou mais) via botão "Adicionar" do card e verificando que todas ficam visíveis na tabela.

**Acceptance Scenarios**:

1. **Given** o card de Transações está visível na tela, **When** o usuário clica em "Adicionar", **Then** uma nova linha de transação em branco aparece na tabela (data corrente pré-preenchida).
2. **Given** há múltiplas transações na tabela, **When** o usuário clica em remover em uma linha específica, **Then** apenas aquela linha é removida e as demais permanecem intactas.
3. **Given** o usuário preenche transações com valores inválidos (data inválida, valor vazio), **When** clica em "Salvar", **Then** o sistema bloqueia a persistência e destaca os campos com erro.

---

### User Story 3 — Edição de investimento existente com transações já registradas (Priority: P3)

Ao abrir a tela para editar um investimento já existente, as transações previamente cadastradas são carregadas e exibidas no card de Transações da mesma tela, permitindo edição e adição de novas transações.

**Why this priority**: Garante que o fluxo de edição seja igualmente completo ao de criação.

**Independent Test**: Pode ser testado abrindo um investimento com transações existentes e verificando que elas aparecem no card, podendo ser editadas.

**Acceptance Scenarios**:

1. **Given** existe um investimento com transações registradas, **When** o usuário abre a tela de edição, **Then** as transações são exibidas na tabela do card, ordenadas por data.
2. **Given** o usuário edita uma transação existente na tabela, **When** clica em "Salvar", **Then** a transação atualizada é persistida junto com os dados do ativo e posição.
3. **Given** o usuário remove uma transação existente da tabela, **When** clica em "Salvar", **Then** a transação é excluída e as demais persistidas normalmente.

---

### Edge Cases

- O que acontece ao tentar salvar com a lista de transações vazia? → Deve ser permitido (posição sem transações é válida).
- O que acontece se o usuário remover todas as transações de um investimento existente e salvar? → As transações devem ser excluídas e o ativo/posição persistidos normalmente.
- O que acontece com o fluxo de transações a partir do histórico? → `onTransactionManagerRequest` passa a abrir `AssetManagementRouting(holdingId)`; `TransactionManagementRouting` e `TransactionFormDialog` são removidos do módulo.
- O que acontece quando o `assetClass` ainda não foi definido (novo cadastro sem seleção)? → O card de transações exibe o estado padrão (Renda Fixa) até que o usuário selecione a classe do ativo.
- O que acontece com os rascunhos de transação ao trocar a `assetClass`? → Todos os rascunhos são limpos (lista resetada para vazia), pois os campos de cada classe são incompatíveis entre si.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A tela de gestão de ativos DEVE exibir o card de Transações diretamente na sua estrutura, sem depender de diálogo separado.
- **FR-002**: O botão "Salvar" principal DEVE persistir o ativo, a posição (holding) e todas as transações em uma única transação de banco de dados — se qualquer parte falhar, nenhuma alteração é persistida (atomicidade total); em caso de falha, reverter todas as alterações, exibir mensagem de erro e manter a tela aberta com os dados preenchidos.
- **FR-003**: O card de Transações DEVE permitir adicionar quantas transações o usuário desejar sem limite predefinido.
- **FR-004**: O card de Transações DEVE permitir remover qualquer transação individualmente.
- **FR-005**: O botão "Salvar" principal DEVE estar habilitado em qualquer momento exceto durante o salvamento em andamento (`isSaving = true`), sem depender de lógica de `isDirty` ou snapshot.
- **FR-006**: `TransactionFormDialog`, `TransactionFormView` e `TransactionManagementRouting` DEVEM ser removidos do módulo; o histórico redireciona `onTransactionManagerRequest` para `AssetManagementRouting(holdingId)`.
- **FR-007**: O estado e a lógica do `TransactionManagementViewModel` DEVEM ser integrados ao `AssetManagementViewModel`, eliminando a instância independente do ViewModel de transações na tela.
- **FR-008**: O carregamento das transações existentes (edição) DEVE ocorrer como parte do carregamento inicial do `AssetManagementViewModel`, usando o `holdingId`.
- **FR-009**: A validação das transações DEVE ser executada antes de iniciar a persistência; erros de campo DEVEM bloquear o salvamento e exibir feedback visual.
- **FR-010**: A classe do ativo selecionada na seção de Ativo DEVE determinar o comportamento dos campos de transação (ex.: Renda Variável habilita Qtde e Valor Unit.; demais classes usam apenas Valor Total).
- **FR-011**: Ao trocar a `assetClass`, a lista de rascunhos de transação DEVE ser completamente limpa, pois os campos de cada classe são incompatíveis entre si.
- **FR-012**: O card de Resumo ESTÁ FORA DO ESCOPO desta feature — não deve ser alterado nem removido.

### Key Entities

- **AssetManagementUiState**: estado unificado contendo dados do ativo, posição e lista de rascunhos de transação (`TransactionDraftUi`).
- **TransactionDraftUi**: representa uma transação em edição (nova ou existente) com campos de data, tipo, quantidade, valor unitário e valor total.
- **AssetManagementEvents**: conjunto de eventos do ViewModel unificado, incluindo os eventos de transação anteriormente em `TransactionManagementEvents`.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O usuário consegue cadastrar um ativo com ao menos uma transação em uma única ação de "Salvar", sem abrir diálogos auxiliares.
- **SC-002**: O usuário consegue adicionar e remover transações na mesma tela de gestão de ativos, com resposta visual imediata a cada ação.
- **SC-003**: Ao abrir um investimento existente com transações, todas as transações previamente cadastradas são exibidas corretamente no card dentro de 1 segundo após abertura da tela.
- **SC-004**: Erros de validação (campo obrigatório vazio, data inválida) são exibidos sem fechar a tela, preservando os dados já preenchidos.
- **SC-005**: A substituição de `TransactionManagementRouting`/`TransactionFormDialog` por `AssetManagementRouting` no fluxo do histórico preserva a capacidade de gerir transações — o utilizador abre o mesmo diálogo unificado de gestão de ativos.

---

## Assumptions

- `TransactionFormDialog`, `TransactionFormView` e `TransactionManagementRouting` serão removidos — o único ponto de entrada para gestão de transações passa a ser `AssetManagementRouting` (inline na tela e via redirecionamento do histórico).
- Um novo caso de uso de persistência unificada será necessário (ex.: `SaveAssetWithTransactionsUseCase`) para garantir atomicidade total — ativo + holding + upserts + deletes de transações em uma única transação de banco de dados. Os use cases individuais existentes (`SaveTransactionUseCase`, `DeleteTransactionUseCase`) podem ser reutilizados internamente ou substituídos conforme o plano técnico.
- O campo `holding` ainda não existe no momento do primeiro salvamento (novo ativo); o `holdingId` será obtido após o upsert da posição e então usado para salvar as transações.
- O card de Resumo exibe dados calculados baseados nas transações, porém sua atualização reativa está fora do escopo — permanece com valores mockados como está atualmente.
- A lógica de `isDirty` do `TransactionManagementViewModel` não será replicada no estado unificado; o botão "Salvar" estará sempre habilitado conforme especificado.
- Testes unitários de novos/modificados casos de uso em `:domain:usecases` são obrigatórios se esta feature introduzir alterações nessa camada.
