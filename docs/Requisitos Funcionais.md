# RF - Tela de Listagem de Ativos (Asset)

## Definições

Este documento reflete a modelagem de dados definida para as entidades de Ativos. A tela de listagem deve apresentar as informações conforme a
estrutura abaixo.

A tela deve conter um título chamado "Lista de Ativos".

Deve conter uma tabela com as seguintes colunas, baseadas no modelo de dados:

- **Id**: `Long`
    - Identificador único do ativo.
    - *Fonte: `Asset.id`*

- **Nome do Ativo**: `String`
    - Nome ou descrição principal do ativo.
    - *Fonte: `Asset.name`*

- **Proprietário**: `String`
    - Nome do proprietário do ativo.
    - *Fonte: `Asset.owner.name`*

- **Corretora**: `String`
    - Nome da corretora onde o ativo está custodiado.
    - *Fonte: `Asset.brokerage.name`*

- **Emissor**: `String`
    - Nome da instituição que emitiu o ativo (banco, empresa, gestora).
    - *Fonte: `Asset.issuer.name`*

- **Categoria**: `String`
    - A categoria principal do ativo, determinada pelo seu tipo no modelo.
    - Opções: "Renda Fixa", "Renda Variável", "Fundo de Investimento".
    - *Fonte: `FixedIncomeAsset`, `VariableIncomeAsset`, `InvestmentFundAsset`*

- **Tipo**: `String`
    - A especificação do tipo de ativo dentro da sua categoria.
    - *Fonte: `Asset.type`*
    - **Para Renda Fixa**:
        - `POST_FIXED`: Pós-Fixado
        - `PRE_FIXED`: Pré-Fixado
        - `INFLATION_LINKED`: Atrelado à Inflação
    - **Para Renda Variável**:
        - `NATIONAL_STOCK`: Ação Nacional
        - `INTERNATIONAL_STOCK`: Ação Internacional
        - `REAL_ESTATE_FUND`: Fundo de Investimento Imobiliário (FII)
        - `ETF`: Exchange Traded Fund
    - **Para Fundos de Investimento**:
        - `PENSION`: Previdência
        - `STOCK_FUND`: Fundo de Ações
        - `MULTIMARKET_FUND`: Fundo Multimercado

- **Subtipo (Renda Fixa)**: `String`
    - O subtipo específico para ativos de Renda Fixa. Apresentar apenas se a categoria for "Renda Fixa".
    - *Fonte: `FixedIncomeAsset.subType`*
    - Opções: `CDB`, `LCI`, `LCA`, `CRA`, `CRI`, `DEBENTURE`.

- **Ticker (Renda Variável)**: `String`
    - O código de negociação do ativo em bolsa. Apresentar apenas se a categoria for "Renda Variável".
    - *Fonte: `VariableIncomeAsset.ticker`*

- **Data de Compra**: `LocalDate`
    - Data em que o ativo foi adquirido.
    - *Fonte: `Asset.purchaseDate`*

- **Data de Vencimento Efetiva**: `LocalDate`
    - Apresenta a data de vencimento (para títulos) ou de liquidação efetiva (para outros ativos), calculada conforme sua categoria e regra de
      liquidez.
    - *Fonte: Valor derivado, obtido a partir da lógica de negócio (ex: `Asset.getEffectiveExpirationDate(today)`).*
    - **Para Renda Fixa**:
        - Exibe a data de vencimento contratada do título.
        - *Origem do dado: `FixedIncomeAsset.expirationDate`*.
    - **Para Renda Variável e Fundos de Investimento**:
        - A data é calculada com base na sua regra de liquidez, que para estes tipos é sempre `OnDaysAfterSale(N)`. O cálculo resulta na data atual +
          N dias úteis.

- **Rentabilidade Contratada (Renda Fixa)**: `Double`
    - A taxa de rentabilidade acordada no momento da compra. Apresentar apenas se a categoria for "Renda Fixa".
    - *Fonte: `FixedIncomeAsset.contractedYield`*

- **Rentabilidade Relativa (Renda Fixa)**: `Double?`
    - A rentabilidade do ativo em relação ao CDI. Apresentar apenas se a categoria for "Renda Fixa" e se o valor estiver disponível.
    - *Fonte: `FixedIncomeAsset.cdiRelativeYield`*

- **Liquidez**: `String`
    - Regra de liquidez do ativo, rigidamente definida pela categoria do ativo.
    - *Fonte: `Asset.liquidity` (tipos específicos como `FixedLiquidity` e `OnDaysAfterSale`)*
    - **Para Renda Fixa** (Opções Exclusivas):
        - `Daily`: "Diária"
        - `AtMaturity`: "No Vencimento"
    - **Para Renda Variável e Fundos de Investimento** (Opção Exclusiva):
        - `OnDaysAfterSale(days)`: "D+{N}" (ex: D+2 para Renda Variável, D+30 para um Fundo).

---

### Observação sobre Valores Financeiros

Os campos relacionados a valores financeiros, como:

- Valor Investido
- Valor Atual
- Valorização (R$ e %)

Não fazem parte da entidade `Asset`. Este modelo representa as características intrínsecas do ativo. Os valores financeiros devem ser modelados em uma
entidade separada (ex: `AssetHolding` ou `Position`), que representaria a posse de um ativo em uma carteira, incluindo quantidade, preço médio, etc.