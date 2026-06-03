# Research: Filtros da carteira no histórico

**Feature**: `015-history-wallet-filters` | **Date**: 2026-06-02

**Diretriz**: menor esforço, menos código, mais simplicidade, legibilidade.

---

## R1 — Onde vive a lógica OR/AND?

**Decision**: Uma função pura `matchesWalletHistoryFilter` em `:domain:usecases` (ficheiro dedicado pequeno), chamada por `GetHistoryTableDataUseCase` após construir cada linha (ou sobre `HoldingHistoryResult` antes do `mapNotNull`).

**Rationale**: FR-007 exige ponto único testável sem duplicar regras na UI. Um use case novo (`FilterWalletHistoryUseCase`) acrescenta Koin, ficheiro e indireção sem ganho. Extrair só a função mantém o pipeline actual legível.

**Alternatives considered**:
- **Novo use case** — rejeitado (YAGNI; mais wiring).
- **Filtrar na UI** — viola FR-007 e constituição (regra de negócio fora do domínio).
- **Filtrar só em `HistoryViewModel`** — rejeitado (não testável como use case; VM inchado).

---

## R2 — Modelo de critérios: domínio vs apresentação

**Decision**: `WalletHistoryFilterCriteria` (data class em `:domain:usecases`) com `Set` dos enums de `:domain:entity` já usados em `GetHistoryTableDataUseCase`. Mapeamento `WalletFiltersUiState → WalletHistoryFilterCriteria` numa extensão `internal` no pacote `history` do composeApp (único ficheiro ~30 linhas).

**Rationale**: Evita dependência domain→features. Reutiliza `InvestmentCategory`, `Liquidity`, subtipos via `WalletFilterSubtype` mapeados para tipos entity no mapper (já existe lógica paralela em `WalletFilterHoldingFacetMappers`).

**Alternatives considered**:
- **Reutilizar `WalletFiltersUiState` no Param do use case** — rejeitado (acoplamento presentation→domain invertido).
- **Duplicar sealed `AssetClassKind` do WIP `WalletFiltersDerivation.kt`** — rejeitado (código morto/incompatível).

---

## R3 — Derivação de opções do painel

**Decision**: Implementar `deriveWalletFiltersPanelOptions(facets: List<WalletFilterHoldingFacet>): WalletFiltersPanelOptions` em `WalletFilters.kt` (ou ficheiro único `WalletFiltersDerivation.kt` **sem** tipos duplicados). **Eliminar** tipos WIP (`AssetClassKind`, `SubtypeKind`, `FilterOptionId`).

**Rationale**: O painel já consome `WalletFiltersPanelOptions` aninhado; derivação é O(n) sobre facetas já calculadas para o mês. Uma função legível > camada extra de `WalletFiltersDerivedUiModel`.

**Alternatives considered**:
- **`WalletFiltersSlotGrid.kt`** — não necessário para 015; layout já está em `WalletFiltersPanel.kt`.
- **Catálogo estático no histórico** — rejeitado (viola FR-012).

---

## R4 — Estado por defeito e reset

**Decision**: `WalletFiltersUiState.defaultForHistory()` e `reset()` devolvem o mesmo snapshot: `selectedSettled = { YesOrNo.NO }`, restantes vazios, `maturitySelection = null`. Constante partilhada `HistoryWalletFiltersDefaults` no pacote `history` ou em `WalletFilters.kt`.

**Rationale**: Spec 015; uma função evita divergência entre `initial()`, reset de período e botão Resetar.

**Alternatives considered**:
- **Manter `initial()` vazio da 014** — rejeitado (quebra US1).
- **Estado só no ViewModel sem helper** — rejeitado (duplicação em 3 sítios).

---

## R5 — Sumário alinhado à tabela filtrada

**Decision**: Calcular aportes/resgates/valorização a partir das **linhas** `HistoryTableData` já filtradas (`totalContributions`, `totalWithdrawals`, património) — **não** chamar `GetTransactionsUseCase` para o agregado do sumário quando filtros de carteira estão activos (ou sempre, para simplicidade).

**Rationale**: FR-010a; menos código que filtrar transações por holding; campos já existem por linha no use case.

**Alternatives considered**:
- **Filtrar `GetTransactionsUseCase` por holdings visíveis** — mais joins e estado; rejeitado para MVP.
- **Segundo use case de sumário** — YAGNI.

---

## R6 — Remoção de filtros legados e exclusão implícita

**Decision**:
- Remover `category`, `liquidity`, `goal` de `HistoryState` / intents / `GetHistoryTableDataUseCase.Param`.
- Remover filtro `goal` do histórico (spec: meta financeira fora).
- Remover `if (previousValue == 0 && currentValue == 0) return null` — substituir por critério **Liquidados** no `matchesWalletHistoryFilter`.
- Manter **corretora** como está (fora do painel).

**Rationale**: FR-008, FR-002a; uma superfície de filtragem.

---

## R7 — Grupo saturado (Sim+Não) e RF-only

**Decision**: Na função de match, tratar `selectedB3.size == 2` e `selectedSettled.size == 2` como grupo inactivo; liquidez/vencimento só avaliam `FixedIncomeAsset` (RV/fundos passam sempre nesses grupos).

**Rationale**: Spec clarifications; lógica localizada num sítio, fácil de testar com tabela de casos.

---

## R8 — Testes e validação

**Decision**: Ficheiro `WalletHistoryFilterTest.kt` em `:domain:usecases` com casos **T1–T9** do contrato (+ OR subtipo na mesma suite); **sem** `./gradlew` automático (princípio IX). **T9** cobre critérios de `defaultForHistory()`/reset (FR-014 «reset lógico equivalente»).

**Rationale**: FR-014; função pura = testes rápidos sem mocks pesados.

---

## R9 — Paralelismo / subagentes na implementação

**Decision**: Três subagentes em ondas (ver `plan.md` § Estratégia de subagentes): Domínio+testes | Apresentação estado+derivação | Integração VM+ecrã+sumário.

**Rationale**: Fronteiras claras, diffs pequenos, revisão independente; evita um agente monolítico reescrever tudo.

**Alternatives considered**:
- **Um único agente end-to-end** — mais risco de regressão e ficheiros gigantes.
