# Contrato de UI: Diálogo de cadastro de investimento

**Feature**: `001-cadastro-investimento-dialog`  
**Módulo**: `:features:asset-management`  
**Pacote**: `com.eferraz.asset_management`

## 1. Superfície pública entre módulos

### 1.1 Composable de entrada

| Símbolo | Assinatura (conceitual) | Responsabilidade |
|---------|-------------------------|------------------|
| `AssetManagementScreen` | `AssetManagementScreen(modifier: Modifier, onDismiss: () -> Unit, …)` | Ponto de entrada; aplica `modifier`; instancia `AssetManagementViewModel` via Koin; delega à implementação interna. |

Parâmetros adicionais (navegação, teclas de back) são definidos em conjunto com `composeApp` (`App.kt` / rotas).

## 2. Contrato MVI (interno ao módulo, testável)

### 2.1 `UiState` (sealed)

Estados globais sugeridos:

- `Loading` — carregamento do catálogo de emissores.
- `LoadError` — falha ao obter emissores; permite `Retry`.
- `Form` — formulário interactivo; inclui sub-estado de gravação (`Idle`/`Saving`/`SaveFailed`).

### 2.2 `Intent`

| Intent | Efeito esperado |
|--------|-----------------|
| `InitialLoad` | Dispara `GetIssuersUseCase`; em sucesso, estado `Form` com default **RF-014**. |
| `RetryLoadIssuers` | Idem após erro. |
| `SelectCategory` | Actualiza campos visíveis; limpa campos não aplicáveis; mantém emissor + observações. |
| `UpdateField` | Actualiza valor e erros de campo na UI. |
| `SelectIssuer` | Id de emissor ou `null`. |
| `Save` | Validação de UI; chama `UpsertInvestmentAssetUseCase`; em falha de rede/BD: **RF-015**; em sucesso: fechar ou feedback externo. |
| `RequestClose` | Se não alterado → `onDismiss`; se alterado → pedir confirmação; **RF-011**/**RF-013**. |
| `ConfirmDiscard` / `DismissDiscard` | Confirma ou cancela fecho; **RF-013**. |

### 2.3 Efeitos secundários

- **Nenhum** `Intent` deve criar emissor; apenas `GetIssuersUseCase` + selecção por id.

## 3. Ficheiros e responsabilidades

| Ficheiro | Conteúdo |
|----------|----------|
| `AssetManagementContract.kt` | `AssetManagementScreen` público. |
| `AssetManagementViewModel.kt` | `StateFlow`, `Intent`, `dispatch`, `viewModelScope`. |
| `AssetManagementScreen.kt` | `when (UiState)`, `Dialog`/`Surface`, ligação a `FormView`. |
| `AssetManagementFormUi.kt` | Modelos `@Immutable`, `toXxxUi()`, validação de formato. |
| `AssetManagementFormView.kt` | Dropdowns, campos condicionais, botões Salvar/Cancelar/X. |

## 4. Acessibilidade e UX (constitution VI)

- Mensagens de erro por campo; estado de **carregamento** em **Salvar** (**RF-016**).
- Botão Salvar desactivado quando lista de emissores vazia e a regra exigir emissor.

## 5. Evidência

- Testes de UI: opcional / manual na primeira versão; **obrigatório** em `usecases` para `UpsertInvestmentAssetUseCase` (princípio V).
