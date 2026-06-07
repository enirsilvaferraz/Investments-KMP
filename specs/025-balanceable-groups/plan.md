# Implementation Plan: Grupos balanceáveis e não balanceáveis

**Branch**: `025-balanceable-groups` | **Date**: 2026-06-07 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/025-balanceable-groups/spec.md`

**Diretriz**: Árvore **única** com raiz **Carteira Total**; **um tipo** (`BalancingTreeNode`); cálculo e log seguem **o mesmo percurso DFS** — secção = nó; filhos directos = linhas da tabela; repetir até fechar a árvore.

> **Alinhamento spec** (2026-06-07): `spec.md` adopta **Carteira Total** como raiz e 1.ª secção no log (filhos = Não balanceável + Balanceável), equivalente funcional ao antigo «Composição da Carteira».

## Summary

Catálogo = **`BalancingTreeNode` raiz** «Carteira Total» → subárvore completa. **Sete regras idênticas para todos os nós** (sem ramos especiais por tipo/raiz/profundidade). Motor e log = mesmo DFS recursivo.

## Technical Context

**Language/Version**: Kotlin 2.x — KMP (`commonMain` / `jvmTest`)

**Primary Dependencies**: `:domain:usecases` (`balancing`), `:domain:entity`

**Storage**: N/A

**Testing**: `:domain:usecases:jvmTest/balancing/` — sem `./gradlew` automático (princípio IX)

**Project Type**: Refactor domínio — árvore única, log = árvore

**Scale/Scope**: ~10 ficheiros em `balancing/`; remover Group/Component/Leaf, WeightCalculator duplo, overlay, flattenForDisplay

## Constitution Check

| Princípio | Status |
|-----------|--------|
| I — KISS | ✅ Um nó, um DFS, uma fórmula |
| II — Clean Architecture | ✅ |
| V — Testes | ✅ DFS log + propagação referenceBase |
| X — Escopo | ✅ |

**Gate**: PASS

## Project Structure

### Source Code

```text
core/domain/usecases/.../balancing/
├── NonBalanceableAssetList.kt
├── BalancingTreeNode.kt              # único tipo de configuração
├── BalancingGroupId.kt
├── PortfolioBalancingModels.kt       # ReportSection + ReportRow (ou lines agrupadas)
├── PortfolioBalancingCatalog.kt    # root: carteiraTotalNode
├── BalancingMatchers.kt
├── BalancingUniverseIndex.kt         # byNodeId recursivo desde raiz
├── BalancingIdealCalculator.kt
├── PortfolioBalancingEngine.kt       # computeAndEmitNode — regras únicas
├── FormatPortfolioBalancingReport.kt # formatTreeReport — espelha DFS do engine
├── PortfolioBalancingCatalogValidator.kt
└── CalculatePortfolioBalancingUseCase.kt
```

## Modelo: árvore única, nós only

### Árvore canónica

```text
carteiraTotalNode                    ← raiz; 1.ª secção no log
├── nonBalanceableNode               ← linha na tabela Carteira Total
│   ├── previdenciaNode              ← terminal; linha na tabela Não Balanceável
│   ├── fgtsNode
│   └── demaisNonBalanceableNode
└── balanceableNode                  ← linha na tabela Carteira Total
    ├── cryptoNode                   ← terminal ou com filhos
    ├── rfNode                       ← linha Balanceável; secção própria se tiver filhos
    │   ├── preNode                  ← linhas na tabela RF
    │   ├── posNode
    │   └── ipcaNode
    ├── rvNode
    │   └── …
    └── demaisInvestimentosNode
```

### Tipo único

```kotlin
internal data class BalancingTreeNode(
    val id: String,
    val displayName: String,
    val targetWeight: TargetWeight,
    val matches: (HoldingHistoryEntry) -> Boolean,  // todos os nós; estreia universo na descida
    val children: List<BalancingTreeNode> = emptyList(),
)

