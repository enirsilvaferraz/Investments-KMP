# Modelagem de Domínio - Ativos de Investimento

Este documento descreve a modelagem do domínio para as entidades de ativos de investimento, com base nos requisitos funcionais. A representação utiliza a sintaxe da linguagem Kotlin para ilustrar as regras e estruturas de dados.

## Arquitetura do Domínio

A modelagem é dividida em três camadas conceituais para garantir clareza, flexibilidade e escalabilidade:

1.  **`Asset` (O Ativo):** Representa as características **intrínsecas** de um ativo negociável (ex: a ação PETR4, um CDB específico). Descreve "o quê" é o ativo.
2.  **`AssetHolding` (A Posição):** Representa a **posse** de um `Asset` por um `Owner` em uma `Brokerage`. Esta é uma entidade rica que encapsula não apenas os dados da posição, mas também as **regras de negócio** associadas a ela.
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
 * @property observations Notas e observações adicionais sobre o ativo (opcional).
 */
sealed interface Asset {
    val id: Long
    val name: String
    val issuer: Issuer
    val observations: String?
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
 * @property observations Notas e observações adicionais sobre o ativo (opcional).
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
    val liquidity: Liquidity,
    override val observations: String? = null
) : Asset

/**
 * Representa um ativo de renda variável.
 *
 * @property type O tipo de ativo de renda variável (ação, FII, etc.).
 * @property ticker O código de negociação único do ativo (ex: "PETR4").
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 * @property liquidityDays O número de dias para resgate quando liquidity é D_PLUS_DAYS.
 * @property observations Notas e observações adicionais sobre o ativo (opcional).
 */
data class VariableIncomeAsset(
    override val id: Long,
    override val name: String,
    override val issuer: Issuer,
    val type: VariableIncomeAssetType,
    val ticker: String,
    val liquidity: Liquidity,
    val liquidityDays: Int,
    override val observations: String? = null
) : Asset

/**
 * Representa um fundo de investimento.
 *
 * @property type A categoria do fundo de investimento (ações, multimercado, etc.).
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 * @property liquidityDays O número de dias para resgate quando liquidity é D_PLUS_DAYS.
 * @property expirationDate Data de vencimento do título (opcional para fundos).
 * @property observations Notas e observações adicionais sobre o ativo (opcional).
 */
data class InvestmentFundAsset(
    override val id: Long,
    override val name: String,
    override val issuer: Issuer,
    val type: InvestmentFundAssetType,
    val liquidity: Liquidity,
    val liquidityDays: Int,
    val expirationDate: LocalDate?,
    override val observations: String? = null
) : Asset
```

---

## Camada 2: AssetHolding (A Posição)

Esta entidade central conecta um `Asset` a um `Owner` e a uma `Brokerage`. Evoluiu de uma simples estrutura de dados para uma **entidade rica**, que encapsula as regras de negócio relativas a uma posição, como o recálculo do custo médio após um novo aporte.

```kotlin
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Representa a posse de um ativo por um proprietário em uma corretora.
 * A modelagem utiliza um sistema de "unidades" para ser universalmente compatível.
 *
 * @property id O identificador único desta posição.
 * @property asset A referência para o ativo intrínseco (o "quê").
 * @property owner O proprietário desta posição (o "quem").
 * @property brokerage A corretora onde esta posição está custodiada (o "onde").
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
    val quantity: Double,
    val averageCost: BigDecimal,
    val investedValue: BigDecimal,
    val currentValue: BigDecimal
) {
    /**
     * Retorna uma nova instância de `AssetHolding` refletindo um novo aporte (compra).
     * A lógica de recálculo do custo médio está encapsulada aqui, garantindo consistência.
     *
     * @param purchaseQuantity A quantidade de novas unidades compradas.
     * @param costPerUnit O custo por unidade na nova compra.
     * @return Uma nova instância de `AssetHolding` com os valores atualizados.
     */
    fun recordPurchase(purchaseQuantity: Double, costPerUnit: BigDecimal): AssetHolding {
        val newQuantity = this.quantity + purchaseQuantity
        val purchaseValue = costPerUnit.multiply(BigDecimal.valueOf(purchaseQuantity))
        val newInvestedValue = this.investedValue.add(purchaseValue)
        val newAverageCost = newInvestedValue.divide(BigDecimal.valueOf(newQuantity), 10, RoundingMode.HALF_UP)

        return this.copy(
            quantity = newQuantity,
            averageCost = newAverageCost,
            investedValue = newInvestedValue
        )
    }
}
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
 * @property id O identificador único do registro de histórico (chave primária).
 * @property holding A referência direta para a `AssetHolding` a que este registro pertence.
 * @property referenceDate O mês e ano de referência para este snapshot.
 * @property endOfMonthValue O valor de mercado total da posição no final do mês.
 * @property endOfMonthQuantity A quantidade do ativo detida no final do mês.
 * @property endOfMonthAverageCost O custo médio do ativo na posição no final do mês.
 * @property earnings A lista de todos os rendimentos (dividendos, juros, etc.) recebidos durante o mês.
 * @property totalInvested O valor total investido na posição até o final do mês.
 */
