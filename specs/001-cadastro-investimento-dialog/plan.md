# Plano de implementação: Diálogo de cadastro de investimento

**Branch**: `001-cadastro-investimento-dialog` | **Data**: 2026-04-09 | **Spec**: [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/ui-investment-registration-dialog.md](./contracts/ui-investment-registration-dialog.md), [quickstart.md](./quickstart.md)

**Entrada**: Especificação em `/specs/001-cadastro-investimento-dialog/spec.md` + pedido de MVI, camadas estilo Pokedex, módulo `core/presentation/asset-management`, novo use case de upsert com validação de negócio, uso de `GetIssuersUseCase`.

**Idioma:** Português do Brasil (pt-BR), conforme `.specify/memory/constitution.md` princípio VIII.

## Resumo

Implementar um **diálogo modal** de cadastro de investimento com **formulário dinâmico** por `InvestmentCategory`, **MVI** (`UiState` + `Intent`), ficheiros **`AssetManagement*`** em `com.eferraz.asset_management`, **ponto de entrada público** em `AssetManagementContract.kt`, validação de **formato** na UI e **regras de negócio** no domínio via novo **`UpsertInvestmentAssetUseCase`** (emissor **apenas** por ID do catálogo, alinhado a **RF-012**). Carregar emissores com **`GetIssuersUseCase`**. Estender **`IssuerRepository`** (e dados) com **`getById`**.

## Contexto técnico

| Item                        | Valor                                                                                     |
|-----------------------------|-------------------------------------------------------------------------------------------|
| **Linguagem / versão**      | Kotlin (KMP), conforme versão no `build-logic`                                            |
| **Dependências principais** | Compose Multiplatform, Material3, Lifecycle ViewModel, Koin, `kotlinx-datetime`           |
| **Armazenamento**           | Room / repositórios existentes (`AssetRepository`, `IssuerRepository`)                    |
| **Testes**                  | `jvmTest` em `:domain:usecases` para o novo caso de uso; padrão `test-patterns.mdc`       |
| **Plataformas-alvo**        | Android, Desktop, iOS (KMP) — UI em `commonMain`                                          |
| **Tipo de projeto**         | App KMP multiplataforma — feature `:features:asset-management`                            |
| **Metas de desempenho**     | CS-004 da spec (fluxo < 3 min caso típico); lista de emissores pequena (carteira pessoal) |
| **Restrições**              | `explicitApi()`; visibilidade mínima; **não** criar emissor no diálogo (**RF-012**)       |
| **Escala / âmbito**         | Um diálogo; três variantes de ativo; sem pré-preenchimento por contexto (**RF-014**)      |

## Verificação da constitution (Constitution Check)

*GATE: cumprida antes da Fase 0; rever após o desenho da Fase 1.*

**Investments-KMP** (`.specify/memory/constitution.md`):

- [x] **Domínio:** impacto em `DOMAIN.md` se o modelo de formulário ou invariantes mudarem (ex.: nome vs ticker em renda variável, liquidez em fundos).
- [x] **Qualidade de código:** camadas `entity` / `usecases` / `presentation`; `explicitApi` e visibilidade mínima (`private`/`internal` por defeito, `public` só no contrato e API entre módulos).
- [x] **Testes:** `UpsertInvestmentAssetUseCase` com testes em `:usecases` (`jvmTest`); UI com estratégia de evidência definida (testes de VM ou manual documentado para fluxos visuais).
- [x] **UX:** estados de carregamento, erro de gravação (**RF-015**), confirmação de descarte (**RF-013**), bloqueio de double submit (**RF-016**).
- [x] **Desempenho:** operações assíncronas no ViewModel; sem bloquear UI principal.
- [x] **Build:** `./gradlew :features:asset-management:compileKotlinJvm` e `:domain:usecases:jvmTest` quando aplicável.
- [x] **Idioma:** documentos da feature em pt-BR.
- [x] **Coerência:** após implementação, actualizar `DOMAIN.md`/`docs` se o comportamento documentado mudar (princípio IX).

## Estrutura do projeto

### Documentação (esta feature)

```text
specs/001-cadastro-investimento-dialog/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ui-investment-registration-dialog.md
└── tasks.md              # /speckit.tasks — não criado por este comando
```

### Código-fonte (raiz do repositório)

```text
core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/
├── AssetManagementContract.kt      # Entrada pública: AssetManagementScreen(modifier, …)
├── AssetManagementScreen.kt        # Composição quando(UiState), diálogo
├── AssetManagementViewModel.kt       # MVI: StateFlow, Intent, dispatch
├── AssetManagementFormUi.kt          # @Immutable, mapeamentos, validação de formato
└── AssetManagementFormView.kt        # Composables de campos e botões

core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/
├── UpsertInvestmentAssetUseCase.kt   # (novo) upsert + validação de negócio, issuer por id
├── cruds/GetIssuersUseCase.kt        # (existente) lista de emissores
└── repositories/IssuerRepository.kt  # + getById(id)

core/data/repositories/…/IssuerRepositoryImpl.kt
core/data/database/…/IssuerDataSource.kt / IssuerDataSourceImpl.kt
```

**Decisão de estrutura:** Feature UI isolada em `:features:asset-management`; domínio e dados seguem o grafo existente (`entity` ← `usecases` ← `presentation`; `usecases` ← `data`).

## Registo de complexidade (Complexity Tracking)

> Nenhuma violação da constitution a justificar; novo caso de uso evita alterar `SaveAssetUseCase` de forma arriscada para fluxos legados com `GetOrCreateIssuerUseCase`.

| Violação | Por que é necessária | Alternativa mais simples recusada porque |
|----------|----------------------|------------------------------------------|
| —        | —                    | —                                        |

---

## Fase 0 — Pesquisa

**Estado:** concluída.

**Saída:** [research.md](./research.md) — decisões: MVI em camadas (Pokedex), contrato como entrada, `UpsertInvestmentAssetUseCase` + `IssuerRepository.getById`, `GetIssuersUseCase` para catálogo, validação em duas camadas.

---

## Fase 1 — Desenho e contratos

**Estado:** concluída.

**Saídas:**

| Artefacto                                                                                          | Descrição                                                 |
|----------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| [data-model.md](./data-model.md)                                                                   | Estado MVI, campos por categoria, extensão de repositório |
| [contracts/ui-investment-registration-dialog.md](./contracts/ui-investment-registration-dialog.md) | Contrato de `Intent`/`UiState` e ficheiros                |
| [quickstart.md](./quickstart.md)                                                                   | Comandos Gradle, Koin, ordem de implementação             |

**Script de contexto do agente:** executar `.specify/scripts/bash/update-agent-context.sh cursor-agent` após este plano.

---

## Fase 2 — Planeamento (sem `tasks.md`)

O comando `/speckit.plan` **não** gera `tasks.md`; usar `/speckit.tasks` para decomposição em tarefas.

**Próximos passos recomendados:**

1. Implementar `IssuerRepository.getById` + `IssuerDataSource.getById`.
2. Implementar `UpsertInvestmentAssetUseCase` + testes `jvmTest`.
3. Implementar ViewModel + UI (Contract, Screen, FormUi, FormView).
4. Integrar `composeApp` (já referencia `AssetManagementScreen`); validar navegação e diálogo.
5. Actualizar `DOMAIN.md` se o contrato de dados do formulário divergir da documentação actual.

---

## Extensões e hooks (opcional)

Conforme `.specify/extensions.yml`, hooks `before_plan` / `after_plan` com `speckit.git.commit` são **opcionais** (`optional: true`). Execução não obrigatória para concluir o plano.
