# Research: Cores semânticas de status no tema v2

**Feature**: `012-status-theme-colors` | **Date**: 2026-05-29

## Decisões Técnicas

### 1. Lookup directo — sem resolvedor (revisto 2026-05-29)

**Decisão**: `MaterialTheme.statusColors(status: StatusKind): StatusColorRoles` — uma extensão sobre `CompositionLocal`; **sem** `AppStatusColors`, **sem** `SummaryCardStatusColors.resolve()`.

```kotlin
internal val LocalStatusColorRoles =
    compositionLocalOf<(StatusKind) -> StatusColorRoles> { error("AppThemeV2 required") }

@Composable
public fun MaterialTheme.statusColors(status: StatusKind): StatusColorRoles =
    LocalStatusColorRoles.current(status)
```

**Alternativas rejeitadas**: `AppStatusColors` wrapper, `resolve()` + `toStatusKind()`, `StatusColorPalette` intermédio.

---

### 1a. Enum único + typealias (revisto 2026-05-29)

**Decisão**: **`StatusKind`** é o único enum. Compat 011 via:

```kotlin
// summary/SummaryCard.kt ou ficheiro dedicado
public typealias SummaryCardStatus = StatusKind
```

**Removido**: enum `SummaryCardStatus` duplicado, `toStatusKind()`, ficheiro `SummaryCardStatusColors.kt`.

---

### 2. Nomenclatura M3 — oito papéis por estado (decisão do utilizador)

**Decisão**: 8 papéis M3 em `StatusColorRoles`; lookup via `MaterialTheme.statusColors(status)`.

```kotlin
@Immutable
public data class StatusColorRoles(/* 8 propriedades M3 */)

public enum class StatusKind { Default, Info, Warning, Positive, Negative }
```

**Cartão** — inline, sem DTO intermédio:

```kotlin
val roles = MaterialTheme.statusColors(status)
OutlinedCard(colors = … containerColor = roles.container …)
Text(…, color = roles.onContainer)
Text(…, color = roles.onFixedVariant) // título
// badge: roles.fixed, roles.onFixed
```

**Alternativas rejeitadas**: `AppStatusColors`, `StatusColorPalette`, `toSummaryCardPalette()`.

---

### 3. Contentor tintado no `SummaryCard` (clarificação Q1)

**Decisão**: **Todos** os status (incl. **Default**) usam `palette.container` e `palette.outline` no `OutlinedCard`. Default deixa de usar `CardDefaults.outlinedCardColors()` implícito — passa a usar a paleta fixa neutra (visualmente equivalente à 011 se calibrada).

**Rationale**: Comportamento uniforme; uma única via de cor (paleta do tema).

**Alternativas consideradas**:
- `ElevatedCard` sem outline — rejeitado (quebra perfil Outlined Card da 011).
- Fundo neutro + acento — rejeitado (clarificação Q1).

---

### 4. Paletas semânticas **independentes** do brand `ColorScheme` (decisão do utilizador)

**Decisão**: **Nenhum** status (incluindo **Default**) deriva de `primary*` / `secondary*` / `tertiary*` / `error*` nem de neutros dinâmicos do `ColorScheme`. **As cinco paletas** são **fixas** no pacote `theme/`, em variantes **light** e **dark**. Alterar primary, secondary, tertiary **ou** `surface`/`onSurface` do brand **não** altera cartões de status.

| Status | Matiz | Origem |
|--------|-------|--------|
| **Default** | Neutro | Paleta fixa neutra (`FixedStatusPalettes`) |
| **Info** | Azul | Paleta fixa |
| **Warning** | Âmbar | Paleta fixa |
| **Positive** | Verde | Paleta fixa |
| **Negative** | Vermelho | Paleta fixa |

**Default fixo**: valores neutros calibrados para **reproduzir** a aparência actual da 011 (equivalente a `surface` / `onSurface` / `outlineVariant` / `surfaceContainerHigh` do Expressive light/dark **no momento da implementação**) — mas **desacoplados** do `ColorScheme` da app daqui em diante.

**Estrutura de tokens** (por `StatusKind`, nomenclatura M3):

| Papel M3 | Propriedade | Uso genérico |
|----------|-------------|--------------|
| `{Status}` | `color` | Cor principal / acento forte |
| `On {Status}` | `onColor` | Conteúdo sobre cor principal |
| `{Status} Container` | `container` | Superfície tintada (cartão, banner) |
| `On {Status} Container` | `onContainer` | Texto/conteúdo sobre contentor |
| `{Status} Fixed` | `fixed` | Superfície fixa (badge, chip) |
| `{Status} Fixed Dim` | `fixedDim` | Variante mais suave de fixed |
| `On {Status} Fixed` | `onFixed` | Conteúdo sobre fixed |
| `On {Status} Fixed Variant` | `onFixedVariant` | Conteúdo secundário / borda |

