# Tasks: Importação de Nota de Corretagem via JSON

**Input**: Design documents from `/specs/029-nota-json-datasource/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Incluídos — spec exige cobertura em `:data:filestore:jvmTest` (FR-010, SC-003, SC-004); sucesso via `loadNote()` + `Nota2JsonFixture`; falhas via `BrokerageNoteJsonMapper.parse(String)`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

**Scope**: Alterações **exclusivamente** em `core/data/filestore` (`:data:filestore`). Sem port em `:domain:usecases`, sem alterações em `:domain:entity`, `:features:*` ou `:apps:*`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Módulo único**: `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/`
- **DTOs**: `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/dto/`
- **Testes**: `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Estrutura de pacotes e verificação de dependências existentes no módulo `:data:filestore`

- [x] T001 Criar estrutura de pacotes `brokeragenote/` e `brokeragenote/dto/` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/`
- [x] T002 Confirmar que `core/data/filestore/build.gradle.kts` já declara `kotlinx.serialization` e dependência de `:domain:entity` — **não alterar** salvo necessidade comprovada

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Fixture JSON e DTOs `@Serializable` — bloqueiam mapper, data source e todas as user stories

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T003 Materializar conteúdo de `docs/nota2.json` como constante `internal val raw: String` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/Nota2JsonFixture.kt`
- [x] T004 [P] Criar `BrokerageNoteJsonDocument.kt` com `@Serializable internal data class` e `@SerialName` para `metadados`, `resumo_financeiro`, `ativos` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/dto/BrokerageNoteJsonDocument.kt`
- [x] T005 [P] Criar `NoteMetadataJson.kt` com campos `numero_nota`, `data_pregao`, `data_liquidacao`, `corretora`, `cnpj_corretora`, `valor_liquido_nota` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/dto/NoteMetadataJson.kt`
- [x] T006 [P] Criar `NoteFinancialSummaryJson.kt` com totais e blocos aninhados em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/dto/NoteFinancialSummaryJson.kt`
- [x] T007 [P] Criar `ApportionableFeesJson.kt` com os 6 campos de `taxas_rateaveis` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/dto/ApportionableFeesJson.kt`
- [x] T008 [P] Criar `WithheldTaxesJson.kt` com `irrf_operacoes` e `irrf_day_trade` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/dto/WithheldTaxesJson.kt`
- [x] T009 [P] Criar `NoteAssetJson.kt` com `ticker`, `especificacao`, `movimentacao`, `quantidade`, `valor_unitario`, `valor_bruto_total` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/dto/NoteAssetJson.kt`

**Checkpoint**: Foundation ready — DTOs `internal`, fixture disponível, user story implementation can now begin

---

## Phase 3: User Story 1 — Carregar nota de corretagem a partir de JSON estruturado (Priority: P1) 🎯 MVP

**Goal**: Parse da constante `Nota2JsonFixture.raw` → `BrokerageNote` completo (metadados, resumo financeiro, 47 ativos) via data source público

**Independent Test**: Invocar `BrokerageNoteJsonDataSource.loadNote()` (ou `BrokerageNoteJsonMapper.parse(Nota2JsonFixture.raw)`) e verificar nota `8827829`, pregão `10/06/2026`, liquidação `12/06/2026`, corretora Nu Investimentos, `netValue = 12294.92`, 47 `NoteAsset` com COMPRA/VENDA corretos

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T010 [P] [US1] Criar teste de sucesso `GIVEN valid fixture WHEN parse THEN returns BrokerageNote` em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T011 [P] [US1] Adicionar asserções campo-a-campo de metadados e resumo financeiro (valores literais de `nota2.json`) em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T012 [P] [US1] Adicionar asserções de 47 ativos, ordem preservada, `TradeType.BUY`/`SELL` e amostra de ticker/preço em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T013 [P] [US1] Adicionar teste de `loadNote()` caminho de sucesso com instância de `BrokerageNoteJsonDataSourceImpl` em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T014 [P] [US1] Adicionar teste de lista `ativos` vazia → sucesso estrutural com `assets.isEmpty()` em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T015 [P] [US1] Adicionar smoke test SC-002: `parse(Nota2JsonFixture.raw)` completa em < 1 s em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`

### Implementation for User Story 1

- [x] T016 [US1] Implementar `BrokerageNoteJsonMapper.kt` com `Json { ignoreUnknownKeys = true }`, `parse(json: String): Result<BrokerageNote>`, `parseBrazilianDate`, `parseTradeType` e mapeamento completo para tipos de `com.eferraz.entities.brokeragenotes` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapper.kt`
- [x] T017 [US1] Criar interface pública `BrokerageNoteJsonDataSource` com `suspend fun loadNote(): Result<BrokerageNote>` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonDataSource.kt`
- [x] T018 [US1] Implementar `BrokerageNoteJsonDataSourceImpl` (`internal`, `@Factory(binds = [BrokerageNoteJsonDataSource::class])`, `withContext(Dispatchers.Default)`, delega a `Nota2JsonFixture.raw` + mapper) em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonDataSourceImpl.kt`

