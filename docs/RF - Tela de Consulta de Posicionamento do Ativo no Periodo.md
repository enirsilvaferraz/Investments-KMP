# Requisitos Funcionais - Tela de Consulta de Posição do Ativo por Período

## 1. Objetivo

Apresentar ao usuário uma visão consolidada de suas posições de ativos (`holdings`) em um determinado período (mês/ano), permitindo comparar com o
período imediatamente anterior, analisar a valorização e registrar movimentações.

## 2. Dados Exibidos na Tabela de Posições

A tabela principal deverá exibir os seguintes dados para cada posição (`HoldingHistoryEntry`) encontrada no período de referência:

| Coluna                  | Descrição                                                                               | Fonte                                                       |
|:------------------------|:----------------------------------------------------------------------------------------|:------------------------------------------------------------|
| **Corretora**           | Nome da corretora onde o ativo está custodiado.                                         | `HoldingHistoryEntry.holding.brokerage.name`                |
| **Categoria**           | Tipo de investimento do ativo.                                                          | Calculado em tempo de execução, baseado no tipo do `Asset`. |
| **SubCategoria**        |                                                                                         |                                                             |
| **Descrição**           | Nome principal do ativo.                                                                | `HoldingHistoryEntry.holding.asset.name`                    |
| **Observaçoes**         |                                                                                         |                                                             |
| **Vencimento**          | Data de vencimento do ativo, se aplicável.                                              | `asset.expirationDate`                                      |
| **Emissor**             |                                                                                         |                                                             |
| **Liquidez**            | Regra de liquidez do ativo.                                                             | `asset.liquidity`                                           |
| **Valor Mercado Ant.**  | Valor de mercado total da posição no final do mês **anterior**.                         | `HoldingHistoryEntryAnterior.endOfMonthValue`               |
| **Valor Mercado Atual** | Valor de mercado total da posição no final do mês **atual**. Campo de entrada de dados. | `HoldingHistoryEntryAtual.endOfMonthValue`                  |
| **Valorização**         | Variação percentual do valor de mercado no mês.                                         | Ver seção **4.1. Cálculo de Valorização**.                  |
| **Situação**            | Classificação da movimentação da posição no mês.                                        | Ver seção **4.2. Cálculo da Situação**.                     |

## 3. Ações da Tela

### 3.1. Selecionar Período de Referência

- **Componente**: Um seletor para que o usuário escolha o mês e o ano de referência.
- **Ação**: Ao selecionar um novo período, a tabela é atualizada para refletir os dados do período escolhido.

### 3.2. Editar Posição do Ativo

- **Componente**: Campo de entrada de dados para o `Valor de Mercado Atual` em cada linha
- **Ação**: Ao inserir um valor no componente acima, o valor é salvo na posição do mês corrente daquele ativo.

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