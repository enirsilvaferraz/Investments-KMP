# Feature Specification: Dialog Unificado de Cadastro de Ativo + Holding

**Feature Branch**: `001-asset-holding-dialog`

**Created**: 2026-05-17

**Status**: Draft

**Input**: User description: "O sistema precisa inserir um investimento junto com seu holding no banco de dados. Já temos implementado o cadastro de assets e holdings separadamente. O formulário de asset deve incorporar o de holding em um dialog, gerenciado por um ViewModel de dialog separado."

## Clarifications

### Session 2026-05-17

- Q: Como o usuário cancela/fecha o dialog sem salvar? → A: Botão "X" (fechar) no topo do dialog, controlado pelo DialogViewModel. Descarta dados sem confirmar.
- Q: Na edição de holding via histórico, quais campos são editáveis? → A: Todos os campos são editáveis exceto a categoria do ativo.
- Q: Se o ativo for salvo mas o holding falhar, qual o comportamento? → A: Sequencial sem rollback — asset permanece salvo e usuário é notificado para tentar o holding novamente.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cadastrar novo investimento com holding (Priority: P1)

O usuário clica no botão de adicionar (+) na navegação principal. Um dialog em tela cheia abre contendo o formulário de cadastro de ativo. Ao preencher os campos do ativo (categoria, emissor, tipo, etc.) e selecionar a corretora (holding), o sistema persiste ambos — ativo e holding — no banco de dados em uma única operação. Após o salvamento bem-sucedido, o dialog é fechado automaticamente.

**Why this priority**: Fluxo principal de uso — sem cadastro de investimento + holding, o app não entrega valor ao usuário.

**Independent Test**: Pode ser testado abrindo o dialog, preenchendo todos os campos obrigatórios do ativo e da corretora, clicando em salvar, e verificando que o ativo e o holding foram persistidos no banco e o dialog fechou.

**Acceptance Scenarios**:

1. **Given** o usuário está na tela principal, **When** clica no FAB (+), **Then** um dialog em tela cheia abre com o formulário de cadastro de ativo incluindo campo de corretora (holding).
2. **Given** o formulário está aberto com todos os campos preenchidos corretamente, **When** o usuário clica em "Salvar", **Then** o ativo e o holding são persistidos no banco e o dialog é fechado.
3. **Given** o formulário possui campos obrigatórios não preenchidos, **When** o usuário clica em "Salvar", **Then** mensagens de erro de validação são exibidas e o dialog permanece aberto.
4. **Given** o dialog está aberto, **When** o usuário clica no botão "X" (fechar), **Then** o dialog é fechado imediatamente e quaisquer dados preenchidos são descartados sem confirmação.

---

### User Story 2 - Editar holding existente via dialog (Priority: P2)

O usuário acessa o histórico de holdings e seleciona uma entrada para editar. O dialog abre pré-populado com os dados do ativo associado e a corretora. O usuário pode alterar a corretora e salvar a alteração.

**Why this priority**: Fluxo secundário — permite correções sem precisar recriar o investimento.

**Independent Test**: Navegar ao histórico, clicar em editar um holding, verificar que o dialog abre com dados pré-populados, alterar a corretora, salvar e confirmar a persistência da mudança.

**Acceptance Scenarios**:

1. **Given** o usuário está no histórico de holdings, **When** clica em editar um holding, **Then** o dialog abre com os campos pré-populados do ativo e da corretora atual, com todos os campos editáveis exceto a categoria do ativo.
2. **Given** o dialog está aberto em modo de edição, **When** o usuário altera qualquer campo (exceto categoria) e salva, **Then** o ativo e o holding são atualizados no banco e o dialog fecha.

---

### User Story 3 - Separação de responsabilidades no dialog (Priority: P1)

O ViewModel do dialog (DialogViewModel) é responsável exclusivamente por gerenciar o ciclo de vida do dialog — abrir e fechar. O ViewModel de asset (AssetManagementViewModel) é responsável por orquestrar a persistência dos dados do ativo e do holding. Ao completar o salvamento com sucesso, o AssetManagementViewModel envia um sinal para o DialogViewModel fechar o dialog.

**Why this priority**: Garante extensibilidade futura (adicionar tela de transação ao mesmo dialog) e manutenibilidade do código.

**Independent Test**: Verificar que após salvar com sucesso, o dialog fecha; que fechar o dialog não depende do ViewModel de asset; que o ViewModel de asset não tem referência direta à navegação.

**Acceptance Scenarios**:

