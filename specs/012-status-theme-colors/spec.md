# Feature Specification: Cores semânticas de status no tema v2

**Feature Branch**: `012-status-theme-colors`

**Created**: 2026-05-29

**Status**: Draft

**Input**: User description: "Baseando-se nas cores Default do core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/summary/SummaryCard.kt crie cores no mesmo padrao para os status info (azul), warning (ambar), positive (verde), negative (vermelho). Quero que as cores fiquem disponiveis no tema do design system v2 para que eu possa reutilizar em outros componentes."

## Clarifications

### Session 2026-05-29

- Q: Para status semânticos, o fundo do contentor deve receber tinta semântica ou permanecer neutro como Default? → A: **Contentor tintado** — fundo do contentor usa superfície semântica; textos, badge e contorno derivam da mesma paleta semântica.
- Q: Como o preview/catálogo deve demonstrar os novos status em relação aos 8 cartões Default existentes? → A: **Remapear subset** — alguns dos 8 cartões passam a status semântico; os restantes permanecem **Default**.
- Q: Qual mapeamento estático cartão → status no catálogo dos 8 modelos? → A: **Por significado do mock** — Default: Valor Atual, Aportes · info: Valor Anterior · warning: % Crescimento · positive: Lucro, Valorização · negative: Retiradas, Crescimento.
- Q: Qual segundo contexto de UI para SC-004 (reutilização das paletas)? → A: **Preview de swatches** — grelha genérica dos 5 status + cartão de resumo como primeiro consumidor.
- Q: Como outros componentes acedem às paletas semânticas no tema v2? → A: **Lookup directo** — `MaterialTheme.statusColors(status)`; cada consumidor aplica `StatusColorRoles` inline (sem resolvedor intermédio).
- Q: O pacote `theme` deve conhecer tipos de `summary`? → A: **Não** — `theme/` autocontido; cartão importa `StatusKind` do tema (via `typealias SummaryCardStatus = StatusKind`).
- Q: Simplificar classes? → A: **Mínimo** — 3 tipos (`StatusKind`, `StatusColorRoles`, `FixedStatusPalettes` internal) + `MaterialTheme.statusColors(status)`; **sem** `AppStatusColors`, `StatusColorPalette`, `SummaryCardStatusColors`, `resolve()`.
- Q: Status semânticos devem seguir primary/secondary/tertiary do brand? → A: **Não** — **todas** as paletas (incl. **Default**) fixas em `FixedStatusPalettes`; independentes do `ColorScheme`.
- Q: Default também deve ser fixo? → A: **Sim** — neutros fixos calibrados à baseline 011; não ler `surface`/`onSurface` em runtime.
- Q: Onde vive `StatusColorPalette`? → A: **Removido** — mapeamento cartão inline em `SummaryCard.kt` a partir de `StatusColorRoles` (sem DTO intermédio).
- Q: Papéis de badge/título pertencem ao tema? → A: **Não** — tema expõe **8 papéis M3** genéricos; `title`, `legend`, `badge*` mapeados **inline** em `summary/SummaryCard.kt`.
- Q: Qual nomenclatura de tokens no tema? → A: **M3 por estado** — cada `StatusKind` expõe **8 papéis** alinhados a Primary/Secondary/Tertiary do Material 3: `{Status}`, `On {Status}`, `{Status} Container`, `On {Status} Container`, `{Status} Fixed`, `{Status} Fixed Dim`, `On {Status} Fixed`, `On {Status} Fixed Variant`. API via `StatusColorRoles` + `MaterialTheme.statusColors(status)`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consumir cores semânticas a partir do tema (Priority: P1)

Quem desenvolve componentes obtém **8 papéis M3** via `MaterialTheme.statusColors(status: StatusKind)` — uma chamada, sem resolvedores. O **SummaryCard** aplica `StatusColorRoles` inline no composable.

**Why this priority**: É o entregável central — tokens reutilizáveis no tema, independentes de um único componente.

**Independent Test**: Aplicar o tema v2 a um componente de demonstração genérico (não necessariamente cartão de resumo) e alternar entre os quatro status; verificar que todos os papéis cromáticos resolvem a partir do tema, sem cores hardcoded na API do componente.

**Acceptance Scenarios**:

1. **Given** o tema v2 ativo, **When** um consumidor chama `MaterialTheme.statusColors(StatusKind.Info)`, **Then** obtém `StatusColorRoles` com matiz **azul** e 8 papéis M3.
2. **Given** o tema v2 ativo, **When** um consumidor chama `statusColors` para **Warning**, **Positive** ou **Negative**, **Then** obtém papéis com matizes **âmbar**, **verde** e **vermelho** respectivamente.
3. **Given** dois componentes distintos no mesmo ecrã, **When** ambos usam o mesmo status semântico, **Then** exibem a **mesma** aparência cromática derivada do tema.

