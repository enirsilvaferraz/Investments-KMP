# Research: Cartões de resumo da carteira

**Feature**: `011-summary-cards` | **Date**: 2026-05-29

## Decisões Técnicas

### 1. Módulo `design-system-v2` vs estender `design-system`

**Decisão**: Criar `:features:design-system-v2` em `core/presentation/design-system-v2/`, pacote `com.eferraz.design_system_v2`.

**Rationale**: Diretriz do plano e do utilizador; permite evoluir componentes alinhados ao protótipo React sem regressões em `UiTableV3`, inputs e diálogos do v1. O v2 inicia só com `SummaryCard`; migrações futuras podem mover outros primitives.

**Alternativas consideradas**:
- Estender `:features:design-system` — rejeitado nesta feature (pedido explícito por v2).
- Colocar em `composeApp` — rejeitado (viola escopo “só design system” e Clean Architecture).

---

### 2. Independência total de `:features:design-system`

**Decisão**: `design-system-v2` **não** declara `implementation` nem `api` de `:features:design-system`. Tema próprio `AppThemeV2` + `ExpressiveColorScheme.kt` + `Shapes.kt` dentro do v2; previews usam `AppThemeV2 { }`.

**Rationale**: Pedido do utilizador — stack completamente nova, sem arrastar `com.eferraz.design_system`, paletas antigas nem acoplamento de release do v1. A spec (FR-002) exige tokens M3, não “reutilizar o módulo v1”.

**Alternativas consideradas**:
- Depender de v1 só para cores — rejeitado (acoplamento; contradiz objetivo “completamente novo”).
- Partilhar módulo `:features:design-tokens` comum — rejeitado nesta entrega (YAGNI); pode surgir depois se v1 e v2 convergirem.

---

### 3. Protótipo — referência qualitativa (não pixel-perfect)

**Decisão**: `prototype/src/App.tsx` valida **intenção**: rótulo pequeno bold uppercase → valor grande → legenda menor; título+ícone na mesma linha; cartão compacto.

**Não** copiar medidas do mock (`110px`, `18px` radius, `14px` padding, `10/15/9 sp`, cores Tailwind, hover, fontes web).

---

### 3a. Layout e espaçamento — tokens M3 Expressive

| Aspecto | Abordagem M3 |
|---------|----------------|
| Contentor | `OutlinedCard` (`enabled = false`) + `CardDefaults.outlinedCardColors()` + `CardDefaults.outlinedCardBorder()` |
| Forma | `MaterialTheme.shapes.medium` (**12 dp** — token `--md-sys-shape-card`) |
| Padding | `16.dp` (padrão cartão M3) |
| Espaçamento | `8.dp` entre blocos (grade 4/8 dp) |
| Ícone | `24.dp` em slot decorativo **40.dp** (`CircleShape` / shape **full**) |
| Altura uniforme | `heightIn(min = …)` de line heights + padding — **não** `110.dp` |
| Preview grade | `spacedBy(8.dp)` |

`SummaryCardDefaults` lê `shapes`/`spacing` do tema — sem `SummaryCardDimensions` com px do Tailwind.

---

### 3b. Tipografia — `MaterialTheme.typography` exclusivo

| Papel | Estilo M3 | Ajuste |
|-------|-----------|--------|
| Título | `labelSmall` | Bold, uppercase |
| Valor | `titleLarge` | 1 linha, ellipsis |
| Legenda | `bodySmall` | slot = `lineHeight` |

**Proibido** `fontSize` avulso. `Typography()` padrão M3 no `AppThemeV2`.

---

### 4. Cores — apenas `Default`; M3 Expressive; extensível para status futuros

**Decisão (escopo reduzido)**:

- **Nesta entrega**: só o status **`Default`** tem cores implementadas.
- **Fonte de cor**: `MaterialTheme.colorScheme` do tema **Material Design 3 Expressive** em `AppThemeV2` — **sem** paletas custom hex por status (info/warning/error/success **fora do escopo**).
- **Extensibilidade**: `SummaryCardStatus` + `SummaryCardStatusColors.resolve(status, colorScheme)` com `when` exaustivo só em `Default`; novos status = novo valor no enum + novo resolvedor (ou ramo) em feature futura.

