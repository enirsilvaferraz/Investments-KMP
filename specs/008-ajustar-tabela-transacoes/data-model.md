# Modelo de dados — Ajuste de corretora e tabela de transações por tipo de asset

## Entidades de apresentação

### 1. AssetFormState

- **Descrição**: Estado principal do formulário de gestão de ativo.
- **Campos relevantes**:
  - `category` (tipo de asset)
  - `brokerage` (campo obrigatório na primeira coluna)
  - `isSaving`
- **Regra**: Corretora é editada somente no formulário principal.

### 2. TransactionTableRowState

- **Descrição**: Representa uma linha da tabela de transações (nova ou existente).
- **Campos relevantes**:
  - `rowId`
  - `isNew`
  - `isDirty`
  - `isValid`
  - `validationErrors`
  - `fieldValuesByType`
- **Regra**: Edição inline válida para todas as linhas.

### 3. TransactionDraftSession

- **Descrição**: Estado em memória de linhas da tabela durante a sessão de edição.
- **Campos relevantes**:
  - `rows`
  - `hasInvalidRows`
  - `pendingFocusRowId` (linha inválida destacada ao tentar adicionar novamente)
- **Regra**: Enquanto existir linha inválida, nova linha em branco não é criada.

### 4. TransactionColumnsSchema

- **Descrição**: Esquema de colunas exibidas conforme o tipo de asset.
- **Campos relevantes**:
  - `assetType`
  - `visibleColumns`
  - `requiredColumns`
- **Regra**: Troca de tipo descarta valores incompatíveis e dispara aviso de revisão.

### 5. SaveValidationResult

- **Descrição**: Resultado da validação final antes de persistir.
- **Campos relevantes**:
  - `canSave`
  - `invalidRowIds`
  - `messages`
- **Regra**: `canSave` só é verdadeiro quando não há nenhuma linha inválida (nova ou existente).

## Relações

- `AssetFormState` 1:1 `TransactionDraftSession`.
- `TransactionDraftSession` 1:N `TransactionTableRowState`.
- `AssetFormState.category` define `TransactionColumnsSchema`.
- `TransactionDraftSession` gera `SaveValidationResult` no salvar final.

## Transições de estado

1. **Abrir diálogo**: carregar linhas existentes e mapear para `TransactionTableRowState`.
2. **Adicionar linha**: criar `isNew = true` se não houver inválida pendente.
3. **Editar célula**: atualizar `fieldValuesByType`, recalcular `isValid` e `validationErrors`.
4. **Trocar tipo de asset**: aplicar novo `TransactionColumnsSchema`, remover campos incompatíveis e avisar revisão.
5. **Salvar**:
   - Se `hasInvalidRows = true`, bloquear salvar e destacar linhas inválidas.
   - Se todas válidas, persistir lote completo.
6. **Falha de persistência**: manter sessão em memória para nova tentativa.

## Regras de validação

- Toda linha inválida (nova ou existente) bloqueia o salvar final.
- Campos obrigatórios variam por `assetType`.
- Não é permitido criar nova linha enquanto existir inválida pendente.
- Inputs de tabela devem reutilizar componentes compartilhados (ex.: `TableInputMoney`).