data class HoldingHistoryEntry(
    val id: Long,
    val holding: AssetHolding,
    val referenceDate: YearMonth,
    val endOfMonthValue: BigDecimal,
    val endOfMonthQuantity: Double,
    val endOfMonthAverageCost: BigDecimal,
    val earnings: List<Earning>,
    val totalInvested: BigDecimal
) {
    /**
     * Calcula e retorna a soma de todos os rendimentos recebidos no mês.
     */
    fun totalEarnings(): BigDecimal {
        return earnings.sumOf { it.value }
    }
}
```

### Nota sobre a Chave Primária em `HoldingHistoryEntry`

Embora a combinação de `(holding, referenceDate)` seja naturalmente única, a entidade utiliza um campo `id` simples como chave primária (uma **chave substituta** ou *surrogate key*). Esta é uma decisão de design pragmática que traz benefícios significativos:

*   **Simplicidade:** Facilita a interação com frameworks de banco de dados (ORMs), que são altamente otimizados para chaves de coluna única em operações de busca, atualização e exclusão.
*   **Relacionamentos:** Simplifica a criação de chaves estrangeiras se, no futuro, outra entidade precisar referenciar um registro de histórico específico.
*   **Flexibilidade:** Permite maior liberdade para futuras alterações no modelo sem quebrar a identidade fundamental do registro.

---

## Entidades Fundamentais

Esta seção detalha as entidades que, embora não façam parte das camadas principais da arquitetura, são conceitos de primeira classe no domínio. Elas representam atores ou instituições do mundo real, possuem identidade própria e ciclo de vida independente. Sua separação em entidades próprias é crucial para a reutilização e consistência dos dados, permitindo, por exemplo, que a mesma corretora (`Brokerage`) seja associada a múltiplas posições (`AssetHolding`).

### Owner (Proprietário)

Representa o "quem" do investimento. É a pessoa física ou jurídica detentora da posição. Esta entidade é fundamental para a organização da carteira e para a funcionalidade de visualização de ativos por proprietário.

```kotlin
/**
 * Representa o proprietário legal (pessoa física ou jurídica) de um ativo.
 * @property id O identificador único do proprietário.
 * @property name O nome do proprietário.
 */
data class Owner(val id: Long, val name: String)
```

### Brokerage (Corretora)

Representa o "onde" do investimento. É a instituição financeira que serve como intermediária e onde a posição está custodiada. A corretora é responsável por executar as ordens de compra e venda e manter o registro dos ativos.

```kotlin
/**
 * Representa a instituição financeira onde o ativo está custodiado.
 * @property id O identificador único da corretora.
 * @property name O nome da corretora.
 */
