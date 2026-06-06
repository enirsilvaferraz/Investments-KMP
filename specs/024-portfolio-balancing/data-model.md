# Data Model: Balanceamento de carteira

**Feature**: `024-portfolio-balancing` | **Date**: 2026-06-06

## Escopo

- **Domínio** (`:domain:usecases`): pacote `balancing` (tipos, catálogo, engine, UC autónomo `Unit` + `DateProvider`).
- **Apresentação** (`:features:composeApp`, history): intent → `useCase(Unit)`, `IconButton`.
- **Sem** persistência de pesos, **sem** novos módulos/ports, **sem** alteração em `:domain:entity`.

---

## Entidades existentes (entrada)

### `HoldingHistoryEntry` (`:domain:entity`)

| Campo | Uso no balanceamento |
|-------|----------------------|
| `holding.asset` | Classificação (classe, tipo, indexador, ticker) |
| `endOfMonthValue`, `endOfMonthQuantity` | Património = produto; `≤ 0` → excluída (FR-002) |
| `referenceDate` | Mês de referência (todas entradas do mesmo período) |

### `Asset` / subclasses

| Tipo | Campos relevantes |
|------|-------------------|
| `FixedIncomeAsset` | `assetClass = FIXED_INCOME`, `indexer: YieldIndexer` |
| `VariableIncomeAsset` | `assetClass = VARIABLE_INCOME`, `type`, `ticker` |
| `InvestmentFundAsset` | `assetClass = INVESTMENT_FUND`, `type: InvestmentFundAssetType` |

---

## Novos tipos (`:domain:usecases`)

### `TargetWeight` (sealed)

| Variant | Semântica | Valor ideal |
|---------|-----------|-------------|
| `Fixed(percent: Double)` | Peso fixo em % do universo de referência | `reference × percent / 100` |
| `Zero` | Peso 0% (FR-006) | `0` |
| `DynamicPension` | Previdência (FR-005) | `actual` (desvio sempre 0) |

### `BalancingComponentId` (enum ou constantes)

Identificadores estáveis para lookup de pai no Grupo 1:

| Id | Grupo | Nome exibido |
|----|-------|--------------|
| `FIXED_INCOME_TOTAL` | 1 | Renda Fixa |
| `VARIABLE_INCOME_TOTAL` | 1 | Renda Variável |
| `PENSION_FUNDS` | 1 | Fundos de Previdência |
| `CRYPTO` | 1 | Cripto Ativos |
| `OTHER_INVESTMENTS` | 1 | Demais investimentos |
| `RF_POST_FIXED` | 2 | Pós-fixados |
| `RF_PRE_FIXED` | 2 | Pré-fixado |
| `RF_INFLATION_LINKED` | 2 | Atrelado a inflação |
| `RV_NATIONAL_STOCKS` | 3 | Ações Nacionais |
| `RV_INTERNATIONAL` | 3 | Ações Internacionais |
| `RV_REITS` | 3 | FIIs |
| `RV_OTHER` | 3 | Outros RV |

### `BalancingComponent`

| Campo | Tipo | Regra |
|-------|------|-------|
| `id` | `BalancingComponentId` | Único no catálogo |
| `displayName` | `String` | Coluna «nome» do log |
| `targetWeight` | `TargetWeight` | Ver tabela FR-007 |
| `matches` | `(HoldingHistoryEntry) -> Boolean` | Predicado determinístico |
| `parentId` | `BalancingComponentId?` | Só grupos 2–3: pai homólogo no Grupo 1 |

### `BalancingGroup`

| Campo | Tipo | Regra |
|-------|------|-------|
| `id` | `BalancingGroupId` | `PORTFOLIO_TOTAL`, `FIXED_INCOME`, `VARIABLE_INCOME` |
| `displayName` | `String` | Cabeçalho no log |
| `components` | `List<BalancingComponent>` | Ordem: específicos → fallback último |
| `referenceUniverse` | derivado | G1: total carteira; G2: actual RF; G3: actual RV sem HASH11 |

### `PortfolioBalancingReportLine`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `groupId` | `BalancingGroupId` | Agrupamento no log |
| `groupName` | `String` | |
| `componentName` | `String` | |
| `actualValue` | `Double` | Soma patrimónios enquadrados |
| `targetWeightDisplay` | `String` | Ex.: `50,00%` ou `dinâmico (12,34%)` |
| `targetWeightPercent` | `Double?` | Numérico quando aplicável (testes) |
| `idealValue` | `Double` | |
| `deviation` | `Double` | `actualValue - idealValue` |

