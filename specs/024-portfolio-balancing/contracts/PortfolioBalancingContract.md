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
  → GetHoldingHistoriesUseCase(ByReferenceDate(period))  // dados já persistidos; sem CreateHistoryUseCase
  → PortfolioBalancingEngine.calculate(entries, period)
  → PortfolioBalancingReport
```

**UI**: um `IconButton` no histórico; **sem** ecrã dedicado (FR-011). ViewModel **não** carrega entradas nem define período.

**Período**: sempre o **mês corrente** (`DateProvider`) — independente do mês seleccionado no selector do histórico.

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

### Orquestração interna (`execute`)

```kotlin
val period = dateProvider.getCurrentYearMonth()
val entries = getHoldingHistoriesUseCase(ByReferenceDate(period)).getOrThrow()
return PortfolioBalancingEngine.calculate(entries, period)
```

| Regra | Detalhe |
|-------|---------|
| Parâmetro | **`Unit`** — sem entradas nem período no `Param` |
| Período | `DateProvider.getCurrentYearMonth()` (padrão `ExportToCsvUseCase`) |
| Leitura | **Só** `GetHoldingHistoriesUseCase` — dados já devem existir; **proibido** `CreateHistoryUseCase` |
| Filtros | **Não** invocar `FilterHoldingHistoryUseCase` |
| Lista vazia | Relatório completo com totais zero (FR-013) — não gera histórico em falta |
| Posições liquidadas | Excluídas no engine (`património <= 0`) |
| Erro de fetch | Propaga `Result.failure` ao caller (VM imprime mensagem) |

### Saída: `PortfolioBalancingReportLine`

| Campo exposto ao log | Origem |
|----------------------|--------|
| `componentName` | `BalancingComponent.displayName` |
| `actualValue` | Soma patrimónios classificados |
| `targetWeightDisplay` | Fixo `%.2f%%`; dinâmico `dinâmico (%.2f%%)` |
| `idealValue` | FR-004/004a/004b/004c/005/006 |
| `deviation` | `actualValue - idealValue` |

**Não expor**: predicado/regra de enquadramento (FR-001, FR-012).

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
| Visibilidade | `internal` — lógica pura testada directamente (FR-014) |
| Catálogo | `PortfolioBalancingCatalog` (estático) |
| Partição | Testes FR-007a no engine/catálogo |

---

## PortfolioBalancingCatalog

```kotlin
internal object PortfolioBalancingCatalog {
    val groups: List<BalancingGroup>
}
```

| Regra | Detalhe |
|-------|---------|
| Extensão | Novo componente = entrada no catálogo; **sem** alterar `Engine` |
| Tickers | `HASH11`, `IVVB11` como constantes internas |

---

## formatPortfolioBalancingReport

```kotlin
internal fun formatPortfolioBalancingReport(report: PortfolioBalancingReport): String
```

| Coluna | Largura sugerida | Formato |
|--------|------------------|---------|
| Nome | 24 | alinhado à esquerda |
| Valor actual | 14 | `R$ %,.2f` (locale pt-BR) |
| Peso alvo | 16 | percentagem ou «dinâmico» |
| Valor ideal | 14 | monetário |
| Desvio | 14 | monetário; sinal explícito |

Entre grupos: linha em branco + cabeçalho `=== {groupName} ===`.

---

## HistoryViewModel

### Intent

```kotlin
data object CalculatePortfolioBalancing : HistoryIntent
```

### Handler `calculatePortfolioBalancing()`

```kotlin
viewModelScope.launch {
    calculatePortfolioBalancingUseCase(Unit)
        .onSuccess { println(formatPortfolioBalancingReport(it)) }
        .onFailure { println("Balanceamento: ${it.message}") }
}
```

| Regra | Detalhe |
|-------|---------|
| Fetch / período | **Não** no VM — só no UC |
| Concorrência | Cada toque = novo `launch`; botão **não** desactiva (FR-010) |
| Erro técnico | Mensagem no log; não falha silenciosa |

### Injeção Koin

Adicionar `CalculatePortfolioBalancingUseCase` ao construtor de `HistoryViewModel`.

---

## AssetHistoryScreen

### `Actions` — parâmetros

```kotlin
onBalancingClick: () -> Unit,
```

### Layout (ordem)

`MonthYearSelector` → `Close?` → `Sync` → `Import B3` → **`Balance`** → `Export CSV`

```kotlin
IconButton(onClick = onBalancingClick) {
    Icon(
        imageVector = Icons.Default.Balance,
        contentDescription = "Balanceamento de carteira",
    )
}
```

**Sem** estado de loading no botão de balanceamento.

---

## Testes (`:domain:usecases:jvmTest`)

| Ficheiro | Âmbito |
|----------|--------|
| `PortfolioBalancingEngineTest` | Cenários FR-014 (motor puro) |
| `PortfolioBalancingPartitionTest` | FR-007a |
| `CalculatePortfolioBalancingUseCaseTest` | Orquestração: mock `DateProvider` + `GetHoldingHistoriesUseCase` |

Nomes em inglês, padrão `GIVEN_WHEN_THEN` (princípio V).
