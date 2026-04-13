# Plano de implementação: Corretora obrigatória e criação de posição no cadastro

**Branch**: `003-corretora-holding-cadastro` | **Data**: 2026-04-13 | **Spec**: [spec.md](./spec.md)  
**Artefactos**: [research.md](./research.md), [data-model.md](./data-model.md), [contracts/ui-brokerage-field-and-save.md](./contracts/ui-brokerage-field-and-save.md), [quickstart.md](./quickstart.md)

**Entrada**: Extensão do diálogo de `001-cadastro-investimento-dialog` — campo obrigatório **corretora** (dropdown alimentado pelo catálogo), criação de **`AssetHolding`** ao **Salvar**, com política **tudo ou nada** (**RF-007**, clarificação 2026-04-13).

**Idioma:** Português do Brasil (pt-BR), conforme `.specify/memory/constitution.md` princípio VIII.

## Resumo

Acrescentar ao módulo **`:features:asset-management`** o carregamento de corretoras (**`GetBrokeragesUseCase`**) e um campo **dropdown obrigatório** (`brokerageId` no `AssetDraft`). Estender **`UpsertInvestmentAssetUseCase`** para validar corretora por **id**, resolver **`Owner`** via **`OwnerRepository.getFirst()`**, e persistir **atómica**mente o **novo ativo** e a **posição inicial** (`AssetHolding` sem meta). Para isso, expor **`BrokerageRepository.getById`**, **`BrokerageDataSource.getById`**, e uma operação transaccional Room (novo *coordinator* / método no módulo **`:data:database`**, consumido pelo grafo **`:data:repositories`** → **usecases**). Actualizar testes **`jvmTest`** do caso de uso e, após código, **`DOMAIN.md`** se o fluxo do diálogo não estiver explícito.

## Contexto técnico

| Item | Valor |
|------|--------|
| **Linguagem / versão** | Kotlin (KMP), versões em `build-logic` / `libs.versions.toml` |
| **Dependências principais** | Compose Multiplatform, Material3, Lifecycle ViewModel, Koin, Room 3 (`androidx.room3`), `kotlinx-datetime` |
| **Armazenamento** | SQLite via Room; tabelas `assets`, `asset_holdings`, `brokerages`, `owners` |
| **Testes** | `jvmTest` em `:domain:usecases` para `UpsertInvestmentAssetUseCase`; padrão `~/.cursor/rules/test-patterns.mdc` |
| **Plataformas-alvo** | Android, Desktop, iOS (KMP) — UI em `commonMain` |
| **Tipo de projeto** | App KMP — feature `:features:asset-management` |
| **Metas de desempenho** | Catálogo de corretoras pequeno (carteira pessoal); lista disponível no mesmo patamar que emissores (**CS-003**) |
| **Restrições** | `explicitApi()`; visibilidade mínima; **atomicidade** perceptível ao utilizador (**RF-007**) |
| **Escala / âmbito** | Cadastro **novo** no diálogo; edição de investimento existente **fora** do mínimo da spec |

## Verificação da constitution (Constitution Check)

*GATE: cumprida antes da Fase 0; rever após o desenho da Fase 1.*

**Investments-KMP** (`.specify/memory/constitution.md`):

- [x] **Domínio:** impacto em `DOMAIN.md` se o fluxo “diálogo cria `AssetHolding` inicial” não estiver documentado (princípio I / IX).
- [x] **Qualidade de código:** camadas `entity` ← `usecases` ← `presentation`; `data` implementa portos; **explicitApi** e visibilidade mínima.
- [x] **Testes:** alterações em `UpsertInvestmentAssetUseCase` **com** `jvmTest` novo ou actualizado; inglês + GIVEN/WHEN/THEN; MockK para portos.
- [x] **UX:** estados vazio / erro de corretora; loading de **Salvar** já coberto por `001`; `@Preview` no mesmo ficheiro que o composable afectado.
- [x] **Desempenho:** lista local; sem requisito extra além da spec.
- [x] **Build:** `./gradlew :domain:usecases:jvmTest`, `:features:asset-management:compileKotlinJvm`, módulos `:data:*` tocados.
- [x] **Idioma:** documentos desta feature em pt-BR.
- [x] **Coerência:** actualizar `DOMAIN.md` quando o comportamento ficar garantido no código (IX).

## Estrutura do projeto

### Documentação (esta feature)

```text
specs/003-corretora-holding-cadastro/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ui-brokerage-field-and-save.md
└── tasks.md              # /speckit.tasks — não criado por este comando
```

### Código-fonte (alvos principais)

```text
core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/
├── AssetDraft.kt                    # + brokerageId; withCategoryPreserving…
├── AssetManagementViewModel.kt     # GetBrokeragesUseCase; validação
├── AssetManagementFormUi.kt # validação / labels de campo brokerage
├── AssetManagementFormView.kt     # dropdown corretora; @Preview se aplicável
├── AssetManagementUpsertParam.kt   # passar brokerageId ao Param
└── … (baseForm / extensões de grelha)

core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/
├── UpsertInvestmentAssetUseCase.kt # + brokerageId; validação; transação
├── repositories/BrokerageRepository.kt # + getById
└── … (novo porto transaccional, se separado)

core/data/database/…
├── datasources/BrokerageDataSource.kt / impl   # + getById
└── … (coordinator transaccional ativo + holding)

core/data/repositories/…
└── BrokerageRepositoryImpl.kt      # + getById
```

**Decisão de estrutura:** Mantém-se o grafo KMP existente; a novidade é o **coordenador transaccional** no módulo de BD e a extensão do caso de uso já usado pelo diálogo.

## Registo de complexidade (Complexity Tracking)

> Nenhuma violação da constitution a justificar.

| Violação | Por que é necessária | Alternativa mais simples recusada porque |
|----------|----------------------|------------------------------------------|
| — | — | — |

---

## Fase 0 — Pesquisa

**Estado:** concluída.

**Saída:** [research.md](./research.md) — transação Room atómica; corretora por id; owner `getFirst()`; extensão de `UpsertInvestmentAssetUseCase`; UI com `GetBrokeragesUseCase`.

---

## Fase 1 — Desenho e contratos

**Estado:** concluída.

**Saídas:**

| Artefacto | Descrição |
|-----------|-----------|
| [data-model.md](./data-model.md) | `AssetDraft`, `Param`, validação, transação |
| [contracts/ui-brokerage-field-and-save.md](./contracts/ui-brokerage-field-and-save.md) | Extensão MVI e mapeamento para o caso de uso |
| [quickstart.md](./quickstart.md) | Ordem de implementação e comandos Gradle |

**Script de contexto do agente:** executar `.specify/scripts/bash/update-agent-context.sh cursor-agent` após gerar este plano.

---

## Fase 2 — Planeamento (sem `tasks.md`)

O comando `/speckit.plan` **não** gera `tasks.md`; usar `/speckit.tasks` para decomposição.

**Próximos passos recomendados:**

1. Implementar `getById` em `BrokerageDataSource` / `BrokerageRepository` / dados.
2. Implementar operação transaccional ativo + `AssetHolding` e integrar em `UpsertInvestmentAssetUseCase` + **testes `jvmTest`**.
3. Implementar UI (draft, ViewModel, formulário, `buildUpsertParam`).
4. Validar compile + testes; actualizar `DOMAIN.md` se necessário.

---

## Extensões e hooks (opcional)

Conforme `.specify/extensions.yml`, hooks `before_plan` / `after_plan` com `speckit.git.commit` são **opcionais** (`optional: true`).
