# Contract: Identificador B3 (Cadastro + Histórico)

**Feature**: `004-fixed-income-b3-id` | **Phase**: 1 | **Date**: 2026-05-24

---

## Visão geral

Contrato de UI e dados entre camadas para o campo opcional **Identificador B3** em ativos de renda fixa e a coluna de status no ecrã **Posicionamento no Período** (`AssetHistoryScreen`).

```
:domain:entity          →  FixedIncomeAsset.b3Identifier
:data:database          →  fixed_income_assets.b3_identifier (+ migration 5→6)
:presentation:asset-management  →  FormTextField "Identificador B3"
:domain:usecases        →  GetHistoryTableDataUseCase → HoldingHistoryView.b3IdentifierStatus
:features:composeApp    →  AssetHistoryScreen — coluna direita UiTableV3
:presentation:naming   →  B3IdentifierStatusCell (ícones + tooltip)
```

---

## Cadastro de ativo (`AssetManagementScreen`)

### Campo

| Propriedade | Valor |
|-------------|--------|
| Label | `Identificador B3` |
| Componente | `FormTextField` (mesmo de “Observações gerais”) |
| Obrigatório | Não |
| `maxLength` | Nenhum |
| Visibilidade | Apenas quando `ui.category == InvestmentCategory.FIXED_INCOME` |
| Localização | Dentro de `FixedIncomeFields` (após linha vencimento/rentabilidade) |

### Eventos

```kotlin
// AssetManagementEvents.kt
data class B3IdentifierChanged(val value: String) : AssetManagementEvents
```

### Persistência ao Salvar

- `buildFixedIncomeAsset()` inclui `b3Identifier = b3Identifier?.trim()?.ifBlank { null }`.
- RV/Fund: estado `b3Identifier` ignorado no build (não existe no tipo de domínio).

---

## Histórico (`AssetHistoryScreen` / `UiTableV3`)

### Coluna

| Propriedade | Valor |
|-------------|--------|
| Posição | Última coluna (após “Valorização”) |
| Cabeçalho | `""` (vazio), como coluna de ícones de categoria |
| Largura | `TableColumnWidth.MaxIntrinsic` |
| `comparable` | Ordenação por status (opcional: `NotInformed` < `Informed` < `NotApplicable`) ou omitir |

### Conteúdo por linha (`B3IdentifierStatus`)

| Status | Categoria | Renderização |
|--------|-----------|--------------|
| `Informed(value)` | Renda fixa | `Icons.Default.Info`, tint `getInfoColor()`, tooltip = `value` |
| `NotInformed` | Renda fixa | `Icons.Default.Warning`, tint `getWarningColor()`, tooltip = `Identificador B3 não informado.` |
| `NotApplicable` | RV, Fundo | **Célula vazia** — sem `Icon`, sem `TooltipBox` |

> **Requisito explícito (planeamento)**: linhas não-RF mantêm a coluna alinhada com célula vazia, não omitem a coluna.

### Composable sugerido

```kotlin
// com.eferraz.naming.B3IdentifierStatusCell.kt
@Composable
fun B3IdentifierStatus.BuildCell(modifier: Modifier = Modifier)
```

Padrão de interação: `TooltipBox` + `PlainTooltip` + `TooltipAnchorPosition.End` (igual `TableIcons.kt`).

---

## Dados na linha (`HoldingHistoryView`)

```kotlin
public val b3IdentifierStatus: B3IdentifierStatus
```

Mapeamento em `HoldingHistoryView(HistoryTableData)`:

```kotlin
b3IdentifierStatus = when (this) {
    is FixedIncomeHistoryTableData ->
        b3Identifier?.trim()?.takeIf { it.isNotBlank() }
            ?.let(B3IdentifierStatus::Informed)
            ?: B3IdentifierStatus.NotInformed
    else -> B3IdentifierStatus.NotApplicable
}
```

---

## Migração de banco (obrigatória)

| Passo | Ação |
|-------|------|
| 1 | Adicionar `b3Identifier` em `FixedIncomeAssetEntity` |
| 2 | `AppDatabase.version = 6` + `AutoMigration(5, 6)` |
| 3 | Compilar `:data:database` e versionar `schemas/.../6.json` |
| 4 | Validar app com DB v5 existente: abrir, listar RF, histórico com ícone amarelo |

---

## Critérios de aceitação (contrato)

1. Salvar RF com identificador `"  ABC-123/XYZ  "` e reabrir → campo mostra `ABC-123/XYZ` (trim aplicado).
2. Salvar RF com campo vazio ou só espaços (antes ou depois de trim) → `NULL` no DB; histórico com ícone amarelo.
3. Linha RV no histórico → coluna direita **vazia**.
4. Linha Fundo no histórico → coluna direita **vazia**.
5. Atualizar app com DB v5 → sem crash; RF legados com identificador ausente.
