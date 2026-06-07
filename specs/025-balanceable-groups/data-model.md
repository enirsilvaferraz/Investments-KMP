# Data Model: Grupos balanceáveis e não balanceáveis

**Feature**: `025-balanceable-groups` | **Date**: 2026-06-07

## Escopo

Árvore única, tipo único, **mesmas regras para todos os nós**.

---

## `BalancingTreeNode`

| Campo | Tipo |
|-------|------|
| `id` | `String` |
| `displayName` | `String` |
| `targetWeight` | `TargetWeight` |
| `matches` | `(HoldingHistoryEntry) -> Boolean` — **obrigatório em todos os nós** |
| `children` | `List<BalancingTreeNode>` |

Contentor = `children.isNotEmpty()` | Terminal = `children.isEmpty()`

---

## Regras universais (R1–R7)

| # | Regra |
|---|--------|
| **R1 Universo** | `byNodeId[filho] = byNodeId[pai].filter(filho.matches)`; raiz ← `activeEntries` |
| **R2 Actual** | Terminal: `Σ património(byNodeId[n])`; contentor: `Σ actual(filhos)` |
| **R3 Referência** | `referenceBase(n) = idealByNodeId[pai.id]`; bootstrap: `idealByNodeId[root.id] = totalPortfolioValue` |
| **R4 Ideal** | `computeIdeal(weight, actual, referenceBase)` |
| **R5 Desvio** | `actual − ideal` |
| **R6 Log** | Contentor → secção; filhos directos → linhas; DFS |
| **R7 Partição** | Filhos **directos terminais** particionam `byNodeId[pai]` (exclusivo + exaustivo) |

### R1 — filtragem progressiva (base → folhas)

A lista **estreia a cada nível** — filho recebe subconjunto do pai, nunca a lista global de novo:

```text
byNodeId[root]         = activeEntries              (matches = always)
byNodeId[nãoBalanceável] = filter(isNonBalanceable)
byNodeId[balanceável]    = filter(isBalanceable)
byNodeId[rf]             = filter(isFixedIncome)      ← sobre lista já balanceável
byNodeId[pré]            = filter(isPreFixed)         ← sobre lista já RF
```

```kotlin
fun buildUniverse(node: BalancingTreeNode, parentId: String?) {
    byNodeId[node.id] = when (parentId) {
        null -> activeEntries
        else -> byNodeId[parentId].filter(node.matches)
    }
    node.children.forEach { buildUniverse(it, node.id) }
}
```

**Sem** campo `universeFilter` — `matches` cumpre os dois papéis: estreia universo na descida (R1) e, em terminais, define a linha (R2).

---

## Árvore canónica (excerpt `matches`)

| Nó | `matches` |
|----|-----------|
| root | `always` |
| nãoBalanceável | `isNonBalanceable` |
| balanceável | `isBalanceable` |
| rf | `isFixedIncome` |
| pré | `isFixedIncomeWithIndexer(PRE)` |
| previdência | `isPensionFund` |

---

## Validação

Por contentor `p`: filhos directos terminais particionam **`byNodeId[p]`** (R7). Σ Fixed+Zero = 100% quando sem Dynamic nos filhos directos.

---

## Saída e fluxo

Inalterado: `sections` em ordem DFS; `balanceableBase` = `actual(balanceável)` derivado pós-cálculo.

```text
BalancingUniverseIndex.build(root)   // R1 — listas progressivas
computeAndEmitNode(root)             // R2–R6
```
