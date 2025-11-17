# Requisitos Funcionais - Tela de Consulta e Gestão de Ativos

## 1. Objetivo

Esta tela tem como objetivo principal fornecer ao usuário uma visão completa de todos os ativos (`Asset`) cadastrados no sistema, independentemente de
haver uma posição (`AssetHolding`) associada a eles. A tela servirá como um catálogo central de ativos, permitindo ao usuário não apenas consultar os
detalhes, mas também navegar para as funcionalidades de **cadastro** de um novo ativo e **edição** de um ativo existente.

A principal funcionalidade da tela será uma tabela que exibirá um resumo de cada ativo, com um botão de ação para "adicionar" um novo ativo.

## 2. Dados Exibidos na Tabela de Ativos

A tabela principal deverá exibir os seguintes dados para cada `Asset` cadastrado:

| Coluna             | Descrição                                                                                | Fonte                                                                                                                                                                                          |
|:-------------------|:-----------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Categoria**      | O tipo principal do ativo.                                                               | Calculado em tempo de execução, baseado na classe do `Asset`: `FixedIncomeAsset` -> "Renda Fixa", `VariableIncomeAsset` -> "Renda Variável", `InvestmentFundAsset` -> "Fundo de Investimento". |
| **Subcategoria**   | O tipo específico do ativo.                                                              | `asset.subType` para `FixedIncomeAsset`, `asset.type` para os demais.                                                                                                                          |
| **Nome/Descrição** | O nome principal do ativo. Para ativos de renda variável, este campo já inclui o ticker. | `asset.name`                                                                                                                                                                                   |
| **Vencimento**     | A data de vencimento do ativo.                                                           | `asset.expirationDate` para `FixedIncomeAsset` (obrigatório) e `InvestmentFundAsset` (opcional). Para `VariableIncomeAsset`, este campo não se aplica e será exibido como "-".                 |
| **Emissor**        | A entidade que emitiu o ativo.                                                           | `asset.issuer.name`                                                                                                                                                                            |
| **Observações**    | Notas e observações adicionais sobre o ativo.                                            | `asset.observations` (opcional, `String?`). Se vazio, será exibido como vazio na tabela.                                                                                                       |

## 3. Ações da Tela

### 3.1. Adicionar Novo Ativo

- **Componente**: Um botão de Ação Flutuante (Floating Action Button - FAB) com um ícone de "+".
- **Ação**: Ao ser clicado, o usuário será redirecionado para a **Tela de Cadastro de Ativo**.

### 3.2. Editar Ativo Existente

- **Componente**: A própria linha da tabela.
- **Ação**: Ao tocar (clicar) na linha, o usuário será redirecionado para a **Tela de Edição de Ativo**, com os campos pré-preenchidos com os dados do
  ativo selecionado. O `id` do ativo será passado como parâmetro para a tela de edição.

## 4. Tela de Cadastro e Edição de Ativo

Esta tela será um formulário único utilizado tanto para criar um novo ativo quanto para modificar um existente. A interface deve se adaptar
dinamicamente com base no tipo de ativo selecionado.

### 4.1. Seleção da Categoria do Ativo

- **Componente**: Um seletor (Dropdown ou Radio Group) no topo do formulário.
- **Opções**: "Renda Fixa", "Renda Variável", "Fundo de Investimento".
- **Comportamento**: A seleção de uma categoria exibirá dinamicamente os campos específicos para aquele tipo de ativo.

### 4.2. Campos do Formulário

#### Campos Comuns (Visíveis para todas as categorias)

- **Nome/Descrição**: `name` (Obrigatório, `String`). Para Renda Variável, este campo deve incluir o ticker (ex: "Magazine Luiza (MGLU3)").
- **Emissor**: `issuer` (Obrigatório, seleção de um `Issuer` previamente cadastrado)
- **Observações**: `observations` (Opcional, `String`). Campo de texto livre para notas e observações adicionais sobre o ativo.

#### Campos Específicos por Categoria

##### Renda Fixa (`FixedIncomeAsset`)

- **Tipo de Renda Fixa**: `type` (Obrigatório, `Enum`: `POST_FIXED`, `PRE_FIXED`, `INFLATION_LINKED`)
- **Subtipo de Renda Fixa**: `subType` (Obrigatório, `Enum`: `CDB`, `LCI`, `LCA`, etc.)
- **Data de Vencimento**: `expirationDate` (Obrigatório, `LocalDate`)
- **Rentabilidade Contratada**: `contractedYield` (Obrigatório, `Double`)
- **Rentabilidade Relativa ao CDI**: `cdiRelativeYield` (Opcional, `Double`)
- **Liquidez**: `liquidity` (Obrigatório, `Enum`: `Daily`, `AtMaturity`)

##### Renda Variável (`VariableIncomeAsset`)

- **Tipo de Renda Variável**: `type` (Obrigatório, `Enum`: `NATIONAL_STOCK`, `INTERNATIONAL_STOCK`, `REAL_ESTATE_FUND`, `ETF`)

##### Fundo de Investimento (`InvestmentFundAsset`)

- **Tipo de Fundo**: `type` (Obrigatório, `Enum`: `PENSION`, `STOCK_FUND`, `MULTIMARKET_FUND`)
- **Liquidez**: `liquidity` (Obrigatório, `OnDaysAfterSale`) - Campo numérico para os dias.
- **Data de Vencimento**: `expirationDate` (Opcional, `LocalDate`)

### 4.3. Ações do Formulário

- **Botão "Salvar"**:
    - **Validação**: Verifica se todos os campos obrigatórios estão preenchidos.
    - **Ação**:
        - Se for um **cadastro**, cria uma nova instância do `Asset` correspondente e a salva no banco de dados.
        - Se for uma **edição**, atualiza os dados do `Asset` existente.
    - **Feedback**: Exibe uma mensagem de sucesso ("Ativo salvo com sucesso") ou de erro.
    - **Redirecionamento**: Após salvar, redireciona o usuário de volta para a **Tela de Consulta de Ativos**.

- **Botão "Cancelar"**:
    - **Ação**: Descarta todas as alterações e redireciona o usuário de volta para a **Tela de Consulta de Ativos**.

## 5. Exemplo de Tabela de Consulta

| Categoria      | Subcategoria  | Nome/Descrição            | Vencimento | Emissor             | Observações |
|:---------------|:--------------|:--------------------------|:-----------|:--------------------|:------------|
| Renda Fixa     | CDB           | CDB Banco Master 110% CDI | 2028-01-01 | Banco Master        |             |
| Renda Variável | Ação Nacional | Magazine Luiza (MGLU3)    | -          | Magazine Luiza S.A. |             |
| Fundo          | Multimercado  | Verde AM                  | -          | Verde Asset         |             |
