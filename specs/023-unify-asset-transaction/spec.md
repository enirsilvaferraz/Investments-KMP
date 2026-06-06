# Feature Specification: Modelo unificado de transações de ativos

**Feature Branch**: `023-unify-asset-transaction`

**Created**: 2026-06-06

**Status**: Draft

**Input**: User description: "vamos unificar as subclasses de AssetTransaction. A partir de agora todas os tipos de transações vao ter id, date, type, quantity, unitPrice e totalValue. Nao haverá mais observations. O campo total value sempre vai ser desabilitado no formulario e calculado multiplicando quantity e unitPrice. Para fundos e renda fixa quantity sera sempre 1 e ficar desabilitado. Devemos alterar o banco de dados e o formulario de cadastro."

## Clarifications

### Session 2026-06-06

- Q: Após a unificação, como deve ser a representação de domínio da transação? → A: Tipo único — uma só classe/registo de transação; subclasses específicas por classe de ativo são removidas.
- Q: Qual regra de arredondamento aplicar a preço unitário e valor total? → A: ~~2 casas decimais~~ **Revisto** — sem arredondamento na persistência (ver Refinamento 2026-06-06).
- Q: A adaptação de fontes externas (ex.: sincronização B3) faz parte desta feature? → A: **Revisto** — não existe sistema de importação de transações no projeto (ver Refinamento 2026-06-06).
- Q: Quais interfaces de cadastro/edição devem ser atualizadas nesta feature? → A: Só gestão de ativos — apenas o formulário inline da AssetManagementScreen (feature 022).
- Q: O valor total deve ser persistido no armazenamento? → A: Derivado na leitura — armazenamento guarda só quantidade e preço unitário; `totalValue` é calculado ao carregar.

### Refinamento 2026-06-06

- Sem validação de quantidade ou preço unitário no formulário (valores zero, negativos ou inválidos não bloqueiam salvamento).
- Quantidade em renda variável: sempre inteiro positivo (sem frações).
- Não há sistema de importação de transações no projeto.
- Sem arredondamento na persistência; quantidade e preço unitário gravados com a precisão informada.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cadastrar transação com campos uniformes (Priority: P1)

O investidor regista uma movimentação (compra ou venda) no formulário de cadastro de transações. Independentemente da classe do ativo (renda fixa, renda variável ou fundo), o formulário apresenta sempre os mesmos campos: data, tipo, quantidade, preço unitário e valor total. O valor total é calculado automaticamente e não pode ser editado manualmente. Para renda fixa e fundos, a quantidade aparece fixa em 1 e não pode ser alterada; o investidor informa o preço unitário (equivalente ao valor da operação) e o sistema calcula o valor total.

**Why this priority**: É o fluxo central da feature — unificar o modelo e simplificar o cadastro para todas as classes de ativo.

**Independent Test**: Abrir o formulário para cada classe de ativo, preencher data, tipo e preço unitário (e quantidade quando aplicável), salvar e verificar que o armazenamento guarda quantidade e preço unitário; ao recarregar, o valor total exibido corresponde a quantidade × preço unitário.

**Acceptance Scenarios**:

1. **Given** o investidor cadastra uma transação de **renda variável**, **When** informa quantidade e preço unitário, **Then** o campo valor total é atualizado automaticamente como quantidade × preço unitário e permanece somente leitura.
2. **Given** o investidor cadastra uma transação de **renda fixa** ou **fundo**, **When** o formulário é exibido, **Then** o campo quantidade mostra 1, está desabilitado e o valor total é calculado como 1 × preço unitário.
3. **Given** o investidor altera quantidade ou preço unitário em qualquer linha do formulário, **When** os campos são alterados, **Then** o valor total reflete imediatamente o produto atualizado, sem permitir edição direta do total.
4. **Given** o investidor preenche os campos e confirma o cadastro, **When** salva, **Then** a transação é gravada com identificador, data, tipo, quantidade e preço unitário; o valor total exibido após recarregar coincide com o calculado no formulário.

---

### User Story 2 - Editar e consultar transações existentes após a unificação (Priority: P1)

O investidor abre transações já cadastradas (em gestão de posição ou histórico). Cada transação é apresentada e editável com o mesmo conjunto de campos uniformes. Dados legados que antes tinham apenas valor total (renda fixa e fundos) são exibidos com quantidade 1 e preço unitário igual ao valor total original. O campo de observações deixa de existir em qualquer tela de cadastro ou edição.

**Why this priority**: Garante continuidade dos dados existentes e elimina inconsistência entre leitura e escrita após a mudança de modelo.