Papéis **só do cartão** (`title`, `legend`, `badge*`) — mapeados em `summary/` a partir dos papéis acima (ver secção 2).

**Implementação** (ficheiro dedicado `theme/FixedStatusPalettes.kt`):

```kotlin
internal object FixedStatusPalettes {
    internal fun light(): FixedSet   // default + info + warning + positive + negative
    internal fun dark(): FixedSet
}

// AppThemeV2
val provider = if (darkTheme) FixedStatusPalettes.dark() else FixedStatusPalettes.light()
CompositionLocalProvider(LocalStatusColorRoles provides provider) { … }
```

Tons derivados de **escalas tonais M3** (blue / amber / green / red) ou valores `Color` nomeados **só em `theme/`** — não expostos na API do cartão. Ajuste fino de hex concentrado num único objecto por modo claro/escuro.

**Rationale**: Brand e neutros de app evoluem livremente; **todas** as paletas de status (incl. Default) comunicam aparência estável. Responde ao requisito: mudar secondary/primary/tertiary **ou** surface **não** altera cartões.

**Alternativas consideradas**:
- Mapeamento directo primary/secondary/tertiary/error — **rejeitado** (acoplamento brand ↔ status).
- Default dinâmico via `ColorScheme`, semânticos fixos — **rejeitado** (utilizador exige Default fixo também).
- Default também fixo — **aceite** (decisão actual).

**Validação**: Previews light/dark; SC-002; alterar `ColorScheme` (primary, secondary, tertiary, surface) e confirmar que **nenhuma** das cinco paletas muda; SC-006 — Default fixo calibrado igual à baseline 011.

---

### 5. Enum único — `StatusKind`

**Decisão**: Um enum em `theme/`; cartão expõe `typealias SummaryCardStatus = StatusKind` para compat 011.

---

### 6. Catálogo FR-010a

**Decisão**: `SummaryCardCatalogItem` ganha campo `status: SummaryCardStatus`; mapeamento estático conforme spec:

| Cartão | Status |
|--------|--------|
| Valor Anterior | `Info` |
| Valor Atual, Aportes | `Default` |
| Retiradas, Crescimento | `Negative` |
| % Crescimento | `Warning` |
| Lucro, Valorização | `Positive` |

Preview `SummaryCard_Catalog8_preview` passa `item.status`; remover ou excluir do preview o item extra **"Edge Case"** (9.º entry actual — fora FR-008).

---

### 7. Preview de swatches (SC-004)

**Decisão**: Ficheiro `theme/StatusColorSwatches.kt` com composable interno `StatusColorSwatchesContent` e `@Preview` **private** `StatusColorSwatches_preview` (light + dark) — grelha 5×8 papéis ou 5 linhas com chips de cor + rótulo do papel.

**Rationale**: Constituição — previews no mesmo ficheiro do composable; segundo consumidor genérico sem novo componente de produção.

**Alternativas consideradas**:
- Preview em `SummaryCard.kt` — rejeitado (domínio tema vs summary).
- Componente público `StatusSwatches` — rejeitado (YAGNI; preview private basta).

---

### 8. Ficheiros e impacto

**Novos**:
- `theme/StatusColors.kt` — `StatusKind`, `StatusColorRoles`, `LocalStatusColorRoles`, `MaterialTheme.statusColors(status)`
- `theme/FixedStatusPalettes.kt` — internal
- `theme/StatusColorSwatches.kt` — preview

**Alterados**:
- `theme/AppThemeV2.kt` — provider `LocalStatusColorRoles`
- `summary/SummaryCard.kt` — lookup inline + `typealias SummaryCardStatus = StatusKind`
- `summary/SummaryCardCatalog.kt` — campo `status`

**Removidos**:
- `summary/SummaryCardStatusColors.kt`

**Sem acoplamento brand/status**: `ExpressiveColorScheme.kt` evolui livremente — **zero** efeito nas cinco paletas de status.

---

### 9. Verificação

**Comando**: `./gradlew :features:design-system-v2:compileKotlinJvm`

**Previews manuais**:
- `StatusColorSwatches_preview` (light/dark)
- `SummaryCard_Catalog8_preview` com status FR-010a
- `SummaryCardPreviewLight` — regressão Default nos itens neutros

**Fora do escopo**: Histórico, testes unitários JVM (sem use cases), Dynamic Color.
