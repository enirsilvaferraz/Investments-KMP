# Modelagem de Domínio - Ativos de Investimento

Este documento descreve a modelagem do domínio para as entidades de ativos de investimento, com base nos requisitos funcionais. A representação utiliza a sintaxe da linguagem Kotlin para ilustrar as regras e estruturas de dados.

## Arquitetura do Domínio

A modelagem é dividida em três camadas conceituais para garantir clareza, flexibilidade e escalabilidade:

1.  **`Asset` (O Ativo):** Representa as características **intrínsecas** de um ativo negociável (ex: a ação PETR4, um CDB específico). Descreve "o quê" é o ativo.
2.  **`AssetHolding` (A Posição):** Representa a **posse** de um `Asset` por um `Owner` em uma `Brokerage`. Descreve "quem" possui, "onde", "quanto" e os dados financeiros **atuais** da posição. É a entidade central da carteira.
3.  **`HoldingHistoryEntry` (O Histórico):** Representa um **snapshot mensal** do desempenho de uma `AssetHolding`, permitindo a análise da evolução da posição ao longo do tempo.

---

## Camada 1: Asset (O Ativo)

Esta camada define as propriedades imutáveis que caracterizam um ativo, independentemente de quem o possui.

```kotlin
import java.time.LocalDate

/**
 * Contrato para as características intrínsecas de um ativo de investimento.
 *
 * @property id O identificador único do ativo.
 * @property name O nome ou descrição principal do ativo.
 * @property issuer A entidade que emitiu o ativo.
 */
sealed interface Asset {
    val id: Long
    val name: String
    val issuer: Issuer
}
```

### Subclasses de Asset

```kotlin
/**
 * Representa um ativo de renda fixa. As suas propriedades definem o "contrato" do título.
 *
 * @property type O tipo de cálculo de rendimento (pós-fixado, pré-fixado, etc.).
 * @property subType O instrumento de renda fixa (CDB, LCI, etc.).
 * @property expirationDate Data de vencimento do título.
 * @property contractedYield Rentabilidade contratada no momento da aplicação.
 * @property cdiRelativeYield Rentabilidade relativa ao CDI (opcional).
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 */
data class FixedIncomeAsset(
    override val id: Long,
    override val name: String,
    override val issuer: Issuer,
    val type: FixedIncomeAssetType,
    val subType: FixedIncomeSubType,
    val expirationDate: LocalDate,
    val contractedYield: Double,
    val cdiRelativeYield: Double?,
    val liquidity: FixedLiquidity
) : Asset

/**
 * Representa um ativo de renda variável.
 *
 * @property type O tipo de ativo de renda variável (ação, FII, etc.).
 * @property ticker O código de negociação único do ativo (ex: "PETR4").
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 */
data class VariableIncomeAsset(
    override val id: Long,
    override val name: String,
    override val issuer: Issuer,
    val type: VariableIncomeAssetType,
    val ticker: String,
    val liquidity: OnDaysAfterSale
) : Asset

/**
 * Representa um fundo de investimento.
 *
 * @property type A categoria do fundo de investimento (ações, multimercado, etc.).
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 */
data class InvestmentFundAsset(
    override val id: Long,
    override val name: String,
    override val issuer: Issuer,
    val type: InvestmentFundAssetType,
    val liquidity: OnDaysAfterSale
) : Asset
```

---

## Camada 2: AssetHolding (A Posição)

Esta entidade central conecta um `Asset` a um `Owner` e a uma `Brokerage`, representando uma posição real e única na carteira com seus dados financeiros atuais.

```kotlin
import java.math.BigDecimal

/**
 * Representa a posse de um ativo por um proprietário em uma corretora.
 * A modelagem utiliza um sistema de "unidades" para ser universalmente compatível.
 *
 * @property id O identificador único desta posição.
 * @property asset A referência para o ativo intrínseco (o "quê").
 * @property owner O proprietário desta posição (o "quem").
 * @property brokerage A corretora onde esta posição está custodiada (o "onde").
 * @property firstPurchaseDate A data da primeira compra que originou esta posição.
 * @property quantity O número de unidades detidas (ações, cotas, ou 1 para um título de Renda Fixa).
 * @property averageCost O custo médio pago por cada unidade.
 * @property investedValue O valor total investido na posição (calculado como quantity * averageCost).
 * @property currentValue O valor de mercado atual da posição.
 */
data class AssetHolding(
    val id: Long,
    val asset: Asset,
    val owner: Owner,
    val brokerage: Brokerage,
    val firstPurchaseDate: LocalDate,
    val quantity: Double,
    val averageCost: BigDecimal,
    val investedValue: BigDecimal,
    val currentValue: BigDecimal
)
```