---

### User Story 2 - Diferenciar significado visual por status (Priority: P1)

Quem revê ou usa a aplicação distingue rapidamente conteúdo informativo, de alerta, favorável ou desfavorável pela cor dominante de cada status, mantendo hierarquia legível (título, valor, legenda, badge).

**Why this priority**: O valor de negócio das cores semânticas é comunicar significado sem depender só de texto.

**Independent Test**: Apresentar quatro instâncias lado a lado (uma por status) com o mesmo texto e ícone; revisor identifica o significado de cada uma em menos de 5 segundos por item.

**Acceptance Scenarios**:

1. **Given** quatro blocos idênticos em conteúdo textual, **When** cada um usa um status diferente (**info**, **warning**, **positive**, **negative**), **Then** são **visualmente distinguíveis** pela matiz dominante sem confundir um status com outro.
2. **Given** status **positive** ou **negative**, **When** o valor exibido contém sinal numérico no texto (ex.: "+R$ 940,05" ou "-R$ 73.375,43"), **Then** a cor do status **reforça** o significado sem alterar o texto fornecido pelo integrador.
3. **Given** status **Default** e qualquer status semântico, **When** comparados, **Then** **Default** permanece **neutro** (contentor sem tinta semântica), enquanto os demais exibem **fundo de contentor tintado** na matiz do status — preservando o comportamento já entregue na feature 011 para **Default**.

---

### User Story 3 - Validar paletas em tema claro e escuro (Priority: P2)

Quem usa a aplicação em modo claro ou escuro vê textos e badges legíveis em **todos** os status semânticos, com contraste adequado e sem “lavar” ou saturar demais a matiz.

**Why this priority**: Tokens de tema só são reutilizáveis se funcionarem nos dois modos de aparência.

**Independent Test**: Alternar tema claro/escuro no preview do design system e inspecionar os quatro status; confirmar legibilidade de título, valor, legenda e ícone do badge.

**Acceptance Scenarios**:

1. **Given** tema claro, **When** qualquer status semântico é aplicado, **Then** texto sobre fundo do contentor e ícone sobre fundo do badge permanecem **legíveis**.
2. **Given** tema escuro, **When** o mesmo status é aplicado, **Then** a matiz semântica permanece **reconhecível** e o contraste mínimo entre texto e fundo é **equivalente** ao exigido para **Default**.
3. **Given** alternância claro ↔ escuro, **When** o utilizador compara o mesmo status, **Then** a **identidade** do status (info / warning / positive / negative) é preservada — apenas luminosidade e contraste adaptam-se ao modo.

---

### User Story 5 - Validar reutilização via preview de swatches (Priority: P2)

Quem desenvolve outros componentes consulta um **preview de swatches** no design system que exibe, para cada status, todos os papéis cromáticos da paleta — comprovando que as cores vivem no **tema** e não só no cartão de resumo.

**Why this priority**: Satisfaz SC-004 com contexto genérico reutilizável, sem exigir novo componente de produção.

**Independent Test**: Abrir preview de swatches; confirmar grelha com 5 status (Default + quatro semânticos) e papéis cromáticos nomeados; comparar paleta **info** com a usada num cartão **info** — mesma origem no tema.

**Acceptance Scenarios**:

1. **Given** o preview de swatches no design system, **When** revisto, **Then** exibe **cinco status**, cada um com os **oito papéis M3** do tema (cor principal, on, container, onContainer, fixed, fixedDim, onFixed, onFixedVariant).
2. **Given** swatch **info** e cartão de resumo com status **info**, **When** comparados, **Then** partilham a **mesma** paleta derivada do tema v2 — **0** definições de cor duplicadas.

---

### User Story 4 - Estender cartão de resumo e catálogo (Priority: P2)

Quem mantém o componente de cartão de resumo (feature 011) passa a selecionar **info**, **warning**, **positive** ou **negative** além de **Default**, com cores resolvidas exclusivamente via tema v2. O catálogo/preview do design system demonstra exemplos representativos de cada status.

**Why this priority**: Confirma integração real dos tokens e documenta uso canónico, sem ser o único consumidor.

**Independent Test**: Renderizar cartões de resumo com cada status no preview; confirmar que a API `status` aceita os novos valores e que nenhum parâmetro de cor avulso é necessário.

**Acceptance Scenarios**:

1. **Given** cartão de resumo com `status` **info**, **warning**, **positive** ou **negative**, **When** renderizado dentro do tema v2, **Then** usa a paleta correspondente do tema — **não** cores definidas localmente no componente.
2. **Given** o catálogo de cartões de resumo (8 modelos FR-008 da feature 011), **When** revisto, **Then** **alguns cartões** usam status semântico e **os demais** permanecem **Default** — cobrindo **pelo menos um exemplo** de cada status (**info**, **warning**, **positive**, **negative**) no conjunto, com dados estáticos do mock.
3. **Given** integração futura em telas de produção, **When** a tela escolhe o status por métrica, **Then** a decisão permanece na camada de apresentação da feature — **sem** regra de negócio centralizada nesta entrega.