**Independent Test**: Carregar posições com transações antigas de cada classe, verificar exibição correta dos campos migrados, editar e salvar sem perda de integridade numérica.

**Acceptance Scenarios**:

1. **Given** existem transações de renda fixa ou fundo cadastradas antes da unificação (apenas com valor total), **When** o investidor abre o formulário, **Then** cada transação mostra quantidade 1, preço unitário igual ao valor total histórico e valor total recalculado coerente.
2. **Given** existem transações de renda variável com quantidade e preço unitário, **When** o investidor abre o formulário, **Then** os valores originais são preservados e o valor total continua derivado do produto.
3. **Given** uma transação antiga continha texto em observações, **When** o investidor abre ou edita essa transação após a migração, **Then** o campo observações não é exibido e o conteúdo anterior não é recuperável na interface nem nos novos registos.
4. **Given** o investidor edita quantidade ou preço unitário de uma transação existente, **When** salva, **Then** quantidade e preço unitário atualizados são gravados e o valor total exibido na releitura corresponde ao novo produto.

---

### User Story 3 - Persistência com esquema unificado (Priority: P2)

O sistema armazena todas as transações com a mesma estrutura persistida: identificador, data, tipo, quantidade e preço unitário. O valor total **não** é coluna de armazenamento — é derivado na leitura. Não existe coluna ou atributo de observações. Leituras de posição e histórico devolvem transações no formato unificado do domínio (incluindo valor total calculado), sem distinção por subtipo.

**Why this priority**: A unificação no armazenamento é pré-requisito para o formulário e para consumidores downstream (histórico, cálculos de saldo).

**Independent Test**: Inspecionar registos persistidos após cadastro e migração; confirmar presença de quantidade e preço unitário, ausência de coluna de valor total e de observações; confirmar valor total correto na leitura do domínio.

**Acceptance Scenarios**:

1. **Given** uma transação é salva para qualquer classe de ativo, **When** é lida de volta pelo sistema, **Then** o armazenamento contém identificador, data, tipo, quantidade e preço unitário; o objeto de domínio expõe ainda valor total derivado — instância do **tipo único**, sem subclasses por classe de ativo.
2. **Given** a base de dados contém registos no formato antigo, **When** a migração é aplicada, **Then** todos os registos passam a ter quantidade e preço unitário preenchidos (renda fixa/fundos: quantidade 1, preço unitário = valor total anterior; renda variável: valores existentes preservados); colunas legadas de valor total e observações são removidas; valor total na leitura permanece coerente com o produto.
3. **Given** a migração concluiu, **When** qualquer consulta de transações é executada, **Then** nenhum registo expõe ou exige o campo observações.

---

### Edge Cases

- O que acontece quando quantidade ou preço unitário são zero, negativos ou inválidos? **Não há validação** — o formulário permite salvar; o valor total permanece somente leitura e continua derivado de quantidade × preço unitário.
- Como o sistema trata quantidade em renda variável? A quantidade é sempre um **valor inteiro positivo** (sem frações decimais).
- O que acontece se o investidor altera apenas o preço unitário em renda fixa/fundos? O valor total deve atualizar automaticamente (1 × novo preço unitário).
- Não existe sistema de importação de transações no projeto; a migração cobre apenas registos já persistidos via cadastro manual.
- Valor total é **derivado** (quantidade × preço unitário) em exibição e leitura — não há coluna de valor total no armazenamento. **Não há arredondamento** na persistência; quantidade e preço unitário são gravados com a precisão informada pelo utilizador.
- Migração legada: valor total na leitura após migração deve corresponder exatamente ao produto quantidade × preço unitário derivado dos dados migrados.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE representar toda transação de ativo com um **único tipo concreto** (sem subclasses por classe de ativo) expondo no domínio: identificador, data, tipo de transação (compra ou venda), quantidade, preço unitário e valor total (**derivado** na leitura).
- **FR-002**: O sistema DEVE remover o atributo observações do modelo de transação, do armazenamento persistente e de todos os formulários de cadastro e edição.
- **FR-003**: O formulário de cadastro DEVE exibir os campos quantidade, preço unitário e valor total para todas as classes de ativo (renda fixa, renda variável e fundos).
- **FR-004**: O campo valor total no formulário DEVE permanecer sempre desabilitado (somente leitura) e DEVE ser calculado automaticamente como quantidade × preço unitário sempre que quantidade ou preço unitário mudarem.
- **FR-005**: Para transações de **renda fixa** e **fundos**, o formulário DEVE fixar quantidade em 1 e manter esse campo desabilitado; o investidor informa o preço unitário para definir o valor da operação.
- **FR-006**: Para transações de **renda variável**, o formulário DEVE permitir edição da quantidade (**inteiro positivo**) e do preço unitário.
- **FR-007**: O armazenamento DEVE persistir apenas quantidade e preço unitário (além de identificador, data e tipo); valor total NÃO é coluna persistida. O investidor não pode gravar valor total manualmente — o formulário calcula e exibe o derivado.
- **FR-008**: O sistema DEVE migrar dados existentes para o modelo unificado: transações de renda fixa e fundos que possuíam apenas valor total passam a ter quantidade 1 e preço unitário igual ao valor total anterior; transações de renda variável preservam quantidade e preço unitário existentes.
- **FR-009**: Após a migração, consultas de posição e histórico DEVEM devolver instâncias do tipo único de transação; consumidores de leitura NÃO DEVEM ramificar por subtipo de transação — a classe do ativo permanece na posição/ativo, não na transação.
- **FR-012**: Valor total DEVE ser calculado como quantidade × preço unitário e exposto na leitura e no formulário — **sem** gravação em coluna própria. Quantidade e preço unitário são persistidos **sem arredondamento** aplicado na gravação.
- **FR-011**: O **único** formulário de cadastro/edição no escopo é o inline da gestão de ativos (`AssetManagementScreen`, feature 022), que DEVE aderir às regras de campos, estados desabilitados e cálculo automático desta feature. Dialogs ou formulários legados de transações permanecem **fora do escopo** de alteração de UI.
- **FR-013**: Não existe sistema de importação de transações; o escopo limita-se a cadastro manual, migração de dados existentes e leitura no formato unificado.

