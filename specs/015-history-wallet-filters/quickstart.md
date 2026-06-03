# Quickstart: Filtros da carteira no histórico

**Feature**: `015-history-wallet-filters`

## Pré-requisitos

- Branch `015-history-wallet-filters`
- Feature **014** entregue (`WalletFiltersPanel`, `filter/*` no design-system-v2)
- `:domain:usecases` + `:features:composeApp`

## Ficheiros tocados (mínimo)

```text
core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/
├── screens/GetHistoryTableDataUseCase.kt    # Param + aplicar filter
└── screens/WalletHistoryFilter.kt           # NOVO: criteria + matches + candidate

core/domain/usecases/src/jvmTest/kotlin/.../
└── WalletHistoryFilterTest.kt               # T1–T9 + OR subtipo (contrato)

core/presentation/composeApp/.../history/
├── HistoryState.kt
├── HistoryViewModel.kt
└── AssetHistoryScreen.kt                    # remover legados; wire painel

core/presentation/composeApp/.../walletfilters/
├── WalletFilters.kt                         # defaultForHistory + derive
└── WalletFiltersDerivation.kt             # limpar WIP OU fundir em WalletFilters.kt
```

**Não criar**: `WalletFiltersSlotGrid.kt`, novo use case separado, módulos novos.

## Ordem de implementação (agentes)

1. **Subagente Domínio** (T003–T008) — `WalletHistoryFilter.kt` + testes T1–T9 + `GetHistoryTableDataUseCase`.
2. **Subagente Filtros UI** (T009–T013) — `defaultForHistory`, `derive` (FR-018), limpar WIP — **paralelo a 1**.
3. **Subagente Histórico** (T014–T027) — MVP US1 inclui remover legados + derivar opções; depois US2–US5.

## Compilar (sob pedido do utilizador)

```bash
./gradlew :domain:usecases:jvmTest :features:composeApp:compileKotlinJvm
```

## Checklist de entrega

- [ ] Painel único; sem segmentados categoria/liquidez/meta
- [ ] Default «Não liquidado»; reset e mudança de período iguais ao default
- [ ] OR intra-grupo, AND inter-grupo; RF-only liquidez/vencimento
- [ ] Sem exclusão implícita património zero
- [ ] Sumário = soma das linhas visíveis
- [ ] Suite `WalletHistoryFilterTest` com T1–T9 do contrato (+ OR subtipo)
- [ ] SC-002 validado manualmente (tabela &lt; 1s percepção)
- [ ] Estado filtros só no `HistoryViewModel` (`StateFlow`)

## Próximo passo

`/speckit.tasks` — gerar `tasks.md` com ondas `[P]` alinhadas aos subagentes acima.
