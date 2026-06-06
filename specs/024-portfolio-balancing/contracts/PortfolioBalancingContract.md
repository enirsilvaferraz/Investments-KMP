# Contract: Balanceamento de carteira

**Feature**: `024-portfolio-balancing` | **Phase**: 1 | **Date**: 2026-06-06

**Depende de**: `016-asset-taxonomy-refactor`, `017-holding-transactions`, `018-holding-history-filter` (balanceamento **ignora** filtros)

---

## Visão geral

```
HistoryViewModel
  → CalculatePortfolioBalancingUseCase(Unit)
  → println(formatPortfolioBalancingReport(report))

CalculatePortfolioBalancingUseCase (autossuficiente)
  → DateProvider.getCurrentYearMonth()
  → GetHoldingHistoriesUseCase(ByReferenceDate(period))
  → PortfolioBalancingEngine.calculate(entries, period)
  → PortfolioBalancingReport
```

**UI**: um `IconButton` no histórico; **sem** ecrã dedicado (FR-011).

**Período**: sempre o **mês corrente** (`DateProvider`).

---

## CalculatePortfolioBalancingUseCase

```kotlin
@Factory
public class CalculatePortfolioBalancingUseCase(
    private val dateProvider: DateProvider,
    private val getHoldingHistoriesUseCase: GetHoldingHistoriesUseCase,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Unit, PortfolioBalancingReport>(context) {

    override suspend fun execute(param: Unit): PortfolioBalancingReport
}
```

| Regra | Detalhe |
|-------|---------|
| Parâmetro | **`Unit`** |
| Período | `DateProvider.getCurrentYearMonth()` |
| Leitura | **Só** `GetHoldingHistoriesUseCase` |
| Filtros | **Não** invocar `FilterHoldingHistoryUseCase` |
| Lista vazia | Relatório completo com totais zero (FR-013) |

### Saída: `PortfolioBalancingReportLine`

| Campo exposto ao log | Origem |
|----------------------|--------|
| `componentName` | `BalancingComponent.displayName` |
| `actualValue` | Soma patrimónios classificados |
| `configuredWeightDisplay` | Catálogo: `XX,XX%`, `0,00%` ou `dinâmico` |
| `normalizedWeightDisplay` | `idealValue / totalPortfolioValue × 100` |
| `idealValue` | FR-004/004a/004b/004c/005/006 |
| `deviation` | `actualValue - idealValue` |

**Log**: linha **`Total`** por grupo — Σ actual/ideal/desvio; configurado `100,00%`; normalizado = Σ normalizados.

**Não expor**: predicado/regra de enquadramento.

---

## PortfolioBalancingEngine (interno)

```kotlin
internal object PortfolioBalancingEngine {
    fun calculate(
        entries: List<HoldingHistoryEntry>,
        referenceDate: YearMonth,
    ): PortfolioBalancingReport
}
```

| Regra | Detalhe |
|-------|---------|
| Grupo 1 | `balanceableBase = total − actual(previdência)`; fixos sobre base |
| Pesos | configurado do catálogo; normalizado = `ideal / total` |

---

## PortfolioBalancingCatalog

Pesos G1: RF 50%, RV **49%**, Cripto 1%, Demais 0%, Previdência `Residual`.

---

## PortfolioBalancingCatalogValidator

Valida Σ configured (Fixed+Zero) = 100% ± 0,01% por grupo.

---

## formatPortfolioBalancingReport

| Coluna | Formato |
|--------|---------|
| Nome | alinhado à esquerda |
| Valor actual | `R$ %,.2f` |
| Peso configurado | `XX,XX%` ou `dinâmico` |
| Peso normalizado | `XX,XX%` |
| Valor ideal | monetário |
| Desvio | monetário |

Entre grupos: linha em branco + `=== {groupName} ===` + linha **Total**.

---

## HistoryViewModel

```kotlin
data object CalculatePortfolioBalancing : HistoryIntent
```

```kotlin
viewModelScope.launch {
    calculatePortfolioBalancingUseCase(Unit)
        .onSuccess { println(formatPortfolioBalancingReport(it)) }
        .onFailure { println("Balanceamento: ${it.message}") }
}
```

---

## Testes (`:domain:usecases:jvmTest`)

| Ficheiro | Âmbito |
|----------|--------|
| `PortfolioBalancingEngineTest` | Base balanceável, pesos, Total no log |
| `PortfolioBalancingCatalogValidatorTest` | FR-016 |
| `PortfolioBalancingPartitionTest` | FR-007a |
| `CalculatePortfolioBalancingUseCaseTest` | Orquestração |
