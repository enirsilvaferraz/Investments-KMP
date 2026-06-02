# Contract: WalletFiltersPanel (composeApp)

**Feature**: `014-wallet-filters` | **Phase**: 1 | **Date**: 2026-06-02

---

## Visão geral

```
:features:composeApp  →  AppThemeV2 { WalletFiltersPanel(...) }
```

**Sem** registo em `umbrellaApp` nesta entrega. **Sem** alterações em `AssetHistoryScreen`.

**Trilhas E1–E5** implementam secções internas; integração final em **WalletFiltersPanel**.

---

## API pública (módulo composeApp)

```kotlin
@Composable
internal fun WalletFiltersPanel(
    portfolio: List<WalletFilterPortfolioItem>,
    modifier: Modifier = Modifier,
    // estado opcional hoisted para previews avançados:
    state: WalletFiltersUiState? = null,
    onStateChange: ((WalletFiltersUiState) -> Unit)? = null,
)
```

| Parâmetro | Contrato |
|-----------|----------|
| `portfolio` | Fonte para `deriveFilterOptions`; actualizações recompõem secções (FR-018) |
| `state` / `onStateChange` | Se `null`, estado interno `remember`; se fornecidos, hoisting para testes |

**Visibilidade**: `internal` — consumo via previews e futura integração Histórico no mesmo módulo.

---

## Estrutura visual (obrigatória)

| Zona | Componentes |
|------|-------------|
| Contentor | `OutlinedCard` — FR-M3-001 |
| Cabeçalho | Título `titleMedium`, ícone `filter_list` decorativo, **Resetar** `TextButton` cor `error` |
| Corpo | Secções visíveis apenas se `derived.visibleSections` contém o grupo |
| Divisórias | `HorizontalDivider` entre secções renderizadas |

Ordem fixa de secções: **Classe** → **Subtipos** → **Liquidez** → (**B3** | **Liquidados** em row se largo) → **Vence até**.

---

## Secções (comportamento)

### Classe

- `FilterToggleGroup` size `Standard`.
- Ícone secção: `layers`.
- Multi-selecção.

### Subtipos por classe

- Visível só com ≥1 classe activa e subcartão não vazio.
- Por classe: `OutlinedCard` + título + `FilterToggleGroup` `Compact`.
- Ordem: Renda Fixa → Renda Variável → Fundos (apenas activas).

### Liquidez / B3 / Liquidados

- `FilterToggleGroup` `Compact`.
- B3 + Liquidados: layout duas colunas em largura expandida.

### Vence até

- `MaturityFilterDropdown` (composeApp): largura total, «Qualquer vencimento» + meses derivados.
- Selecção **única**; não usar `FilterToggleGroup`.

---

## Reset

`onReset` limpa todos os `Set`, `MaturitySelection.Any`, fecha dropdown se aberto.

---

## Previews (obrigatórios)

| Preview | Conteúdo |
|---------|----------|
| `WalletFiltersPanelPreviewFull` | `WalletFiltersPreviewCatalog.fullPortfolio` — mockup completo |
| `WalletFiltersPanelPreviewDynamic` | Subconjunto (só RF + 2 vencimentos) — omissão de secções |
| Tema | `AppThemeV2` light + dark (`@Preview` night) |

Todos **`private`** no ficheiro do painel ou `WalletFiltersPreviews.kt`.

---

## Dependências de módulo

- `implementation(projects.features.designSystemV2)` — já presente.
- **Não** adicionar `implementation(projects.features.designSystem)` para este painel (evitar `SegmentedControl` v1).

---

## Integração futura (fora do contract)

- `AssetHistoryScreen` injeta `portfolio` real.
- ViewModel expõe `StateFlow<WalletFiltersUiState>`.