### `PortfolioBalancingReport`

| Campo | Tipo |
|-------|------|
| `referenceDate` | `YearMonth` |
| `totalPortfolioValue` | `Double` |
| `lines` | `List<PortfolioBalancingReportLine>` | Ordem: grupos 1 → 2 → 3, ordem do catálogo |

---

## Catálogo inicial (FR-007)

### Grupo 1 — Carteira Total

| Componente | Peso | `matches` (resumo) |
|------------|------|---------------------|
| Renda Fixa | 50% Fixed | `asset is FixedIncomeAsset` |
| Renda Variável | 40% Fixed | `asset is VariableIncomeAsset` && ticker ≠ HASH11 |
| Fundos de Previdência | DynamicPension | `asset is InvestmentFundAsset` && type == PENSION |
| Cripto Ativos | 1% Fixed | `asset is VariableIncomeAsset` && ticker == HASH11 |
| Demais investimentos | Zero | fallback: activa não matched acima (incl. fundos não-previdência) |

**Ordem de avaliação sugerida**: Cripto → Previdência → RF → RV → Demais (garante HASH11 fora de RV; fundos não-previdência caem em Demais).

### Grupo 2 — Renda Fixa

Universo: posições activas `FixedIncomeAsset`. Ideal: `peso × ideal(FIXED_INCOME_TOTAL)`.

| Componente | Peso | `matches` |
|------------|------|-----------|
| Pós-fixados | 33,33% | `indexer == POST_FIXED` |
| Pré-fixado | 33,33% | `indexer == PRE_FIXED` |
| Atrelado a inflação | 33,33% | `indexer == INFLATION_LINKED` |

### Grupo 3 — Renda Variável

Universo: posições activas `VariableIncomeAsset` com ticker ≠ HASH11. Ideal: `peso × ideal(VARIABLE_INCOME_TOTAL)`.

| Componente | Peso | `matches` |
|------------|------|-----------|
| Ações Nacionais | 50% | `type == NATIONAL_STOCK` && ticker ∉ {HASH11, IVVB11} |
| Ações Internacionais | 10% | `ticker == IVVB11` |
| FIIs | 30% | `type == REAL_ESTATE_FUND` |
| Outros RV | 10% | fallback no universo RV |

---

## Regras de validação (invariantes)

| ID | Regra |
|----|-------|
| V1 | Soma dos `actualValue` dos componentes do Grupo 1 = `totalPortfolioValue` (posições activas, sem double-count) |
| V2 | Cada posição activa pertence a exactamente um componente por grupo elegível |
| V3 | `DynamicPension`: `deviation == 0` quando `totalPortfolioValue > 0` |
| V4 | `totalPortfolioValue == 0` → todos `actualValue`, `idealValue`, `deviation` = 0; peso dinâmico exibido como 0% |
| V5 | Grupos 2 e 3 sempre presentes no relatório (mesmo com actual zero) |

---

## Estado da UI (`:features:composeApp`)

### `HistoryIntent` — adição

```kotlin
data object CalculatePortfolioBalancing : HistoryIntent
```

### `HistoryState`

**Sem alteração** — não há `isBalancing`; botão permanece activo (FR-010).

### `AssetHistoryScreen` / `Actions`

| Parâmetro novo | Tipo |
|----------------|------|
| `onBalancingClick` | `() -> Unit` |

`IconButton` após import B3; **sem** `CircularProgressIndicator` substituto.

---

## Período de referência

| Fonte | Semântica |
|-------|-----------|
| `DateProvider.getCurrentYearMonth()` | Mês corrente — única fonte no UC (não usa selector do histórico) |

---

## Diagrama de fluxo

```
[UI: AssetHistoryScreen]
    onBalancingClick → HistoryIntent.CalculatePortfolioBalancing
[HistoryViewModel]
    launch { calculatePortfolioBalancingUseCase(Unit) → println(format(...)) }

[CalculatePortfolioBalancingUseCase — param Unit]
    period = DateProvider.getCurrentYearMonth()
    entries = GetHoldingHistoriesUseCase(ByReferenceDate(period))  // sem CreateHistory
    → PortfolioBalancingEngine.calculate(entries, period)

[PortfolioBalancingEngine]
    active = entries.filter { patrimony > 0 }
    classify + agregação + fórmulas FR-004*
    → PortfolioBalancingReport
```
