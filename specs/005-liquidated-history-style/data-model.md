# Data Model: Destaque visual de investimentos liquidados no Histórico

**Feature**: `005-liquidated-history-style` | **Phase**: 1 | **Date**: 2026-05-24

---

## Decisões arquiteturais

| Decisão | Escolha |
|---------|---------|
| Persistência | **Nenhuma** — estado derivado de `currentValue` já na view |
| Regra de negócio | `isLiquidated ⇔ currentValue == 0.0` em `HoldingHistoryView` |
| Camada | `:domain:usecases` (`entities/HoldingHistoryView.kt`) |
| UI | `:features:composeApp` — `AssetHistoryScreen.kt` |
| Migração Room | Não aplicável |
| Filtro futuro | Consumir `HoldingHistoryView.isLiquidated` (fora do escopo desta entrega) |

---

## View de domínio/apresentação (`:domain:usecases`)

### `HoldingHistoryView` (alteração)

| Membro | Tipo | Regras |
|--------|------|--------|
| `currentValue` | `Double` | Já existente; fonte da liquidação |
| `isLiquidated` | `Boolean` (derivado) | `get() = currentValue == 0.0`; **não** incluir no construtor primário |
| `isCurrentValueEnabled()` | `Boolean` | Inalterado (categoria RV) |

**Invariantes**:

1. `isLiquidated == true` ⟺ `currentValue == 0.0` (comparação exata, incluindo `-0.0` tratado como zero em Kotlin `==`).
2. `currentValue < 0` ⟹ `isLiquidated == false`.
3. Alterar `currentValue` numa nova instância da view reflete imediatamente em `isLiquidated` (imutabilidade por instância).

**Exemplo (conceitual)**:

```kotlin
public data class HoldingHistoryView(
    // ... campos existentes ...
    val currentValue: Double,
    // ...
) {
    public val isLiquidated: Boolean
        get() = currentValue == 0.0

    // construtor secundário HistoryTableData inalterado nos demais campos
}
```

---

## Estado derivado (sem entidade persistida)

```text
                    ┌─────────────────────┐
                    │ HoldingHistoryView  │
                    │  currentValue: D    │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │  isLiquidated       │
                    │  (currentValue==0)  │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
     Colunas gerais      Valorização      Transações
     → cinza muted      → regra atual    → regra atual
     (se liquidado)     (prioridade)     (prioridade)
```

---

## Camada de apresentação (derivados visuais)

| Conceito | Onde | Notas |
|----------|------|-------|
| `historyMutedTextColor()` | `:presentation:design-system` | Tom = valorização zero / cinza existente |
| Colunas com cinza condicional | `AssetHistoryScreen` | Quando `row.isLiquidated` e coluna **não** é Valorização/Transações/ícones/B3 |
| Tooltips B3 | `B3IdentifierStatusCell` | Sem alteração de cor de tooltip |
| Ícones categoria/liquidez | `BuildIcon()` | Sem alteração |

---

## Documentação `DOMAIN.md`

**Não obrigatório** nesta feature: liquidação é estado de **view** para histórico mensal, não atributo de `Asset` ou `HoldingHistoryEntry`. Se o filtro futuro promover regra de negócio partilhada, reavaliar menção em `DOMAIN.md` na feature do filtro.

---

## Fora do modelo

- Painel `Summary` — sem `isLiquidated`
- Lista de transações detalhada — escopo FR-007
- Filtro “só liquidados” — usará `isLiquidated` em feature futura
