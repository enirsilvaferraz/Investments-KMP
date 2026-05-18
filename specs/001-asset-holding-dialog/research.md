# Research: Dialog Unificado de Cadastro de Ativo + Holding

**Feature**: 001-asset-holding-dialog | **Date**: 2026-05-17

## 1. Comunicação entre ViewModels (AssetManagementViewModel → DialogViewModel)

### Decisão: Callback via `onComplete` + `LaunchedEffect` no Composable

**Raciocínio**: O projeto já adota este padrão de forma consistente em todas as `*FormView`:
- `AssetFormView` recebe `onComplete: (assetId: Long) -> Unit` e usa `LaunchedEffect(state.isCompleted)` para disparar o callback.
- `HoldingFormView` recebe `onComplete: () -> Unit` com o mesmo padrão.
- `TransactionFormView` recebe `onComplete: () -> Unit` com o mesmo padrão.

O `DialogViewModel` não precisa ser referenciado diretamente pelo `AssetManagementViewModel`. A orquestração acontece na camada Composable: quando `AssetManagementViewModel.state.isCompleted == true`, o `LaunchedEffect` no Composable dispara o callback `onDismiss` que é controlado pelo `DialogViewModel` (ou diretamente pela navegação).

**Alternativas consideradas**:
- **SharedFlow entre VMs**: Não usado em nenhum lugar do projeto. Introduziria acoplamento entre ViewModels e uma dependência de escopo de Koin não existente. Rejeitado por KISS e por desvio do padrão existente.
- **Channel (event bus)**: Mesmo problema de acoplamento. Mais complexo sem benefício.

## 2. DialogViewModel — Padrão de Gerenciamento de Ciclo de Vida do Dialog

### Decisão: DialogViewModel como controlador de estado do dialog, separado da lógica de persistência

**Raciocínio**: O `DialogViewModel` será responsável apenas por:
- Manter o estado do dialog (aberto/fechado, `holdingId` se edição).
- Expor `StateFlow<DialogState>` com `field = MutableStateFlow(...)` (padrão do projeto).
- Receber eventos `Dismiss` para fechar.

O entry em `App.kt` (`:features:composeApp`) já usa `backStack.removeLastOrNull()` para fechar o dialog — isto funciona perfeitamente com Navigation 3. O `DialogViewModel` pode ser simplificado: na prática, o controle de abrir/fechar é feito pela navegação (`NavBackStack`). O `DialogViewModel` serve como coordenador que agrupa o estado do fluxo multi-step (asset → holding → futuro: transação).

**Alternativas consideradas**:
- **Sem DialogViewModel (apenas backStack)**: Funcional para cenário simples, mas a spec exige separação explícita de responsabilidades (FR-004, US-003) e extensibilidade para transações. Rejeitado por violar a spec.
- **ViewModel com navegação interna (sub-steps)**: Excesso de engenharia para o escopo atual. YAGNI.

## 3. Incorporação do Campo de Corretora (Holding) no Formulário de Asset

### Decisão: Adicionar campo de corretora (Brokerage dropdown) diretamente no `AssetFormView`

**Raciocínio**: O `HoldingFormView` atual é um único `StableExposedDropdown` para seleção de corretora. É mais simples e alinhado ao DRY incorporar este campo diretamente no `AssetFormView`, em vez de compor dois formulários separados.

Alterações necessárias:
- `AssetManagementUiState`: adicionar campos `brokerage: Brokerage?`, `brokerages: List<Brokerage>`, `brokerageError: String?`.
- `AssetManagementEvents`: adicionar `BrokerageChanged(brokerage: Brokerage)`.
- `AssetManagementViewModel`: injetar `GetBrokeragesUseCase` e `UpsertAssetHoldingUseCase`. Carregar lista de corretoras no `loadInitialState`. Após salvar o asset com sucesso, salvar o holding sequencialmente.
- `AssetFormView`: adicionar `StableExposedDropdown` para corretora, reutilizando `BROKERAGE_FIELD_LABEL`.

**Alternativas consideradas**:
- **Compor HoldingFormView dentro de AssetFormView**: Mantém dois ViewModels para o formulário — viola princípio S (o formulário seria gerido por dois VMs) e complica a persistência sequencial. Rejeitado.
- **Novo Composable "CombinedFormView"**: Camada de indireção sem valor. O `AssetFormView` já é o formulário correto para estender.

