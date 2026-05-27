# Contract: B3Position

**Módulo**: `:core:data:filestore`
**Pacote**: `com.eferraz.filestore.b3.dto`
**Arquivo**: `B3Position.kt`
**Tipo de alteração**: Adição de dois métodos abstratos à interface selada existente

## Interface Atualizada

```kotlin
package com.eferraz.filestore.b3.dto

internal sealed interface B3Position {

    /** Retorna `true` se todos os campos da linha estiverem em branco. */
    fun isBlankRow(): Boolean

    /**
     * Retorna o identificador que será usado para correlacionar esta posição com
     * o ativo correspondente no banco de dados.
     *
     * - [B3StockPosition]       → `ticker`
     * - [B3EtfPosition]         → `ticker`
     * - [B3FundPosition]        → `ticker` (FIIs = VariableIncomeAsset no sistema)
     * - [B3FixedIncomePosition] → `code`
     * - [B3TreasuryPosition]    → `isinCode`
     */
    fun b3Identifier(): String

    /**
     * Retorna o valor financeiro atualizado desta posição, convertido para [Double].
     *
     * Campo de origem por subtipo:
     * - [B3StockPosition]       → `updatedValue`   ("Valor Atualizado")
     * - [B3EtfPosition]         → `updatedValue`   ("Valor Atualizado")
     * - [B3FundPosition]        → `updatedValue`   ("Valor Atualizado")
     * - [B3FixedIncomePosition] → `curveValue`     ("Valor Atualizado CURVA")
     * - [B3TreasuryPosition]    → `updatedValue`   ("Valor Atualizado")
     *
     * @throws NumberFormatException se o campo de valor não puder ser convertido.
     *   O chamador ([B3ImportDataSourceImpl]) deve capturar esta exceção por linha.
     */
    fun b3Value(): Double
}
```

## Implementação por Subtipo

### `B3StockPosition`
```kotlin
override fun b3Identifier(): String = ticker
override fun b3Value(): Double = updatedValue.toDouble()
```

### `B3EtfPosition`
```kotlin
override fun b3Identifier(): String = ticker
override fun b3Value(): Double = updatedValue.toDouble()
```

### `B3FundPosition`
```kotlin
override fun b3Identifier(): String = ticker   // FIIs — corresponde a VariableIncomeAsset no sistema
override fun b3Value(): Double = updatedValue.toDouble()
```

### `B3FixedIncomePosition`
```kotlin
override fun b3Identifier(): String = code
override fun b3Value(): Double = curveValue.toDouble()
```

### `B3TreasuryPosition`
```kotlin
override fun b3Identifier(): String = isinCode
override fun b3Value(): Double = updatedValue.toDouble()
```

## Nota sobre parsing de número

Verificado com arquivo real (`posicao-2026-05-23.xlsx`): valores numéricos chegam com **ponto decimal** direto (ex.: `3616.08`, `12329.27`, `37.55`). `toDouble()` funciona sem nenhum `replace`.

Campos `Valor Atualizado MTM` e `Valor Atualizado FECHAMENTO` de `B3FixedIncomePosition` chegam como `"-"` (traço literal) quando não disponíveis — confirma que `curveValue` é o único campo confiável para renda fixa. Como não usamos esses campos, o traço não causa problema.

`B3ImportDataSourceImpl` captura `NumberFormatException` por linha:

```kotlin
try {
    B3Record(identifier = position.b3Identifier(), value = position.b3Value())
} catch (e: NumberFormatException) {
    println("WARN: valor inválido para '${position.b3Identifier()}' — linha ignorada")
    null
}
```
