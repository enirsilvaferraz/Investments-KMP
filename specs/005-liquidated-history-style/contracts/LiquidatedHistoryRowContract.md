# Contract: Linha liquidada no Histórico

**Feature**: `005-liquidated-history-style` | **Phase**: 1 | **Date**: 2026-05-24

---

## Visão geral

Contrato entre `:domain:usecases` (`HoldingHistoryView.isLiquidated`) e `:features:composeApp` (`AssetHistoryScreen` / `UiTableV3`) para estilo visual de posições com valor atual zero.

```
:domain:usecases   →  HoldingHistoryView.isLiquidated  (regra: currentValue == 0.0)
:design-system     →  historyMutedTextColor()          (tom de referência)
:composeApp        →  AssetHistoryScreen.Table         (aplicação por coluna)
:composeApp        →  TableInputMoney (textColor)      (coluna Valor Atual — ver secção abaixo)
```

---

## Domínio / view (`HoldingHistoryView`)

| Propriedade | Tipo | Regra |
|-------------|------|--------|
| `isLiquidated` | `Boolean` | `currentValue == 0.0` |
| Visibilidade | `public` | Consumo por UI e filtros futuros |
| Persistência | — | Derivado; não serializar |

**Uso futuro (filtro)**:

```kotlin
rows.filter { it.isLiquidated }
rows.filterNot { it.isLiquidated }
```

---

## Cor de texto muted (`historyMutedTextColor`)

| Propriedade | Valor |
|-------------|--------|
| Referência visual | Igual a Valorização com percentual zero na tabela atual |
| Implementação alvo | `Color.Gray.copy(alpha = 0.5f)` centralizado (DRY) |
| Módulo | `:presentation:design-system` (`theme` ou ficheiro dedicado) |

---

## Tabela `AssetHistoryScreen` — regras por coluna

| Coluna | `isLiquidated == true` | `isLiquidated == false` |
|--------|------------------------|-------------------------|
| Ícones (categoria, liquidez) | Cores atuais dos ícones | Igual |
| Corretora | `historyMutedTextColor()` | `LocalContentColor` / default |
| Display Name | muted | default |
| Observação | muted | default |
| Valor Anterior | muted | default |
| Valor Atual (`TableInputMoney`) | `textColor = muted` (campo pode permanecer `enabled`) | default do componente |
| **Transações** | **Regra atual** (verde/vermelho/cinza zero/“Adicionar”) | Igual |
| **Valorização** | **Regra atual** (verde/vermelho/cinza zero) | Igual |
| Identificador B3 | Ícones + tooltip padrão | Igual |

**Precedência**: Para Transações e Valorização, `isLiquidated` **não** altera a lógica `when` de cor existente.

---

## `TableInputMoney` (extensão de contrato)

| Item | Valor |
|------|--------|
| **Módulo** | `:features:composeApp` |
| **Ficheiro** | `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/design_system/components/inputs/TableInputMoney.kt` |
| **Import no Histórico** | `com.eferraz.presentation.design_system.components.inputs.TableInputMoney` |

> Existe homónimo em `:presentation:design-system`; **não** é o usado por `AssetHistoryScreen`.

| Parâmetro novo | Tipo | Default |
|----------------|------|---------|
| `textColor` | `Color?` | `null` → `onSurface` / `onSurfaceVariant` conforme `enabled` |

Quando `row.isLiquidated`:

```kotlin
TableInputMoney(
    value = row.currentValue,
    onValueChange = { ... },
    enabled = row.isCurrentValueEnabled(),
    textColor = historyMutedTextColor(),
)
```

---

## Tooltips

| Elemento | Comportamento | Verificação |
|----------|----------------|-------------|
| Texto do tooltip (B3 via `B3IdentifierStatusCell`, etc.) | Estilo padrão do `TooltipBox` — **sem** `historyMutedTextColor()` | Linha liquidada + hover no ícone B3 → tooltip legível com estilo habitual |
| Implementação | **Sem alteração** em `core/presentation/naming/src/commonMain/kotlin/com/eferraz/naming/B3IdentifierStatusCell.kt` nesta feature | Regressão manual em [quickstart.md](../quickstart.md) |

---

## Testes contratuais (`:domain:usecases:jvmTest`)

| Caso | `currentValue` | `isLiquidated` esperado |
|------|----------------|-------------------------|
| Liquidado | `0.0` | `true` |
| Ativo | `100.0` | `false` |
| Negativo | `-1.0` | `false` |

Padrão de nome: `GIVEN_..._WHEN_..._THEN_...`

---

## Verificação manual

Ver [quickstart.md](../quickstart.md).