### Convenção de Unidades para `AssetHolding`

Para que a entidade `AssetHolding` seja universal, suas propriedades (`quantity` e `averageCost`) devem ser interpretadas de acordo com a categoria do `Asset` vinculado:

*   **Para Renda Fixa (`FixedIncomeAsset`):**
    *   A "unidade" é o próprio título ou contrato de investimento.
    *   `quantity`: Será sempre **`1.0`**, representando a posse de um único título.
    *   `averageCost`: Será o valor total do aporte inicial.
    *   `investedValue`: Será igual ao `averageCost`.

*   **Para Renda Variável (`VariableIncomeAsset`):**
    *   A "unidade" é a **ação** (ou FII, ETF, etc.).
    *   `quantity`: Representa o número total de ações possuídas.
    *   `averageCost`: É o preço médio de compra por ação, que deve ser recalculado a cada nova compra.

*   **Para Fundo de Investimento (`InvestmentFundAsset`):**
    *   A "unidade" é a **cota** do fundo.
    *   `quantity`: Representa o número total de cotas que o investidor possui.
    *   `averageCost`: É o custo médio de aquisição por cota.
    *   **Aportes:** Um novo aporte (contribuição) em um fundo existente é tratado como a **compra de novas cotas** pelo preço do dia. Isso resulta em um aumento da `quantity` total e um recálculo do `averageCost` da posição.

---

## Camada 3: HoldingHistoryEntry (O Histórico)

Esta entidade é um snapshot mensal de uma `AssetHolding`, armazenando dados de desempenho para permitir análises de evolução ao longo do tempo.

```kotlin
import java.math.BigDecimal
import java.time.YearMonth

/**
 * Representa um registro de histórico mensal para uma `AssetHolding`.
 *
 * @property id O identificador único do registro de histórico.
 * @property holdingId A referência ao ID da `AssetHolding` a que este registro pertence.
 * @property referenceDate O mês e ano de referência para este snapshot (ex: 2023-10).
 * @property endOfMonthValue O valor de mercado total da posição no final do mês.
 * @property earnings Os rendimentos (juros, dividendos) recebidos durante o mês.
 * @property quantity A quantidade do ativo detida no final do mês.
 * @property totalInvested O valor total investido na posição até o final do mês.
 */
data class HoldingHistoryEntry(
    val id: Long,
    val holdingId: Long,
    val referenceDate: YearMonth,
    val endOfMonthValue: BigDecimal,
    val earnings: MonthlyEarnings,
    val quantity: Double,
    val totalInvested: BigDecimal
)
```

---

## Entidades e Tipos de Suporte

```kotlin
/**
 * Representa o proprietário legal (pessoa física ou jurídica) de um ativo.
 * @property id O identificador único do proprietário.
 * @property name O nome do proprietário.
 */
data class Owner(val id: Long, val name: String)

/**
 * Representa a instituição financeira onde o ativo está custodiado.
 * @property id O identificador único da corretora.
 * @property name O nome da corretora.
 */
data class Brokerage(val id: Long, val name: String)

/**
 * Representa a entidade que emitiu o ativo.
 * @property id O identificador único do emissor.
 * @property name O nome do emissor.
 */
data class Issuer(val id: Long, val name: String)

/**
 * Agrupa os diferentes tipos de rendimentos recebidos em um mês.
 * @property interest O total de juros ou rendimentos recebidos no mês.
 * @property dividends O total de dividendos ou JCP recebidos.
 */
data class MonthlyEarnings(
    val interest: BigDecimal,
    val dividends: BigDecimal
)

// --- Liquidez e Enums ---

sealed interface Liquidity
sealed interface FixedLiquidity : Liquidity {
    object Daily : FixedLiquidity
    object AtMaturity : FixedLiquidity
}
data class OnDaysAfterSale(val days: Int) : Liquidity

enum class FixedIncomeAssetType { POST_FIXED, PRE_FIXED, INFLATION_LINKED }
enum class FixedIncomeSubType { CDB, LCI, LCA, CRA, CRI, DEBENTURE }
enum class VariableIncomeAssetType { NATIONAL_STOCK, INTERNATIONAL_STOCK, REAL_ESTATE_FUND, ETF }
enum class InvestmentFundAssetType { PENSION, STOCK_FUND, MULTIMARKET_FUND }
```