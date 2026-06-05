# Feature Specification: Cadastro de investimento — cards Ativo e Posicionamento

**Feature Branch**: `021-asset-registration-form`

**Created**: 2026-06-05

**Status**: Draft

**Input**: User description: "Vamos terminar a implementação da tela de novos investimentos. Campo novo: Isento de IR (valor default Não, persistido em renda fixa). Botão Salvar no final da página, habilitado em formulário em branco para novo ativo ou quando algum campo foi modificado. Escopo: somente card de ativo e card de posicionamento. Fora do escopo: transações e resumo (permanecem inalterados)."

## Clarifications

### Session 2026-06-05

- Q: Escopo de "completar o card ATIVO" além de Isento de IR → A: Card ATIVO **100% funcional** end-to-end (validações, persistência e UX de **todos** os campos da seção ATIVO).
- Q: Valor de Isento de IR para investimentos de renda fixa existentes sem o campo persistido → A: Tratar como **"Não"** (default retrocompatível).
- Q: Cards Transações e Resumo → A: **Fora do escopo** — permanecem como estão hoje, sem wiring de persistência nem alteração funcional nesta entrega.
- Q: O que o salvamento persiste nesta feature → A: **Ativo + holding (posicionamento/corretora)** em operação única iniciada pelo botão no final da página — alinhado à feature 001; **não** exige transação.
- Q: Comportamento do dialog após salvamento bem-sucedido → A: Dialog **fecha automaticamente** (mesmo critério da feature 001).
- Q: Campo **Titular** no card Posicionamento — editável ou automático? → A: **Somente leitura** — exibe o titular carregado automaticamente; **não** é editável nesta feature (corretora permanece o campo editável).
- Q: Barra inferior **Excluir / Concluir** — o que entra no escopo? → A: **Concluir = Salvar** — renomear e wirear como único ponto de persistência; **Excluir fora do escopo** (permanece desabilitado ou oculto).
- Q: Troca de **classe do ativo** em cadastro novo — o que acontece com os campos já preenchidos? → A: **Reset parcial** — limpar tipo e campos específicos da classe anterior; **manter** emissor e observações quando aplicável.
- Q: Comportamento quando a **lista de corretoras está vazia** → A: **Sempre haverá corretora** — assumir ao menos uma corretora disponível; sem UX especial para lista vazia nesta feature.

### Session 2026-06-05 (plano)

- Q: Lógica de habilitação do botão Salvar (`isDirty`/snapshot) → A: **Sem snapshot** — botão Salvar **sempre habilitado** na UI; cliques durante `isSaving` ignorados no ViewModel.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Card ATIVO completo e funcional (Priority: P1)

O investidor utiliza a seção **ATIVO** do dialog para cadastrar ou editar **todos** os dados do investimento (classe, tipo, emissor, campos específicos por classe, observações, identificador B3 quando aplicável e isenção de IR em renda fixa). Cada campo visível no card responde a interação, validação e mapeamento de estado de forma consistente — sem controles locais desconectados do formulário.

**Why this priority**: Define o critério de "terminar a implementação" da seção ATIVO; sem wiring completo, o salvamento falha na origem.

**Independent Test**: Percorrer cada campo do card ATIVO em cadastro novo e em edição; confirmar que alterações refletem no estado global do formulário e participam da validação do salvamento.

**Acceptance Scenarios**:

1. **Given** o card ATIVO está aberto, **When** o usuário interage com qualquer campo editável da seção, **Then** a alteração reflete no estado do formulário (sem estado local órfão na UI).
2. **Given** cadastro **novo** e o usuário **troca a classe do ativo**, **When** a nova classe é aplicada, **Then** tipo e campos específicos da classe anterior são **limpos**, preservando emissor e observações quando aplicável, e as opções de **Tipo** passam a refletir a nova classe.
3. **Given** campos obrigatórios do card ATIVO estão inválidos ou vazios, **When** o usuário aciona o salvamento no final da página, **Then** feedback de erro aparece nos campos afetados e **nada** é persistido.
4. **Given** todos os campos obrigatórios de ATIVO e Posicionamento estão válidos, **When** o usuário aciona o salvamento, **Then** os valores informados no card ATIVO são persistidos e recuperados ao reabrir o investimento.

