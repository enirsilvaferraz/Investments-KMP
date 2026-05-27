# Tasks: Sincronização de Histórico via Importação B3

**Input**: Design documents from `/specs/006-b3-import-sync-history/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

**Módulos alterados**: `:core:domain:usecases` · `:core:data:filestore` — nenhum módulo novo criado.

**Testes obrigatórios**: `SyncB3HistoryUseCase` exige testes unitários antes do merge (Princípio V da constituição).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode ser executada em paralelo (ficheiros distintos, sem dependências incompletas)
- **[Story]**: User story à qual a tarefa pertence (US1, US2, US3)

---

## Phase 1: Setup (Verificação Inicial)

**Propósito**: Confirmar estrutura existente antes de qualquer alteração — nenhum módulo novo é criado.

- [x] T001 Confirmar caminhos dos ficheiros a modificar nos módulos `:core:domain:usecases` e `:core:data:filestore` (zero criações de módulo; alterações concentradas nos pacotes `entities/`, `repositories/`, `services/` e `b3/dto/`)

---

## Phase 2: Foundational (Pré-requisitos Bloqueantes)

**Propósito**: Interface e entidades base que DEVEM ser concluídas antes de qualquer user story — B3Record, B3ImportDataSource refatorada e métodos abstratos em B3Position.

**⚠️ CRÍTICO**: Nenhuma user story pode começar antes desta fase estar completa.

- [x] T002 Criar `B3Record.kt` com `public data class B3Record(val identifier: String, val value: Double)` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/entities/B3Record.kt`
- [x] T003 Atualizar `B3ImportDataSource.kt` — substituir o método `importAndLog(): Result<Unit>` por `import(): Result<List<B3Record>>` com KDoc conforme contract em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/B3ImportDataSource.kt`
- [x] T004 Adicionar métodos abstratos `b3Identifier(): String` e `b3Value(): Double` (com KDoc mapeando cada subtipo) à `sealed interface B3Position` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3Position.kt`
- [x] T005 [P] Implementar `b3Identifier(): String = ticker` e `b3Value(): Double = updatedValue.toDouble()` em `B3StockPosition.kt` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3StockPosition.kt`
- [x] T006 [P] Implementar `b3Identifier(): String = ticker` e `b3Value(): Double = updatedValue.toDouble()` em `B3EtfPosition.kt` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3EtfPosition.kt`
- [x] T007 [P] Implementar `b3Identifier(): String = ticker` e `b3Value(): Double = updatedValue.toDouble()` em `B3FundPosition.kt` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3FundPosition.kt`
- [x] T008 [P] Implementar `b3Identifier(): String = code` e `b3Value(): Double = curveValue.toDouble()` em `B3FixedIncomePosition.kt` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3FixedIncomePosition.kt`
- [x] T009 [P] Implementar `b3Identifier(): String = isinCode` e `b3Value(): Double = updatedValue.toDouble()` em `B3TreasuryPosition.kt` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3TreasuryPosition.kt`

**Checkpoint**: Tipos de domínio e contratos prontos — implementação das user stories pode iniciar.

---

## Phase 3: User Story 1 + User Story 3 — Importação XLSX e Atualização do Histórico (Priority: P1) 🎯 MVP

**Goal**: O usuário importa um arquivo XLSX exportado da B3; o sistema lê cada posição, aplica as regras de correspondência por tipo de ativo e substitui `endOfMonthValue` nos registros de histórico do mês corrente. Persiste as alterações e emite logs de `ATUALIZADO` / `NÃO REGISTRADO`.

**Independent Test**: Importar arquivo com ao menos uma ação (ex.: `PETR4`), um FII (`HGLG11`) e uma renda fixa com identificador (`CDB-001`); verificar que os três registros no banco tiveram `endOfMonthValue` atualizado e que os logs correspondentes foram emitidos.

### Testes obrigatórios para US1 / US3 (Princípio V da constituição) ⚠️

> **GATE**: Escrever todos os testes antes da implementação e confirmar que FALHAM antes de prosseguir.

- [x] T010 [US1] Criar `SyncB3HistoryUseCaseTest.kt` com os 10 cenários obrigatórios definidos no contract (`SyncB3HistoryUseCase.kt.md`): T1=VariableIncome atualizado, T2=FixedIncome atualizado, T3=FII atualizado, T4=NÃO_REGISTRADO, T5=IDENTIFICADOR_INEXISTENTE, T6=case-sensitive rejeitado, T7=IGNORADO fundo, T8=IGNORADO renda fixa sem b3Identifier, T9=execute(emptyList()) sem erro nem upsert, T10=dois holdings do mesmo ticker (ambos upserted) — capturar `println` via `val baos = ByteArrayOutputStream(); System.setOut(PrintStream(baos))` antes de cada teste e assertar `baos.toString()` no THEN; usar MockK para `HoldingHistoryRepository` e `DateProvider` em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/services/SyncB3HistoryUseCaseTest.kt`

