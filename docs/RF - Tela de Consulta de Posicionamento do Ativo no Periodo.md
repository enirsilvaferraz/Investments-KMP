# Requisitos Funcionais - Tela de Consulta de Posição do Ativo por Período

## 1. Objetivo

Apresentar ao usuário uma visão consolidada de suas posições de ativos (`holdings`) em um determinado período (mês/ano), permitindo comparar com o
período imediatamente anterior e analisar a valorização de cada ativo.

A tela deverá ter um seletor (ex: Dropdown) para que o usuário escolha o mês e o ano de referência (o "período atual").

## 2. Dados Exibidos na Tela

A tabela principal exibirá os seguintes dados para cada posição encontrada no **período atual** selecionado:

- **Corretora**:
    - **Descrição**: Nome da corretora onde o ativo está custodiado.
    - **Fonte**: `HoldingHistoryEntry.holding.brokerage.name`

- **Categoria**:
    - **Descrição**: Tipo de investimento do ativo.
    - **Fonte**: Calculado em tempo de execução, baseado no tipo do `Asset`:
        - `FixedIncomeAsset` -> "Renda Fixa"
        - `VariableIncomeAsset` -> "Renda Variável"
        - `InvestmentFundAsset` -> "Fundo de Investimento"

- **Descrição**:
    - **Descrição**: Nome principal do ativo.
    - **Fonte**: `HoldingHistoryEntry.holding.asset.name`

- **Vencimento**:
    - **Descrição**: Data de vencimento do ativo, se aplicável.
    - **Fonte**:
        - Se o `Asset` for `FixedIncomeAsset` ou `InvestmentFundAsset`: `asset.expirationDate`
        - Caso contrário, ou se a data não existir, apresentar "—".

- **Liquidez**:
    - **Descrição**: Regra de liquidez do ativo.
    - **Fonte**: `HoldingHistoryEntry.holding.asset.liquidity`. Deve ser formatado para o usuário:
        - `FixedLiquidity.Daily` -> "Diária"
        - `FixedLiquidity.AtMaturity` -> "No Vencimento"
        - `OnDaysAfterSale(days)` -> "D+{days}" (ex: "D+2")

---

### 2.1. Dados do Período Atual

- **Quantidade**:
    - **Descrição**: Quantidade de unidades do ativo no final do mês de referência.
    - **Fonte**: `HoldingHistoryEntryAtual.endOfMonthQuantity`

- **Custo Médio**:
    - **Descrição**: Custo médio por unidade do ativo no final do mês de referência.
    - **Fonte**: `HoldingHistoryEntryAtual.endOfMonthAverageCost`

- **Custo Total**:
    - **Descrição**: Valor total investido na posição no final do mês de referência.
    - **Fonte**: Calculado em tempo de execução:
        - `Custo Total = HoldingHistoryEntryAtual.endOfMonthQuantity * HoldingHistoryEntryAtual.endOfMonthAverageCost`

- **Valor de Mercado**:
    - **Descrição**: Valor de mercado total da posição no final do mês.
    - **Fonte**: `HoldingHistoryEntryAtual.endOfMonthValue`

---

### 2.2. Dados do Período Anterior

Para fins de comparação, a tela também deve exibir os dados do mês imediatamente anterior ao selecionado.

- **Quantidade (Anterior)**:
    - **Descrição**: Quantidade de unidades do ativo no final do mês anterior.
    - **Fonte**: `HoldingHistoryEntryAnterior.endOfMonthQuantity`

- **Custo Médio (Anterior)**:
    - **Descrição**: Custo médio por unidade do ativo no final do mês anterior.
    - **Fonte**: `HoldingHistoryEntryAnterior.endOfMonthAverageCost`

- **Custo Total (Anterior)**:
    - **Descrição**: Valor total investido na posição no final do mês anterior.
    - **Fonte**: Calculado em tempo de execução:
        - `Custo Total (Anterior) = HoldingHistoryEntryAnterior.endOfMonthQuantity * HoldingHistoryEntryAnterior.endOfMonthAverageCost`