1. **Given** o salvamento do ativo + holding é concluído com sucesso, **When** o AssetManagementViewModel sinaliza conclusão, **Then** o DialogViewModel fecha o dialog.
2. **Given** o dialog está aberto, **When** ocorre um erro de salvamento, **Then** o dialog permanece aberto com feedback de erro.

---

### Edge Cases

- O que acontece se o usuário fecha o app enquanto o dialog está aberto? O estado parcial do formulário é descartado.
- Como o sistema trata falha de persistência no banco? Se o asset falhar, exibe erro e nada é salvo. Se o asset for salvo mas o holding falhar, o asset permanece salvo e o usuário é notificado para re-tentar o holding.
- O que acontece se o asset já existir (edição) mas o holding for novo? O sistema cria o novo holding vinculado ao asset existente.
- O que acontece se a lista de corretoras estiver vazia? O campo de corretora exibe estado vazio com indicação de que não há opções disponíveis.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE exibir o formulário de cadastro de ativo dentro de um dialog em tela cheia ao navegar para `AssetManagementRouting`.
- **FR-002**: O formulário de cadastro de ativo DEVE incorporar inline o campo de seleção de corretora (holding) que atualmente reside em `HoldingManagementView`.
- **FR-003**: O sistema DEVE persistir o ativo e o holding no banco de dados de forma sequencial ao clicar em "Salvar": primeiro o ativo, depois o holding. Se o ativo for salvo com sucesso mas o holding falhar, o ativo permanece persistido e o usuário é notificado para tentar salvar o holding novamente.
- **FR-004**: O dialog DEVE ser gerenciado por um ViewModel de dialog separado (`DialogViewModel`) que controla abrir/fechar, incluindo um botão "X" no topo para fechamento imediato sem confirmação.
- **FR-005**: O `AssetManagementViewModel` DEVE enviar um sinal ao `DialogViewModel` para fechar o dialog após salvamento bem-sucedido.
- **FR-006**: O `AssetManagementViewModel` DEVE permanecer focado apenas em orquestrar a persistência de dados (ativo + holding), sem gerenciar navegação.
- **FR-007**: O arquivo `HoldingManagementView.kt` DEVE ser removido após a incorporação do campo de corretora no formulário de ativo.
- **FR-008**: O sistema DEVE validar campos obrigatórios antes de persistir e exibir mensagens de erro caso haja campos inválidos.
- **FR-009**: O sistema DEVE suportar tanto criação de novo investimento (asset + holding) quanto edição de holding existente via o mesmo dialog. Na edição, todos os campos são editáveis exceto a categoria do ativo.
- **FR-010**: A arquitetura DEVE permitir futura adição de tela de transação ao mesmo dialog sem refatoração significativa.

### Key Entities

- **Asset (Ativo)**: Representa o investimento cadastrado — contém categoria, emissor, tipo, subtipo, rentabilidade, vencimento e observações.
- **Holding**: Vínculo de um ativo a uma corretora — identifica onde o investimento está custodiado.
- **Brokerage (Corretora)**: Entidade de referência representando a instituição financeira custodiante.
- **DialogState**: Estado do dialog (aberto/fechado), gerenciado pelo DialogViewModel.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O usuário consegue cadastrar um investimento completo (ativo + holding) em uma única interação de dialog sem navegar entre telas.
- **SC-002**: O dialog fecha automaticamente em até 1 segundo após a confirmação de salvamento bem-sucedido.
- **SC-003**: Todos os campos obrigatórios não preenchidos geram feedback visual de erro antes de qualquer tentativa de persistência.
- **SC-004**: A edição de um holding existente carrega os dados em menos de 2 segundos e permite alteração sem perder dados do ativo associado.
- **SC-005**: A incorporação do holding no formulário de asset permite futura extensão (ex.: transações) sem necessidade de alterar a estrutura do dialog ou seus ViewModels.

## Assumptions

- O sistema de navegação existente (Navigation 3 com `DialogSceneStrategy`) já suporta dialogs em tela cheia e será reutilizado.
- O `AssetManagementRouting` já está registrado como rota de dialog na app — apenas o conteúdo interno precisa ser implementado.
- Os UseCases e Repositories para persistência de asset e holding já existem e podem ser reutilizados.
- A comunicação entre ViewModels (AssetManagementViewModel → DialogViewModel) será feita via mecanismo reativo (ex.: SharedFlow ou callback).
- A remoção de `HoldingManagementView.kt` é segura pois nenhum outro ponto do app depende diretamente desse arquivo.
- A tela de transação será incorporada no futuro mas está fora do escopo desta feature.
