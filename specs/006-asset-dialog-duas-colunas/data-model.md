# Modelo de dados — Dialog de asset em duas colunas

## Entidades de apresentação

### 1. AssetHoldingForm

- **Descrição**: Representa os campos de cadastro/edição exibidos na coluna esquerda.
- **Campos relevantes**:
  - `assetName`
  - `issuer`
  - `quantity`
  - `price`
  - `liquidity`
  - `...demais campos atuais da coluna de formulário`
- **Regra**: Não inclui `brokerage` na coluna esquerda.

### 2. BrokerageSelection

- **Descrição**: Campo de corretora exibido no topo da coluna direita.
- **Campos relevantes**:
  - `brokerageId`
  - `brokerageLabel`
- **Regra**: Deve aparecer antes da tabela de transações.

### 3. HoldingTransaction

- **Descrição**: Registro de transação vinculado à asset/holding.
- **Campos relevantes**:
  - `transactionId`
  - `date`
  - `type`
  - `quantity`
  - `unitPrice`
  - `totalValue`
  - `note` (opcional)
- **Regra**: Lista completa da holding exibida na coluna direita.

### 4. TransactionTableState

- **Descrição**: Estado de exibição do histórico na coluna direita.
- **Estados**:
  - `hasData`: renderiza tabela com rolagem interna.
  - `empty`: renderiza mensagem "Histórico disponível após salvar a holding".
- **Regra**: Deve preservar altura estável do dialog em ambos os estados.

## Relações

- `AssetHoldingForm` 1:1 `BrokerageSelection` (vínculo da holding editada).
- `AssetHoldingForm` 1:N `HoldingTransaction`.
- `TransactionTableState` depende da cardinalidade de `HoldingTransaction`.

## Regras de validação de exibição

- O layout sempre renderiza duas colunas e separador visual suave.
- A coluna esquerda mantém os campos existentes, exceto corretora.
- A coluna direita sempre contém corretora no topo e histórico abaixo.
- Para listas extensas, somente a área da tabela rola.
