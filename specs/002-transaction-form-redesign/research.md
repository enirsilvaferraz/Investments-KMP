# Phase 0 — Research

**Feature**: 002-transaction-form-redesign
**Date**: 2026-05-19

Não há entradas marcadas como **NEEDS CLARIFICATION** no `Technical Context` do plano. O `spec.md` já registra três clarifications resolvidos (`Session 2026-05-19`): comportamento de falha de save, pareamento por posição, e tratamento de `observations`. Esta seção consolida decisões de design/implementação derivadas dessas clarifications, das restrições da constitution e do código existente.

---

## D1 — Layout Row/Column substituindo `UiTableV3`

- **Decision**: Renderizar a tabela como um `Column` no qual:
  - O cabeçalho é um único `Row` com colunas dimensionadas por `Modifier.width(...)` / `Modifier.weight(...)` **idênticas** às de `NewTransactionsTable` (Data `width(125.dp)`; Transação `width(130.dp)`; Quantidade `weight(.5f)`; Valor Unitário `weight(1.1f)`; Valor Total `weight(1.1f)`; espaço final para a ação X). Para categorias sem Quantidade/Valor Unitário, essas duas colunas simplesmente são omitidas (sem ajustar o resto — os `weight` restantes ocupam o espaço naturalmente).
  - Cada linha é um `Row` (mesmos modificadores do cabeçalho) cujas células são **`FormTextField`** (helper `com.eferraz.asset_management.helpers.FormTextField`, idêntico ao usado pelo protótipo) — um por coluna editável — e um `IconButton(Icons.Default.Close, tint = MaterialTheme.colorScheme.error)` para a ação de remover.
  - As colunas variáveis (Quantidade, Valor Unitário) aparecem condicionalmente quando `category == VARIABLE_INCOME`. Para `FIXED_INCOME` e `INVESTMENT_FUND`, apenas Data, Transação e Valor Total + ação de remover.
- **Rationale**:
  - O protótipo `NewTransactionsTable` (`TransactionManagementScreen.kt`) define explicitamente o layout pretendido com `Row`/`Column`/`FormTextField` e é a referência visual/estrutural definitiva (cf. `spec.md`: "trocando o layout do TransactionTable pelo layout prototipado em ... NewTransactionsTable").
  - `FormTextField` já é usado em todo o módulo `:features:asset-management` (incluindo `AssetManagementScreen.kt`), garantindo consistência visual com o `AssetManagementDialog` (`User Story 5`) **sem** introduzir novos componentes.
  - `UiTableV3` é proibido pelo `SC-006` e adiciona complexidade desnecessária (KISS/FR-016).
- **Alternatives considered**:
  - **Usar `TableInputDate`/`TableInputSelect`/`TableInputMoney`** do `:features:design-system` — **rejeitado** por instrução explícita: o protótipo `NewTransactionsTable` usa `FormTextField` para **todas** as colunas (data, transação, quantidade, valor unitário, valor total) e a feature segue esse protótipo. Adotar componentes diferentes introduziria divergência visual com o protótipo e com o `AssetManagementDialog`, e adicionaria dependência cruzada desnecessária para uma "implementação simples" (FR-016).
  - **`LazyColumn` para as linhas** — rejeitado porque o número esperado de linhas por dialog é pequeno (dezenas no pior caso) e os filhos contêm `TextField` com foco/keyboard. Um `Column` simples com `verticalScroll` (quando precisar) é mais previsível.
  - **Manter `UiTableV3`** — rejeitado por contrato (`FR-003`, `SC-006`).

### D1.1 — Tradução de campos em `FormTextField`

Como `FormTextField` é um input de texto livre (sem máscara/dropdown nativos), o ViewModel mantém a representação textual dos campos exatamente como hoje (`dateDigits: String`, `quantity: String`, `unitPrice: String`, `totalValue: String`) e o tipo de transação fica em `String` na UI também:

| Coluna | Valor (`value`) | `onValueChange` → evento |
|---|---|---|
| Data | `row.dateDigits` (formato `YYYYMMDD`) | `DraftTransactionDateChanged(index, raw)` (o VM continua chamando `dateToDigits(raw)`). |
| Transação | label PT do `row.type` (`row.type.asLabel()`) digitada/lida como texto | `DraftTransactionTypeChanged(index, type)` (a UI converte a string editada para `TransactionType` via parsing/`asLabel` inverso; ver §D1.2). |
| Quantidade | `row.quantity` | `DraftTransactionQuantityChanged(index, value)`. |
| Valor Unitário | `row.unitPrice` | `DraftTransactionUnitPriceChanged(index, value)`. |
| Valor Total | `row.totalValue` | `DraftTransactionTotalValueChanged(index, value)`. |
| X | — | `DraftTransactionDeleteClicked(index)`. |

`errorMessage` em cada `FormTextField` é derivada das flags já existentes em `TransactionDraftUi` (`dateError`, `quantityError`, `unitPriceError`, `totalValueError`); quando `true`, passamos uma mensagem curta (ex.: `"Inválido"`); quando `false`, `null`. A regra de "ignorar linhas inválidas" no `Save` continua valendo (sem bloqueio).