---

### User Story 2 - Cadastrar investimento de renda fixa com isenção de IR (Priority: P1)

O investidor abre o fluxo de **novo investimento** e preenche o card **ATIVO** com classe **renda fixa**. Entre os campos do título, informa se o ativo é **isento de imposto de renda (IR)**. O valor inicial apresentado é **"Não"**. Ao concluir o cadastro, a escolha (Sim ou Não) fica **registrada junto ao investimento de renda fixa** e permanece disponível quando o investimento for aberto novamente para edição.

**Why this priority**: O campo fiscal é requisito explícito do produto e condiciona cálculos futuros de IR.

**Independent Test**: Abrir cadastro novo, manter ou alterar "Isento de IR", preencher Posicionamento, salvar e reabrir verificando persistência.

**Acceptance Scenarios**:

1. **Given** o usuário está cadastrando um **novo** investimento de renda fixa, **When** o card ATIVO é exibido, **Then** o campo **"Isento de IR"** está visível com valor padrão **"Não"**.
2. **Given** o formulário está completo e "Isento de IR" está em **"Sim"**, **When** o salvamento conclui com sucesso, **Then** ao reabrir o investimento o campo exibe **"Sim"**.
3. **Given** o formulário está completo e "Isento de IR" permanece **"Não"**, **When** o salvamento conclui com sucesso, **Then** ao reabrir o investimento o campo exibe **"Não"**.

---

### User Story 3 - Card Posicionamento funcional (Priority: P1)

O investidor visualiza o **titular** (carregado automaticamente, somente leitura) e **seleciona a corretora** no card **POSICIONAMENTO** para vincular o investimento à carteira. O titular é resolvido pelo sistema na abertura do formulário; a corretora reflete o estado global editável e participa da validação e persistência do holding junto com o ativo.

**Why this priority**: Sem posicionamento wired, o cadastro não completa o fluxo asset + holding da feature 001.

**Independent Test**: Confirmar titular exibido em modo leitura; selecionar corretora em cadastro novo, salvar pelo botão final, reabrir e confirmar corretora persistida; tentar salvar sem corretora e confirmar bloqueio.

**Acceptance Scenarios**:

1. **Given** o card POSICIONAMENTO está aberto, **When** o formulário carrega, **Then** o **titular** é exibido em **somente leitura** com o valor resolvido automaticamente pelo sistema.
2. **Given** o card POSICIONAMENTO está aberto, **When** o usuário seleciona uma corretora, **Then** a escolha reflete no estado global do formulário.
3. **Given** o usuário não informou **corretora** válida, **When** tenta salvar, **Then** o sistema bloqueia o salvamento e exibe feedback de campo obrigatório no Posicionamento.
4. **Given** ATIVO e corretora estão válidos, **When** o salvamento conclui com sucesso, **Then** o holding é persistido com titular automático e corretora selecionada, recuperados na edição.

---

### User Story 4 - Botão Salvar no final da página (Priority: P1)

O investidor encontra **um único botão de persistência na barra inferior** do dialog — o botão **Concluir** existente é **renomeado para "Salvar"** e passa a acionar a persistência de ativo + holding. O botão **Excluir** permanece **fora do escopo** (desabilitado ou oculto). O botão **Salvar permanece sempre habilitado** na UI (cadastro novo, edição, com ou sem alterações). Não há botões Salvar funcionais dentro dos cards ATIVO ou POSICIONAMENTO.

**Why this priority**: Centraliza persistência e evita salvamentos parciais por card.

**Independent Test**: Salvar clicável ao abrir (novo e edição); aciona persistência; validação bloqueia save inválido; sucesso fecha dialog; Excluir inactivo; sem Salvar nos cards.

**Acceptance Scenarios**:

1. **Given** cadastro **novo** ou **edição** de investimento, **When** o formulário é exibido, **Then** o botão **Salvar** na barra inferior está **habilitado**.
2. **Given** campos obrigatórios inválidos, **When** o usuário aciona Salvar, **Then** feedback de erro aparece e **nada** é persistido (dialog aberto).
3. **Given** salvamento em andamento, **When** o usuário clica Salvar novamente, **Then** cliques extra são **ignorados** no ViewModel (botão permanece habilitado na UI).
4. **Given** salvamento concluído com sucesso, **When** a operação termina, **Then** o dialog **fecha automaticamente**.
5. **Given** a barra inferior do dialog, **When** o formulário é exibido, **Then** o botão **Excluir** está **desabilitado ou oculto** (fora do escopo desta feature).