#### Mapeamento `Default` → Outlined Card + conteúdo (M3 Components)

| Papel no cartão | Token / API |
|-----------------|-------------|
| Fundo do cartão | `surface` via `CardDefaults.outlinedCardColors()` |
| Borda do cartão | `outlineVariant` via `CardDefaults.outlinedCardBorder()` |
| Valor | `onSurface` |
| Título / legenda | `onSurfaceVariant` |
| Badge (slot ícone decorativo) | `surfaceContainerHigh` + ícone `onSurfaceVariant`; forma **full** (círculo); **não** usar componente `Badge` de notificação M3 |
| Elevação | **0** (Outlined Card — sem sombra no repouso) |

Factories **`lightExpressiveColorScheme()`** / **`darkExpressiveColorScheme()`** em `theme/ExpressiveColorScheme.kt` com `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` — paleta tonal Expressive oficial Material3, não Tailwind do protótipo.

#### Estrutura Kotlin (extensível)

```kotlin
public enum class SummaryCardStatus {
    Default,
    // Info, Warning, Error, Success — features futuras
}

@Immutable
internal data class SummaryCardColors(
    val container: Color,
    val onContainer: Color,
    val title: Color,
    val legend: Color,
    val outline: Color,
    val badgeContainer: Color,
    val badgeIcon: Color,
    val badgeOutline: Color,
)

internal object SummaryCardStatusColors {
    internal fun resolve(
        status: SummaryCardStatus,
        colorScheme: ColorScheme,
    ): SummaryCardColors = when (status) {
        SummaryCardStatus.Default -> defaultColors(colorScheme)
        // SummaryCardStatus.Info -> infoColors(colorScheme)  // futuro
    }

    private fun defaultColors(cs: ColorScheme) = SummaryCardColors(
        container = cs.surface,           // Outlined Card — alinhado a CardDefaults
        onContainer = cs.onSurface,
        title = cs.onSurfaceVariant,
        legend = cs.onSurfaceVariant,
        outline = cs.outlineVariant,
        badgeContainer = cs.surfaceContainerHigh,
        badgeIcon = cs.onSurfaceVariant,
        badgeOutline = cs.outlineVariant,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun AppThemeV2(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkExpressiveColorScheme() else lightExpressiveColorScheme(),
        typography = AppTypographyV2,
        shapes = AppShapesV2,
        content = content,
    )
}

// No SummaryCard:
OutlinedCard(
    enabled = false,
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.outlinedCardColors(),
    border = CardDefaults.outlinedCardBorder(),
) { /* conteúdo; cores de texto via resolve */ }
```

**Removido do escopo**: `AppColorSchemeV2` com cinco famílias; `SummaryCardColorTokens` com hex do protótipo por status; previews que alternam cinco paletas.

**Rationale**: Entrega incremental; M3 Expressive para cromia; protótipo só inspira hierarquia e composição.

**Alternativas consideradas**:
- Cinco paletas custom nesta feature — rejeitado (redução de escopo).
- `AppColorSchemeV2` paralelo — rejeitado (desnecessário com um só status; `ColorScheme` Expressive basta).

**Acessibilidade**: Contraste **WCAG AA** (≥ 4,5:1) para `onSurface` / `onSurfaceVariant` sobre `surface` e badge sobre `surfaceContainerHigh` em light e dark Expressive.

---

### 4a. Tema M3 Expressive — `AppThemeV2`

| Pilar Expressive | Implementação v2 |
|------------------|------------------|
| **Cor** | `lightExpressiveColorScheme()` / `darkExpressiveColorScheme()` |
| **Shape** | `AppShapesV2` em `theme/Shapes.kt` — escala Expressive (medium **12 dp** cartão; large **20 dp**; extraLarge **32 dp**; badge = `CircleShape`) |
| **Tipografia** | `Typography()` Material3 (type scale M3) |
| **Motion** | **Não** no `SummaryCard` (estático — FR-007); tema preparado para componentes interativos futuros |
| **Personalização** | Dynamic Color do sistema fica para integração na app shell; v2 usa schemes Expressive estáticos |

```kotlin
// theme/Shapes.kt — valores Expressive (dp)
val AppShapesV2 = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),   // cartão SummaryCard
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
```

---