### D1.2 — Edição do campo "Transação" como texto

`TransactionType` é um enum (`PURCHASE`, `SALE`, ...) com label PT-BR via `asLabel()`. Como `FormTextField` não tem dropdown, a tradução texto↔enum fica no Composable da célula:

- **Display**: `value = row.type.asLabel()`.
- **Parse**: `onValueChange = { raw ->
      val parsed = TransactionType.entries.firstOrNull { it.asLabel().equals(raw, ignoreCase = true) }
      if (parsed != null) onEvent(DraftTransactionTypeChanged(index, parsed))
  }`.
- Enquanto o texto digitado não corresponde a um `TransactionType` conhecido, o estado **não é atualizado** (a UI mantém o `value` do estado e o `FormTextField` apenas reflete a digitação efêmera dentro do próprio `TextField` — comportamento padrão do Compose para `value`/`onValueChange` controlados). Não é exibido erro adicional além das flags já existentes.

> **Limitação assumida (implementação simples — FR-016)**: o campo aceita apenas as labels PT-BR existentes; não há autocomplete/dropdown. Caso o utilizador deseje um seletor mais rico no futuro, isso seria uma evolução fora do escopo desta feature.

---

## D2 — Sistema de draft com snapshot inicial

- **Decision**: Estender `TransactionManagementUiState` com:
  - `initialSnapshot: List<TransactionDraftUi>` — cópia imutável da lista carregada do banco em `loadInitialState`.
  - Propriedade derivada `isDirty: Boolean` calculada como `!transactions.matchesByPosition(initialSnapshot)`.
  - O método `matchesByPosition` compara, índice a índice, apenas os campos editáveis aplicáveis à categoria: `dateDigits`, `type`, e — só quando `category == VARIABLE_INCOME` — `quantity` e `unitPrice`; e sempre `totalValue`. **Não** inclui `observations`.
  - `id` e `isNew` **não** participam da comparação (uma linha existente com edição preserva `id`; uma adição tem `id == null`, e ao ser confirmada via Save passa a ter `id` real apenas na próxima carga).
