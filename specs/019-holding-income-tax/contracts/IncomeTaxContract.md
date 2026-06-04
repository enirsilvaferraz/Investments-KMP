# Contract: IR regressivo sobre rendimentos (019)

**Feature**: `019-holding-income-tax` | **Phase**: 1 | **Date**: 2026-06-04

Contrato mínimo — **um** artefacto em `:domain:entity` / `holdings`, sem novos subprojetos Gradle.

---

## `IncomeTax` (`:domain:entity` / `holdings`)

```kotlin
public data class IncomeTax private constructor(
    public val taxRate: Double,
    public val taxValue: Double,
) {
    public companion object {
        public fun calculate(
            profit: Double,
            purchaseDate: LocalDate,
            referenceDate: LocalDate,
        ): IncomeTax
    }
}
```

**Semântica**

- `daysInvested = purchaseDate.daysUntil(referenceDate)` (kotlinx-datetime).
- Se `purchaseDate > referenceDate` → lançar `IllegalArgumentException`.
- Tabela: `daysInvested <= 180` → 22.5%; `<= 360` → 20%; `<= 720` → 17.5%; senão 15%.
- `taxValue = if (profit > 0) profit * taxRate / 100 else 0.0` (sem arredondamento monetário no domínio).

**Proibido**: importar `AssetHolding`, `AssetTransaction`, use cases, Compose; derivar `purchaseDate` de transações nesta entrega.

---

## Testes (`entity` / `jvmTest`)

| Ficheiro | Obrigatório |
|----------|-------------|
| `IncomeTaxTest.kt` | Sim — faixas, fronteiras 180/181/360/361/720/721, lucro ≤ 0, data inválida |

Padrão: `GIVEN_WHEN_THEN`, delta `0.01` em `taxValue`.

---

## Fora do contrato v1

- `earliestPurchaseDate` / extensões em `List<AssetTransaction>`
- `:domain:usecases`, `:data`, `:features`
- Arredondamento contábil regulado na UI
- IR por lote / múltiplas compras com alíquotas distintas