---

### Edge Cases

- **Status desconhecido ou não suportado**: Consumidores internos do design system devem tratar apenas os cinco valores definidos (**Default**, **info**, **warning**, **positive**, **negative**); valores inválidos são erro de integração, não cenário de utilizador final.
- **Texto longo ou truncado**: Cores semânticas **não** alteram comportamento de truncamento nem layout — apenas aparência cromática.
- **Ícone ou legenda ausentes**: Slots reservados do cartão de resumo mantêm-se; cores do badge aplicam-se apenas quando o ícone está presente.
- **Daltonismo**: Matizes devem ser distinguíveis não só por hue, mas por luminosidade/contraste relativa entre contentor e acentos (ex.: badge vs. fundo).
- **Combinação Default + semântico na mesma grade**: Cartões adjacentes com status diferentes não devem “vazar” cores entre si — cada instância resolve sua paleta isoladamente a partir do tema.
- **Reutilização fora do cartão**: Outros componentes (chips, banners, linhas de lista) devem poder usar **as mesmas paletas** sem duplicar definições de cor no projeto.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O tema DEVE expor **oito papéis M3** por `StatusKind` em `StatusColorRoles`, lookup via **`MaterialTheme.statusColors(status: StatusKind)`** — nomenclatura alinhada a Primary/Secondary/Tertiary:

  | Papel M3 | Propriedade |
  |----------|-------------|
  | Info | `color` |
  | On Info | `onColor` |
  | Info Container | `container` |
  | On Info Container | `onContainer` |
  | Info Fixed | `fixed` |
  | Info Fixed Dim | `fixedDim` |
  | On Info Fixed | `onFixed` |
  | On Info Fixed Variant | `onFixedVariant` |

  Valores em `FixedStatusPalettes` (internal); **independentes** do `ColorScheme`.
- **FR-001b**: Mapeamento UI do cartão (título, legenda, badge) **inline** em `SummaryCard.kt` a partir de `StatusColorRoles` — **sem** DTO intermédio nem resolvedor.
- **FR-002**: O cartão DEVE obter cores com **`MaterialTheme.statusColors(status)`** e aplicar papéis M3 directamente — **proibido** `StatusColorPalette`, `SummaryCardStatusColors`, `resolve()`.
- **FR-002a**: **Todas** as paletas (incl. **Default**) são **fixas** em `FixedStatusPalettes`. **Default** = neutros fixos; semânticos = contentor tintado na matiz. **Nenhuma** lê `ColorScheme` em runtime.
- **FR-003**: As matizes dominantes DEVEM ser: **info** → azul; **warning** → âmbar; **positive** → verde; **negative** → vermelho — alinhadas à convenção semântica do produto e distinguíveis entre si.
- **FR-004**: **Default** permanece **neutro** (sem matiz semântica), com aparência calibrada à baseline 011 via paleta fixa — independente do `ColorScheme` da app.
- **FR-005**: Cada paleta DEVE existir em variantes **tema claro** e **tema escuro**, com adaptação de luminosidade/contraste mantendo identidade do status.
- **FR-006**: Texto e ícones sobre fundos de contentor e badge DEVEM respeitar contraste mínimo **equivalente** ao aplicado em **Default** (referência: legibilidade WCAG AA entre pares texto/fundo).
- **FR-007**: Cores **somente** via `status: StatusKind` + `MaterialTheme.statusColors(status)` — **proibido** `Color` na API; **proibido** inferir status por conteúdo.
- **FR-008**: Lookup directo: `statusColors(status)` → `StatusColorRoles` — **sem** camadas intermédias.
- **FR-009**: `SummaryCardStatus` é **`typealias` de `StatusKind`** (compat 011); cinco valores: Default, Info, Warning, Positive, Negative.
- **FR-010**: O design system DEVE **atualizar o catálogo** dos 8 cartões de referência (FR-008, feature 011): **remapear um subset** para status semântico e manter os restantes em **Default** — garantindo **pelo menos uma instância** de cada status (**info**, **warning**, **positive**, **negative**) no conjunto dos oito; incluir documentação de como **outros componentes** consomem as mesmas paletas do tema.
- **FR-010a**: Mapeamento **canónico e estático** cartão → status no catálogo (demonstração, não regra de negócio):

