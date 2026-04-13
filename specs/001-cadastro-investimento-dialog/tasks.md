---
description: "Lista de tarefas — Diálogo de cadastro de investimento (Investments-KMP)"
---

# Tarefas: Diálogo de cadastro de investimento

**Entrada:** Documentos em `/Users/enirferraz/AndroidStudioProjects/Investments-KMP/specs/001-cadastro-investimento-dialog/`  
**Pré-requisitos:** [plan.md](./plan.md), [spec.md](./spec.md), [data-model.md](./data-model.md), [contracts/ui-investment-registration-dialog.md](./contracts/ui-investment-registration-dialog.md), [research.md](./research.md), [quickstart.md](./quickstart.md)

**Idioma:** Português do Brasil (pt-BR), conforme `.specify/memory/constitution.md` princípio VIII.

**Coerência:** Tarefas que alterarem domínio ou invariantes **devem** incluir actualização de `core/domain/entity/docs/DOMAIN.md` quando aplicável (princípio IX).

**Usecases:** Alterações em `core/domain/usecases/` **devem** incluir testes em `:domain:usecases` (`src/jvmTest/`) no mesmo ciclo (princípio V).

**Kotlin (`explicitApi`):** Respeitar `~/.cursor/rules/explicit-api.mdc` em todos os `.kt` novos ou alterados (princípio IV).

**Compose (`@Preview`):** Pré-visualizações **no mesmo ficheiro** que o composable ou formulário; **não** ficheiros dedicados só a previews (princípio VI; `.cursorrules`).

**Testes:** Seguir `~/.cursor/rules/test-patterns.mdc` (inglês, GIVEN/WHEN/THEN, MockK, dados inline).

**Refinamentos (análise de consistência):** Cobertura explícita de **CNPJ** opcional (RF-006 / edge case), **nome + ticker** distintos em renda variável, **liquidez em fundos** alinhada ao domínio, **sem inputs de liquidez** para renda variável (RF-006), decisão documentada sobre **`AssetHolding`/corretora/meta** no `UpsertInvestmentAssetUseCase`, e ordem **T009 → T010** (sem paralelizar modelo UI antes do ViewModel).

## Formato: `[ID] [P?] [História] Descrição`

- **[P]:** Pode executar em paralelo (ficheiros diferentes, sem dependência de tarefa incompleta na mesma frente)
- **[História]:** Apenas nas fases de user story (US1, US2, US3)

## Convenções de caminhos

Raiz do repositório: `/Users/enirferraz/AndroidStudioProjects/Investments-KMP/`. Módulos Gradle: `:features:asset-management`, `:domain:usecases`, `:data:database`, `:data:repositories`, `:apps:umbrellaApp`.

---

## Fase 1: Configuração (infraestrutura partilhada)

**Objetivo:** Garantir módulo feature e dependências Gradle prontos para MVI + Koin.

- [x] T001 [P] Rever e completar dependências Compose/ViewModel/Material3 em `core/presentation/asset-management/build.gradle.kts` conforme `plan.md`
- [x] T002 [P] Confirmar `implementation(projects.features.assetManagement)` em `core/apps/umbrellaApp/build.gradle.kts`

---

## Fase 2: Fundações (pré-requisitos bloqueantes)

**Objetivo:** Cadeia `Issuer` por ID, caso de uso de upsert com regras de negócio, testes `jvmTest`, DI Koin para o pacote `com.eferraz.asset_management`.

**⚠️ CRÍTICO:** Nenhuma história de utilizador fica completa até `UpsertInvestmentAssetUseCase` + `getById` + testes estarem implementados (excepto trabalho puramente visual sem persistência).

