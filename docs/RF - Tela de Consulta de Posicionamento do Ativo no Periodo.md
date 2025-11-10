# Requisitos Funcionais - Tela de Consulta de Posição do Ativo por Período

## 1. Objetivo

Apresentar ao usuário uma visão consolidada de suas posições de ativos (`holdings`) em um determinado período (mês/ano), permitindo comparar com o
período imediatamente anterior, analisar a valorização e registrar movimentações.

## 2. Dados Exibidos na Tabela de Posições

A tabela principal deverá exibir os seguintes dados para cada posição (`HoldingHistoryEntry`) encontrada no período de referência:

| Coluna                  | Descrição                                                       | Fonte                                                       |
|:------------------------|:----------------------------------------------------------------|:------------------------------------------------------------|
| **Corretora**           | Nome da corretora onde o ativo está custodiado.                 | `HoldingHistoryEntry.holding.brokerage.name`                |
| **Categoria**           | Tipo de investimento do ativo.                                  | Calculado em tempo de execução, baseado no tipo do `Asset`. |
| **Descrição**           | Nome principal do ativo.                                        | `HoldingHistoryEntry.holding.asset.name`                    |
| **Vencimento**          | Data de vencimento do ativo, se aplicável.                      | `asset.expirationDate`                                      |
| **Liquidez**            | Regra de liquidez do ativo.                                     | `asset.liquidity`                                           |
| **Valor Mercado Ant.**  | Valor de mercado total da posição no final do mês **anterior**. | `HoldingHistoryEntryAnterior.endOfMonthValue`               |
| **Valor Mercado Atual** | Valor de mercado total da posição no final do mês **atual**.    | `HoldingHistoryEntryAtual.endOfMonthValue`                  |
| **Valorização**         | Variação percentual do valor de mercado no mês.                 | Ver seção **4.1. Cálculo de Valorização**.                  |
| **Situação**            | Classificação da movimentação da posição no mês.                | Ver seção **4.2. Cálculo da Situação**.                     |

## 3. Ações da Tela

### 3.1. Selecionar Período de Referência

- **Componente**: Um seletor para que o usuário escolha o mês e o ano de referência.
- **Ação**: Ao selecionar um novo período, a tabela é atualizada para refletir os dados do período escolhido.

### 3.2. Editar Posição do Ativo

- **Componente**: A própria linha da tabela.
- **Ação**: Ao tocar na linha, o usuário será redirecionado para a **Tela de Edição de Posição do Ativo** (ver seção 7), com os campos pré-preenchidos
  e comportamento adaptado ao tipo de ativo.

### 3.3. Registrar Posição Inicial

- **Componente**: Um Botão de Ação Flutuante (FAB) com um ícone de "+".
- **Ação**: Ao ser clicado, abre a **Tela de Edição de Posição do Ativo** em modo "criação", para registrar um ativo que ainda não consta na carteira.

## 4. Regras de Negócio e Cálculos

### 4.1. Cálculo de Valorização

- **Fórmula**: `Valorização = (Valor de Mercado Atual / Valor de Mercado Anterior) - 1`
- **Regras**:
    - O cálculo só deve ser feito se houver posição no mês anterior com valor de mercado maior que zero.
    - Se o `Valor de Mercado Atual` for zero (Venda Total), a valorização deve ser exibida como "—".

### 4.2. Cálculo da Situação

A situação é calculada comparando as quantidades do ativo entre o período atual (`Qtde. Atual`) e o anterior (`Qtde. Ant.`). A existência de um
registro (`HoldingHistoryEntry`) para o mês atual é o principal fator.

- Se existe posição no mês anterior mas não há registro para o mês atual: **"Não Registrado"**
- Se não há registro no mês anterior e um novo registro é criado no mês atual: **"Compra"**
- `Qtde. Ant. > 0` e `Qtde. Atual == 0`: **"Venda Total"**
- `Qtde. Ant. > 0` e `Qtde. Atual < Qtde. Ant.`: **"Venda Parcial"**
- `Qtde. Ant. > 0` e `Qtde. Atual > Qtde. Ant.`: **"Aporte"**
- `Qtde. Ant. > 0` e `Qtde. Atual == Qtde. Ant.`: **"Manutenção"**

