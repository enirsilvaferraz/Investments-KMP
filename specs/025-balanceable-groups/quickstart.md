# Quickstart: Grupos balanceáveis e não balanceáveis

**Feature**: `025-balanceable-groups` | **Date**: 2026-06-07

## Validação manual (log)

1. Acionar balanceamento no histórico.
2. Confirmar que secções aparecem **na ordem da árvore** (DFS):

```text
=== Carteira Total ===
  Não Balanceável | …
  Balanceável     | …
  Total

=== Carteira Não Balanceável ===
  …
  Total

=== Carteira Balanceável ===
  Cripto | …
  Renda Fixa | …
  …
  Total

=== Renda Fixa ===
  Pré-fixado | …
  …
  Total
```

### Checklist

- [x] 1.ª secção = **Carteira Total** (2 linhas + Total)
- [x] Filhos directos = linhas (RF é linha em Balanceável **e** secção própria depois)
- [x] Pré/Pós/IPCA = linhas em RF, **sem** secção «Pré-fixado»
- [x] Cinco colunas; sem peso normalizado
- [x] `desvio = actual − ideal`

## Testes unitários (sob pedido)

```bash
./gradlew :domain:usecases:jvmTest --tests "com.eferraz.usecases.balancing.*"
```

| ID | Cenário | Esperado |
|----|---------|----------|
| Q1 | Mix balanceável + não balanceável | Carteira Total: 2 linhas somam total |
| Q2 | Ordem sections | Pre-order catálogo |
| Q3 | RF sob Balanceável | RF row actual = soma secção RF |
| Q4 | ideal RF filho | `balanceableBase × peso RF × peso Pré` |

Detalhes: [data-model.md](data-model.md), [contracts/PortfolioBalancingContract.md](contracts/PortfolioBalancingContract.md).
