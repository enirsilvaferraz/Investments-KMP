# Data Model: Dialog Unificado de Cadastro de Ativo + Holding

**Feature**: 001-asset-holding-dialog | **Date**: 2026-05-17

## Entidades de Domínio (existentes — sem alteração)

### Asset (sealed interface)

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `id` | `Long` | Sim | Identificador único. `0` para novos. |
| `issuer` | `Issuer` | Sim | Entidade emissora do ativo. |
| `observations` | `String?` | Não | Notas adicionais. |
| `category` | `InvestmentCategory` | Sim | Enum: `FIXED_INCOME`, `VARIABLE_INCOME`, `INVESTMENT_FUND`. |

**Implementações**:
- `FixedIncomeAsset` — adiciona `type`, `subType`, `expirationDate`, `contractedYield`, `cdiRelativeYield`, `liquidity`.
- `VariableIncomeAsset` — adiciona `name`, `type`, `ticker`, `cnpj`.
- `InvestmentFundAsset` — adiciona `name`, `type`, `liquidity`, `liquidityDays`, `expirationDate`.

**Localização**: `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/assets/Asset.kt`

### AssetHolding

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `id` | `Long` | Sim | Identificador único da posição. |
| `asset` | `Asset` | Sim | Referência ao ativo intrínseco. |
| `owner` | `Owner` | Sim | Proprietário da posição. |
| `brokerage` | `Brokerage` | Sim | Corretora custodiante. |
| `goal` | `FinancialGoal?` | Não | Meta financeira vinculada. |

**Localização**: `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/holdings/AssetHolding.kt`

### Brokerage

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `id` | `Long` | Sim | Identificador único. |
| `name` | `String` | Sim | Nome da corretora. |

**Localização**: `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/holdings/Brokerage.kt`

### Owner

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `id` | `Long` | Sim | Identificador único. |
| `name` | `String` | Sim | Nome do proprietário. |

**Localização**: `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/holdings/Owner.kt`

### Issuer

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `id` | `Long` | Sim | Identificador único. |
| `name` | `String` | Sim | Nome do emissor. |
| `isInLiquidation` | `Boolean` | Sim | Se está em liquidação. |

**Localização**: `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/assets/Issuer.kt`

## Entidades de Apresentação (modificações)

### AssetManagementUiState (modificar)

Campos adicionados para incorporar o holding:

| Campo | Tipo | Default | Descrição |
|-------|------|---------|-----------|
| `brokerage` | `Brokerage?` | `null` | Corretora selecionada. |
| `brokerages` | `List<Brokerage>` | `emptyList()` | Lista de corretoras disponíveis. |
| `brokerageError` | `String?` | `null` | Mensagem de erro de validação. |
| `holdingId` | `Long?` | `null` | ID do holding (edição). |
| `owner` | `Owner?` | `null` | Proprietário (carregado com o holding ou padrão). |

**Localização**: `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementUiState.kt`

### AssetManagementEvents (modificar)

Eventos adicionados:

| Evento | Payload | Descrição |
|--------|---------|-----------|
| `BrokerageChanged` | `brokerage: Brokerage` | Usuário selecionou corretora. |

**Localização**: `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementEvents.kt`

### DialogState (novo)

| Campo | Tipo | Default | Descrição |
|-------|------|---------|-----------|
| `isOpen` | `Boolean` | `true` | Dialog aberto/fechado. |
| `holdingId` | `Long?` | `null` | ID do holding para edição. |

**Localização**: `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/dialog/DialogViewModel.kt` (inline no mesmo arquivo)

## Relacionamentos

```text
AssetManagementRouting(holdingId?)
        │
        ▼
 ┌──────────────────┐
 │  DialogViewModel  │◄── controla abrir/fechar
 │  state: DialogState│
 └────────┬─────────┘
          │ instancia
          ▼
 ┌────────────────────────────┐
 │  AssetManagementScreen     │◄── Scaffold + TopAppBar + Close
 │  (public Composable)       │
 └────────┬───────────────────┘
          │ contém
          ▼
 ┌────────────────────────────┐
 │  AssetFormView              │
 │  + campo Corretora inline   │
 │  AssetManagementViewModel   │◄── orquestra persistência
 │  state: AssetManagementUiState
 └────────────────────────────┘
          │ onSave()
          ▼
 ┌────────────────────────────┐
 │  1. UpsertAssetUseCase      │ → Asset salvo
 │  2. UpsertAssetHoldingUseCase│ → Holding salvo
 └────────────────────────────┘
          │ isCompleted = true
          ▼
 LaunchedEffect → onDismiss() → backStack.removeLastOrNull()
```

## Regras de Validação

| Campo | Regra | Origem |
|-------|-------|--------|
| Corretora | Obrigatória (não nula) | `UpsertAssetHoldingUseCase.catalogRefErrors()` |
| Issuer | Obrigatório (não nulo) | `UpsertAssetUseCase.catalogRefErrors()` |
| Campos por categoria | Específicos por `InvestmentCategory` | `UpsertAssetUseCase.validate*()` |

## Transições de Estado

### Fluxo de Criação

```text
[Idle] → FAB(+) → [Dialog Aberto: formulário vazio]
  → preencher campos → [Formulário preenchido]
  → Salvar → [Salvando: isSaving=true]
    → Asset salvo → [Salvando holding]
      → Holding salvo → [isCompleted=true] → Dialog fecha
      → Holding falhou → [Erro holding: isSaving=false, erro exibido]
    → Asset falhou → [Erro asset: isSaving=false, erro exibido]
  → Fechar (X) → [Dialog fechado: dados descartados]
```

### Fluxo de Edição

```text
[Histórico] → Editar holding → [Dialog Aberto: formulário pré-populado]
  → alterar campos (exceto categoria) → [Formulário alterado]
  → Salvar → [Salvando: isSaving=true]
    → Asset atualizado → Holding atualizado → [isCompleted=true] → Dialog fecha
  → Fechar (X) → [Dialog fechado: alterações descartadas]
```
