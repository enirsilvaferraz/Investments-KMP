# Tarefas: Ajuste de corretora e tabela de transações por tipo de asset

**Entrada:** Documentos de desenho em `specs/008-ajustar-tabela-transacoes/`  
**Pré-requisitos:** `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/ui-asset-transactions-table-alignment.md`, `quickstart.md`

**Idioma:** Redigido em português do Brasil (pt-BR), conforme constitution.

## Formato: `[ID] [P?] [História] Descrição`

- **[P]**: Pode executar em paralelo (ficheiros diferentes, sem dependências diretas)
- **[História]**: História de utilizador (`[US1]`, `[US2]`, `[US3]`)
- Todas as tarefas incluem caminho de ficheiro explícito

---

## Fase 1: Configuração (infraestrutura partilhada)

**Objetivo:** Preparar estrutura de estado/eventos e mapear reuso dos inputs de tabela.

- [ ] T001 Revisar estado e eventos atuais da feature em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/UiState.kt` e `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/VMEvents.kt`
- [ ] T002 Mapear componentes de input em tabela já usados no histórico em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt`
- [ ] T003 [P] Validar disponibilidade de `TableInputMoney` e componentes correlatos em `core/presentation/design-system/src/commonMain/kotlin/com/eferraz/presentation/design_system/components/inputs/`

---

## Fase 2: Fundações (pré-requisitos bloqueantes)

**Objetivo:** Criar fundações comuns para colunas por tipo, validação global e sessão de linhas.

**⚠️ CRÍTICO:** Nenhuma história inicia antes desta fase.

- [ ] T004 Implementar estado de sessão de linhas (novas/existentes, validação e foco pendente) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/UiState.kt`
- [ ] T005 [P] Implementar estratégia de schema de colunas por tipo de asset em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`
- [ ] T006 Implementar regra central de bloqueio de salvar para qualquer linha inválida em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [ ] T007 [P] Ajustar/migrar inputs de tabela para camada compartilhada quando necessário em `core/presentation/design-system/src/commonMain/kotlin/com/eferraz/presentation/design_system/components/inputs/`

**Checkpoint:** Fundação pronta para desenvolvimento das histórias.

---

## Fase 3: História de utilizador 1 — Reposicionar corretora no formulário principal (Prioridade: P1) 🎯 MVP

**Objetivo:** Corretora volta para a primeira coluna e deixa de existir na seção de transações.

**Teste independente:** Abrir o diálogo e confirmar corretora apenas na primeira coluna.

- [ ] T008 [US1] Reintroduzir corretora na primeira coluna em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormFields.kt`
- [ ] T009 [US1] Remover corretora da seção de transações em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`
- [ ] T010 [US1] Garantir wiring de corretora no fluxo da tela em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`

**Checkpoint:** US1 funcional e validável de forma independente.

---

## Fase 4: História de utilizador 2 — Adicionar nova transação por linha em branco na tabela (Prioridade: P2)

**Objetivo:** Remover formulário de transação e criar nova linha em branco diretamente na tabela.

**Teste independente:** Tocar em adicionar e validar nova linha editável na tabela.

- [ ] T011 [US2] Remover bloco de formulário dedicado de transação em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`
- [ ] T012 [US2] Implementar ação de adicionar linha em branco na tabela em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [ ] T013 [US2] Implementar bloqueio de novo adicionar quando já houver linha inválida pendente em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [ ] T014 [US2] Exibir destaque/foco da linha inválida ao bloquear nova adição em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`

**Checkpoint:** US2 funcional e validável de forma independente.

---

## Fase 5: História de utilizador 3 — Campos por tipo e validação no salvar (Prioridade: P3)

**Objetivo:** Adaptar colunas por tipo de asset, permitir edição inline em todas as linhas e validar salvar global.

**Teste independente:** Trocar tipo de asset, editar linhas novas/existentes e validar bloqueio de salvar quando houver qualquer inválida.

- [ ] T015 [US3] Implementar renderização de colunas por tipo de asset em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`
- [ ] T016 [US3] Implementar descarte de valores incompatíveis ao trocar tipo com aviso de revisão em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [ ] T017 [US3] Aplicar edição inline para linhas novas e existentes usando componentes compartilhados em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`
- [ ] T018 [US3] Integrar uso de `TableInputMoney` (ou equivalente compartilhado) na edição inline em `core/presentation/design-system/src/commonMain/kotlin/com/eferraz/presentation/design_system/components/inputs/` e `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormTransaction.kt`
- [ ] T019 [US3] Implementar validação final que bloqueia salvar com qualquer linha inválida (nova ou existente) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`
- [ ] T020 [US3] Garantir persistência em lote somente com todas as linhas válidas em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/vm/AssetManagementViewModel.kt`

**Checkpoint:** US3 funcional e validável de forma independente.

---

## Fase 6: Polimento e preocupações transversais

**Objetivo:** Finalizar documentação, validação e build da feature.

- [ ] T021 [P] Revisar e ajustar roteiro de validação em `specs/008-ajustar-tabela-transacoes/quickstart.md` conforme implementação final
- [ ] T022 Executar compilação do módulo `:features:asset-management` via `core/presentation/asset-management/build.gradle.kts`
- [ ] T023 Executar validação manual completa descrita em `specs/008-ajustar-tabela-transacoes/quickstart.md`

---

## Dependências e ordem de execução

### Dependências entre fases

- **Fase 1:** sem dependências externas.
- **Fase 2:** depende da Fase 1 e bloqueia histórias.
- **Fase 3 (US1):** depende da Fase 2 e forma o MVP.
- **Fase 4 (US2):** depende da Fase 2 e da estrutura de UI estabilizada em US1.
- **Fase 5 (US3):** depende de US2 para operar sobre modelo final de tabela.
- **Fase 6:** depende das histórias concluídas.

### Dependências entre histórias

- **US1 (P1):** entrega base de layout e posicionamento de corretora.
- **US2 (P2):** entrega criação por linha na tabela.
- **US3 (P3):** entrega validação completa, colunas por tipo e integração de inputs compartilhados.

### Oportunidades de paralelização

- T003 pode rodar em paralelo com T001/T002.
- T005 e T007 podem rodar em paralelo após T004.
- T021 pode rodar em paralelo com T022/T023 no fechamento.

### Exemplos de execução paralela por história

- **US1:** T009 e T010 podem avançar em paralelo após T008.
- **US2:** T013 e T014 podem avançar em paralelo após T012.
- **US3:** T016 e T018 podem avançar em paralelo após T015.

---

## Estratégia de implementação

### MVP primeiro (US1)

1. Concluir Fases 1 e 2.
2. Entregar US1 (corretora reposicionada e removida da seção de transações).
3. Validar fluxo base do diálogo.

### Entrega incremental

1. Implementar US2 (criação de linha em branco e bloqueio de múltiplas inválidas).
2. Implementar US3 (colunas por tipo, edição inline completa, validação global no salvar e reuso de inputs compartilhados).
3. Fechar com build e checklist manual do quickstart.
