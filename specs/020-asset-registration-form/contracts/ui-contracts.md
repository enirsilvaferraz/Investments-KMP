# UI Contracts: Cadastro — cards Ativo e Posicionamento

**Feature**: `020-asset-registration-form` | **Phase**: 1 | **Date**: 2026-06-05

---

## Visão geral

Contrato de UI e estado para completar os cards **ATIVO** e **POSICIONAMENTO** no dialog existente (`AssetManagementDialog`), reutilizando persistência da feature **001**.

```
:domain:entity          →  FixedIncomeAsset.incomeTaxExempt
:data:database          →  fixed_income_assets.income_tax_exempt (+ migration 7→8)
:features:asset-management
  ├── AssetManagementViewModel   → onSave (existente), reset classe
  ├── AssetManagementUiState     → + incomeTaxExempt
  ├── AssetManagementMap         → round-trip RF
  └── AssetManagementScreen      → wiring UI, barra inferior Salvar
```

**Fora deste contrato**: `TransactionFormView`, secção RESUMO, botão Excluir.

---

## Entrypoint (inalterado)

```kotlin
@Composable
public fun AssetManagementDialog(
    modifier: Modifier = Modifier,
    holdingId: Long?,
    onDismiss: () -> Unit,
)
```

| `holdingId` | Modo |
|-------------|------|
| `null` | Cadastro novo |
| `Long` | Edição |

Botão **Salvar** habilitado em **todos** os modos (sem excepção na UI).

---

## Card ATIVO

### Campos comuns

| Campo | Componente | Estado | Evento |
|-------|------------|--------|--------|
| Classe | `AppDropdownField` | `assetClass` | `AssetClassChanged` |
| Tipo | `AppDropdownField` | `type` | `TypeChanged` — opções **por classe** |
| Emissor | `AppDropdownField` | `issuer` | `IssuerChanged` |

### Renda fixa (`FixedIncomeFields`)

| Campo | Componente | Estado | Evento |
|-------|------------|--------|--------|
| Liquidez | dropdown | `fixedLiquidity` | `FixedLiquidityChanged` |
| Vencimento | `FormTextField` | `fixedExpiration` | `FixedExpirationChanged` |
| Indexador | dropdown | `yieldIndexer` | `YieldIndexerChanged` |
| Rent. (a.a.) | `FormTextField` | `fixedYield` | `FixedYieldChanged` |
| Rent. (CDI) | `FormTextField` | `fixedCdi` | `FixedCdiChanged` |
| Observações | `FormTextField` | `observations` | `ObservationsChanged` |
| Identificador B3 | `FormTextField` | `b3Identifier` | `B3IdentifierChanged` |
| **Isento de IR** | `SegmentedControl` Sim/Não | `incomeTaxExempt` | `IncomeTaxExemptChanged` |

**Isento de IR**:
- Visível **somente** `assetClass == FIXED_INCOME`
- Default UI: **"Não"** (`incomeTaxExempt = false`)
- **Proibido** estado local `remember` desligado do ViewModel

### Renda variável (`VariableIncomeFields`)

| Campo | Componente | Estado | Evento |
|-------|------------|--------|--------|
| Tipo | dropdown | `type` | `TypeChanged` |
| Ticker | `FormTextField` | `variableTicker` | `VariableTickerChanged` |
| CNPJ | `FormTextField` | `variableCnpj` | `VariableCnpjChanged` |
| Observações | `FormTextField` | `observations` | `ObservationsChanged` |

**Sem** Identificador B3 em RV (domínio não suporta).

### Fundo (`FundFields`)

Campos visíveis wired ao estado global (sem `Isento de IR`).

### Acções do card

| Regra | Valor |
|-------|-------|
| `FormCardActions` interno | **Removido** — não chama `Save` |

---

## Card POSICIONAMENTO

| Campo | Componente | Editável | Estado |
|-------|------------|----------|--------|
| Titular | `FormTextField` `readOnly = true` | **Não** | `owner?.name` |
| Corretora | `AppDropdownField` | **Sim** | `brokerage` → `BrokerageChanged` |

| Regra | Valor |
|-------|-------|
| Validação | `brokerage == null` → erro "Obrigatório" |
| `FormCardActions` interno | **Removido** |

---

## Barra inferior

| Controlo | Comportamento |
|----------|---------------|
| **Excluir** | `enabled = false` ou oculto — **sem implementação** |
| **Salvar** (ex-Concluir) | `onClick → AssetManagementEvents.Save`; **`enabled = true` sempre** |
| Double-tap | Ignorado no ViewModel via `if (isSaving) return` em `onSave()` |
| Sucesso | `isCompleted` → `onDismiss()` (existente) |

---

## Eventos ViewModel (delta)

| Evento | Acção |
|--------|-------|
| `IncomeTaxExemptChanged(exempt)` | `state.incomeTaxExempt = exempt` |
| `AssetClassChanged` | `partialResetForAssetClass` + `assetClass` |
| `Save` | `onSave()` — **sem alteração de assinatura** |

---

## Mapeamento UI ↔ Domínio (RF)

```kotlin
// buildFixedIncomeAsset()
incomeTaxExempt = incomeTaxExempt,

// toUiState() from FixedIncomeAsset
incomeTaxExempt = currentAsset.incomeTaxExempt,
// legado: campo ausente na DB → false via migration/default
```

---

## Critérios de conformidade

1. Nenhum campo editável ATIVO/POSICIONAMENTO usa estado Compose local órfão.
2. Um único ponto de persistência (barra inferior).
3. Cards Transações e Resumo renderizam como antes (sem novos eventos de save).
4. Dialog fecha ≤ 1s após sucesso (LaunchedEffect existente).
