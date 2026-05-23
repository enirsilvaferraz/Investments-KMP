# Contract: B3 Import Port

**Feature**: `003-b3-import` | **Phase**: 1 — Design & Contracts | **Date**: 2026-05-23

---

## Visão Geral

Esta feature expõe **um único port** em `:domain:usecases` e sua implementação em `:data:filestore`. O contrato foi desenhado para que nenhum detalhe de infraestrutura (XLSX, FileMapper-KMP, DTOs B3, log de console) vaze para camadas superiores.

```
:domain:usecases  →  B3ImportPort (interface)
:data:filestore   →  B3ImportPortImpl (implementação — FileMapper-KMP)
:domain:usecases  →  ImportB3FileUseCase (orquestra timeout; não conhece DTOs)
```

---

## Port: `B3ImportPort`

**Pacote**: `com.eferraz.usecases.repositories`
**Módulo**: `:domain:usecases` (`commonMain`)

```kotlin
public interface B3ImportPort {
    /**
     * Abre o seletor de arquivo nativo, lê o XLSX da B3 e loga as posições no console.
     *
     * Retorna:
     *   Result.success(Unit) — importação concluída com sucesso.
     *   Result.success(Unit) — usuário cancelou o seletor (cancelamento silencioso).
     *   Result.failure(FileMapperException) — arquivo inválido, corrompido ou colunas ausentes.
     *   Result.failure(IOException) — falha de leitura após seleção.
     */
    public suspend fun importAndLog(): Result<Unit>
}
```

### Pré-condições
- Chamado a partir de uma coroutine (função `suspend`).
- Nenhuma pré-condição sobre arquivo — a seleção ocorre internamente.

### Pós-condições
- Efeito colateral: as posições de cada guia são impressas no console via `println`.
- Nenhum dado é persistido ou retornado ao chamador.
- Cancelamento do seletor pelo usuário **não** é tratado como erro — retorna `Result.success(Unit)`.

### Contrato de erro

Todas as falhas abaixo DEVEM ser **registadas no console da IDE** (`println`) pela implementação **antes** de devolver `Result.failure`, exceto cancelamento e importação vazia bem-sucedida (FR-013). A UI **não** exibe Snackbar nem `errorMessage`. **Excepção:** timeout — `println` no **UseCase** (FR-011a).

| Causa | Console (exemplo) | Retorno ao UseCase |
|-------|-------------------|-------------------|
| Usuário cancelou o diálogo | *(silêncio)* | `Result.success(Unit)` |
| Arquivo sem guias B3 conhecidas (FR-013) | *(silêncio)* | `Result.success(Unit)` |
| Arquivo 0 bytes / workbook ilegível | `println` motivo `EMPTY_FILE` | `Result.failure(...)` |
| Formato inválido (não XLSX) | `println` motivo `INVALID_FORMAT` | `Result.failure(...)` |
| Colunas ausentes (FR-015) | `println` guia + `MISSING_COLUMNS`; **sem** dados de guias no console | `Result.failure(...)` |
| Falha de leitura (I/O) / permissão | `println` motivo | `Result.failure(...)` |
| Timeout externo (30 s) | `println` no **UseCase** (`ImportB3FileUseCase`) ao capturar `TimeoutCancellationException` | `Result.failure` via `AppUseCase` |

---

## Implementação: `B3ImportPortImpl`

**Pacote**: `com.eferraz.filestore.b3`
**Módulo**: `:data:filestore` (`commonMain`)
**Visibilidade**: `internal` — não exposto fora do módulo

### Dependências internas (não visíveis ao domínio)
- `FileMapperPicker` — seletor de arquivo nativo multiplataforma (FileMapper-KMP)
- `FileMapper` / `importData<T>` — parser XLSX → data class (FileMapper-KMP)
- DTOs internos: `B3StockPosition`, `B3EtfPosition`, `B3FundPosition`, `B3FixedIncomePosition`, `B3TreasuryPosition`

### Pseudocódigo da implementação

```kotlin
internal class B3ImportPortImpl : B3ImportPort {

    private val knownSheets = listOf(
        "Acoes" to B3StockPosition::class,
        "ETF" to B3EtfPosition::class,
        // ... demais pares nome → DTO
    )

    override suspend fun importAndLog(): Result<Unit> = runCatching {
        val file = FileMapperPicker.pickFile(FileType.XLSX)
            ?: return@runCatching  // cancelamento silencioso — sem console

        val bytes = file.readBytes()

        // Fase A — parse/validação SEM println de dados (FR-015 atómico)
        val parsed = knownSheets.mapNotNull { (sheetName, _) ->
            if (!workbookHasSheet(bytes, sheetName)) null
            else sheetName to parseSheet(bytes, sheetName) // falha → exceção, sem log de dados
        }
        if (parsed.isEmpty()) return@runCatching  // FR-013 — sucesso silencioso

        // Fase B — só após todas as guias presentes passarem: log no console
        parsed.forEach { (sheetName, dataRows) ->
            logSheetToConsole(sheetName, dataRows)
        }
    }.onFailure { ex ->
        logErrorToConsole(ex)  // FR-014 — único canal de erro nesta fase
    }

    // parseSheet: importData + filter blank/total rows; onFailed → throw (sem println de dados)
    // logSheetToConsole: header | linhas | totais calculados (FR-006)

    // Extensões de filtragem (aplicadas sobre os campos String do DTO via reflexão/manual)
    // Linha em branco: todos os campos são nulos, vazios ou "-"
    // Linha de total do arquivo: o campo `product` (primeira coluna mapeada) começa com "Total" ou "Subtotal"
    //   → descartada do parse; o total exibido no log é recalculado a partir de `dataRows`
}
```