internal object PortfolioBalancingCatalog {
    val root: BalancingTreeNode  // carteiraTotalNode
}
```

| Papel | Critério |
|-------|----------|
| **Contentor** | `children.isNotEmpty()`; `actual` = Σ actual dos filhos |
| **Terminal** | `children.isEmpty()`; `actual` = Σ património em `byNodeId[nó]` |

Todos os nós têm **`matches`** — na descida da árvore, estreia a lista herdada do pai.

---

## Regras universais (todos os nós, zero excepções)

| # | Regra | Fórmula / comportamento |
|---|--------|-------------------------|
| **R1** | Universo | `byNodeId[filho] = byNodeId[pai].filter(filho.matches)`; raiz: `activeEntries` (`root.matches` = sempre true) |
| **R2** | Actual | Terminal: `Σ património(byNodeId[n])`; contentor: `Σ actual(filhos directos)` |
| **R3** | Base de referência | `referenceBase(n) = idealByNodeId[pai.id]`; **inicialização**: `idealByNodeId[root.id] = totalPortfolioValue` |
| **R4** | Ideal | `ideal(n) = computeIdeal(n.targetWeight, actual(n), referenceBase(n))` |
| **R5** | Desvio | `deviation(n) = actual(n) − ideal(n)` |
| **R6** | Log | Se `n.children.isNotEmpty()`: secção `=== n.displayName ===`; cada filho directo = linha; Total; recursão nos filhos contentores |
| **R7** | Partição | Filhos directos terminais particionam **`byNodeId[pai]`** (exclusivo + exaustivo) |

**R1 — filtragem progressiva** (raiz → folhas):

```text
byNodeId[root]        = activeEntries
byNodeId[balanceável] = byNodeId[root].filter(isBalanceable)
byNodeId[rf]          = byNodeId[balanceável].filter(isFixedIncome)
byNodeId[pré]         = byNodeId[rf].filter(isPreFixed)
```

Cada nível recebe a lista **já filtrada do pai** e aplica **só** o seu `matches` — sem `universeFilter` separado. A cadeia de filtros acumula da base às folhas.

**Índice** (`BalancingUniverseIndex.build`): DFS desde raiz; uma passagem; listas materializadas (ou views imutáveis) por `node.id`.

**Propagação**: ao processar filho `c` de contentor `p`, calcular-lhe actual/ideal/desvio com `referenceBase = idealByNodeId[p.id]`, guardar `idealByNodeId[c.id] = ideal(c)`, recursão.

**Consequência** (sem código especial): filhos Dynamic de Carteira Total têm `ideal = actual` (R4); filhos Fixed de Balanceável usam `idealByNodeId[balanceable] = balanceableBase` (R3+R4); filhos de RF usam `idealByNodeId[rf]` (FR-008).

**Catálogo** (conteúdo, não regra): pesos Dynamic vs Fixed/Zero variam por nó; validator verifica Σ Fixed+Zero = 100% nos filhos directos de **qualquer** contentor que não tenha filhos Dynamic.

### Log = percurso da árvore (pre-order DFS)

```text
=== Carteira Total ===
  Não Balanceável  | actual | dinâmico | ideal | desvio
  Balanceável      | …
  Total

=== Carteira Não Balanceável ===
  Previdência | …
  FGTS        | …
  Demais      | …
  Total

=== Carteira Balanceável ===
  Cripto | …
  RF     | …
  RV     | …
  Demais | …
  Total

=== Renda Fixa ===
  Pré-fixado | …
  Pós-fixado | …
  IPCA       | …
  Total

… (até fechar a árvore)
```

**Regra R6**: nó contentor → secção; filhos directos → linhas; recursão. Terminal → só linha do pai.

### Motor — pseudocódigo único

```kotlin
fun computeAndEmitNode(node: BalancingTreeNode, parent: BalancingTreeNode?, …) {
    if (node.children.isEmpty()) return  // terminal: linha emitida pelo pai

    val section = PortfolioBalancingReportSection(node.id, node.displayName)
    for (child in node.children) {
        val actual = if (child.children.isEmpty()) {
            index.byNodeId[child.id].sumOf(::patrimony)  // R2 terminal
        } else {
            child.children.sumOf { actual(it) }  // R2 contentor — ou pré-calculado no state
        }
        val ref = idealByNodeId[node.id]  // R3: pai é sempre o contentor actual
        val ideal = computeIdeal(child.targetWeight, actual, ref)  // R4
        idealByNodeId[child.id] = ideal
        section.rows += row(child, actual, ideal, actual - ideal)  // R5
    }
    section.totalRow = sumRows(section.rows)
    sections += section
    for (child in node.children) {
        computeAndEmitNode(child, node, …)  // R6
    }
}

// Inicialização (única excepção de bootstrap, não por tipo de nó):
idealByNodeId[root.id] = totalPortfolioValue
computeAndEmitNode(root, parent = null, …)
```

**Sem** `BalancingReferenceBase`, **sem** tabela por tipo de pai, **sem** ramos «principal vs aninhado».

### Relatório

```kotlin
data class PortfolioBalancingReportSection(
    val nodeId: String,
    val nodeName: String,
    val rows: List<PortfolioBalancingReportLine>,  // uma por filho directo
    val totalRow: PortfolioBalancingReportLine,
)

data class PortfolioBalancingReport(
    val sections: List<PortfolioBalancingReportSection>,  // ordem = pre-order DFS
    …
)
```

`FormatPortfolioBalancingReport` itera `sections` — **sem** lógica de ordem própria.

## Estratégia de implementação

| Onda | Entregável |
|------|------------|
| **1** | `BalancingTreeNode`, catálogo `root`, migrar nested para filhos |
| **2** | Index, `computeAndEmitNode` (7 regras), formatter DFS |
| **3** | Testes: ordem secções = ordem árvore; linhas = filhos; totais; referenceBase |

### Prompt canónico — Onda 1

```text
Feature 025. BalancingTreeNode only (no Leaf). PortfolioBalancingCatalog.root = carteiraTotalNode with children nonBalanceableNode, balanceableNode. RF/RV/Crypto as children of balanceable; Pre/Pos/IPCA as terminal children of rfNode. Fallback terminals for demais. NonBalanceableAssetList. Validator: siblings partition universe; Fixed+Zero=100% per contentor node where Dynamic forbidden.
```

### Prompt canónico — Onda 2

```text
Feature 025. BalancingUniverseIndex (R1). computeAndEmitNode: idealByNodeId[root]=totalPortfolioValue; for every contentor, children use referenceBase=idealByNodeId[parent.id] (R3-R5); log R6. Single computeIdeal. No BalancingReferenceBase, no per-node-type branches. FormatPortfolioBalancingReport iterates sections.
```

### Prompt canónico — Onda 3

```text
Feature 025. Tests: log order matches tree pre-order; Carteira Total first with 2 child rows; RF section follows Balanceável; terminal nodes only as rows; ideal propagation. quickstart.md updated.
```

## Complexity Tracking

Sem violações.
