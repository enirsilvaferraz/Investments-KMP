# UI Contracts: Dialog Unificado de Cadastro de Ativo + Holding

**Feature**: 001-asset-holding-dialog | **Date**: 2026-05-17

## 1. AssetManagementScreen (Entrypoint Público)

Composable público do módulo `:features:asset-management`, registrado como dialog no `NavDisplay` via `AssetManagementRouting`.

### Assinatura

```kotlin
@Composable
public fun AssetManagementScreen(
    holdingId: Long?,
    onDismiss: () -> Unit,
)
```

### Comportamento

| Parâmetro | Descrição |
|-----------|-----------|
| `holdingId` | `null` → modo criação. Valor → modo edição (pré-popula formulário). |
| `onDismiss` | Callback para fechar o dialog (conectado a `backStack.removeLastOrNull()`). |

### Estrutura Visual

```text
┌─────────────────────────────────────┐
│ [X]  Novo investimento              │ ← TopAppBar
├─────────────────────────────────────┤
│                                     │
│  Categoria    [dropdown]            │
│  Emissor      [dropdown]            │
│  ... campos por categoria ...       │
│  Corretora    [dropdown]            │ ← NOVO: campo incorporado
│  Observações  [text field]          │
│                                     │
│                      [Salvar]       │ ← Button
└─────────────────────────────────────┘
```

## 2. DialogViewModel

ViewModel interno que gerencia o ciclo de vida do dialog.

### Estado

```kotlin
internal data class DialogState(
    val isOpen: Boolean = true,
    val holdingId: Long? = null,
)
```

### Eventos

```kotlin
internal sealed class DialogEvents {
    data object Dismiss : DialogEvents()
}
```

### Contrato

| Evento | Resultado |
|--------|-----------|
| `Dismiss` | `state.isOpen = false` → dialog fecha |

## 3. AssetManagementViewModel (Modificado)

### Novas Dependências Injetadas

```kotlin
@KoinViewModel
internal class AssetManagementViewModel(
    private val getIssuersUseCase: GetIssuersUseCase,
    private val getAssetUseCase: GetAssetUseCase,
    private val upsertAssetUseCase: UpsertAssetUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,          // NOVO
    private val getAssetHoldingUseCase: GetAssetHoldingUseCase,      // NOVO
    private val upsertAssetHoldingUseCase: UpsertAssetHoldingUseCase, // NOVO
) : ViewModel()
```

### Novos Eventos

| Evento | Payload | Ação |
|--------|---------|------|
| `ScreenEntered` | `holdingId: Long?` (alterado de `assetId`) | Carrega issuers, brokerages, e holding (se edição). |
| `BrokerageChanged` | `brokerage: Brokerage` | Atualiza `state.brokerage`, limpa `brokerageError`. |

### Fluxo `onSave()` (modificado)

```text
1. Validar campos → se erro, retornar
2. state.isSaving = true
3. upsertAssetUseCase(asset) → 
   onSuccess: newAsset obtido
     → upsertAssetHoldingUseCase(holding com newAsset) →
       onSuccess: state.isCompleted = true
       onFailure: state com erro de holding, isSaving = false
   onFailure: state com erro de asset, isSaving = false
```

## 4. AssetManagementRouting (Existente — sem alteração)

```kotlin
@Serializable
public data class AssetManagementRouting(
    val holdingId: Long? = null,
) : NavKey
```

## 5. Integração com App.kt (Descomentar)

```kotlin
entry<AssetManagementRouting>(metadata = DialogSceneStrategy.dialog(
    DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false
    )
)) {
    AssetManagementScreen(
        holdingId = it.holdingId,
        onDismiss = { backStack.removeLastOrNull() },
    )
}
```

## 6. Arquivos Removidos

| Arquivo | Motivo |
|---------|--------|
| `HoldingManagementView.kt` | FR-007: campo incorporado em AssetFormView |
| `HoldingManagementViewModel.kt` | Funcionalidade absorvida por AssetManagementViewModel |
| `HoldingManagementUiState.kt` | Estado consolidado em AssetManagementUiState |
| `HoldingManagementEvents.kt` | Eventos consolidados em AssetManagementEvents |
