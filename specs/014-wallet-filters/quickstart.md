# Quickstart: Filtros da carteira

**Feature**: `014-wallet-filters`

## Pré-requisitos

- Branch `014-wallet-filters`
- `:features:design-system-v2` com `AppThemeV2`, `SummaryCard`, `MonthYearSelector` (referência)
- `:features:composeApp` com dependência v2

## Estrutura de ficheiros (final)

```text
core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/filter/
├── FilterToggleGroup.kt
├── FilterToggleGroupDefaults.kt
└── FilterSectionHeader.kt

core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/
├── WalletFiltersPanel.kt
├── WalletFiltersUiState.kt
├── WalletFiltersDerivation.kt
├── WalletFiltersPreviewCatalog.kt
├── WalletFilterSectionIcons.kt      # FR-M3-017
├── MaturityFilterDropdown.kt
├── sections/
│   ├── ClassFilterSection.kt
│   ├── SubtypeFilterSections.kt
│   ├── LiquidityFilterSection.kt
│   ├── B3SettledFilterRow.kt
│   └── FilterOptionToggleMapping.kt
```

## Compilar (sob pedido)

```bash
./gradlew :features:design-system-v2:compileKotlinJvm :features:composeApp:compileKotlinJvm
```

## Checklist de entrega

- [x] `FilterToggleGroup` multi-selecção + morph pill/quadrado + movimento reduzido
- [x] Painel em `composeApp`, não monólito v2
- [x] Secções sem opções **ocultas** (FR-018)
- [x] Subtipos só com classe activa; purge ao desactivar classe
- [x] B3/Liquidados ocultos sem Sim **e** Não nos dados
- [x] Vence até: meses só da carteira; secção oculta se N=0
- [x] Reset limpa tudo + Qualquer vencimento + fecha dropdown
- [x] Tooltips M3 plain para abreviaturas (`shortLabel` ≠ `fullLabel`)
- [x] Previews light + dark + cenários edge no catálogo
- [x] **Zero** diff obrigatório em `AssetHistoryScreen`
- [x] Sem ViewModel / Koin nesta entrega
- [x] Matriz de ícones `WalletFilterSectionIcons` (FR-M3-017)

## Próximo passo (fora desta feature)

Integrar `WalletFiltersPanel` no cartão de histórico de activos quando o wiring de domínio existir.
