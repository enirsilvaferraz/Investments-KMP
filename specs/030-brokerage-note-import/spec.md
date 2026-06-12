# Feature Specification: Importação de Nota de Corretagem JSON

**Feature Branch**: `030-brokerage-note-import`

**Created**: 2026-06-12

**Status**: Draft

**Input**: User description: "Importar nota de corretagem JSON e integrar transações ao app, mapeando NoteAsset → AssetTransaction com AssetHolding correspondente, salvando taxa proporcional (NoteFeeAllocation) em cada transação, e exibindo taxa e valor líquido no formulário de cadastro de ativos."

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Importar nota e salvar transações com taxa (Priority: P1)

O usuário carrega um arquivo JSON de nota de corretagem. O app interpreta os ativos da nota, calcula as taxas proporcionais a cada transação e persiste cada transação vinculada ao respectivo `AssetHolding`, já com o valor da taxa alocada registrado.

**Why this priority**: É o fluxo central da feature — sem a importação e persistência, nenhum outro comportamento tem valor.

**Independent Test**: Pode ser testado fornecendo um JSON válido de nota, verificando que as transações aparecem na listagem de posições com o campo de taxa preenchido e o valor líquido correto.

**Acceptance Scenarios**:

1. **Given** um arquivo JSON de nota de corretagem válido, **When** o usuário aciona a importação, **Then** cada ativo da nota é salvo como uma `AssetTransaction` no `AssetHolding` correspondente, com a taxa proporcional calculada e persistida.
2. **Given** uma nota com múltiplos ativos e taxas, **When** a importação é concluída, **Then** a soma das taxas alocadas a cada transação é igual à soma total das taxas rateáveis da nota.
3. **Given** uma nota cujo `AssetHolding` ainda não existe no banco, **When** a importação é acionada, **Then** nenhuma transação é persistida e o ocorrido é registrado no log de console.

---

### User Story 2 — Visualizar taxa e valor líquido no formulário de cadastro (Priority: P2)

No formulário de cadastro/edição de transações de ativos, o usuário vê o campo **valor líquido** (valor bruto ± taxa, conforme direção da operação).

**Why this priority**: Dá visibilidade ao impacto das taxas na posição, sem bloquear o fluxo principal de importação.

**Independent Test**: Pode ser testado abrindo o formulário de cadastro de uma transação já existente e confirmando que o campo de valor líquido é exibido corretamente.

**Acceptance Scenarios**:

1. **Given** uma transação de compra com taxa alocada, **When** o formulário é aberto, **Then** o valor líquido exibido é `valor_bruto + taxa`.
2. **Given** uma transação de venda com taxa alocada, **When** o formulário é aberto, **Then** o valor líquido exibido é `valor_bruto − taxa`.
3. **Given** uma transação sem taxa alocada (cadastro manual), **When** o formulário é aberto, **Then** o valor líquido exibido é igual ao valor bruto.

---


### Edge Cases

- O que acontece se o `AssetHolding` para um ticker da nota não existir no banco de dados?
- Nota com mesmo ticker em múltiplas linhas: uma `AssetTransaction` por linha, sem agrupamento.
- Como o sistema lida com taxas zero (nota sem taxas rateáveis)?
- Importação duplicada do mesmo arquivo cria novas transações sem verificação de duplicidade.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE calcular a taxa proporcional de cada transação da nota usando a lógica existente de `NoteFeeAllocation.calculate`.
- **FR-002**: O sistema DEVE persistir a taxa alocada como campo da `AssetTransaction` salva no banco de dados.
- **FR-003**: O sistema DEVE associar cada `AssetTransaction` importada ao `AssetHolding` correto, identificado pelo ticker do ativo; a corretora é sempre Nubank (código 2), fixo para todas as importações.
- **FR-004**: O sistema DEVE cancelar a importação (sem persistir nada) se qualquer `AssetHolding` referenciado na nota não existir previamente no banco, registrando o ocorrido apenas no log de console.
- **FR-007**: O formulário de cadastro de transações DEVE exibir o campo **valor líquido**, calculado em tempo real com base no valor bruto e na taxa alocada, respeitando a direção da operação (compra/venda).
- **FR-008**: O valor líquido exibido DEVE ser coerente com a fórmula: compra → `valor_bruto + taxa`; venda → `valor_bruto − taxa`.

### Key Entities

- **NoteAsset** (DTO de entrada): representação de um ativo dentro do JSON da nota; atributos: `ticker`, `specification`, `movement`, `quantity`, `unitPrice`, `grossValue`.
- **AssetTransaction** (domínio): transação de um ativo; precisa receber novo campo `allocatedFee` para armazenar a taxa proporcional calculada.
- **AssetHolding** (domínio): posição do ativo em corretora; vincula transações ao ativo e ao proprietário.
- **NoteFeeAllocation** (domínio): resultado do rateio de taxas — mapeia cada `AssetTransaction` ao respectivo valor líquido.
- **BrokerageNote** (domínio): nota de corretagem completa com metadados, ativos e resumo financeiro; já existente.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O usuário consegue importar uma nota de corretagem válida e visualizar todas as transações resultantes no app em menos de 5 segundos após confirmar a importação.
- **SC-002**: 100% das transações importadas apresentam taxa alocada corretamente calculada — a soma das taxas alocadas deve fechar com a soma das taxas rateáveis da nota.
- **SC-003**: O formulário de cadastro exibe o campo de valor líquido em 100% das transações visualizadas, inclusive as cadastradas manualmente (com taxa = 0).
- **SC-004**: 0 (zero) transações são persistidas quando algum `AssetHolding` referenciado na nota não existe no banco; o cancelamento é registrado apenas no log de console, sem interrupção da UI.

---

## Clarifications

### Session 2026-06-12

- Q: Se o usuário importar a mesma nota duas vezes, qual deve ser o comportamento? → A: Permitir duplicatas — cria novas transações sem verificação.
- Q: Como o sistema identifica a corretora para fazer o match ticker → AssetHolding? → A: Corretora fixa — sempre Nubank (código 2); match feito apenas pelo ticker.
- Q: Se o mesmo ticker aparecer em múltiplas linhas da nota, quantas transações são criadas? → A: Uma `AssetTransaction` por linha, sem agrupamento.

---

## Assumptions

- O arquivo JSON de nota de corretagem já é carregado, parseado e validado pelo `BrokerageNoteJsonDataSource` existente em `core/data/filestore`; esta feature consome apenas o resultado já validado, sem reimplementar parser ou validação de estrutura/fechamento contábil.
- O `AssetHolding` correspondente a cada ticker da nota deve ser criado manualmente pelo usuário antes da importação; a feature não cria holdings automaticamente. A corretora é sempre Nubank (código 2) — escopo restrito a notas do Nubank.
- A taxa é sempre um campo de leitura no contexto de transações importadas de nota; para transações manuais, a taxa começa em zero e pode ser editada manualmente.
- O formulário de cadastro de ativos referenciado é o `TransactionManagementView` em `:features:asset-management`.
- A persistência ocorre de forma transacional: ou todas as transações da nota são salvas, ou nenhuma (rollback em caso de falha).
- A feature não cobre importação de múltiplas notas em lote — escopo limitado a uma nota por vez.
