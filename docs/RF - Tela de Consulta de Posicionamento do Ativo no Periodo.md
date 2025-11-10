# Requisitos Funcionais - Tela de Consulta de Posição do Ativo por Período

## 1. Objetivo

Apresentar ao usuário uma visão consolidada de suas posições de ativos (`holdings`) em um determinado período (mês/ano), permitindo comparar com o período imediatamente anterior e analisar a valorização e a movimentação de cada ativo. A tela servirá como um painel de controle para a performance mensal da carteira.

A principal funcionalidade da tela será uma tabela que exibirá um resumo de cada posição, com um seletor para definir o período de análise.

## 2. Dados Exibidos na Tabela de Posições

A tabela principal deverá exibir os seguintes dados para cada posição (`HoldingHistoryEntry`) encontrada no período de referência:

| Coluna | Descrição | Fonte |
|:---|:---|:---|
| **Corretora** | Nome da corretora onde o ativo está custodiado. | `HoldingHistoryEntry.holding.brokerage.name` |
| **Categoria** | Tipo de investimento do ativo. | Calculado em tempo de execução, baseado no tipo do `Asset`: `FixedIncomeAsset` -> "Renda Fixa", `VariableIncomeAsset` -> "Renda Variável", `InvestmentFundAsset` -> "Fundo de Investimento". |
| **Descrição** | Nome principal do ativo. | `HoldingHistoryEntry.holding.asset.name` |
| **Vencimento** | Data de vencimento do ativo, se aplicável. Apresentar "—" caso não se aplique. | `asset.expirationDate` (para `FixedIncomeAsset` ou `InvestmentFundAsset`). |
| **Liquidez** | Regra de liquidez do ativo. | `asset.liquidity`, formatado para exibição (ex: "Diária", "No Vencimento", "D+2"). |
| **Valor Mercado Ant.** | Valor de mercado total da posição no final do mês **anterior**. | `HoldingHistoryEntryAnterior.endOfMonthValue` |
| **Valor Mercado Atual** | Valor de mercado total da posição no final do mês **atual**. | `HoldingHistoryEntryAtual.endOfMonthValue` |
| **Valorização** | Variação percentual do valor de mercado no mês. | Ver seção **4.1. Cálculo de Valorização**. |
| **Situação** | Classificação da movimentação da posição no mês. | Ver seção **4.2. Cálculo da Situação**. |

## 3. Ações da Tela

### 3.1. Selecionar Período de Referência

- **Componente**: Um seletor (Dropdown ou similar) para que o usuário escolha o mês e o ano de referência (o "período atual").
- **Ação**: Ao selecionar um novo período, a tabela de posições é atualizada para refletir os dados do período escolhido e do seu anterior.

### 3.2. Detalhar Posição do Ativo

- **Componente**: A própria linha da tabela.
- **Ação**: Ao tocar (clicar) na linha, o usuário será redirecionado para a **Tela de Detalhes do Ativo**, permitindo uma análise completa da performance do ativo ao longo de todo o seu ciclo de vida.

## 4. Regras de Negócio e Cálculos

### 4.1. Cálculo de Valorização

A valorização representa o crescimento (ou decrescimento) do **valor de mercado** da posição no período.

- **Fórmula**:
  - `Valorização = (Valor de Mercado Atual / Valor de Mercado Anterior) - 1`
  - O resultado deve ser exibido como uma porcentagem (ex: 1,5%).

- **Regras**:
  - O cálculo só deve ser feito se existir um `HoldingHistoryEntry` para o **mês anterior** com valor de mercado maior que zero.
  - Se não houver posição no mês anterior (`Valor de Mercado Anterior` é zero ou nulo), a valorização deve ser exibida como "—".
  - Se a posição foi totalmente vendida (`Valor de Mercado Atual` é zero), a valorização deve ser exibida como "—".

### 4.2. Cálculo da Situação

A situação classifica a movimentação da posição no mês, comparando as quantidades do ativo entre o período atual e o anterior.