### 5. Layout com slots reservados (legenda e ícone ausentes)

**Decisão**: `Column`/`Row` com `heightIn(min = …)` calculado a partir de métricas tipográficas M3 + `SummaryCardDefaults.verticalSpacing`; header `Row` com badge slot fixo (tamanho ícone M3); legenda com slot = `bodySmall.lineHeight`.

**Rationale**: FR-004b/FR-004c e SC-005b exigem zero reflow quando opcionais omitidos.

**Alternativas consideradas**:
- `Column` condicional sem spacer — rejeitado (cartão encolhe ou valor sobe).

---

### 6. Interação, elevação e motion Expressive vs protótipo

**Decisão**: **Não** replicar `hover:scale`, `hover:shadow-md`, `transition-all` do protótipo. **Outlined Card** = elevação **0**, sem sombra animada. Motion Expressive (springs, morphing) **não** se aplica a este componente somente leitura.

**Rationale**: Spec FR-006/FR-007; aderência ao perfil **Outlined Card** M3 Components; exceção documentada à diretriz Expressive de motion.

**Alternativas consideradas**:
- `ElevatedCard` com `surfaceContainerLow` — rejeitado (diferente semântica; Outlined é neutro e alinhado ao mock sem hover).
- Paridade total com hover — rejeitado (fora do contrato KMP).

---

### 7. Ícones Material (FR-008)

**Decisão**: Parâmetro `icon: ImageVector?`; dependência `libs.compose.material.icons.extended` no v2. Mapeamento catálogo preview:

| # | Título | `ImageVector` (extended) |
|---|--------|--------------------------|
| 1 | Valor Anterior | `Icons.Outlined.Work` ou `BusinessCenter` (maleta) |
| 2 | Valor Atual | `Icons.Outlined.AccountBalanceWallet` |
| 3 | Aportes | `Icons.Outlined.Add` |
| 4 | Retiradas | `Icons.Outlined.Close` |
| 5 | Crescimento | `Icons.Outlined.Layers` |
| 6 | % Crescimento | `Icons.Outlined.BarChart` |
| 7 | Lucro | `Icons.Outlined.TrendingUp` |
| 8 | Valorização | `Icons.Outlined.ShowChart` |

**Rationale**: Lucide no protótipo → Material Symbols na app; semanticamente equivalente.

**Alternativas consideradas**:
- Drawable multiplataforma — rejeitado (spec pede Material Design).

---

### 8. Container — `OutlinedCard` M3 Expressive

**Decisão**: `OutlinedCard` não clicável; `shape = MaterialTheme.shapes.medium`; `colors = CardDefaults.outlinedCardColors()`; `border = CardDefaults.outlinedCardBorder()`; conteúdo interno com cores de texto do resolvedor.

**Rationale**: Perfil oficial **Outlined Card** (surface + outlineVariant, elevação 0); máximo de componente stock Material3.

---

### 8a. Checklist de aderência M3 Expressive (esta feature)

| Critério | Status |
|----------|--------|
| ColorScheme Expressive light/dark | ✅ planejado |
| Shapes medium 12 dp em cartão | ✅ planejado |
| Outlined Card + CardDefaults | ✅ planejado |
| Tipografia type scale M3 | ✅ planejado |
| Espaçamento 4/8 dp | ✅ planejado |
| Motion / springs no cartão | ➖ N/A (estático) |
| Dynamic Color sistema | ➖ fora do escopo v2 |
| Formas decorativas Expressive (flower, burst…) | ➖ N/A (clareza > delight) |
| WCAG AA contraste | ✅ validar no preview light/dark |

---

### 9. Acessibilidade

**Decisão**: `Icon` com `contentDescription = null` e `modifier = Modifier.semantics { invisibleToUser() }` (ou equivalente); `mergeDescendants` no cartão desativado para não anunciar badge.

**Rationale**: FR-015 / SC-007.

---

### 10. Registo Gradle e verificação

**Decisão**: Entrada em `settings.gradle.kts` espelhando `design-system`; accessor `projects.features.designSystemV2`.

**Comando**: `./gradlew :features:design-system-v2:compileKotlinJvm`

**Alternativas**: Não registrar em `umbrellaApp` nesta feature (DS puro, previews locais).
