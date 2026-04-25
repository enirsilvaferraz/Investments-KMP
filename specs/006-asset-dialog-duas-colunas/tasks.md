# Tarefas: Dialog de asset em duas colunas

**Entrada:** Documentos de desenho em `specs/006-asset-dialog-duas-colunas/`  
**Pré-requisitos:** `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/ui-asset-dialog-two-columns.md`, `quickstart.md`

**Idioma:** Redigido em português do Brasil (pt-BR), conforme constitution.

## Fase 1: Configuração (infraestrutura partilhada)

**Objetivo:** Preparar base de trabalho e confirmar escopo dos pontos de entrada da UI.

- [X] T001 Mapear o estado atual do dialog em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T002 Revisar os campos e componentes do formulário em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormFields.kt`
- [X] T003 Consolidar decisões de layout e contrato da feature em `specs/006-asset-dialog-duas-colunas/contracts/ui-asset-dialog-two-columns.md`

---

## Fase 2: Fundações (pré-requisitos bloqueantes)

**Objetivo:** Criar estrutura base do layout em duas colunas para destravar as histórias.

**⚠️ CRÍTICO:** Nenhuma história começa antes desta fase.

- [X] T004 Implementar container base de duas colunas no dialog em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T005 [P] Implementar separador visual suave entre colunas em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T006 Garantir manutenção das ações Salvar/Cancelar com layout novo em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`

**Checkpoint:** Layout base pronto para implementar histórias.

---

## Fase 3: História de utilizador 1 — Visualizar formulário e histórico lado a lado (Prioridade: P1) 🎯 MVP

**Objetivo:** Exibir formulário e histórico simultaneamente no dialog, com comportamento correto para dados e vazio.

**Teste independente:** Abrir o dialog e confirmar duas colunas, separador suave, tabela de transações na direita e mensagem de vazio quando não houver dados.

### Implementação da US1

- [X] T007 [US1] Renderizar tabela de transações na coluna direita em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T008 [US1] Implementar estado vazio com mensagem "Histórico disponível após salvar a holding" em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T009 [US1] Implementar rolagem interna da área de histórico para listas longas em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T010 [US1] Ajustar composição para preservar altura estável do dialog com histórico extenso em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`

**Checkpoint:** US1 funcional e validável de forma independente.

---

## Fase 4: História de utilizador 2 — Manter a experiência atual do formulário (Prioridade: P2)

**Objetivo:** Preservar a estrutura atual da coluna de formulário sem regressão de comportamento.

**Teste independente:** Comparar com o dialog atual e validar que a coluna esquerda mantém os campos existentes (exceto corretora) e o comportamento de edição.

### Implementação da US2

- [X] T011 [US2] Remover o campo corretora da composição da coluna esquerda em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormFields.kt`
- [X] T012 [US2] Preservar ordem e comportamento dos demais campos da coluna esquerda em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormFields.kt`
- [X] T013 [US2] Integrar a coluna esquerda no novo layout sem regressão visual em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`

**Checkpoint:** US2 funcional e validável de forma independente.

---

## Fase 5: História de utilizador 3 — Encontrar corretora junto ao histórico (Prioridade: P3)

**Objetivo:** Posicionar corretamente a corretora no topo da coluna de histórico e manter edição funcional.

**Teste independente:** Confirmar campo corretora no topo da coluna direita e tabela logo abaixo, com edição de corretora funcionando.

### Implementação da US3

- [X] T014 [US3] Mover renderização do campo corretora para o topo da coluna direita em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`
- [X] T015 [P] [US3] Ajustar vínculo de eventos/estado da corretora após reposicionamento em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormFields.kt`
- [X] T016 [US3] Validar ordem final na coluna direita (corretora antes da tabela) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt`

**Checkpoint:** US3 funcional e validável de forma independente.

---

## Fase 6: Polimento e preocupações transversais

**Objetivo:** Fechar validações finais, build e documentação da feature.

- [X] T017 [P] Atualizar notas finais de validação em `specs/006-asset-dialog-duas-colunas/quickstart.md`
- [ ] T018 Executar validação manual completa do fluxo descrito em `specs/006-asset-dialog-duas-colunas/quickstart.md`
- [X] T019 Executar build do módulo com `./gradlew :features:asset-management:compileKotlinJvm` na raiz do repositório

---

## Dependências e ordem de execução

### Dependências entre fases

- Fase 1 -> sem dependências
- Fase 2 -> depende da Fase 1
- Fase 3 (US1) -> depende da Fase 2
- Fase 4 (US2) -> depende da Fase 2
- Fase 5 (US3) -> depende da Fase 2 e da conclusão da US1 (coluna direita pronta)
- Fase 6 -> depende da conclusão das US escolhidas

### Dependências entre histórias

- US1 (P1) é o MVP e pode ser entregue primeiro.
- US2 (P2) pode rodar em paralelo com US1 após Fase 2, com integração final em `AssetManagementFormView.kt`.
- US3 (P3) depende da estrutura da coluna direita definida na US1.

## Oportunidades de execução paralela

- **Fase 2:** T005 pode ocorrer em paralelo parcial com T004 (após criar estrutura mínima da coluna).
- **US2:** T011 e T012 podem ser executadas em paralelo.
- **US3:** T015 pode ser executada em paralelo parcial com T014.
- **Polimento:** T017 pode ocorrer em paralelo com T018.

## Estratégia de implementação

### MVP primeiro (recomendado)

1. Concluir Fases 1 e 2.
2. Entregar US1 (Fase 3) como incremento inicial.
3. Validar rapidamente com `quickstart.md`.

### Entrega incremental

1. Adicionar US2 para preservar experiência legada da coluna esquerda.
2. Adicionar US3 para completar o reposicionamento da corretora.
3. Fechar com Fase 6 (validação completa + build).

### Critérios de completude por história

- **US1:** duas colunas + histórico + vazio orientativo + rolagem interna.
- **US2:** coluna esquerda equivalente à atual, sem corretora.
- **US3:** corretora no topo da coluna direita e interação funcionando.