- **Rationale**:
  - Pareamento por posição já está acordado em `spec.md` (`FR-010`, clarification 2026-05-19 #2).
  - Manter snapshot imutável em estado evita comparações com fontes externas e mantém o ViewModel testável.
  - Excluir `observations` do diff cumpre `FR-005a`.
- **Alternatives considered**:
  - **Comparar por `id`** — rejeitado: a feature exige pareamento por posição; lista pode conter novas (id nulo) e remoções, e o diff precisa considerar isso.
  - **Calcular `isDirty` no Composable** — rejeitado: regra é de domínio do estado; melhor manter no UI state para evitar reexecutar a comparação em cada recomposição.

---

## D3 — Persistência atómica do Save (delete + upsert)

- **Decision**: Em `onSave`, calcular o conjunto de exclusões e o conjunto de upserts a partir do `initialSnapshot` e da lista corrente:
  1. **Exclusões** = `initialSnapshot.mapNotNull { it.id }.toSet() - transactions.mapNotNull { it.id }.toSet()` — IDs presentes na snapshot inicial mas ausentes da lista corrente.
  2. **Upserts** = `transactions.mapNotNull { it.toDomainTransaction(holding, category) }` — todas as linhas editáveis convertíveis em domínio (linhas inválidas são ignoradas, espelhando o comportamento atual descrito em `Edge Cases`).
  3. Executar em sequência dentro de um `runCatching { ... }`:
     - `deleteTransactionUseCase` para cada `id` em **Exclusões**.
     - `saveTransactionUseCase` para cada transação em **Upserts**.
  4. `onSuccess` → marcar `isCompleted = true` e `isSaving = false` (o `LaunchedEffect(state.isCompleted)` já fecha o dialog).
  5. `onFailure` → apenas `isSaving = false`. Estado (transactions, initialSnapshot, observations preservados) é mantido para nova tentativa. Sem mensagem de erro dedicada (clarification 2026-05-19 #1, `FR-015`).
- **Rationale**:
  - Mantém a regra de "tudo só persiste ao Save" (`FR-009`).
  - Reutiliza UseCases existentes sem alterar contratos de domínio (KISS, princípio II).
  - Não há transação atómica no repositório (`AssetTransactionRepository` expõe operações individuais); a ordem **delete → upsert** minimiza o risco de violar invariantes durante o `Save` parcial e mantém o comportamento "best effort" descrito em `FR-015`.
- **Alternatives considered**:
  - **Persistir incrementalmente em cada edição** — rejeitado: viola o sistema de draft (`FR-009`).
  - **Introduzir um novo UseCase atómico `SyncTransactionsForHolding`** — rejeitado por YAGNI/KISS: a feature exige *implementação simples* (`FR-016`); não há sinal de necessidade para uma operação de domínio nova nesta entrega.

---

## D4 — Preservação de `observations` no round-trip

- **Decision**:
  - `TransactionDraftUi` mantém o campo `observations: String` (já existe). O Composable de tabela **não** expõe esse campo.
  - O evento `DraftTransactionObservationChanged` é removido (não há mais UI para editá-lo).
  - `fromDomain(...)` continua copiando o valor original. `toDomainTransaction(...)` continua usando `observations.ifBlank { null }`.
- **Rationale**: Cumpre `FR-005a` sem alterar o domínio. Como `observations` não participa do diff (D2), edições inocentes de outros campos não disparam `isDirty` por causa de um round-trip corrompido.
- **Alternatives considered**: nenhum, decisão é direta a partir da clarification.

---

## D5 — Ordenação por data apenas na carga inicial

- **Decision**: Em `loadInitialState`, ao receber a lista do `GetTransactionsByHoldingUseCase`, ordenar por `LocalDate` ascendente *antes* de mapear para `TransactionDraftUi`. Não há reordenação subsequente no ViewModel nem no Composable.
- **Rationale**: `FR-002` define explicitamente "ordenação apenas ao buscar do banco". O contrato `AssetTransactionRepository.getAllByHolding` não documenta ordenação, então fazemos o sort no caller para garantir o comportamento.
- **Alternatives considered**:
  - **Confiar no repositório/Room** — rejeitado: dependência implícita; explicitar a ordenação no UI/ViewModel é mais seguro e auto-documentável.
  - **Ordenar no Composable** — rejeitado: a regra é parte do contrato de estado, não da view; ordenar lá esconderia recomputos a cada recomposição.

---

## D6 — Adicionar/Salvar como botões abaixo da tabela (não no footer da tabela)

- **Decision**: O footer atual do `UiTableV3` que dispara `AddTransactionDraft` é removido. Em seu lugar, abaixo da `Column` da tabela, fica um `Row` com:
  - Botão **`OutlinedButton("Adicionar")`** (ou equivalente do design-system) à esquerda.
  - Botão **`Button("Salvar")`** alinhado ao padrão de `Actions(...)` em `AssetManagementScreen.kt` (`Arrangement.spacedBy(8.dp, Alignment.End)`), habilitado quando `state.isDirty && !state.isSaving`.
- **Rationale**: Cumpre `FR-006`, `FR-007`, `FR-010`, `FR-011`. Alinha-se visualmente ao bloco `Actions` do `AssetManagementDialog` (`User Story 5`).
- **Alternatives considered**:
  - **Manter "Adicionar" como footer da tabela** — rejeitado: o spec exige "abaixo da tabela", separado do conteúdo da tabela.
  - **Salvar como FAB ou ícone** — rejeitado: padrão visual do `AssetManagementDialog` é botão de texto alinhado à direita.

---

## D7 — Fecho do dialog sem confirmação

- **Decision**: O `IconButton` de fechar continua na top bar do `AppContentDialog` e chama o `onDismiss` recebido. Não há diálogo de confirmação ao fechar com draft pendente — todas as alterações em memória são descartadas.
- **Rationale**: `FR-013` + `Edge Case` explícito + paridade com `AssetManagementDialog`.
- **Alternatives considered**: diálogo de confirmação — rejeitado por escopo (`FR-016`).

---

## D8 — Testes

- **Decision**: Não há alterações em `:domain:usecases`; logo, princípio V não obriga novos testes de domínio. Para o ViewModel, novos testes unitários (jvmTest) são **opcionais** nesta feature e ficam ao critério do executor. Caso o módulo `:features:asset-management` venha a adotar `jvmTest` neste ciclo, os casos sugeridos são:
  - `isDirty == false` na carga inicial.
  - `isDirty == true` após editar, adicionar e remover (e três variações isoladas).
  - `isDirty == false` após reverter manualmente todas as alterações.
  - `onSave` chama `DeleteTransactionUseCase` apenas para IDs presentes na snapshot e ausentes da lista corrente.
  - `onSave` em falha mantém `transactions`/`initialSnapshot` inalterados e `isSaving == false`.
- **Rationale**: Os clarifications e FRs implicam contratos suficientemente verificáveis manualmente; a constitution exige testes em use cases (não na camada de apresentação).

---

## D9 — Verificação Gradle

- **Decision**: Após implementar, validar com `./gradlew :features:assetManagement:compileKotlinJvm` (ou o accessor exato exposto por `settings.gradle.kts`; vide `core/presentation/asset-management/build.gradle.kts`). Como `App.kt` em `:apps:umbrellaApp` referencia `TransactionFormDialog`, rodar também `./gradlew :apps:umbrellaApp:compileKotlinJvm` para garantir que o entrypoint público continua compatível.
- **Rationale**: Aderência ao fluxo de desenvolvimento da constitution (compilar JVM do módulo tocado e do shell que o consome).

---

## Conclusão

Todas as decisões necessárias estão fechadas. Nenhum bloqueio para Phase 1.