- [x] T003 Adicionar `getById(id: Long): Issuer?` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/datasources/IssuerDataSource.kt` e implementar em `core/data/database/src/commonMain/kotlin/com/eferraz/database/datasources/impl/IssuerDataSourceImpl.kt` usando `core/data/database/src/commonMain/kotlin/com/eferraz/database/daos/IssuerDao.kt`
- [x] T004 Adicionar `getById(id: Long): Issuer?` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/IssuerRepository.kt` e em `core/data/repositories/src/commonMain/kotlin/com/eferraz/repositories/IssuerRepositoryImpl.kt`
- [x] T005 Implementar `UpsertInvestmentAssetUseCase` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/UpsertInvestmentAssetUseCase.kt`: emissor **apenas** via `issuerRepository.getById`, **sem** `GetOrCreateIssuerUseCase` (**RF-012**); validação de negócio alinhada a `SaveAssetUseCase` onde aplicável; `assetRepository.upsert` para o `Asset`. **Decisão de âmbito (registar no PR):** este fluxo do diálogo **não** recolhe corretora/meta — **não** criar nem actualizar `AssetHolding` neste use case (apenas persistência do ativo), **salvo** alteração explícita de âmbito acordada na equipa. **Renda variável:** mapear **nome do ativo** e **ticker** como campos distintos para `VariableIncomeAsset` (**RF-006** / entidade). **Fundos:** preencher `liquidity` e `liquidityDays` conforme `InvestmentFundAsset` e **RF-007** (evitar assumir só o hardcode legado de `SaveAssetUseCase` sem cruzar com o domínio). Incluir testes de regra para estes mapeamentos em **T006** onde couber.
- [x] T006 [P] Adicionar `UpsertInvestmentAssetUseCaseTest.kt` em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/UpsertInvestmentAssetUseCaseTest.kt` com MockK e cenários GIVEN/WHEN/THEN (**depende de T005** concluída; em paralelo **apenas** com **T007**/**T008** após **T005** estar estável)
- [x] T007 Criar `AssetManagementModule.kt` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/di/AssetManagementModule.kt` com `@Module` e `@ComponentScan("com.eferraz.asset_management")`
- [x] T008 Incluir `AssetManagementModule::class` no array `modules` de `core/apps/umbrellaApp/src/commonMain/kotlin/com/eferraz/investments/MyKoinApp.kt`

**Checkpoint:** `./gradlew :data:repositories:compileKotlinJvm`, `./gradlew :domain:usecases:compileKotlinJvm`, `./gradlew :domain:usecases:jvmTest` com sucesso.

---

## Fase 3: História de utilizador 1 — Abrir e preencher cadastro por categoria (Prioridade: P1) 🎯 MVP

**Objetivo:** Diálogo com dropdown de `InvestmentCategory` (default renda fixa), campos condicionais por categoria, estado inicial **RF-014**, carregamento de emissores via `GetIssuersUseCase`.

**Teste independente:** Abrir o diálogo, alternar as três categorias e verificar apenas os campos esperados; estado inicial = renda fixa + resto vazio; lista de emissores carregada.

### Implementação da US1

- [x] T009 [US1] Definir modelos `@Immutable`, validação de **formato** e mensagens por campo em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementFormUi.kt` (campos por categoria conforme `data-model.md` e **RF-005**–**RF-007**). Incluir: **renda variável** com **nome** e **ticker** separados; **CNPJ** opcional com aceitação de entrada com ou sem máscara e normalização/validação claras para o utilizador (edge case da spec), mapeando para o tipo de domínio `CNPJ` quando preenchido. **Não** marcar como [P] — **T010** depende dos tipos/campos definidos aqui.
- [x] T010 [US1] Implementar MVI (`UiState`, `Intent`, `dispatch`) e carregamento com `GetIssuersUseCase` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementViewModel.kt` com `@KoinViewModel` e `@Provided` para use cases (**depende de T009** para alinhar estado aos modelos `FormUi`)
- [x] T011 [US1] Implementar UI de campos e dropdown de categoria em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementFormView.kt`: primeiro controlo = categoria (**RF-002**/**RF-003**); rodapé **Salvar**/**Cancelar** (**RF-008**); controlo **X** no canto superior direito com o mesmo fluxo que Cancelar (**RF-009**). **Renda variável:** **não** apresentar liquidez como input editável (valores fixos no domínio — **RF-006** e caso extremo da spec); verificação de aceitação antes de fechar **T014**.
- [x] T012 [US1] Implementar composição do diálogo e `when (UiState)` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementScreen.kt`
- [x] T013 [US1] Actualizar ponto de entrada público em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementContract.kt` (`koinViewModel`, `modifier`, callbacks de fecho alinhados a `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/App.kt`)
- [x] T014 [US1] Executar `./gradlew :features:asset-management:compileKotlinJvm` e corrigir erros de compilação

**Checkpoint:** US1 verificável manualmente: categorias e campos visíveis corretos; emissores listados.

---

## Fase 4: História de utilizador 2 — Salvar ou abandonar o cadastro (Prioridade: P2)

**Objetivo:** Salvar válido persiste e fecha coerente; Cancelar/X sem alterações fecha já; com alterações pede confirmação; validação com mensagens; falha de sistema **RF-015**; gravação em curso **RF-016**; catálogo vazio bloqueado (edge case).

**Teste independente:** Percorrer cenários de aceitação da US2 na spec; `./gradlew :domain:usecases:jvmTest` após alterações em use cases.

### Implementação da US2

- [x] T015 [US2] Integrar `UpsertInvestmentAssetUseCase` no fluxo `Intent.Save` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementViewModel.kt` mapeando erros de validação (`ValidateException` e chaves de campo) para o estado da UI; garantir que **CNPJ** (se aplicável) e restantes campos passam pela validação de UI antes do use case e que o payload só contém dados da **categoria corrente**
- [x] T016 [US2] Implementar `RequestClose`, confirmação de descarte e `onDismiss` em `AssetManagementViewModel.kt` e `AssetManagementFormView.kt` (comparação com estado inicial **RF-013**/**RF-014**)
- [x] T017 [US2] Implementar flag de gravação, impedir double submit e indicador visual em `AssetManagementViewModel.kt` e `AssetManagementFormView.kt` (**RF-016**)
- [x] T018 [US2] Tratar excepções/falha de gravação sem fechar o diálogo e com mensagem clara em `AssetManagementViewModel.kt` (**RF-015**)
- [x] T019 [US2] Tratar lista de emissores vazia (desactivar Salvar e mensagem) em `AssetManagementViewModel.kt` e `AssetManagementFormView.kt`
- [x] T020 [US2] Executar `./gradlew :domain:usecases:jvmTest` e `./gradlew :features:asset-management:compileKotlinJvm`