---

### User Story 5 - Campo Isento de IR apenas em renda fixa (Priority: P2)

O investidor que cadastra **renda variável** ou **fundo de investimento** não vê o campo "Isento de IR", pois a isenção aplica-se somente a títulos de **renda fixa**.

**Independent Test**: Alternar classe do investimento; verificar presença/ausência do campo e ausência de persistência de isenção em classes não-RF.

**Acceptance Scenarios**:

1. **Given** classe **renda fixa**, **When** o card ATIVO é renderizado, **Then** **"Isento de IR"** está visível.
2. **Given** classe **renda variável** ou **fundo**, **When** o card ATIVO é renderizado, **Then** **"Isento de IR"** **não** é exibido.
3. **Given** troca de renda fixa para outra classe **antes** de salvar, **When** o salvamento conclui, **Then** nenhuma isenção de IR é associada ao ativo.

---

### Edge Cases

- **Troca de classe durante cadastro novo**: reset **parcial** — limpar tipo e campos específicos da classe anterior; **preservar** emissor e observações; opções de Tipo atualizadas para a nova classe; ao voltar para renda fixa, "Isento de IR" reaparece com padrão **"Não"** se ainda não persistido.
- **Edição com classe bloqueada**: classe imutável; "Isento de IR" editável em RF existente.
- **Renda fixa legada sem isenção persistida**: carregar como **"Não"** até alteração explícita.
- **Salvamento em andamento**: botão Salvar **permanece habilitado** na UI; `onSave()` ignora cliques enquanto `isSaving`.
- **Falha parcial** (ativo salvo, holding falha): dialog aberto, dados preservados, usuário notificado — sequencial sem rollback (feature 001).
- **Sucesso**: dialog **fecha automaticamente** após persistência de ativo + holding.
- **Cards Transações e Resumo**: **inalterados** nesta entrega — sem alteração funcional nem wiring de persistência desta feature.
- **Identificador B3 em renda variável**: **não aplicável** — remover da UI de RV se presente; B3 só em renda fixa (domínio).
- **Botão Excluir**: **fora do escopo** — permanece desabilitado ou oculto na barra inferior.
- **Integração com IR regressivo (019)**: registra flag isento; motor de cálculo inalterado neste escopo.
- **Lista de corretoras vazia**: **não aplicável** — o produto assume ao menos uma corretora cadastrada (sem tratamento de empty state nesta entrega).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE entregar o card **ATIVO** **100% funcional** end-to-end: todos os campos visíveis DEVEM ter captura, validação e mapeamento de estado consistentes — incluindo **Isento de IR** (renda fixa, default **Não**); opções de **Tipo** DEVEM corresponder à classe selecionada.
- **FR-001a**: Em **cadastro novo**, ao trocar a **classe do ativo**, o sistema DEVE aplicar **reset parcial**: limpar **tipo** e campos específicos da classe anterior, **preservando** emissor e observações quando aplicável.
- **FR-002**: O sistema DEVE exibir **"Isento de IR"** (Sim/Não) **somente** em **renda fixa**.
- **FR-003**: O valor padrão de **"Isento de IR"** DEVE ser **"Não"** em cadastro novo de renda fixa.
- **FR-004**: **"Isento de IR"** DEVE ser persistido em renda fixa e recarregado na edição; registros legados sem valor DEVEM ser interpretados como **"Não"**.
- **FR-005**: Renda variável e fundos NÃO DEVEM exibir nem persistir isenção de IR.
- **FR-006**: O card **POSICIONAMENTO** DEVE estar **wired** ao estado global do formulário: **titular** exibido em **somente leitura** (resolvido automaticamente na abertura); **corretora** editável com validação e mapeamento para holding.
- **FR-007**: DEVE existir **um único** botão **Salvar** na **barra inferior** do dialog (botão **Concluir** renomeado e wireado); botões Salvar dentro dos cards ATIVO e POSICIONAMENTO DEVEM ser **removidos** (secção `FormCardActions` de persistência); botão **Excluir** permanece **fora do escopo** (desabilitado ou oculto).
- **FR-008**: O salvamento iniciado pelo botão final DEVE persistir **ativo** e **holding** (corretora) em sequência operacional única — **sem** exigir transação.
- **FR-009**: O salvamento DEVE exigir **corretora** válida (card Posicionamento) antes de concluir.
- **FR-010**: O botão Salvar final DEVE permanecer **sempre habilitado** na UI (cadastro novo e edição), **sem** lógica de snapshot ou `isDirty`.
- **FR-011**: Cliques repetidos durante persistência DEVEM ser **ignorados** no ViewModel (`isSaving`); o botão **não** deve ser desabilitado na UI por esse motivo.
- **FR-012**: Validação pré-salvamento DEVE cobrir campos obrigatórios de **ATIVO** e **Posicionamento**; erros impedem persistência e mantêm o dialog aberto.
- **FR-013**: Os cards **Transações** e **Resumo** DEVEM permanecer **fora do escopo** — sem alteração funcional, wiring de persistência ou impacto na lógica do botão Salvar desta feature.
- **FR-014**: Após salvamento **bem-sucedido**, o dialog DEVE **fechar automaticamente** (mesmo critério da feature 001).
- **FR-015**: O botão **Excluir** na barra inferior NÃO DEVE ser implementado nesta feature — permanece **desabilitado ou oculto**.