> **Nota de implementação**: O `@ColumnName` em cada DTO é usado pelo FileMapper-KMP para localizar a coluna pelo cabeçalho da sheet e popular o campo correspondente. O header da sheet **não é incluído em `items`**, mas é reconstruído para o log a partir das anotações `@ColumnName` do DTO (`T::class.columnHeaders()`). Linhas em branco e a linha de total do arquivo são descartadas antes de qualquer log; o total exibido é calculado pela implementação sobre as linhas de dados já parseadas.

### Source set
| Source set | Classe | Comportamento |
|------------|--------|---------------|
| `commonMain` | `B3ImportPortImpl` | FileMapper-KMP (KMP nativo — sem código de plataforma) |

> **Não há** implementações `jvmMain`, `androidMain` ou `iosMain` do port: FileMapper-KMP é `commonMain`. Em **Android/iOS** o `ImportB3FileUseCase` **não é invocado** (bypass): o botão pode estar visível na UI compartilhada, mas sem ação (spec §Assumptions).

---

## UseCase: `ImportB3FileUseCase`

**Pacote**: `com.eferraz.usecases.services`
**Módulo**: `:domain:usecases` (`commonMain`)

O UseCase é um **orquestrador de timeout e estado** — não conhece DTOs, XLSX, guias ou FileMapper.

```kotlin
public class ImportB3FileUseCase(
    private val port: B3ImportPort,
    coroutineContext: CoroutineContext,
) : AppUseCase<Unit, Unit>(coroutineContext) {

    override suspend fun execute(input: Unit) {
        withTimeout(30_000L) {
            port.importAndLog().getOrThrow()
        }
    }
}
```

### Contrato de interação UseCase ↔ Port
| Retorno do port | Ação do UseCase |
|-----------------|-----------------|
| `Result.success(Unit)` | Propaga sucesso — ViewModel restaura estado |
| `Result.failure(...)` | `getOrThrow()` lança exceção → `AppUseCase` captura como `Result.failure` |
| Timeout de 30 s | `TimeoutCancellationException` → `AppUseCase` captura como `Result.failure` |

---

## Registro Koin

```kotlin
// core/data/filestore/src/commonMain/.../FileStoreModule.kt
singleOf(::B3ImportPortImpl).bind<B3ImportPort>()
```

> Apenas uma entrada Koin. Nenhum módulo condicional por plataforma.

---

## Contrato da UI (`:features:composeApp`)

### Saída do `HistoryViewModel`

```kotlin
internal data class HistoryState(
    // ... campos existentes ...
    val isImporting: Boolean = false,
)
```

### Entrada (Intent)

```kotlin
data object ImportB3File : HistoryIntent
```

### Comportamento esperado (UI ↔ ViewModel)

| Evento de UI | VM emite estado | Resultado final |
|--------------|-----------------|-----------------|
| Toque no botão de importação | `isImporting = true` | Spinner exibido |
| UseCase concluído com sucesso | `isImporting = false` | Botão restaurado; dados no console |
| UseCase cancelado (arquivo não selecionado) | `isImporting = false` | Botão restaurado; sem mensagem |
| UseCase falhou (timeout / corrompido) | `isImporting = false` | Botão restaurado; detalhe no console (FR-014) |
| Plataforma Android/iOS | *(sem mudança de estado de import)* | Intent ignorado; sem picker |

---

## Dependências entre Módulos (impacto desta feature)

```
:features:composeApp
    └─► implementation(projects.domain.usecases)   ← já existe

:domain:usecases
    └─► api(projects.domain.entity)                ← já existe
        (+ B3ImportPort — novo, sem nova dependência de módulo)

:data:filestore
    └─► implementation(projects.domain.usecases)   ← já existe (para bind do port)
    └─► commonMain: "io.github.mamon-aburawi:filemapper-kmp:1.0.0"  ← NOVO
    └─► commonMain: "org.jetbrains.kotlinx:kotlinx-serialization-core:*"  ← NOVO (se não presente)
```

> Nenhum novo subprojeto Gradle. O grafo de módulos não é alterado.