| # | Cartão (FR-008) | Status |
|---|-----------------|--------|
| 1 | Valor Anterior | **info** |
| 2 | Valor Atual | **Default** |
| 3 | Aportes | **Default** |
| 4 | Retiradas | **negative** |
| 5 | Crescimento | **negative** |
| 6 | % Crescimento | **warning** |
| 7 | Lucro | **positive** |
| 8 | Valorização | **positive** |
- **FR-010b**: O design system DEVE incluir **preview de swatches** — grelha genérica que demonstra os **cinco status** e respetivos papéis cromáticos, como **segundo contexto** de consumo das paletas do tema (complementar ao cartão de resumo).
- **FR-011**: Esta feature **NÃO DEVE** alterar telas de produção (ex.: Histórico); escopo limita-se ao **design system v2** (tema, tokens, componente(s) consumidores e previews).
- **FR-012**: Esta feature **NÃO DEVE** introduzir animações, interatividade ou regras de negócio que mapeiem automaticamente métricas financeiras → status; a escolha do status continua responsabilidade de quem integra a tela.

### Key Entities

- **Paleta de status (tema)**: `StatusColorRoles` + `MaterialTheme.statusColors(status)`.
- **Seletor**: `StatusKind` (enum único; `SummaryCardStatus` = typealias).
- **Status semântico**: Identificador discreto — **Default**, **info**, **warning**, **positive**, **negative** — selecionado pelo consumidor do design system.
- **Tema v2**: Fonte única de verdade para cores base e semânticas; variantes claro/escuro; **acesso directo** à paleta por status.
- **Resolvedor de status**: **Removido** — lookup directo no composable.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em preview com cinco status (**Default** + quatro semânticos), **100%** dos revisores identificam corretamente a matiz esperada (azul / âmbar / verde / vermelho / neutro) em **menos de 5 segundos** por item.
- **SC-001a**: No catálogo atualizado, **100%** dos oito cartões usam o status da tabela FR-010a (2 Default, 1 info, 1 warning, 2 positive, 2 negative).
- **SC-002**: Em inspeção de contraste (claro e escuro) para os quatro status semânticos, **100%** dos pares texto/fundo e ícone/fundo do badge atendem ao **mesmo patamar mínimo** validado para **Default**.
- **SC-003**: Em auditoria de código do design system v2, **0** componentes públicos novos ou alterados expõem parâmetros de cor avulsos para status semântico — apenas identificador de status + lookup `MaterialTheme.statusColors(status)`; **sem** cores hardcoded nem resolvedores intermédios.
- **SC-004**: Em teste de reutilização, **dois** contextos de UI distintos — **cartão de resumo** e **preview de swatches** — consomem a **mesma** paleta **info** do tema sem duplicar definições hex/rgb.
- **SC-005**: Em alternância tema claro/escuro no preview, **100%** dos status mantêm identidade semântica reconhecível (mesmo rótulo info/warning/positive/negative atribuído por revisor independente).
- **SC-006**: Integrações com `status` **Default** mantêm aparência **equivalente** à baseline 011 — via paleta fixa neutra calibrada na implementação (não via `ColorScheme` dinâmico).

## Assumptions

- **Referência estrutural**: O padrão de papéis cromáticos segue o mapeamento já implementado para **Default** no cartão de resumo (superfícies, variantes on-surface, outline, badge) — esta feature **replica a estrutura**, trocando a origem cromática para matizes semânticas; status semânticos usam **contentor tintado**, não fundo neutro.
- **Nomenclatura**: **positive** e **negative** correspondem a outcomes favoráveis/desfavoráveis (equivalentes conceituais a success/error em outros sistemas); a API usa os nomes **positive** e **negative** pedidos pelo stakeholder.
- **Fonte cromática**: **Todas** as paletas (`Default`, `Info`, `Warning`, `Positive`, `Negative`) via **`FixedStatusPalettes`** no tema — valores fixos light/dark. **Default** neutro calibrado à aparência 011; semânticos com matizes azul, âmbar, verde e vermelho. **Nenhuma** paleta de status lê `ColorScheme` em runtime. Consumidores em `summary/` (e futuros) usam lookup directo `MaterialTheme.statusColors(status)`.
- **Escopo de consumidores**: Cartão de resumo é o primeiro consumidor obrigatório; **preview de swatches** é o segundo contexto obrigatório (SC-004); outros componentes de produção usam as mesmas paletas conforme necessidade futura.
- **Preview**: Catálogo estático dos 8 cartões FR-008 com mapeamento canónico (FR-010a); sem dados reais de carteira. Escolha de status é **ilustrativa** — telas de produção decidem `status` independentemente.
- **Dependência**: Requer tema v2 (`AppThemeV2`) e componente de cartão de resumo com API `status` extensível (feature 011) já entregues ou em merge compatível.
- **Fora de escopo**: Integração na tela de Histórico, cálculo de métricas, animações Expressive, parâmetros de cor custom por instância, status além dos cinco definidos.