## 4. Persistência Sequencial (Asset → Holding)

### Decisão: No `AssetManagementViewModel.onSave()`, salvar asset primeiro, depois holding

**Raciocínio**: Conforme FR-003 — sequencial sem rollback. O fluxo será:
1. `upsertAssetUseCase(asset)` → sucesso → obtém `newAsset` (com `id`).
2. Construir `AssetHolding` com `newAsset`, `brokerage` selecionada, `owner` padrão.
3. `upsertAssetHoldingUseCase(holding)` → sucesso → `isCompleted = true`.
4. Se o holding falhar, `asset` permanece salvo. Exibir erro. `isCompleted = false`.

**Owner**: Conforme a entidade `AssetHolding`, um `Owner` é obrigatório. Precisa ser resolvido — provavelmente há um owner padrão ou o owner é obtido via contexto. Pesquisa no uso existente do `HoldingManagementViewModel` mostra que o `holding.owner` vem da entidade `AssetHolding` carregada. Para novos holdings, o owner precisará ser definido (owner padrão do sistema, ou primeiro owner).

**Alternativas consideradas**:
- **Transação atômica (Room)**: Violaria Clean Architecture — a camada de apresentação não deve conhecer transações de banco. Além disso, FR-003 especifica persistência sequencial sem rollback.
- **Único UseCase combinado**: YAGNI — reutilizar os UseCases existentes é mais simples e alinhado ao princípio O (aberto/fechado).

## 5. Edição de Holding Existente (US-002)

### Decisão: `AssetManagementRouting(holdingId)` carrega dados do AssetHolding no dialog

**Raciocínio**: Quando `holdingId != null`:
1. `getAssetHoldingUseCase(ById(holdingId))` retorna o `AssetHolding` com `asset` e `brokerage`.
2. Pré-popular `AssetManagementUiState` com dados do asset (via `toUiState()` existente) + campos de corretora.
3. Categoria do ativo fica desabilitada (já implementado: `enabled = ui.asset == null` no dropdown de Categoria).
4. Salvar atualiza o holding existente (o `upsertAssetHoldingUseCase` já lida com upsert por design).

**Alternativas consideradas**:
- **Rota separada para edição**: Viola FR-009 (mesmo dialog para criação e edição). Rejeitado.

## 6. Remoção de `HoldingManagementView.kt` e Arquivos Relacionados

### Decisão: Remover `holdings/` subpacote inteiro após migração

**Raciocínio**: Conforme FR-007 e verificação de que nenhum outro ponto do app importa diretamente estes arquivos (confirmado pela exploração). Arquivos a remover:
- `HoldingManagementView.kt`
- `HoldingManagementViewModel.kt`
- `HoldingManagementUiState.kt`
- `HoldingManagementEvents.kt`

O `HoldingManagementViewModel` tinha dependências (`GetAssetHoldingUseCase`, `GetBrokeragesUseCase`, `UpsertAssetHoldingUseCase`) que serão absorvidas pelo `AssetManagementViewModel`.

## 7. Composable de Entrypoint (`AssetManagementScreen`)

### Decisão: Criar `AssetManagementScreen` como Composable público — entrypoint do dialog

**Raciocínio**: O `App.kt` já referencia `AssetManagementScreen(holdingId, onDismiss)` no entry comentado. Este Composable será o shell do dialog em tela cheia, contendo:
- `Scaffold` com `TopAppBar` (título + botão X de fechar).
- `AssetFormView` como conteúdo.

O `DialogViewModel` será instanciado neste nível (via `koinViewModel`) para gerenciar o estado do dialog. O `onDismiss` virá da navegação (`backStack.removeLastOrNull()`).

## 8. Estrutura do Dialog em Tela Cheia

### Decisão: `Scaffold` + `TopAppBar` com botão X (Close)

**Raciocínio**: O `TransactionFormView` já usa este padrão em seu preview. O `DialogSceneStrategy` em `App.kt` já configura `usePlatformDefaultWidth = false` para dialog em tela cheia. O `AssetManagementScreen` seguirá:
- `TopAppBar` com título "Novo investimento" (criação) ou "Editar investimento" (edição).
- Botão `IconButton` com `Icons.Default.Close` para fechar.
- Conteúdo: `AssetFormView` (que já existe e inclui botão Salvar).
