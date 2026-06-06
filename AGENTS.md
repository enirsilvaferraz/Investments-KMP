# AGENTS.md — Investments-KMP

Seguir as orientações de @../.specify/memory/constitution.md

## Validação (princípio IX)

**Não** executar `./gradlew` (compile, test, run) para validar funcionamento após alterações de código — só quando o utilizador pedir, a tarefa exigir artefacto de build, ou em CI/revisão. Escrever testes em `:domain:usecases` continua obrigatório (princípio V).

## Simplicidade e escopo (princípio X)

Priorizar **menos código e mais valor**: simplicidade, manutenabilidade e legibilidade. Implementar **apenas** o que a spec/plano/tasks pedem — sem inventar funcionalidades fora do escopo. Diff mínimo: alterar o menor conjunto de ficheiros necessário e reutilizar código existente.

## Módulos de apresentação (referência)

| Gradle                       | Caminho                                                                                                          |
|------------------------------|------------------------------------------------------------------------------------------------------------------|
| `:features:design-system`    | `core/presentation/design-system/`                                                                               |
| `:features:design-system-v2` | `core/presentation/design-system-v2/` — M3 Expressive; `SummaryCard`, `MonthYearSelector`, `AppThemeV2` (feature `011-summary-cards`); cores semânticas via `StatusKind` + `MaterialTheme.statusColors(status)` (feature `012-status-theme-colors`); filtros: `FilterToggleGroup`, `FilterSectionHeader` em `filter/` (feature `014-wallet-filters`) |
| `:features:composeApp`       | `core/presentation/composeApp/` — inclui `walletfilters/` (`WalletFiltersPanel`, feature `014-wallet-filters`; wiring no histórico via `HistoryViewModel`, feature `015-history-wallet-filters`) |
| `:features:asset-management` | `core/presentation/asset-management/`                                                                            |

`design-system-v2` é biblioteca Compose (sem `*Contract.kt` / sem registo obrigatório em `umbrellaApp`, como o v1).

## Filtros da carteira (design-system-v2 + composeApp)

- **`FilterToggleGroup`** / **`FilterToggleOption`** (`filter/FilterToggleGroup.kt`): multi-selecção com **`FilterChip`** M3 (tamanho e tipografia padrão do componente, sem ícones), `FlowRow` com espaçamento 8dp; tooltips quando `contentDescription != label`.
- **`FilterSectionHeader`**: cabeçalho de secção com ícone + label uppercase.
- **`WalletFiltersPanel`** (`composeApp/.../walletfilters/`): painel em `OutlinedCard`; modelos/catálogo/previews em `WalletFilters.kt`, UI em `WalletFiltersPanel.kt`.
- **Histórico + filtros** (`composeApp/.../history/`): opções estáticas via `WalletFiltersCatalog.staticPanelOptions(maturityMonths, brokerageOptions)`; corretoras de `GetBrokeragesUseCase`; `WalletFiltersUiState.selectedBrokerage`; `toWalletHistoryFilterCriteria()`; `GetHistoryTableDataUseCase.Param(referenceDate, walletFilter)` (feature `018-holding-history-filter`).

## Posição e transações (`:domain:entity`, features `017-holding-transactions`, `023-unify-asset-transaction`)

- **Grafo de leitura**: `HoldingHistoryEntry` → `AssetHolding` → `transactions` (lista completa, ordenada por data).
- **Transação**: `AssetTransaction` **tipo único** (`data class` com `quantity`, `unitPrice`, `totalValue` derivado); **sem** propriedade `holding`; **sem** subtipos (`FixedIncomeTransaction`, etc. removidos); escrita via `SaveTransactionUseCase.Param(holding, transaction)`.
- **Persistência**: Room v10 — tabela achatada `asset_transactions` (sem tabelas satélite, sem `observations`/`asset_class`); classe do ativo via `AssetHolding.asset`.
- **UI**: formulário inline em `:features:asset-management` (`TransactionManagementView`); legado `composeApp/features/transactions/` removido.
- **Leitura**: não usar `GetTransactionsByHoldingUseCase`; transações via `@Relation` Room em `AssetHoldingWithDetails` → `HoldingMappers.toDomain`.
- Planos: `specs/017-holding-transactions/plan.md`, `specs/023-unify-asset-transaction/plan.md`.

## Taxonomia de ativos (`:domain:entity`, feature `016-asset-taxonomy-refactor`)

- **`AssetClass`**: classe de ativo (renda fixa, variável, fundo) — substitui `InvestmentCategory`.
- **`YieldIndexer`**: indexador de rentabilidade em renda fixa (somente RF).
- **`AssetType`**: interface marcadora; tipo de produto via `FixedIncomeAssetType`, `VariableIncomeAssetType`, `InvestmentFundAssetType`.
- Documentação canónica: `core/domain/entity/docs/DOMAIN.md`.

## Cores semânticas de status (design-system-v2)

- **`StatusKind`** (`theme/StatusColors.kt`): enum único — `Default`, `Info`, `Warning`, `Positive`, `Negative`.
- **`MaterialTheme.statusColors(status)`**: lookup de 8 papéis M3 (`StatusColorRoles`); requer `AppThemeV2`.
- **`SummaryCardStatus`**: `typealias` de `StatusKind` em `summary/SummaryCard.kt` (compat 011).
- Novos componentes com status: importar `StatusKind` do tema; obter cores com `MaterialTheme.statusColors(status)` — **sem** parâmetros `Color` na API pública.