## 5. Exemplo de Tabela de Consulta

| Corretora | Categoria      | Descrição          | Vencimento | Liquidez | Valor Mercado Ant. | Valor Mercado Atual | Valorização | Situação       |
|-----------|----------------|--------------------|------------|----------|--------------------|---------------------|-------------|----------------|
| NuBank    | Renda Fixa     | CDB de 100% do CDI | 2028-01-01 | Diária   | R$ 1.000,00        | R$ 1.010,00         | 1,0%        | Manutenção     |
| BTG       | Renda Variável | ETF IVVB11         | —          | D+2      | —                  | R$ 26.000,00        | —           | Compra         |
| XP        | Renda Variável | Ação AAPL          | —          | D+2      | R$ 3.200,00        | R$ 3.456,00         | 8,0%        | Aporte         |
| Clear     | Renda Variável | Ação MGLU3         | —          | D+2      | R$ 5.500,00        | R$ 2.200,00         | -60,0%      | Venda Parcial  |
| Rico      | Renda Variável | Ação B3SA3         | —          | D+2      | R$ 1.400,00        | R$ 0,00             | —           | Venda Total    |
| Rico      | Renda Variável | Ação B3SA3         | —          | D+2      | R$ 1.400,00        | —                   | —           | Não Registrado |

## 6. Casos de Uso

A lógica dos casos de uso para "Compra", "Venda", "Aporte", etc., continua válida e será o resultado das operações realizadas na tela de edição.

## 7. Tela de Edição de Posição do Ativo

Esta tela é o centro das operações de atualização, compra e venda para uma posição existente. O seu layout se adapta à categoria do ativo selecionado.

### 7.1. Campos do Formulário

A estrutura da tela de edição varia conforme a categoria do ativo.

| Campo                      | Descrição                                | Comportamento/Regras                                                                                                                            |
|:---------------------------|:-----------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------|
| **Ativo**                  | Nome do ativo sendo editado.             | Exibido no cabeçalho, não editável.                                                                                                             |
| **Corretora**              | Nome da corretora da posição.            | Exibido no cabeçalho, não editável.                                                                                                             |
| **Período**                | Mês/Ano de referência da edição.         | Informativo, não editável.                                                                                                                      |
| **Valor de Mercado Atual** | Valor total da posição.                  | **Renda Fixa/Fundos:** Campo editável.<br>**Renda Variável:** Campo informativo, não editável, calculado (`quantidade total * valor unitário`). |
| **Quantidade (Mov.)**      | Quantidade de cotas a comprar ou vender. | **Apenas Renda Variável:** Visível na seção "Movimentação".                                                                                     |
| **Valor Unitário (Mov.)**  | Cotação atual do ativo para a operação.  | **Apenas Renda Variável:** Informativo, obtido via API.                                                                                         |

### 7.2. Fluxo para Renda Fixa e Fundos de Investimento

- **Objetivo**: Permitir a atualização do valor de mercado ou a liquidação total do ativo.
- **Ações**:
    - **Botão "Salvar"**: Atualiza o `HoldingHistoryEntry` do período com o novo **Valor de Mercado Atual**. A quantidade não é alterada, resultando
      na situação "Manutenção".
    - **Botão "Vender Ativo"**: Zera a quantidade e o valor de mercado da posição no período, resultando na situação "Venda Total".

### 7.3. Fluxo para Renda Variável

- **Objetivo**: Permitir o registro de compras e vendas de cotas/ações.
- **Seção "Movimentação"**:
    - Esta seção contém os campos `Quantidade` e `Valor Unitário Atual` para registrar uma nova transação.
- **Ações da Seção "Movimentação"**:
    - **Botão "Comprar"**: Adiciona a `Quantidade` informada à posição, atualiza o custo médio e o valor de mercado. Resulta em "Compra" ou "Aporte".
    - **Botão "Vender"**: Subtrai a `Quantidade` informada da posição, atualiza o valor de mercado e, opcionalmente, calcula o resultado da operação.
      Resulta em "Venda Parcial" ou "Venda Total".