- **Valor de Mercado (Anterior)**:
    - **Descrição**: Valor de mercado total da posição no final do mês anterior.
    - **Fonte**: `HoldingHistoryEntryAnterior.endOfMonthValue`

---

### 2.3. Indicador de Performance

- **Valorização do Mês**:
    - **Descrição**: Variação percentual do valor de mercado da posição entre o mês anterior e o mês atual.
    - **Fonte**: Veja a seção **"3. Cálculo de Valorização"**.

## 3. Cálculo de Valorização

A valorização representa o crescimento (ou decrescimento) do **valor de mercado** da posição no período.

- **Fórmula**:
    - `Valorização = (Valor de Mercado Atual / Valor de Mercado Anterior) - 1`
    - O resultado deve ser exibido como uma porcentagem (ex: 1,5%).

- **Regras**:
    - O cálculo só deve ser feito se existir um `HoldingHistoryEntry` para o **mês anterior**.
    - Se não houver posição no mês anterior (`Valor de Mercado Anterior` é zero ou nulo), a valorização deve ser exibida como "—" ou 0%.

## 4. Exemplo de Tabela

| Corretora | Categoria      | Descrição                      | Vencimento | Liquidez | Qtde. Ant. | Custo Médio Ant. | Custo Total Ant. | Qtde. Atual | Custo Médio Atual | Custo Total Atual | Valorização | Situação   |
|-----------|----------------|--------------------------------|------------|----------|------------|------------------|------------------|-------------|-------------------|-------------------|-------------|------------|
| NuBank    | Renda Fixa     | CDB de 100% do CDI             | 2028-01-01 | Diária   | 1          | R$ 1.000,00      | R$ 1.000,00      | 1           | R$ 1.000,00       | R$ 1.000,00       | 1,0%        | Manutenção |
| BTG       | Renda Variável | ETF IVVB11                     | —          | D+2      | —          | —                | —                | 100         | R$ 250,00         | R$ 25.000,00      | —           | Compra     |
| Inter     | Fundo          | Fundo de Previdência Arca Grão | 2050-01-01 | No Venc. | 1          | R$ 100.000,00    | R$ 100.000,00    | 1           | R$ 100.000,00     | R$ 100.000,00     | 0,8%        | 0,8%       |
| Rico      | Renda Variável | Ação B3SA3                     | —          | D+2      | 50         | R$ 27,11         | R$ 1.355,50      | 0           | —                 | —                 | —           | Venda      |

## 5. Casos de Uso

- **Nenhuma posição registrada:**
    - **Dado que** não há nenhum `HoldingHistoryEntry` no banco de dados para nenhum período.
    - **Quando** o usuário abrir a tela.
    - **Então** a tela deve exibir uma mensagem indicando "Nenhuma posição encontrada".

- **Posição apenas no período anterior:**
    - **Dado que** existe um `HoldingHistoryEntry` para o mês anterior, mas não para o mês atual selecionado (ex: ativo foi vendido).
    - **Quando** o usuário selecionar o período atual.
    - **Então** a linha do ativo deve ser exibida, mostrando os dados do período anterior, "0" ou "—" nos campos do período atual.

- **Posição apenas no período atual:**
    - **Dado que** existe um `HoldingHistoryEntry` para o mês atual, mas não para o anterior (ex: primeira compra do ativo).
    - **Quando** o usuário selecionar o período atual.
    - **Então** a linha do ativo deve ser exibida, com "—" nos campos do período anterior e na valorização.

- **Posição em ambos os períodos:**
    - **Dado que** existem `HoldingHistoryEntry` para o mês atual e o anterior.
    - **Quando** o usuário selecionar o período atual.
    - **Então** a linha do ativo deve ser exibida com todos os dados preenchidos, incluindo a valorização calculada.
