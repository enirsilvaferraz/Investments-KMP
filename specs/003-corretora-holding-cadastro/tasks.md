---
description: "Lista de tarefas — corretora obrigatória e posição no cadastro (003)"
---

# Tarefas: Corretora obrigatória e criação de posição no cadastro

**Entrada:** `/Users/enirferraz/AndroidStudioProjects/Investments-KMP/specs/003-corretora-holding-cadastro/`  
**Pré-requisitos:** [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/ui-brokerage-field-and-save.md](./contracts/ui-brokerage-field-and-save.md)

**Idioma:** pt-BR (constitution VIII). **Usecases + testes:** constitution V. **explicitApi / previews:** constitution IV e VI.

## Formato: `- [ ] Tnnn [P?] [USn?] Descrição com caminho`

---

## Fase 1: Configuração (âmbito da feature)

**Objetivo:** Alinhar execução aos artefactos antes de tocar no código.

- [X] T001 [P] Rever ordem e módulos em `specs/003-corretora-holding-cadastro/quickstart.md` e critérios em `specs/003-corretora-holding-cadastro/spec.md` (histórias P1/P2).

---

## Fase 2: Fundações (bloqueia todas as histórias)

**Objetivo:** Catálogo corretora por id, persistência atómica ativo + `AssetHolding`, caso de uso e testes.

**Teste independente (base):** `UpsertInvestmentAssetUseCase` com mocks — corretora inválida / owner ausente falham; sucesso implica chamada única ao porto transaccional (verificável com MockK).

**CRÍTICO:** Nenhuma história de UI completa até T006 passar.

- [X] T002 Adicionar `getById(id: Long): Brokerage?` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/datasources/BrokerageDataSource.kt` e implementar em `core/data/database/src/commonMain/kotlin/com/eferraz/database/datasources/impl/BrokerageDataSourceImpl.kt` usando `core/data/database/src/commonMain/kotlin/com/eferraz/database/daos/BrokerageDao.kt`.
- [X] T003 Adicionar `getById` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/BrokerageRepository.kt` e `core/data/repositories/src/commonMain/kotlin/com/eferraz/repositories/BrokerageRepositoryImpl.kt`.
- [X] T004 Criar porto `RegisterInvestmentAssetPersistence` (ou nome equivalente) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/RegisterInvestmentAssetPersistence.kt` com operação `suspend fun persistNewAssetAndInitialHolding(asset: Asset, ownerId: Long, brokerageId: Long): Long`; implementar em `core/data/repositories/src/commonMain/kotlin/com/eferraz/repositories/RegisterInvestmentAssetPersistenceImpl.kt` usando `AppDatabase.withTransaction` e DAOs existentes (`AssetDao`, `AssetHoldingDao`); registar *Factory* Koin em `core/data/database/src/commonMain/kotlin/com/eferraz/database/di/DatabaseModule.kt` e/ou módulo Koin de `core/data/repositories` conforme padrão do projeto.
- [X] T005 Estender `UpsertInvestmentAssetUseCase` e selados `Param` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/UpsertInvestmentAssetUseCase.kt` com `brokerageId: Long`, validar `BrokerageRepository.getById` e `OwnerRepository.getFirst()`, delegar persistência a `RegisterInvestmentAssetPersistence`; actualizar KDoc (remover menção de não criar `AssetHolding` neste fluxo).
- [X] T006 Adicionar ou actualizar testes em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/UpsertInvestmentAssetUseCaseTest.kt` (criar ficheiro se não existir): cenários **GIVEN/WHEN/THEN** em inglês, MockK para portos, cobrir sucesso atómico, corretora inexistente, owner `null`, e falha do porto transaccional; executar `./gradlew :domain:usecases:jvmTest`.

**Checkpoint:** Domínio e dados prontos para a UI consumir `brokerageId`.

---

## Fase 3: História de utilizador 1 — Escolher corretora ao cadastrar (P1) — MVP

**Objetivo:** Dropdown obrigatório de corretoras, lista do catálogo, **Salvar** só com corretora; integração com caso de uso já estendido.

**Teste independente:** Abrir diálogo “Novo investimento”, ver corretoras carregadas, **Salvar** sem corretora bloqueado; com corretora e restantes campos válidos, gravação bem-sucedida cria ativo + posição (ver BD ou ecrã dependente).

### Implementação US1

- [X] T007 [US1] Acrescentar `brokerageId: Long?` e preservação na mudança de categoria em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetDraft.kt` (função `withCategoryPreservingIssuerAndObs` ou equivalente).
- [X] T008 [P] [US1] Incluir validação de corretora obrigatória e catálogo vazio em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementDraftValidation.kt` e `AssetManagementViewModel.kt` (chave de erro estável `brokerage`; catálogo vazio — mensagem de *banner* alinhada ao emissor).
- [X] T009 [P] [US1] Acrescentar dropdown de corretora e labels em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementFormFields.kt` (extensão `baseForm`), reutilizando padrão dos outros dropdowns.
- [X] T010 [US1] Mapear `brokerageId` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementUpsertParam.kt` para cada variante de `UpsertInvestmentAssetUseCase.Param`.
- [X] T011 [US1] Injetar `GetBrokeragesUseCase`, estado `brokerages`, carga no arranque, guardas de **Salvar** (lista vazia / corretora obrigatória) e `UiState.Form` actualizado em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementViewModel.kt`; ajustar `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/di/AssetManagementModule.kt` se o Koin exigir declaração explícita.
- [X] T012 [US1] Passar `brokerages` e fechar *wiring* do formulário em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementFormView.kt` e `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementScreen.kt`.

**Checkpoint:** US1 cumpre **RF-001**–**RF-006** e cenários 1–5 da spec P1.

---

## Fase 4: História de utilizador 2 — Coerência com cancelamento e alterações (P2)

**Objetivo:** Corretora conta como campo do rascunho para descarte e confirmação; troca de categoria não limpa corretora indevidamente.

**Teste independente:** Seleccionar corretora, tentar fechar — confirmação se alterado; confirmar descarte — sem persistência; mudar só categoria — `brokerageId` mantém-se.

- [X] T013 [US2] Rever `onRequestDismiss`, `onConfirmDiscard` e comparação com `initialAssetDraft()` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementViewModel.kt` para garantir que `brokerageId` participa do estado inicial e da deteção de alterações (alinhado a `AssetDraft.kt`).