### Key Entities

- **Transação de ativo**: Movimentação financeira (compra ou venda) associada a uma posição, modelada por **um único tipo concreto** partilhado por todas as classes de ativo. **Persistidos**: identificador, data, tipo, quantidade (inteiro em renda variável; 1 em renda fixa/fundos), preço unitário (sem arredondamento na gravação). **Derivado na leitura**: valor total (= quantidade × preço unitário). Sem observações e sem subclasses por categoria de produto.
- **Classe de ativo**: Categoria do produto (renda fixa, renda variável, fundo) que determina regras de interface para quantidade — editável em renda variável; fixa em 1 e somente leitura em renda fixa e fundos.
- **Rascunho de transação (formulário)**: Representação editável no cadastro com os mesmos campos da entidade persistida; valor total é campo calculado, nunca entrada livre do utilizador.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das transações persistidas após a feature (novas e migradas) possuem quantidade e preço unitário no armazenamento, sem coluna de observações nem de valor total; na leitura do domínio, valor total derivado está disponível e é coerente com o produto.
- **SC-002**: Em testes de cadastro para as três classes de ativo, o investidor consegue registar uma transação em até 2 minutos sem precisar calcular manualmente o valor total.
- **SC-003**: 100% dos registos legados de renda fixa e fundos, após migração, exibem quantidade 1 e preço unitário igual ao valor total histórico, com valor total derivado idêntico ao produto quantidade × preço unitário.
- **SC-004**: No formulário inline da gestão de ativos, nenhum campo de observações é exibido ou aceito; layout uniforme (quantidade, preço unitário, valor total) para todas as classes de ativo.
- **SC-005**: 95% dos utilizadores que já cadastravam transações conseguem completar o novo fluxo na primeira tentativa sem dificuldade relacionada a campos desabilitados ou cálculo automático de total.

## Assumptions

- Os tipos de transação (compra e venda) permanecem inalterados; apenas o modelo de dados e o formulário são unificados.
- A remoção de observações é definitiva: conteúdo histórico em observações não será migrado nem exibido.
- Quantidade em renda variável é sempre **inteiro positivo** (sem frações decimais).
- A migração de armazenamento é aplicada uma vez, de forma que aplicações atualizadas leiam apenas o esquema unificado.
- As subclasses de domínio por classe de ativo serão **removidas** e substituídas por um tipo único; consumidores que hoje ramificam por subtipo serão atualizados nesta mesma feature (escopo inclui persistência e formulário de cadastro mencionados pelo utilizador).
- Não existe sistema de importação de transações (FR-013); a migração cobre apenas dados já cadastrados manualmente (FR-008).
- Alteração de UI limita-se ao formulário inline da gestão de ativos (FR-011); dialogs legados de transações não são atualizados nesta feature.
- Valor total é atributo de domínio e de interface, mas **não** é coluna de armazenamento — derivado de quantidade × preço unitário na leitura (FR-007, FR-012).
- O formulário **não valida** quantidade nem preço unitário (sem bloqueio por zero, negativo ou formato inválido).
- Persistência **não aplica arredondamento** a quantidade nem a preço unitário.
