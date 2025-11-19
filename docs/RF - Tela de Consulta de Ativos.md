# Requisitos Funcionais - Tela de Consulta e Gestão de Ativos

## Índice

1. [Objetivo](#1-objetivo)
2. [Dados Exibidos na Tabela de Ativos](#2-dados-exibidos-na-tabela-de-ativos)
3. [Exemplos](#3-exemplos)
4. [Casos de Uso](#4-casos-de-uso)

---

## 1. Objetivo

Esta tela tem como objetivo principal fornecer ao usuário uma visão completa de todos os ativos (`Asset`) cadastrados no sistema, independentemente de
haver uma posição (`AssetHolding`) associada a eles. A tela serve como um catálogo central de ativos, permitindo ao usuário consultar os detalhes de cada ativo.

A principal funcionalidade da tela é uma tabela que exibe um resumo de cada ativo cadastrado.

---

## 2. Dados Exibidos na Tabela de Ativos

A tabela principal deverá exibir os seguintes dados para cada `Asset` cadastrado:

| Coluna           | Descrição                                     | Fonte                                                                                                                                                  |
|:-----------------|:----------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Categoria**    | O tipo principal do ativo.                    | Baseado na classe do `Asset`: `FixedIncomeAsset` ("Renda Fixa"), `VariableIncomeAsset` ("Renda Variável"), `InvestmentFundAsset` ("Fundos").           |
| **Subcategoria** | O tipo específico do ativo.                   | `asset.subType.name` para `FixedIncomeAsset`, `asset.type.formated()` para os demais.                                                                  |
| **Descrição**    | O nome principal do ativo.                    | Calculado dinamicamente conforme o tipo de ativo (ver detalhes abaixo).                                                                                |
| **Vencimento**   | A data de vencimento do ativo.                | `asset.expirationDate` para `FixedIncomeAsset` (obrigatório) e `InvestmentFundAsset` (opcional). Para `VariableIncomeAsset`, este campo não se aplica. |
| **Emissor**      | A entidade que emitiu o ativo.                | `asset.issuer.name`                                                                                                                                    |
| **Liquidez**     | A regra de liquidez que se aplica ao ativo.   | `asset.liquidity` formatado conforme o tipo (ver detalhes abaixo).                                                                                     |
| **Observação**   | Notas e observações adicionais sobre o ativo. | `asset.observations` (opcional).                                                                                                                       |

### 2.1. Formatação do Campo Descrição por Tipo de Ativo

#### Renda Fixa (`FixedIncomeAsset`)

O nome é formatado dinamicamente com base no tipo de rendimento:

- **Pós-fixado** (`POST_FIXED`):
  ```
  "{subType} de {contractedYield}% do CDI"
  ```
  Exemplo: `"CDB de 110% do CDI"`

- **Pré-fixado** (`PRE_FIXED`):
  ```
  "{subType} de {contractedYield}% a.a."
  ```
  Exemplo: `"LCI de 12.5% a.a."`

- **IPCA** (`INFLATION_LINKED`):
  ```
  "{subType} de IPCA + {contractedYield}%"
  ```
  Exemplo: `"LCA de IPCA + 6.5%"`

#### Renda Variável (`VariableIncomeAsset`)

- O nome é exibido diretamente do campo `asset.name` e deve representar o Ticker do ativo.
- Exemplo: `"B3SA3"`

#### Fundo de Investimento (`InvestmentFundAsset`)

- O nome é exibido diretamente do campo `asset.name`
- Exemplo: `"Verde AM"`

### 2.2. Formatação do Campo Liquidez por Tipo

A liquidez é formatada conforme o tipo de regra aplicada ao ativo:

- **Diária** (`Liquidity.DAILY`): Exibido como `"Diária"` - resgate pode ser solicitado a qualquer momento
- **No vencimento** (`Liquidity.AT_MATURITY`): Exibido como `"No vencimento"` - liquidez apenas na data de vencimento do título
- **Dias após venda** (`Liquidity.D_PLUS_DAYS`): Exibido como `"D+{days}"` onde `{days}` é o número de dias para o resgate ser efetivado (armazenado na propriedade `liquidityDays`)
  - Exemplo: `"D+2"` (resgate em 2 dias após solicitação)
  - Exemplo: `"D+60"` (resgate em 60 dias após solicitação)

**Nota**: `Liquidity.DAILY` e `Liquidity.AT_MATURITY` são aplicáveis apenas a ativos de Renda Fixa (`FixedIncomeAsset`). `Liquidity.D_PLUS_DAYS` é aplicável a ativos de Renda Variável (`VariableIncomeAsset`) e Fundos de Investimento (`InvestmentFundAsset`), que possuem a propriedade `liquidityDays` para armazenar o número de dias.

---

## 3. Exemplos

### 3.1. Exemplo de Tabela de Consulta

| Categoria      | Subcategoria | Nome/Descrição     | Vencimento | Emissor             | Liquidez      | Observação                   |
|:---------------|:-------------|:-------------------|:-----------|:--------------------|:--------------|:-----------------------------|
| Renda Fixa     | CDB          | CDB de 110% do CDI | 2028-01-01 | Banco Master        | Diária        | Aplicação inicial            |
| Renda Fixa     | LCI          | LCI de 12.5% a.a.  | 2029-06-15 | Banco Inter         | No vencimento |                              |
| Renda Fixa     | LCA          | LCA de IPCA + 6.5% | 2030-03-20 | Banco do Brasil     | Diária        | Isento de IR                 |
| Renda Variável | Ação         | MGLU3              | -          | Magazine Luiza S.A. | D+2           |                              |
| Renda Variável | ETF          | BOVA11             | -          | BlackRock           | D+2           | ETF de índice                |
| Renda Variável | FII          | HGLG11             | -          | HGLG                | D+2           | FII de galpões logísticos    |
| Fundos         | Multimercado | Verde AM           | -          | Verde Asset         | D+30          | Fundo multimercado           |
| Fundos         | Previdência  | XP Previdência     | 2045-12-31 | XP Investimentos    | D+60          | Plano de previdência privada |

---

## 4. Casos de Uso

### UC-01: Consultar Lista de Ativos

**Ator**: Usuário do sistema

**Pré-condições**:

- Sistema inicializado
- Acesso ao repositório de ativos

**Fluxo Principal**:

1. Usuário acessa a Tela de Consulta de Ativos
2. Sistema recupera lista de ativos do repositório
3. Sistema formata os dados de cada ativo
4. Sistema exibe tabela com todos os ativos cadastrados
5. Usuário visualiza os dados na tabela

**Pós-condições**:

- Tela exibindo lista de ativos