**Checkpoint**: User Story 1 funcional — parse de referência retorna `BrokerageNote` completo; testes T010–T015 passam

---

## Phase 4: User Story 2 — Isolar formato de arquivo do modelo de domínio (Priority: P1)

**Goal**: DTOs intermediários isolados na camada de dados; API pública expõe apenas `BrokerageNote`; domínio inalterado

**Independent Test**: Inspecionar que tipos em `brokeragenote/dto/` são `internal` com `@SerialName` PT→EN; nenhum `@Serializable` em `:domain:entity`; `BrokerageNoteJsonDataSource` retorna só tipos de domínio

> **Nota**: T019–T021 são gates de aceitação sobre o trabalho de Phase 2 + T016 — não duplicam implementação nova.

### Tests for User Story 2 ⚠️

- [x] T019 [P] [US2] Adicionar teste que verifica mapeamento de nomes de domínio (`noteNumber`, `tradingDate`, `settlementDate`, `ApportionableFees`, `TradeType`) após parse em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T020 [P] [US2] Adicionar teste de pass-through `valor_liquido_nota` → `netValue = 12294.92` sem inversão de sinal em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T021 [P] [US2] Adicionar teste de zeros monetários preservados (`corretagem = 0.0`, `irrfDayTrade = 0.0`) e quantidade inteira JSON como `Double` em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`

### Implementation for User Story 2

- [x] T022 [US2] Verificar gate de aceitação: todos os DTOs e mapper são `internal`; `@SerialName` em cada campo JSON conforme `data-model.md` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/dto/` e `BrokerageNoteJsonMapper.kt`

**Checkpoint**: User Story 2 verificada — desacoplamento JSON/domínio confirmado por testes e visibilidade

---

## Phase 5: User Story 3 — Falhar de forma previsível em entradas inválidas (Priority: P2)

**Goal**: JSON malformado, incompleto ou com `movimentacao` desconhecida retorna `Result.failure` sem `BrokerageNote` parcial

**Independent Test**: Invocar `BrokerageNoteJsonMapper.parse(String)` com JSON inválido, JSON sem `ativos`/`metadados`, `movimentacao` desconhecida e data malformada — todos falham

### Tests for User Story 3 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T023 [P] [US3] Teste JSON sintaticamente inválido → `Result.failure` em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T024 [P] [US3] Teste JSON válido omitindo `metadados` ou `ativos` → `Result.failure` em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T025 [P] [US3] Teste ativo com `movimentacao` desconhecida → `Result.failure` em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T026 [P] [US3] Teste data malformada em `data_pregao` → `Result.failure` em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`
- [x] T027 [P] [US3] Teste JSON com propriedade extra desconhecida + campos obrigatórios válidos → sucesso (ignoreUnknownKeys) em `core/data/filestore/src/jvmTest/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapperTest.kt`

### Implementation for User Story 3

- [x] T028 [US3] Encapsular `SerializationException` e `IllegalArgumentException` em `Result.failure` dentro de `BrokerageNoteJsonMapper.parse` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonMapper.kt`
- [x] T029 [US3] Garantir que `BrokerageNoteJsonDataSourceImpl.loadNote()` propaga falhas do mapper sem engolir exceções em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteJsonDataSourceImpl.kt`

**Checkpoint**: User Story 3 completa — SC-003 satisfeito; nenhum cenário de falha retorna nota parcial

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validação final de escopo e quickstart

