# Data Model: 019-holding-income-tax

**Feature**: `019-holding-income-tax` | **Phase**: 1 | **Date**: 2026-06-04

---

## Visão

```text
lucro (Double) ────────┐
purchaseDate ──────────┼──► IncomeTax.calculate(...) ──► IncomeTax { taxRate, taxValue }
referenceDate ─────────┘
```

Sem persistência, sem leitura de transações, sem migração Room.

---

## `IncomeTax` (`com.eferraz.entities.holdings`)

| Campo | Tipo | Semântica |
|-------|------|-----------|
| `taxRate` | `Double` | Alíquota da faixa em percentual legível (22.5 = 22,5%) |
| `taxValue` | `Double` | Imposto em reais; zero se lucro ≤ 0 |

| Operação | Entrada | Saída / erro |
|----------|---------|----------------|
| `calculate(profit, purchaseDate, referenceDate)` | Lucro, datas | `IncomeTax` |
| Validação | `purchaseDate > referenceDate` | `IllegalArgumentException` |

**Invariantes**

- `taxValue >= 0` sempre.
- Se `profit <= 0` → `taxValue == 0` (`taxRate` ainda reflete a faixa temporal — spec US1 cenário 5).
- `taxRate` ∈ {22.5, 20.0, 17.5, 15.0} para entradas válidas.

**Faixas** (dias = `purchaseDate.daysUntil(referenceDate)`):

| Dias investidos | `taxRate` |
|-----------------|-----------|
| 0–180 | 22.5 |
| 181–360 | 20.0 |
| 361–720 | 17.5 |
| > 720 | 15.0 |

---

## Relação com entidades existentes

| Entidade | Papel nesta feature |
|----------|---------------------|
| `Growth` | Padrão estrutural (`calculate` estático, resultado imutável) |
| `AssetHolding` / `AssetTransaction` | **Sem alteração**; integração futura resolve `purchaseDate` fora deste módulo |

---

## Fora do modelo v1

- `List<AssetTransaction>.earliestPurchaseDate()` ou equivalente

---

## Documentação

Atualizar `core/domain/entity/docs/DOMAIN.md` — subsecção curta em `holdings` (`IncomeTax`, tabela regressiva).