data class Brokerage(val id: Long, val name: String)
```

### Issuer (Emissor)

Representa o "criador" do ativo. É a entidade que emitiu o ativo financeiro. O emissor varia conforme a natureza do ativo: para um CDB, é um banco; para uma ação, é a própria companhia; para um fundo, é a gestora (asset management). Esta entidade é vital para a análise de risco e origem do ativo.

```kotlin
/**
 * Representa a entidade que emitiu o ativo.
 * @property id O identificador único do emissor.
 * @property name O nome do emissor.
 */
data class Issuer(val id: Long, val name: String)
```

---

## Tipos de Valor e Classificações

Esta seção agrupa os blocos de construção e classificações que descrevem ou restringem as propriedades das entidades principais. Diferente das entidades fundamentais, estes tipos não possuem identidade ou ciclo de vida próprios; eles são **Value Objects** ou enumerações que adicionam significado e segurança ao modelo.

### Rendimentos (Earning)

Define um contrato polimórfico para os diferentes tipos de proventos que uma posição pode gerar. Esta abordagem permite que o sistema seja estendido para acomodar novos tipos de rendimentos no futuro (ex: bonificação em ações) sem alterar as entidades existentes, seguindo o Princípio Aberto/Fechado.

```kotlin
/**
 * Contrato para qualquer tipo de rendimento (provento) recebido em um mês.
 * Esta abordagem polimórfica permite que o sistema seja facilmente estendido
 * para novos tipos de rendimentos no futuro.
 *
 * @property value O valor monetário do rendimento recebido.
 */
sealed interface Earning {
    val value: BigDecimal
}

/** Representa o recebimento de dividendos. */
data class Dividend(override val value: BigDecimal) : Earning

/** Representa o recebimento de Juros Sobre Capital Próprio (JCP). */
data class Jcp(override val value: BigDecimal) : Earning

/** Representa juros recebidos, tipicamente de ativos de Renda Fixa. */
data class Interest(override val value: BigDecimal) : Earning

/** Representa a amortização de um ativo, comum em FIIs e CRIs/CRAs. */
data class Amortization(override val value: BigDecimal) : Earning
```

### Liquidez (Liquidity)

Modela as diferentes regras de conversão do ativo em dinheiro. O uso de um `enum class` fornece um vocabulário controlado para o domínio, ideal para lógicas de negócio, filtros de UI e para garantir a integridade dos dados, evitando o uso de strings arbitrárias.

```kotlin
enum class Liquidity {
    /**
     * Representa a liquidez diária, onde o resgate pode ser solicitado a qualquer momento.
     */
    DAILY,
    
    /**
     * Representa a liquidez apenas no vencimento do título.
     */
    AT_MATURITY,
    
    /**
     * Representa a liquidez onde o resgate ocorre um número específico de dias após a solicitação.
     * O número de dias deve ser armazenado separadamente na entidade que utiliza este tipo.
     */
    D_PLUS_DAYS
}
```

**Nota sobre `D_PLUS_DAYS`:** Para liquidez do tipo `D_PLUS_DAYS`, o número de dias deve ser armazenado separadamente nas entidades que utilizam este tipo de liquidez. Por exemplo, `VariableIncomeAsset` e `InvestmentFundAsset` possuem uma propriedade `liquidityDays: Int` que armazena o número de dias para resgate.

### Classificações de Ativos (Enums)

Representam conjuntos fixos e pré-definidos de opções para classificar os ativos. O uso de `enum class` fornece um vocabulário controlado para o domínio, ideal para lógicas de negócio, filtros de UI e para garantir a integridade dos dados, evitando o uso de strings arbitrárias.

```kotlin
enum class FixedIncomeAssetType { POST_FIXED, PRE_FIXED, INFLATION_LINKED }
enum class FixedIncomeSubType { CDB, LCI, LCA, CRA, CRI, DEBENTURE }
enum class VariableIncomeAssetType { NATIONAL_STOCK, INTERNATIONAL_STOCK, REAL_ESTATE_FUND, ETF }
enum class InvestmentFundAssetType { PENSION, STOCK_FUND, MULTIMARKET_FUND }
```