- [ ] T030 Executar `./gradlew :data:filestore:jvmTest` conforme `specs/029-nota-json-datasource/quickstart.md` *(opcional para agente — princípio IX; executar quando o utilizador pedir validação)*
- [x] T031 [P] Auditar diff — confirmar que **nenhum** ficheiro fora de `core/data/filestore/` foi alterado (incl. `:domain:entity`, `:domain:usecases`, `FileStoreModule.kt`)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup — **BLOCKS** all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational — MVP core (mapper + data source)
- **User Story 2 (Phase 4)**: Depends on Foundational + mapper from US1 (T016) — gates de aceitação
- **User Story 3 (Phase 5)**: Depends on Foundational + mapper from US1 (T016) — tratamento de erros
- **Polish (Phase 6)**: Depends on US1–US3 desejadas

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational — **no dependencies on other stories** — **MVP**
- **User Story 2 (P1)**: Depends on mapper (T016) from US1 — gates de aceitação, testável após mapper existir
- **User Story 3 (P2)**: Depends on mapper (T016) from US1 — testável via `parse(String)` sem depender de US2

### Within Each User Story

- Tests MUST be written and FAIL before implementation (T010–T015 antes de T016–T018; T023–T027 antes de T028–T029)
- DTOs (Phase 2) before mapper
- Mapper before data source impl
- Story complete before Polish

### Parallel Opportunities

- **Phase 2**: T004–T009 (DTOs) em paralelo após T003 (fixture)
- **Phase 3 tests**: T010–T015 em paralelo (mesmo ficheiro de teste — coordenar ou sequenciar commits)
- **Phase 5 tests**: T023–T027 em paralelo
- **Phase 6**: T031 em paralelo com T030
- US2 e US3 podem avançar em paralelo **após** T016 (mapper base) estar completo

---

## Parallel Example: User Story 1

```bash
# Após Phase 2, escrever testes de sucesso em paralelo (T010–T012):
Task: "Criar teste de sucesso parse fixture em BrokerageNoteJsonMapperTest.kt"
Task: "Asserções metadados/resumo financeiro em BrokerageNoteJsonMapperTest.kt"
Task: "Asserções 47 ativos e TradeType em BrokerageNoteJsonMapperTest.kt"

# Após mapper (T016), DTOs já criados — data source (T017–T018) sequencial
```

---

## Parallel Example: Foundational DTOs

```bash
# Após T003 (Nota2JsonFixture), lançar em paralelo:
Task: "BrokerageNoteJsonDocument.kt"
Task: "NoteMetadataJson.kt"
Task: "NoteFinancialSummaryJson.kt"
Task: "ApportionableFeesJson.kt"
Task: "WithheldTaxesJson.kt"
Task: "NoteAssetJson.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — fixture + DTOs)
3. Complete Phase 3: User Story 1 (testes → mapper → data source)
4. **STOP and VALIDATE**: `./gradlew :data:filestore:jvmTest` — nota `8827829` com 47 ativos
5. US2/US3 incrementais depois

### Incremental Delivery

1. Setup + Foundational → infra pronta
2. User Story 1 → parse completo → **MVP entregue**
3. User Story 2 → isolamento JSON/domínio confirmado
4. User Story 3 → falhas previsíveis (SC-003)
5. Polish → quickstart + auditoria de escopo

### Parallel Team Strategy

1. Equipa completa Phase 1 + Phase 2 em conjunto
2. Após T016 (mapper):
   - Dev A: US1 data source + testes de sucesso (T013–T018)
   - Dev B: US2 gates de aceitação (T019–T022)
   - Dev C: US3 testes e tratamento de erro (T023–T029)

---

## Notes

- [P] tasks = ficheiros diferentes, sem dependências entre si
- [Story] label mapeia tarefa à user story para rastreabilidade
- `BrokerageNoteValidator` e rateio **fora de escopo** (FR-009) — não invocar nos testes
- Registo Koin: `@Factory` auto-descoberto por `@ComponentScan("com.eferraz.filestore")` — **não editar** `FileStoreModule.kt`
- T030 (`./gradlew`): opcional para agente conforme princípio IX — escrever testes é obrigatório; executá-los fica a critério do utilizador
- Commit após cada fase ou grupo lógico; parar em checkpoints para validar story independentemente
