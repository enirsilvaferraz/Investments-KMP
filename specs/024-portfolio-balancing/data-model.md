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

---

## Novos tipos (`:domain:usecases`)

### `TargetWeight` (sealed)

| Variant | Semântica | Valor ideal (G1) |
|---------|-----------|------------------|
| `Fixed(percent: Double)` | Peso configurado % do universo de referência | `reference × percent / 100` |
| `Zero` | Peso 0% (FR-006) | `0` |
| `Residual` | Previdência (FR-005) — excluída da base balanceável | `actual` (desvio sempre 0) |

### `PortfolioBalancingReportLine`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `groupId` | `BalancingGroupId` | Agrupamento no log |
| `groupName` | `String` | |
| `componentName` | `String` | |
| `actualValue` | `Double` | Soma patrimónios enquadrados |
| `configuredWeightDisplay` | `String` | Ex.: `50,00%`, `0,00%`, `dinâmico` |
| `configuredWeightPercent` | `Double?` | Numérico para Fixed/Zero; `null` para Residual |
| `normalizedWeightDisplay` | `String` | Ex.: `45,00%` |
| `normalizedWeightPercent` | `Double` | `idealValue / totalPortfolioValue × 100` |
| `idealValue` | `Double` | |
| `deviation` | `Double` | `actualValue - idealValue` |

---

## Catálogo inicial (FR-007)

### Grupo 1 — Carteira Total

| Componente | Peso configurado | Universo ideal |
|------------|------------------|----------------|
| Renda Fixa | 50% Fixed | base balanceável |
| Renda Variável | 49% Fixed | base balanceável |
| Fundos de Previdência | Residual | actual |
| Cripto Ativos | 1% Fixed | base balanceável |
| Demais investimentos | Zero | 0 |

**Base balanceável** = `totalCarteira − actual(previdência)`.

### Grupos 2 e 3

Inalterados (33,33% × 3; 50/10/30/10). Ideal = peso × ideal(pai no G1).

---

## Regras de validação (invariantes)

| ID | Regra |
|----|-------|
| V1 | Soma `actualValue` G1 = `totalPortfolioValue` |
| V2 | Partição exclusiva e exaustiva por grupo (FR-007a) |
| V3 | `Residual`: `deviation == 0` quando `totalPortfolioValue > 0` |
| V4 | `totalPortfolioValue == 0` → actual/ideal/desvio/normalizado = 0; configurados visíveis |
| V5 | Grupos 2 e 3 sempre presentes |
| V6 | Σ configured (Fixed+Zero, excl. Residual) = 100% ± 0,01% por grupo |
| V7 | Σ normalized G1 = 100% ± 0,01% quando total > 0 |

---

## Log (FR-012)

Colunas: nome, valor actual, peso configurado, peso normalizado, valor ideal, desvio.

Linha **`Total`** por grupo: somas monetárias; configurado `100,00%`; normalizado = Σ normalizados.
