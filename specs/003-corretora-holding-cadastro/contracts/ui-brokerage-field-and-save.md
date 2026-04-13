# Contrato de UI: Campo de corretora e gravação com posição

**Feature**: `003-corretora-holding-cadastro`  
**Módulo**: `:features:asset-management`  
**Pacote**: `com.eferraz.asset_management`  
**Estende**: [../001-cadastro-investimento-dialog/contracts/ui-investment-registration-dialog.md](../001-cadastro-investimento-dialog/contracts/ui-investment-registration-dialog.md)

## 1. Superfície pública

**Sem alteração obrigatória** da assinatura pública de `AssetManagementScreen` — o campo de corretora é **interno** ao estado `Form`, a menos que `composeApp` precise de novos parâmetros (não previstos nesta spec).

## 2. Extensão MVI

### 2.1 Estado `Form` (`AssetManagementViewModel`)

| Campo adicional | Tipo | Significado |
|-----------------|------|-------------|
| `brokerages` | `List<Brokerage>` | Catálogo carregado (ex.: no `init` ou `LoadIssuers` alargado a “catálogos”). |
| `draft.brokerageId` | `Long?` | Selecção actual. |

### 2.2 Intents

| Intent | Efeito |
|--------|--------|
| `LoadBrokerages` (ou fusão com carga inicial) | Dispara `GetBrokeragesUseCase`; actualiza `brokerages`; em lista vazia, mensagem global coerente com **RF-005**. |
| `DraftChanged` | Já existe; passa a incluir `brokerageId` via `AssetDraft`. |

### 2.3 Validação antes de `UpsertInvestmentAssetUseCase`

- Se `brokerages.isEmpty()` → comportamento análogo a emissores vazios (mensagem, sem sucesso de gravação).
- Se `draft.brokerageId == null` → erro de campo `brokerage` (obrigatório).

### 2.4 Mapeamento para o caso de uso

`buildUpsertParam(AssetDraft)` passa a exigir `brokerageId` não nulo e inclui `brokerageId` em cada `UpsertInvestmentAssetUseCase.Param.*`.

## 3. Composable / layout

- Dropdown de corretora na grelha do formulário (`baseForm` ou secção comum), **mesmo padrão** visual que o dropdown de categoria / emissor (**RF-001**).
- Rótulo e mensagens de erro alinhados a `FieldLabels` / helpers existentes (novo identificador de campo `brokerage` se necessário).

## 4. Evidência

- Testes **`jvmTest`** em `:domain:usecases` para `UpsertInvestmentAssetUseCase`: corretora inválida, owner inexistente, sucesso com criação transaccional (com *fakes* / MockK conforme `test-patterns.mdc`).
- Pré-visualizações `@Preview`: **no mesmo ficheiro** que o composable alterado (constitution VI).
