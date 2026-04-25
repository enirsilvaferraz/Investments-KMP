# Modelo de dados — Cadastro e edição de transações no diálogo de ativo

## Entidades de apresentação

### 1. AssetManagementEditSession

- **Descrição**: Estado da sessão de edição do diálogo de ativo durante sua abertura.
- **Campos relevantes**:
  - `holdingId`
  - `baseTransactions` (lista carregada inicialmente)
  - `draftTransactions` (lista efetiva exibida e editável)
  - `hasPendingChanges`
- **Regra**: É descartada integralmente quando o utilizador cancela/fecha sem salvar.

### 2. DraftTransaction

- **Descrição**: Representa uma transação visível e editável na tabela durante a sessão.
- **Campos relevantes**:
  - `transactionId` (pode ser temporário para itens novos)
  - `date`
  - `type`
  - `quantity`
  - `unitPrice`
  - `fees`
  - `totalValue`
  - `notes`
  - `isNew`
  - `isDirty`
- **Regra**: Todos os campos exibidos na tabela são editáveis inline.

### 3. InlineCellEditState

- **Descrição**: Estado transitório de edição de uma célula da tabela.
- **Campos relevantes**:
  - `rowKey`
  - `fieldKey`
  - `rawInput`
  - `validationError` (opcional)
- **Regra**: Finalização inválida não altera `DraftTransaction`; preserva valor anterior e exibe erro.

### 4. SaveBatchPayload

- **Descrição**: Conjunto final de transações criadas/editadas enviado no salvar do formulário principal.
- **Campos relevantes**:
  - `holdingId`
  - `transactionsToPersist`
- **Regra**: Só é produzido no salvar final; não existe persistência parcial durante edição.

## Relações

- `AssetManagementEditSession` 1:N `DraftTransaction`.
- `DraftTransaction` 1:0..N `InlineCellEditState` (transitório por célula em edição).
- `AssetManagementEditSession` 1:1 `SaveBatchPayload` no momento de salvar.

## Transições de estado

1. **Carregar diálogo**: `baseTransactions` é preenchido e copiado para `draftTransactions`.
2. **Criar transação**: novo `DraftTransaction` com `isNew = true` é adicionado em memória.
3. **Editar célula válida**: atualiza `DraftTransaction`, marca `isDirty = true` e limpa erro da célula.
4. **Editar célula inválida**: mantém valor anterior, registra erro em `InlineCellEditState`.
5. **Salvar formulário**: monta `SaveBatchPayload` com alterações pendentes e persiste no banco.
6. **Cancelar/fechar sem salvar**: descarta `AssetManagementEditSession` e todas as mudanças.

## Regras de validação

- Campos obrigatórios para criação devem estar válidos antes de inserir linha na tabela.
- Edição inline inválida não pode ser confirmada no estado em memória.
- Lista exibida deve permanecer ordenada por `date` decrescente (mais recente primeiro).
- Exclusão de transação não é permitida nesta versão.