### Key Entities

- **Investimento (Ativo)**: dados do título; RF inclui **isenção de IR**, emissor, tipo, indexador, rentabilidade, liquidez, vencimento, observações, B3.
- **Holding**: vínculo ativo ↔ titular (automático) + corretora (seleção do usuário).
- **Titular (Owner)**: proprietário legal resolvido automaticamente; exibido no Posicionamento sem edição nesta feature.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: **100%** dos cadastros novos RF exibem "Isento de IR" com padrão **"Não"** antes de interação.
- **SC-002**: **100%** dos salvamentos RF com Sim/Não em isenção recuperam o mesmo valor ao reabrir.
- **SC-003**: Em cadastro novo **e** edição, botão Salvar **habilitado** ao abrir o formulário em **100%** dos casos.
- **SC-004**: Cliques em Salvar durante `isSaving` **não** disparam segunda persistência em **100%** dos casos (guard no ViewModel).
- **SC-005**: Formulário inválido bloqueia persistência com feedback antes de gravar em **100%** dos campos obrigatórios documentados.
- **SC-006**: **100%** dos salvamentos bem-sucedidos persistem **ativo + holding** verificáveis ao reabrir.
- **SC-007**: **100%** das tentativas sem corretora válida são **bloqueadas** com feedback antes de persistir.
- **SC-008**: **100%** dos campos editáveis do card ATIVO passam ida e volta via salvamento.
- **SC-009**: Após salvamento bem-sucedido, o dialog fecha em **até 1 segundo** (mesmo critério da feature 001).

## Assumptions

- Opções **Sim/Não** reutilizadas para "Isento de IR" (mesmo padrão de outros campos binários do produto).
- **Cadastro novo** = fluxo sem investimento/holding pré-carregado para edição.
- Botão **Salvar sempre habilitado** na UI — sem snapshot/`isDirty`; validação impede persistência inválida.
- Cards **Transações** e **Resumo** permanecem como implementados hoje até histórias futuras.
- Botão **Concluir** da barra inferior é **renomeado para Salvar** e wireado; **Excluir** fica fora do escopo.
- **Sempre existirá ao menos uma corretora** cadastrada no sistema — dropdown de corretora nunca vazio em produção.
- Ordem de persistência e tratamento de falha parcial seguem padrão da feature 001 (sequencial, sem rollback automático).
- Classe do ativo **imutável** em edição.
- **Titular** obtido via resolução automática na abertura (feature 001); **não** há seleção de titular pelo usuário nesta entrega.
- Motor fiscal **IncomeTax (019)** não integrado nesta feature — apenas persistência da flag isento.