### Implementação de US1 + US3

- [x] T011 [US1] [US3] Criar `SyncB3HistoryUseCase.kt` com Passe 1 completo: buscar `historyEntries` via `holdingHistoryRepository.getByReferenceDate(currentMonth)`, iterar cada `B3Record`, usar `filter { (asset as? VariableIncomeAsset)?.ticker == record.identifier || (asset as? FixedIncomeAsset)?.b3Identifier == record.identifier }` (case-sensitive, atualiza todos os matches), para cada match: `upsert(entry.copy(endOfMonthValue = record.value))`; emitir `println("ATUALIZADO: …")` se matches não-vazio ou `println("NÃO REGISTRADO: …")` se vazio — anotado com `@Factory` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/services/SyncB3HistoryUseCase.kt` **(⚠️ iniciar apenas após T010 estar escrito e falhando)**
- [x] T012 [P] [US3] Refatorar `B3ImportDataSourceImpl.kt` para implementar `import(): Result<List<B3Record>>`: iterar abas do XLSX, para cada `B3Position` não-blank chamar `try { B3Record(position.b3Identifier(), position.b3Value()) } catch (e: NumberFormatException) { println("WARN: …"); null }` e filtrar nulos, retornando `Result.success(list)` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/B3ImportDataSourceImpl.kt`
- [x] T013 [US1] Atualizar `ImportB3FileUseCase.kt` para chamar `port.import().getOrThrow()` e passar o resultado para `syncUseCase(records).getOrThrow()`, removendo a chamada anterior a `port.importAndLog()` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/services/ImportB3FileUseCase.kt`

**Checkpoint**: Neste ponto US1 e US3 são funcionalmente completos e testáveis de forma independente.

---

## Phase 4: User Story 2 — Log Progressivo por Categoria (Priority: P2)

**Goal**: À medida que o sistema processa cada posição e cada entrada do histórico, emite mensagens de log individuais nas quatro categorias: `ATUALIZADO`, `NÃO REGISTRADO`, `IDENTIFICADOR INEXISTENTE` e `IGNORADO`.

**Independent Test**: Importar arquivo com (a) ativo com correspondência, (b) ativo B3 não cadastrado, (c) ativo do banco ausente na importação e (d) fundo multimercado no histórico. Verificar que exatamente quatro mensagens de log distintas são emitidas — uma de cada categoria.

### Implementação de US2

- [x] T014 [US2] Implementar Passe 2 em `SyncB3HistoryUseCase.kt`: construir `importedIds = records.map { it.identifier }.toSet()`, iterar `historyEntries` e emitir `println("IDENTIFICADOR INEXISTENTE: $key")` para `VariableIncomeAsset` e `FixedIncomeAsset com b3Identifier != null` cujo key não esteja em `importedIds`; emitir `println("IGNORADO: …")` para `InvestmentFundAsset` e `FixedIncomeAsset com b3Identifier == null` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/services/SyncB3HistoryUseCase.kt`
- [x] T015 [US2] Validar que os cenários T5 (`IDENTIFICADOR_INEXISTENTE`), T7 (`IGNORADO` fundo), T8 (`IGNORADO` renda fixa sem id) e T9 (`execute(emptyList())` sem erro) no `SyncB3HistoryUseCaseTest.kt` passam com o Passe 2 implementado em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/services/SyncB3HistoryUseCaseTest.kt`

**Checkpoint**: Todas as user stories são funcionais e independentemente testáveis.

---

## Phase 5: Polish & Concerns Transversais

**Propósito**: Compilação limpa, testes verdes e conformidade com a constituição.

- [x] T016 [P] Executar `./gradlew :core:data:filestore:compileKotlinJvm` e corrigir erros de compilação nos subtipos de `B3Position` (T005–T009)
- [x] T017 [P] Executar `./gradlew :core:domain:usecases:compileKotlinJvm` e corrigir erros de compilação em `B3Record`, `B3ImportDataSource`, `SyncB3HistoryUseCase` e `ImportB3FileUseCase`
- [x] T018 Executar `./gradlew :core:domain:usecases:jvmTest` e garantir que todos os 8 cenários de `SyncB3HistoryUseCaseTest` passam sem falha
- [x] T019 Verificar conformidade `explicitApi()` em `B3Record.kt` (modificador `public` explícito em `data class` e propriedades) e em `SyncB3HistoryUseCase.kt` (classe `public`, método `execute` `override public`)

---

## Dependencies & Execution Order

### Dependências entre fases

- **Setup (Phase 1)**: Sem dependências — pode iniciar imediatamente
- **Foundational (Phase 2)**: Depende de Phase 1 — **bloqueia todas as user stories**
- **US1 + US3 (Phase 3)**: Depende de Phase 2 (todos os T002–T009 concluídos)
- **US2 (Phase 4)**: Depende de T011 (Passe 1 do `SyncB3HistoryUseCase`) estar completo
- **Polish (Phase 5)**: Depende de todas as user stories completadas

### Dependências entre user stories

- **US1 + US3 (Phase 3)**: Pode iniciar após Phase 2 — independente de US2
- **US2 (Phase 4)**: Pode iniciar após T011; integra com o mesmo ficheiro `SyncB3HistoryUseCase.kt`

### Dependências dentro de cada fase

**Phase 2**:
- T002 → T003 (B3Record deve existir antes de atualizar a interface)
- T003 → T004 (interface atualizada antes dos subtipos)
- T005, T006, T007, T008, T009 → dependem de T004 (podem rodar em paralelo entre si)

**Phase 3**:
- T010 (testes) → deve ser escrito e **falhar** antes de T011 (gate Princípio V — não é paralelo)
- T011 → depende de T010; T012 → independente de T011 (ficheiros distintos, podem rodar em paralelo após T010)
- T013 → depende de T011 e T012 (usa `SyncB3HistoryUseCase` e `import()`)

**Phase 4**:
- T014 → depende de T011 (mesmo ficheiro, extensão do Passe 1)
- T015 → depende de T014

---

## Parallel Example: Phase 2 — Subtipos de B3Position

```bash
# Após T004 ser concluído, os 5 subtipos podem ser implementados em paralelo:
Task T005: B3StockPosition.kt     ← agente/dev A
Task T006: B3EtfPosition.kt       ← agente/dev B
Task T007: B3FundPosition.kt      ← agente/dev C
Task T008: B3FixedIncomePosition.kt ← agente/dev D
Task T009: B3TreasuryPosition.kt  ← agente/dev E
```

## Parallel Example: Phase 3 — Implementação principal

```bash
# Sequência obrigatória para T010 → T011 (gate Princípio V):
Step 1: T010 — escrever testes (confirmar que FALHAM)
Step 2: T011 — implementar SyncB3HistoryUseCase (após T010 falhando)

