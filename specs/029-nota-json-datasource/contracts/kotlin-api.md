# Contrato da API Kotlin — Importação de Nota JSON

Feature: `029-nota-json-datasource` | Módulo: `:data:filestore` (**único alterado**)

---

## Interface pública (data source)

**Módulo**: `:data:filestore`  
**Pacote**: `com.eferraz.filestore.brokeragenote`

```kotlin
import com.eferraz.entities.brokeragenotes.BrokerageNote

public interface BrokerageNoteJsonDataSource {

    /**
     * Parses [Nota2JsonFixture.raw] and maps to [BrokerageNote].
     *
     * Does NOT run [BrokerageNoteValidator] — structural mapping only (FR-009).
     */
    public suspend fun loadNote(): Result<BrokerageNote>
}
```

> **Limite de escopo**: interface e impl residem em `:data:filestore`. Nenhum ficheiro em `:domain:usecases`, `:domain:entity`, `:features:*` ou `:apps:*` é alterado nesta feature.

---

## Implementação

**Classe**: `BrokerageNoteJsonDataSourceImpl` (`internal`, Koin `@Factory(binds = [BrokerageNoteJsonDataSource::class])`)

| Aspecto | Regra |
|---------|-------|
| Visibilidade interface | `public` |
| Visibilidade impl / DTOs / mapper / fixture | `internal` |
| Registo DI | Auto-descoberto por `@ComponentScan("com.eferraz.filestore")` — **sem editar** `FileStoreModule.kt` |
| Dispatcher | `withContext(Dispatchers.Default)` |
| Fonte JSON | `Nota2JsonFixture.raw` |
| Serializador | `Json { ignoreUnknownKeys = true }` |

### Mapper interno (testável)

```kotlin
internal object BrokerageNoteJsonMapper {
    internal fun parse(json: String): Result<BrokerageNote>
}
```

---

## Tipo de retorno (existente, não alterado)

`BrokerageNote` de `com.eferraz.entities.brokeragenotes` — ver [feature 026](../026-sinacor-fee-rateio/contracts/kotlin-api.md).

Mapeamento JSON → domínio: ver [data-model.md](data-model.md).

---

## Comportamento de `loadNote()`

### Sucesso

| Condição | Resultado |
|----------|-----------|
| JSON válido (`nota2.json`) | `Result.success(BrokerageNote)` |
| 47 ativos na fixture | 47 `NoteAsset`, ordem preservada |
| `"COMPRA"` / `"VENDA"` | `TradeType.BUY` / `TradeType.SELL` |

### Falha (`Result.failure`)

| Condição | Causa típica |
|----------|--------------|
| JSON inválido / incompleto | `SerializationException` |
| Data malformada | `IllegalArgumentException` no mapper |
| `movimentacao` desconhecida | `IllegalArgumentException` no mapper |

---

## Fora do contrato

| Item | Motivo |
|------|--------|
| `BrokerageNoteValidator` / rateio | FR-009 — chamador |
| Port em `:domain:usecases` | Fora do escopo desta feature |
| Alteração de `:domain:entity` | Tipos já existem (026) |

---

## Garantias (SC-001 a SC-004)

Ver [quickstart.md](quickstart.md).
