# Contract: SyncB3HistoryUseCase

**Módulo**: `:core:domain:usecases`
**Pacote**: `com.eferraz.usecases.services`
**Arquivo**: `SyncB3HistoryUseCase.kt`
**Tipo de alteração**: Novo use case

## Assinatura

```kotlin
package com.eferraz.usecases.services

import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.entities.B3Record
import com.eferraz.usecases.repositories.DateProvider
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class SyncB3HistoryUseCase(
    private val holdingHistoryRepository: HoldingHistoryRepository,
    private val dateProvider: DateProvider,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<List<B3Record>, Unit>(context) {

    override suspend fun execute(param: List<B3Record>)
}
```

## Comportamento

Veja algoritmo completo em [data-model.md](../data-model.md#syncb3historyusecase--novo-use-case).

**Pós-condição garantida**: nenhuma entrada de histórico é removida; somente `endOfMonthValue` é substituído nas entradas com correspondência.

## Testes obrigatórios (Princípio V)

Arquivo: `SyncB3HistoryUseCaseTest.kt`

Cenários mínimos exigidos (log verificado via `println` capturado ou mock de output):

| ID | Dado (GIVEN) | Ação (WHEN) | Resultado esperado (THEN) |
|----|-------------|-------------|--------------------------|
| T1 | Histórico com `VariableIncomeAsset(ticker="PETR4")`; `B3Record("PETR4", 150.0)` | `execute(listOf(record))` | `upsert` chamado **uma vez** com `endOfMonthValue=150.0`; println contém "ATUALIZADO" e "PETR4" |
| T2 | Histórico com `FixedIncomeAsset(b3Identifier="CDB-001")`; `B3Record("CDB-001", 1050.0)` | `execute(listOf(record))` | `upsert` chamado com `endOfMonthValue=1050.0`; println contém "ATUALIZADO" |
| T3 | Histórico com FII `VariableIncomeAsset(ticker="HGLG11", type=REAL_ESTATE_FUND)`; `B3Record("HGLG11", 120.0)` | `execute(listOf(record))` | `upsert` chamado (FII = renda variável no sistema) |
| T4 | `B3Record("XYZ99", 200.0)` sem ativo correspondente no histórico | `execute(listOf(record))` | `upsert` NÃO chamado; println contém "NÃO REGISTRADO" e "XYZ99" |
| T5 | Histórico com `VariableIncomeAsset(ticker="BBDC4")`; importação não contém "BBDC4" | `execute(emptyList())` | println contém "IDENTIFICADOR INEXISTENTE" e "BBDC4" |
| T6 | `B3Record("petr4", 150.0)` (minúsculas); ativo no banco tem ticker "PETR4" | `execute(listOf(record))` | `upsert` NÃO chamado (case-sensitive); println contém "NÃO REGISTRADO" |
| T7 | `InvestmentFundAsset` no histórico; importação vazia | `execute(emptyList())` | println contém "IGNORADO" para o fundo; NÃO contém "IDENTIFICADOR INEXISTENTE" |
| T8 | `FixedIncomeAsset(b3Identifier=null)` no histórico; importação vazia | `execute(emptyList())` | println contém "IGNORADO" para renda fixa sem id; NÃO contém "IDENTIFICADOR INEXISTENTE" |
| T9 | Lista de records vazia; histórico vazio | `execute(emptyList())` | nenhum `upsert` chamado; nenhum println de "ATUALIZADO" ou "NÃO REGISTRADO"; termina sem exceção (edge case: arquivo vazio) |
| T10 | Histórico com dois `VariableIncomeAsset(ticker="ITSA4")`; `B3Record("ITSA4", 88.0)` | `execute(listOf(record))` | `upsert` chamado **duas vezes**, ambas com `endOfMonthValue=88.0`; println contém "ATUALIZADO" (todos os registros coincidentes são atualizados) |