# T012 pode rodar em paralelo a T011 (ficheiro distinto):
Paralelo com T011: T012 — B3ImportDataSourceImpl.kt ← agente B (data)

# T013 aguarda T011 + T012
Step 3: T013 — atualizar ImportB3FileUseCase
```

---

## Implementation Strategy

### MVP First (US1 + US3 apenas)

1. Completar Phase 1: Setup
2. Completar Phase 2: Foundational (**crítico — bloqueia tudo**)
3. Completar Phase 3: US1 + US3
4. **PARAR e VALIDAR**: testar `./gradlew :core:domain:usecases:jvmTest` com cenários T1–T4, T6
5. Importar arquivo XLSX real e verificar atualização do histórico

### Entrega Incremental

1. Setup + Foundational → base pronta
2. US1 + US3 → historico atualizado corretamente (MVP!)
3. US2 → log completo com 4 categorias → entrega final
4. Polish → compilação limpa + 8 testes verdes

---

## Notes

- `[P]` = ficheiros distintos, sem dependências incompletas; podem ser delegados a subagentes em paralelo
- `[US?]` mapeia cada tarefa à user story correspondente para rastreabilidade
- Testes (T010) devem ser escritos e **falhar** antes da implementação de T011 (gate Princípio V — T011 **não** é paralelizável com T010)
- Log via `println` direto — sem sealed interface de eventos (decisão do research.md §2)
- Comparação de identificadores é **case-sensitive** (decisão do research.md §8)
- Parsing de `Double` usa `toDouble()` direto — valores chegam com ponto decimal padrão (research.md §9)
- Commit após cada phase ou grupo lógico de tarefas
