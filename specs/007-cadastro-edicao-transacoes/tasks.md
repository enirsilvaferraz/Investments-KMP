# Tarefas: Cadastro e edição de transações no diálogo de ativo

**Entrada:** Documentos de desenho em `specs/007-cadastro-edicao-transacoes/`  
**Pré-requisitos:** `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/ui-asset-transactions-inline-edit.md`, `quickstart.md`

**Idioma:** Redigido em português do Brasil (pt-BR), conforme constitution.

## Formato: `[ID] [P?] [História] Descrição`

- **[P]**: Pode executar em paralelo (ficheiros diferentes, sem dependências diretas)
- **[História]**: História de utilizador (`[US1]`, `[US2]`, `[US3]`)
- Todas as tarefas incluem caminho de ficheiro explícito

---

## Fase 1: Configuração (infraestrutura partilhada)

**Objetivo:** Preparar base do módulo para suportar sessão de transações em memória e novo fluxo de edição.

- [X] T001 Revisar e alinhar contratos de estado/eventos em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/UiState.kt` e `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/VMEvents.kt`
- [X] T002 Mapear pontos de integração do diálogo e da tabela em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T003 [P] Consolidar helpers de validação/normalização de campos de transação em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`

---

## Fase 2: Fundações (pré-requisitos bloqueantes)

**Objetivo:** Criar fundações comuns para todas as histórias (estado em memória, ordenação e ciclo de vida de sessão).

**⚠️ CRÍTICO:** Nenhuma história começa antes desta fase estar concluída.

- [X] T004 Implementar estrutura de sessão de edição em memória (base + draft + pendências) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/UiState.kt` e `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [X] T005 [P] Implementar estratégia única de ordenação por data decrescente para lista exibida em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [X] T006 Integrar ciclo de vida da sessão (inicialização e descarte ao cancelar/fechar) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt` e `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`

**Checkpoint:** Fundações prontas para iniciar histórias de utilizador.

---

## Fase 3: História de utilizador 1 — Criar transação associada ao holding (Prioridade: P1) 🎯 MVP

**Objetivo:** Permitir inclusão de nova transação vinculada ao holding atual e mantê-la em memória até salvar.

**Teste independente:** Criar uma nova transação válida no diálogo e validar exibição imediata na tabela sem persistência antes do salvar final.

- [X] T007 [US1] Implementar ação de adicionar nova transação ao draft da sessão em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [X] T008 [US1] Implementar fluxo de UI para criação de transação dentro da seção de transações em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`
- [X] T009 [US1] Integrar atualização imediata da tabela após criação no diálogo em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T010 [US1] Validar campos obrigatórios antes de inserir nova linha na tabela em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`

**Checkpoint:** US1 funcional e testável isoladamente.

---

## Fase 4: História de utilizador 2 — Editar transação na própria tabela (Prioridade: P2)

**Objetivo:** Permitir edição inline de todos os campos com commit em memória e rejeição de valor inválido.

**Teste independente:** Editar uma transação na célula da tabela, confirmar atualização em memória para valor válido e rejeição com feedback para valor inválido.

- [X] T011 [US2] Implementar edição inline para todos os campos da linha de transação em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`
- [X] T012 [US2] Implementar commit de edição válida no draft da sessão em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [X] T013 [US2] Implementar rejeição de edição inválida com restauração do valor anterior em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [X] T014 [US2] Implementar feedback visual de erro por célula inválida em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`

**Checkpoint:** US2 funcional e testável isoladamente.

---

## Fase 5: História de utilizador 3 — Persistir alterações apenas no salvar final (Prioridade: P3)

**Objetivo:** Persistir em lote no salvar final e descartar rascunho ao cancelar/fechar sem salvar.

**Teste independente:** Criar/editar transações, cancelar sem salvar para validar descarte; repetir e salvar para validar persistência em lote.

- [X] T015 [US3] Integrar payload de persistência em lote a partir do draft da sessão no fluxo de salvar em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [X] T016 [US3] Conectar salvar final do formulário para enviar criações/edições pendentes em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T017 [US3] Implementar descarte explícito de rascunho no cancelar/fechar sem salvar em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T018 [US3] Implementar tratamento de falha no salvar final mantendo pendências para nova tentativa em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [X] T019 [US3] Garantir ausência de ação de exclusão de transações no fluxo de UI em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`

**Checkpoint:** US3 funcional e testável isoladamente.

---

## Fase 6: Polimento e preocupações transversais

**Objetivo:** Fechar validação funcional, build e coerência documental.

- [ ] T020 [P] Atualizar documentação de comportamento da feature em `specs/007-cadastro-edicao-transacoes/quickstart.md` se ajustes de implementação mudarem passos de validação
- [X] T021 Executar validação de build do módulo `:features:asset-management` a partir de `core/presentation/asset-management/build.gradle.kts`
- [ ] T022 Executar checklist manual de aceite da feature conforme `specs/007-cadastro-edicao-transacoes/quickstart.md`

---

## Dependências e ordem de execução

### Dependências entre fases

- **Fase 1:** sem dependências externas.
- **Fase 2:** depende da Fase 1; bloqueia histórias.
- **Fase 3 (US1):** depende da Fase 2.
- **Fase 4 (US2):** depende da Fase 2 e pode iniciar após US1 estar estável no fluxo de tabela.
- **Fase 5 (US3):** depende de US1 e US2 para persistir lote completo.
- **Fase 6:** depende das histórias concluídas.

### Dependências entre histórias

- **US1 (P1):** primeira entrega de valor (MVP).
- **US2 (P2):** agrega edição inline sobre base de criação/listagem da US1.
- **US3 (P3):** fecha persistência e regras de cancelamento com base em criação/edição já funcionais.

### Oportunidades de paralelização

- T003 pode rodar em paralelo com T001/T002.
- T005 pode rodar em paralelo com T004.
- T020 pode rodar em paralelo com T021/T022 no fechamento.

### Exemplos de execução paralela por história

- **US1:** T008 e T010 podem avançar em paralelo após T007.
- **US2:** T013 e T014 podem avançar em paralelo após T011.
- **US3:** T017 e T019 podem avançar em paralelo após T016.

---

## Estratégia de implementação

### MVP primeiro (US1)

1. Concluir Fase 1 + Fase 2.
2. Entregar US1 (criação + exibição em memória).
3. Validar fluxo mínimo com salvar final.

### Entrega incremental

1. Adicionar US2 (edição inline completa e validação de erro).
2. Adicionar US3 (persistência em lote, descarte no cancelamento e falha de salvar).
3. Fechar com build e checklist de validação do quickstart.
