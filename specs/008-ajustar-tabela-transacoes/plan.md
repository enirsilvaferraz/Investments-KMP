# Plano de implementação: Ajuste de corretora e tabela de transações por tipo de asset

**Branch**: `008-ajustar-tabela-transacoes` | **Data**: 2026-04-25 | **Spec**: `specs/008-ajustar-tabela-transacoes/spec.md`  
**Entrada**: Especificação da feature em `specs/008-ajustar-tabela-transacoes/spec.md`

**Nota:** Preenchido pelo comando `/speckit.plan`. Fluxo de execução: ver `.specify/templates/plan-template.md`.

**Idioma:** Redigir este documento em **português do Brasil (pt-BR)**, conforme `.specify/memory/constitution.md` princípio VIII.

## Resumo

Ajustar o diálogo de gestão de ativo para recolocar o campo corretora na primeira coluna, remover o formulário separado de transações e adotar criação por linha em branco via botão adicionar na própria tabela. A tabela deve adaptar campos por tipo de asset, reutilizar componentes de entrada já usados no histórico (ex.: `TableInputMoney`) e bloquear salvamento final se qualquer linha (nova ou existente) estiver inválida.

## Contexto técnico

**Linguagem / versão**: Kotlin Multiplatform (Kotlin 2.x do projeto)  
**Dependências principais**: Compose Multiplatform, Material3, Koin, módulos `:domain:entity`, `:domain:usecases`, `:features:design-system` e `:core:presentation:composeApp`  
**Armazenamento**: persistência no fluxo atual de salvar da gestão de ativo, com staging em memória da tabela durante a sessão  
**Testes**: validação funcional por cenários da spec + compilação de `:features:asset-management`; validações manuais guiadas no quickstart  
**Plataformas-alvo**: Android e Desktop  
**Tipo de projeto**: app KMP em camadas  
**Metas de desempenho**: adição de nova linha em até 1 segundo e manutenção de edição inline responsiva na tabela  
**Restrições**: sem exclusão de transações nesta versão; bloqueio total de salvar se houver qualquer linha inválida; reaproveitar componentes de entrada de tabela já existentes  
**Escala / âmbito**: módulo `:features:asset-management` com eventual extração/migração de componentes para `:features:design-system`

## Verificação da constitution (Constitution Check)

*GATE: Deve passar antes da fase 0 de pesquisa. Rever após o desenho da fase 1.*

**Investments-KMP** (`.specify/memory/constitution.md`):

- [x] **Domínio:** Sem alteração direta em invariantes de domínio financeiro; foco em comportamento de UI e validação do fluxo de edição.
- [x] **Qualidade de código:** Escopo concentrado em `presentation`, com possibilidade controlada de mover componente para `design-system` sem violar fronteiras de camada.
- [x] **Testes:** Estratégia de evidência definida por cenários de aceitação e compilação do módulo; mudanças fora de `core/domain/usecases/`.
- [x] **UX:** Reuso explícito de componentes de input de tabela, consistência de erro e validação no salvar final.
- [x] **Desempenho:** Critério de atualização imediata da linha adicionada e edição estável em tabela.
- [x] **Build:** Verificação alvo prevista em `:features:asset-management:compileKotlinJvm`.
- [x] **Idioma:** Artefatos da feature em pt-BR.
- [x] **Coerência:** Decisões de clarificação já refletidas na spec e direcionadas no plano.

## Estrutura do projeto

### Documentação (esta feature)

```text
specs/008-ajustar-tabela-transacoes/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ui-asset-transactions-table-alignment.md
└── tasks.md
```

### Código-fonte (raiz do repositório)

```text
core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/
├── view/
│   ├── AssetManagementFormView.kt
│   ├── AssetManagementFormFields.kt
│   └── AssetManagementFormTransaction.kt
└── vm/
    ├── UiState.kt
    ├── VMEvents.kt
    └── AssetManagementViewModel.kt

core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/
└── AssetHistoryScreen.kt

core/presentation/design-system/src/commonMain/kotlin/com/eferraz/presentation/design_system/components/inputs/
└── TableInputMoney.kt
```

**Decisão de estrutura**: implementação principal em `:features:asset-management`, usando `AssetHistoryScreen.kt` como referência de input em tabela e movendo/ajustando componentes compartilháveis para `design-system` se necessário.

## Fase 0 — Pesquisa e decisões

Ver `research.md` para decisões sobre:
- Política de criação de linha em branco e bloqueio de adicionar quando existir inválida.
- Mapeamento de colunas por tipo de asset.
- Estratégia de reutilização/migração dos inputs de tabela (`TableInputMoney` e afins).
- Regra única de bloqueio de salvar para qualquer linha inválida.

## Fase 1 — Desenho e contratos

Entregáveis gerados:
- `data-model.md` com modelo de estado de linha editável e transições de validação.
- `contracts/ui-asset-transactions-table-alignment.md` com contrato funcional da tabela e regras de validação.
- `quickstart.md` com roteiro de validação funcional e build.

## Reavaliação da constitution (pós-desenho)

- [x] Estrutura respeita fronteiras entre módulo de feature e design system compartilhado.
- [x] Regras de UX e validação ficaram objetivas e testáveis.
- [x] Não há violações que exijam exceção de complexidade.

## Registo de complexidade (Complexity Tracking)

Sem violações que exijam exceção.
