# Requisitos Funcionais - Tela de consulta de ativos posicionados em uma determinada data (mês e ano)

## Objetivo

Essa tela deve apresentar a lista com todos os ativos cadastrados que possuam posicionamento (holding) em um determinado período (mês/ano).

Deverá apresentar um campo de entrada de dados (dropbox) com algumas opções de mês e ano para selecionar o período corrente da tela.
Ex.: 2025/01, 2025/02, 2025/03 assim por diante.

## Dados exibidos na tela

A tabela que será apresentada na tela possuirá os seguintes campos:

- Corretora
    - Nome da corretora onde o ativo está custodiado.
    - Fonte: `Asset.brokerage.name`

- Categoria
    - Tipo de investimento: Renda Fixa, Renda Variável, Fundo de Investimento.
    - Fonte: Calculada em runtime
        - Se Asset for do tipo FixedIncomeAsset: "Renda Fixa"
        - Se Asset for do tipo VariableIncomeAsset: "Renda Variável"
        - Se Asset for do tipo InvestmentFundAsset: "Fundo de Investimento"

- Descrição
    - Nome ou descrição principal do ativo.
    - Fonte: `Asset.name`

- Vencimento
    - Data de vencimento do ativo
    - Fonte:
        - Se Asset for do tipo FixedIncomeAsset ou InvestmentFundAsse: `Asset.expirationDate`
        - Senao, apresentar `-` para indicar que o ativo não possui vencimento.

- Liquidez
    - Indica a liquidez do ativo (`Liquidity`)
    - Fonte: `Asset.liquidity`

- Quantidade
    - Indica quantos ativos uma posição possui no mês da consulta (conforme seleção do dropbox de mês e ano)
    - Fonte: `HoldingHistoryEntry.endOfMonthQuantity`

- Custo Médio
    - Indica o custo médio do ativo na posição do mês da consulta (conforme seleção do dropbox de mês e ano)
    - Fonte: `HoldingHistoryEntry.endOfMonthAverageCost`

- Custo Total
    - Indica o valor total do ativo na posição do mês da consulta (conforme seleção do dropbox de mês e ano).
    - Fonte: Calculado em runtime
        - valor total = `HoldingHistoryEntry.endOfMonthQuantity` * `HoldingHistoryEntry.endOfMonthAverageCost`

## Exemplo

| Corretora | Categoria      | Descrição                      | Vencimento | Liquidez | Quantidade (anterior) | Custo Médio (anterior) | Custo Total (anterior) | Quantidade | Custo Médio   | Custo Total   | Valorização |
|-----------|----------------|--------------------------------|------------|----------|-----------------------|------------------------|------------------------|------------|---------------|---------------|-------------|
| NuBank    | Renda Fixa     | CDB de 100% do CDI             | 2028-01-01 | Diária   | 1                     | R$ 1000,00             | R$ 1000,00             | 1          | R$ 1000,00    | R$ 1000,00    | 1%          |
| BTG       | Renda Variável | ETF IVVB11                     | -          | D + 2    | 100                   | R$ 250,00              | R$ 2700,00             | 100        | R$ 250,00     | R$ 2700,00    | 0%          |
| Inter     | Renda Variável | FII RBB11                      | -          | D + 2    | 5                     | R$ 4,11                | R$ 20,55               | 5          | R$ 4,11       | R$ 20,55      | 0%          |
| Inter     | Renda Variável | Ação B3SA3                     | -          | D + 2    | 50                    | R$ 27,11               | R$ 135,55              | 50         | R$ 27,11      | R$ 135,55     | 0%          |
| Inter     | Fundo          | Fundo de Previdência Arca Grão | 2050-01-01 | No Venc  | 1                     | R$ 100.000,00          | R$ 100.000,00          | 1          | R$ 100.000,00 | R$ 100.000,00 | 0%          |

## Casos de Uso

### Nenhuma posição (holding) registrada no banco de dados

Dado que não registrei nenhum posicionamento sobre os investimentos
Quando abro a tela
Então não devo apresentar nenhuma posição

### Posição registrada para um periodo anterior

Dado que registrei uma posição para um periodo anterior mas não para o periodo atual
Quando abro a tela
Então devo apresentar a posição para o periodo anterior

### Posição registrada para o periodo selecionado

Dado que registrei uma posição para o periodo selecionado mas não para um periodo anterior
Quando abro a tela
Então devo apresentar a posição atual mas não para o periodo anterior

### Posição registrada para o periodo selecionado e para o periodo anterior

Dado que registrei uma posição para o periodo atual e anterior
Quando abro a tela
Então devo apresentar a posição atual e anterior, além dos dados de valorização do investimento no mês

