# Quickstart: Validação — Importação de Nota JSON

Feature: `029-nota-json-datasource` | Módulo: `:data:filestore` (**único alterado**)

---

## Pré-requisitos

- JDK 17+
- Branch `029-nota-json-datasource` ativa
- Alterações **somente** em `core/data/filestore`

---

## Execução dos Testes

```bash
./gradlew :data:filestore:jvmTest
```

Testes em:
```text
core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/
└── BrokerageNoteJsonMapperTest.kt
```

---

## Cenário Canônico (User Story 1)

```kotlin
// Via data source (Koin ou instância de teste)
val result = brokerageNoteJsonDataSource.loadNote()

// Ou direto no mapper (testes unitários)
val result = BrokerageNoteJsonMapper.parse(Nota2JsonFixture.raw)
```

Verificações: nota `8827829`, 47 ativos, `netValue = 12294.92` — tabela completa em [data-model.md](data-model.md#fixture-de-referência).

---

## Cenário de Isolamento (User Story 2)

- DTOs `internal` em `com.eferraz.filestore.brokeragenote.dto`
- Interface `BrokerageNoteJsonDataSource` em `:data:filestore` (não em `:domain:usecases`)
- Nenhum `@Serializable` adicionado em `:domain:entity`

---

## Cenários de Falha (User Story 3)

Via `BrokerageNoteJsonMapper.parse(invalidJson)` — exemplos:

| Cenário | Entrada | Resultado esperado |
|---------|---------|-------------------|
| JSON inválido | `"{ not json"` | `Result.failure` |
| Campo obrigatório ausente | JSON sem `metadados` ou sem `ativos` | `Result.failure` |
| `movimentacao` desconhecida | `"movimentacao": "DIVIDENDO"` | `Result.failure` |
| Data malformada | `"data_pregao": "2026-06-10"` | `Result.failure` |
| Propriedade extra | JSON válido + campo desconhecido na raiz | `Result.success` (ignoreUnknownKeys) |

Detalhes: [spec.md — User Story 3](spec.md#user-story-3--falhar-de-forma-previsível-em-entradas-inválidas-priority-p2).

---

## Referências

- [Spec](spec.md) · [Plano](plan.md) · [Contrato](contracts/kotlin-api.md)
- [Fixture fonte](../../docs/nota2.json)