**Checkpoint:** US2 alinhada aos cenários P2 da spec.

---

## Fase 5: Polimento e transversal

**Objetivo:** Documentação de domínio, strings e verificação de build.

- [X] T014 Actualizar `core/domain/entity/docs/DOMAIN.md` se o fluxo “cadastro via diálogo cria `AssetHolding` inicial com corretora” não estiver explícito (constitution IX).
- [X] T015 [P] Uniformizar rótulo/erro do campo corretora em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/helpers/FieldLabels.kt` (se aplicável ao padrão actual).
- [X] T016 Executar `./gradlew :features:asset-management:compileKotlinJvm`, `./gradlew :data:database:compileKotlinJvm`, `./gradlew :data:repositories:compileKotlinJvm` e `./gradlew :domain:usecases:jvmTest` após alterações.

---

## Dependências e ordem de execução

### Entre fases

| Fase | Depende de | Bloqueia |
|------|------------|----------|
| 1 | — | — |
| 2 | 1 (recomendado) | US1, US2 |
| 3 (US1) | 2 | US2 recomendada após UI base |
| 4 (US2) | 3 | — |
| 5 | 3 (e idealmente 4) | — |

### Entre histórias

- **US1:** requer Fase 2 completa.
- **US2:** requer US1 funcional (mesmos ficheiros de VM/draft).

### Paralelização ([P])

- **T001** isolado.
- **T008** e **T009** após **T007** (ficheiros diferentes).
- **T015** com **T016** apenas após código estável (T016 valida; pode ser sequencial após T014).

---

## Estratégia de implementação

- **MVP:** concluir Fases 1–3 + **T016** (fluxo P1 utilizável).
- **Incremento seguinte:** Fase 4 (P2), depois Fase 5.
- **Evidência:** Fase 2 **T006** obrigatória antes de considerar a feature dominial fechada (constitution V).

---

## Resumo numérico

| Métrica | Valor |
|---------|--------|
| **Total de tarefas** | 16 |
| **US1 (P1)** | 6 (T007–T012) |
| **US2 (P2)** | 1 (T013) |
| **Fundação + testes** | 5 (T002–T006) |
| **Config + polimento** | 4 (T001, T014–T016) |

---

## Notas

- Caminhos absolutos na raiz do repo: `/Users/enirferraz/AndroidStudioProjects/Investments-KMP/…` (equivalente aos relativos `core/…` acima).
- Ajustar nome do ficheiro `UpsertInvestmentAssetUseCaseTest.kt` se o projeto usar outra convenção; manter pacote espelhado ao use case (constitution V).