- Se `Qtde. Ant. > 0` e `Qtde. Atual == 0`: "Venda Total"
- Se `Qtde. Ant. > 0` e `Qtde. Atual < Qtde. Ant.`: "Venda Parcial"
- Se `Qtde. Ant. == 0` (ou nulo) e `Qtde. Atual > 0`: "Compra"
- Se `Qtde. Ant. > 0` e `Qtde. Atual > Qtde. Ant.`: "Aporte"
- Se `Qtde. Ant. > 0` e `Qtde. Atual == Qtde. Ant.`: "Manutenção"
- Se não houver histórico anterior, a situação é "Compra".

## 5. Exemplo de Tabela de Consulta

| Corretora | Categoria      | Descrição                      | Vencimento | Liquidez | Valor Mercado Ant. | Valor Mercado Atual | Valorização | Situação      |
|-----------|----------------|--------------------------------|------------|----------|--------------------|---------------------|-------------|---------------|
| NuBank    | Renda Fixa     | CDB de 100% do CDI             | 2028-01-01 | Diária   | R$ 1.000,00        | R$ 1.010,00         | 1,0%        | Manutenção    |
| BTG       | Renda Variável | ETF IVVB11                     | —          | D+2      | —                  | R$ 26.000,00        | —           | Compra        |
| XP        | Renda Variável | Ação AAPL                      | —          | D+2      | R$ 3.200,00        | R$ 3.456,00         | 8,0%        | Aporte        |
| Inter     | Fundo          | Fundo de Previdência Arca Grão | 2050-01-01 | No Venc. | R$ 100.000,00      | R$ 100.800,00       | 0,8%        | Manutenção    |
| Clear     | Renda Variável | Ação MGLU3                     | —          | D+2      | R$ 5.500,00        | R$ 2.200,00         | -60,0%      | Venda Parcial |
| Rico      | Renda Variável | Ação B3SA3                     | —          | D+2      | R$ 1.400,00        | —                   | —           | Venda Total   |

## 6. Casos de Uso e Situações

- **Nenhuma Posição Registrada:**
  - **Dado que** não há nenhum `HoldingHistoryEntry` no banco de dados para nenhum período.
  - **Quando** o usuário abrir a tela.
  - **Então** a tela deve exibir uma mensagem indicando "Nenhuma posição encontrada".

- **Situação: Compra**
  - **Dado que** existe um `HoldingHistoryEntry` para o mês atual, mas não para o anterior.
  - **Quando** o usuário selecionar o período atual.
  - **Então** a linha do ativo deve ser exibida com a situação "Compra", e os campos do período anterior e a valorização devem exibir "—".

- **Situação: Venda Total**
  - **Dado que** existe um `HoldingHistoryEntry` para o mês anterior, mas não para o mês atual.
  - **Quando** o usuário selecionar o período atual.
  - **Então** a linha do ativo deve ser exibida com a situação "Venda Total", mostrando os dados do período anterior e "0" ou "—" nos campos do período atual, valorização.

- **Situação: Manutenção**
  - **Dado que** existem `HoldingHistoryEntry` para o mês atual e o anterior, e a quantidade não mudou (`Qtde. Atual == Qtde. Ant.`).
  - **Quando** o usuário selecionar o período atual.
  - **Então** a linha do ativo deve ser exibida com a situação "Manutenção" e todos os dados preenchidos.

- **Situação: Aporte**
  - **Dado que** existem `HoldingHistoryEntry` para o mês atual e o anterior, e a quantidade aumentou (`Qtde. Atual > Qtde. Ant.`).
  - **Quando** o usuário selecionar o período atual.
  - **Então** a linha do ativo deve ser exibida com a situação "Aporte" e todos os dados preenchidos.

- **Situação: Venda Parcial**
  - **Dado que** existem `HoldingHistoryEntry` para o mês atual e o anterior, e a quantidade diminuiu, mas não zerou (`0 < Qtde. Atual < Qtde. Ant.`).
  - **Quando** o usuário selecionar o período atual.
  - **Então** a linha do ativo deve ser exibida com a situação "Venda Parcial" e todos os dados preenchidos.