**Checkpoint:** Fluxo completo de salvar/cancelar/fechar conforme US2.

---

## Fase 5: História de utilizador 3 — Alternar categoria após edição (Prioridade: P3)

**Objetivo:** Ao mudar categoria, limpar apenas campos específicos obsoletos; manter emissor e observações.

**Teste independente:** Preencher campos de uma categoria, mudar categoria, salvar e verificar payload/domínio sem valores cruzados.

### Implementação da US3

- [x] T021 [US3] Actualizar lógica de `SelectCategory` / limpeza de estado em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementViewModel.kt` preservando emissor e observações
- [x] T022 [US3] Garantir mapeamento seguro para `UpsertInvestmentAssetUseCase` (apenas dados da categoria corrente; **sem** valores “presos” de outra categoria após `SelectCategory`) em `AssetManagementViewModel.kt` e, se necessário, validação defensiva em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/UpsertInvestmentAssetUseCase.kt`. Confirmar explicitamente **nome**/**ticker**/**CNPJ** em RV e campos de **liquidez**/**dias**/**vencimento** em fundos conforme **T005**/**T009**

**Checkpoint:** Cenários P3 da spec verificáveis.

---

## Fase 6: Polimento e preocupações transversais

**Objetivo:** Documentação de domínio alinhada, quickstart validado, integração compilada.

- [x] T023 [P] Actualizar `core/domain/entity/docs/DOMAIN.md` se o formulário do diálogo alterar invariantes ou vocabulário documentado
- [x] T024 [P] Revisar e ajustar `specs/001-cadastro-investimento-dialog/quickstart.md` face aos comandos e classes finais; opcionalmente documentar **passos manuais** para critérios **CS-002** e **CS-004** (usabilidade / tempo de fluxo), por serem métricas de ensaio e não tarefas de build
- [x] T025 Executar `./gradlew :apps:umbrellaApp:compileKotlinJvm` para validar `MyKoinApp`, `composeApp` e feature integrados

---

## Dependências e ordem de execução

### Dependências entre fases

- **Fase 1:** Sem dependências externas à feature — pode começar de imediato.
- **Fase 2:** Bloqueia conclusão plena das US2/US3 (persistência e testes de domínio); US1 pode avançar em paralelo em **protótipo UI** mas **T014** só fecha MVP US1 com integração real se Fase 2 estiver pronta para `Save` (recomendação: completar Fase 2 antes de **T015**).
- **Fase 3 (US1):** Depende de **T007**/**T008** para registar `AssetManagementViewModel` no Koin (`GetIssuersUseCase` já vem do `UseCaseModule` — não confundir com o scan do pacote `com.eferraz.asset_management`). **Ordem dentro da US1:** **T009** → **T010** → **T011** → **T012** → **T013** → **T014**.
- **Fase 4 (US2):** Depende de **T005**–**T006** e das tarefas **T009**–**T014**.
- **Fase 5 (US3):** Depende da Fase 4.
- **Fase 6:** Depende das histórias pretendidas estarem completas.

### Dependências entre histórias

- **US1 (P1):** Após Fase 2 (recomendado para fluxo vertical) e **T007**/**T008**; **T009** antecede **T010** (modelos `FormUi` antes do ViewModel).
- **US2 (P2):** Depende de US1 estruturalmente e de `UpsertInvestmentAssetUseCase` testado (**T006**).
- **US3 (P3):** Depende de US2 para estado de formulário completo.

### Oportunidades de paralelização

- **T001** e **T002** (Fase 1).
- **T006** só depois de **T005**; em paralelo com **T007**/**T008** quando **T005** estiver estável (ficheiros distintos).
- **T023** e **T024** (Fase 6).
- **Não** paralelizar **T009** com **T010** (dependência directa).

### Critérios de teste independente (resumo)

| História | Como validar sozinha |
|----------|----------------------|
| US1 | UI: categorias, campos visíveis, estado inicial, lista de emissores; RV sem inputs de liquidez editáveis; CNPJ opcional com mensagens claras |
| US2 | Persistência, fechos, confirmações, erro de gravação, loading ao salvar |
| US3 | Mudança de categoria sem “vazamento” de dados no save |

### Estratégia de implementação (MVP)

- **MVP mínimo:** Fase 2 + US1 (diálogo útil e navegável) — **sem** persistência completa até integrar **T015**.
- **Incremento seguinte:** US2 (valor de produto completo para cadastro).
- **Incremento final:** US3 + polimento.

---

## Notas

- [P] = ficheiros diferentes, sem conflito de dependência imediata.
- Commits sugeridos após cada fase ou após grupo **T003**–**T008**.
- Hook opcional: `.specify/extensions.yml` `hooks.before_tasks` / `after_tasks` com `speckit.git.commit` (`optional: true`).
