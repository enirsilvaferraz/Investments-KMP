# Contract: Grupos balanceáveis e não balanceáveis

**Feature**: `025-balanceable-groups` | **Phase**: 1 | **Date**: 2026-06-07

---

## Regras universais (obrigatório — sem ramos por tipo de nó)

| # | Regra | Implementação |
|---|--------|---------------|
| R1 | Universo | `byNodeId[child] = byNodeId[parent].filter(child.matches)`; root ← `activeEntries` |
| R2 | Actual | Terminal: Σ `byNodeId[n]`; contentor: Σ actual(filhos) |
| R3 | Referência | `idealByNodeId[parent.id]`; init `idealByNodeId[root.id] = totalPortfolioValue` |
| R4 | Ideal | `BalancingIdealCalculator.computeIdeal(weight, actual, referenceBase)` |
| R5 | Desvio | `actual − ideal` |
| R6 | Log | Secção por contentor; linhas = filhos directos; pre-order DFS |
| R7 | Partição | Filhos directos terminais particionam `byNodeId[pai]` |

**Proibido**: ramos por `node.id`, `universeFilter`, bases especiais no motor.

---

## Tipos

```kotlin
internal data class BalancingTreeNode(…)
internal object PortfolioBalancingCatalog { val root: BalancingTreeNode }
```

---

## Engine

```kotlin
internal object PortfolioBalancingEngine {
    fun calculate(entries, referenceDate, root = PortfolioBalancingCatalog.root): PortfolioBalancingReport
}
```

```kotlin
// Bootstrap
state.idealByNodeId[root.id] = index.totalPortfolioValue

// Por contentor `node`, filho `child`:
val ref = state.idealByNodeId[node.id]           // R3
val actual = subtreeSum(child, index)            // R2
val ideal = computeIdeal(child.targetWeight, actual, ref)  // R4
state.idealByNodeId[child.id] = ideal
// deviation = actual - ideal                         // R5
```

Recursão idêntica em **todos** os contentores (R6).

---

## Validator

```kotlin
internal fun validateNode(node: BalancingTreeNode)
```

Mesma função recursiva em toda a árvore: R7 + regra de pesos (Σ Fixed+Zero = 100% quando sem Dynamic nos filhos directos).

---

## Relatório

`PortfolioBalancingReport.sections` — ordem de emissão R6. Formatter sem lógica adicional.

---

## Testes

- `idealByNodeId[balanceable] == actual(balanceable)` com peso Dynamic sob root
- `ideal(pre) == idealByNodeId[rf] × prePercent`
- `byNodeId[rf].size <= byNodeId[balanceable].size <= byNodeId[root].size` (R1 — filtros acumulam)
- `byNodeId[pre]` contém só pré-fixados
