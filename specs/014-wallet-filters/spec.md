# Feature Specification: Filtros da carteira

**Feature Branch**: `014-wallet-filters`

**Created**: 2026-06-02

**Status**: Draft

**Input**: User description: "vamos desenvolver a tela de filtros (somente design) no design system v2. A tela ficará posicionada dentro de um card conforme mockup. Devemos usar icones do material design. Vamos usar o button selected conforme M3E (material design expressive), de forma que o botao fique redondo se nao selecionado e quadrado com bordas se selecionado. Temos um exemplo no codigo de como isso pode ser usado. O tamanho desse botao deve ser reduzido conforme mockup. Ao selecionar uma classe, um subgrupo com caracteristicas dessa classe deve aparecer para ser usado como filtro. Ex.: Ao selecionar Renda Fixa, aparece um sub grupo com os subtipos de renda fixa. Use um viewModel para controlar os estados. Filtros como 'Vence Ate' deve conter a lista de possiveis de filtragem, ou seja, so os meses que foram retornados na lista de investimentos. Outros filtros podem usufruir dessa regra de forma a apresentar as opções que realmente fazem sentido. Filtros podem ser abreviados para caber em uma linha, ao passar o mouse sobre eles, teremos uma descrição mais amigavel. O filtro não afetará a tela de historico por enquanto, somente design."

## Clarifications

### Session 2026-06-02

- Q: Quando a carteira não tem activos de uma classe (ex.: Fundos), como o grupo Classe deve comportar-se? → A: **Derivado dos dados** — ocultar botões de classe sem pelo menos um activo na carteira injectada.
- Q: Como funciona a selecção em B3 informado e Liquidados (Sim / Não)? → A: ~~Exclusivo opcional~~ **Revisto** — ver sessão *selecção global* abaixo.
- Q: Quando nenhuma classe está seleccionada, a secção Subtipos por classe deve aparecer? → A: **Ocultar secção inteira** — sem cabeçalho nem subcartões até existir ≥1 classe activa.
- Q: Como derivar os botões Sim / Não de B3 informado e Liquidados? → A: **Por valor nos dados** — o grupo só aparece se **ambos** Sim e Não forem possíveis na carteira; se só uma opção for possível (ou nenhuma), **ocultar o grupo inteiro**.
- Q: Ao tocar de novo no botão já seleccionado em B3/Liquidados, o que acontece? → A: ~~Toggle off exclusivo~~ **Revisto** — ver sessão *selecção global* (toggle off por botão, como nas outras secções).

### Session 2026-06-02 (correcção de entrega)

- Q: Onde vive a implementação do painel de filtros — design system v2 ou composeApp? → A: **composeApp** (módulo de apresentação da app), no mesmo espírito que `SummaryGridWidget` + integração futura no Histórico; **priorizar componentização**: primitivos **genéricos e reutilizáveis** só entram em **design-system-v2 quando necessário**; o painel composto (layout do mockup, secções, catálogo de subtipos) fica em **composeApp**.

### Session 2026-06-02 (estado da UI)

- Q: O estado do painel usa ViewModel nesta entrega? → A: **Não** — **sem ViewModel por agora**; estado no widget/previews (ex.: `remember` + modelo imutável no `composeApp`); ViewModel fica para integração futura no Histórico.

### Session 2026-06-02 (Material Design 3 Expressive)

- Q: Qual nível de aderência ao M3 Expressive? → A: **À risca** — componentes, tokens, tipografia, ícones, formas, estados de interacção, menus e acessibilidade conforme especificação M3 / M3 Expressive; mockup só como referência qualitativa de hierarquia, **sem** cores ou medidas hex avulsas fora de tokens.

### Session 2026-06-02 (critérios binários)

- Q: Se só Sim ou só Não existir nos dados, mostrar um botão ou ocultar o grupo? → A: **Ocultar o grupo inteiro** — critério binário só é filtrável quando há **variação** (Sim **e** Não possíveis na carteira).

### Session 2026-06-02 (selecção global)

- Q: Alguma secção usa selecção exclusiva (ex.: só Sim **ou** Não)? → A: **Não** — **todas** as secções com botões toggles (Classe, Subtipos, Liquidez, B3 informado, Liquidados) são **multi-selecção não exclusiva**: qualquer combinação de opções activas é permitida; toque alterna cada botão; segundo toque no mesmo botão desactiva-o. **Excepção:** **Vence até** é **selecção única** no menu (um mês ou «Qualquer vencimento») — não é grupo de toggles.

### Session 2026-06-02 (visibilidade de secções)

- Q: Mostrar cabeçalho de secção sem opções para marcar? → A: **Não** — **nenhuma** secção de filtro (cabeçalho + controlos) é renderizada se, após derivação dos dados, **não existir ≥1 opção seleccionável** nessa secção; subcartões vazios também **não** aparecem.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Visualizar painel de filtros no cartão (Priority: P1)

Quem revisa o produto ou integra telas abre o **preview Compose** do módulo de apresentação da app (`composeApp`) e vê o widget **Filtros da carteira** contido num **cartão** único, com cabeçalho (título + ação de reset), secções empilhadas e separadores visuais conforme o mockup fornecido.

**Why this priority**: É o contentor e a hierarquia visual que todas as outras interacções dependem.

**Independent Test**: Abrir o `@Preview` do widget no `composeApp` (tema `AppThemeV2`); confirmar cartão com título "Filtros da carteira", ícone de filtro, botão "Resetar" e **apenas** secções com opções no dataset de demo (sem cabeçalhos órfãos).

**Acceptance Scenarios**:

1. **Given** o preview do widget no `composeApp` com tema claro, **When** o revisor abre a composição de filtros da carteira, **Then** o painel aparece **dentro de um cartão** com cantos arredondados, padding interno generoso e divisórias horizontais entre grupos de filtro.
2. **Given** o painel renderizado, **When** observado o cabeçalho, **Then** exibe título com escala tipográfica M3, ícone **Material Symbols** (24dp, `onSurfaceVariant`, decorativo), e acção **Resetar** como **text button** com cor semântica **`error`** (token), alinhada à direita.
3. **Given** qualquer largura de preview suportada pelo catálogo, **When** o conteúdo excede a altura visível, **Then** o cartão permite deslocamento vertical **sem** quebrar o alinhamento interno das secções.

---

### User Story 2 - Seleccionar classes e subtipos dependentes (Priority: P1)

O utilizador (no preview ou demonstração) **activa** uma ou mais **classes** de investimento — Renda Fixa, Renda Variável e Fundos — e, para cada classe activa, surge um **subcartão** listando **subtipos** específicos dessa classe (ex.: CDB, LCI… para Renda Fixa), também seleccionáveis.

**Why this priority**: É o fluxo central do mockup e a principal forma de refinar a carteira por tipo de activo.

**Independent Test**: No preview, seleccionar só "Renda Fixa" e verificar que apenas o subgrupo de subtipos de renda fixa aparece; adicionar "Fundos" e verificar segundo subgrupo; desactivar "Renda Fixa" e confirmar que o subgrupo correspondente desaparece.

**Acceptance Scenarios**:

1. **Given** nenhuma classe seleccionada, **When** o painel é exibido, **Then** a secção **Subtipos por classe** **não** é visível (sem cabeçalho nem subcartões).
2. **Given** nenhuma classe seleccionada, **When** o utilizador activa "Renda Fixa", **Then** aparece a secção **Subtipos por classe** com um subcartão titulado "Renda Fixa" listando apenas subtipos presentes nos dados (ex.: CDB, LCI, LCA, CRI, CRA, Tesouro Direto, Debêntures quando existirem na carteira).
3. **Given** "Renda Variável" activa, **When** o subcartão é exibido, **Then** lista apenas subtipos presentes nos dados (ex.: FII, Ação Nacional, Ação Internacional, ETF).
4. **Given** "Fundos" activa, **When** o subcartão é exibido, **Then** lista apenas subtipos presentes nos dados (ex.: Ação, Multimercado, Previdência).
5. **Given** várias classes activas em simultâneo, **When** visualizado o painel, **Then** cada classe activa tem o **seu** subcartão de subtipos visível, empilhados na ordem fixa: Renda Fixa → Renda Variável → Fundos (apenas os activos).
6. **Given** uma classe activa com subtipos já seleccionados, **When** o utilizador **desactiva** essa classe, **Then** o subcartão desaparece e as selecções de subtipo dessa classe **deixam de contar** no estado (limpas ou ignoradas até reactivação).
7. **Given** a última classe activa é desactivada, **When** o painel actualiza, **Then** a secção **Subtipos por classe** **deixa de ser exibida** por completo.

---

### User Story 3 - Filtrar por liquidez, B3 e liquidados com botões compactos (Priority: P1)

O utilizador escolhe opções em grupos de **botões de selecção** de tamanho **reduzido** (menores que os botões de classe no mockup), com comportamento visual **expressivo**: forma **totalmente arredondada** quando não seleccionado e forma **quadrada com cantos arredondados** quando seleccionado.

**Why this priority**: Cobre a maior parte dos critérios do mockup (liquidez, B3 informado, liquidados) e o padrão M3 Expressive pedido.

**Independent Test**: Alternar selecção em cada grupo e verificar mudança de forma e cor; confirmar ícones Material por secção (calendário, informação, actualização).

**Acceptance Scenarios**:

1. **Given** a secção **Liquidez**, **When** renderizada, **Then** mostra ícone de calendário/agenda, rótulo "Liquidez" em caixa alta e opções: D+1, Diária, No Vencimento — cada uma como botão compacto seleccionável (multi-selecção permitida).
2. **Given** a linha com **B3 informado** e **Liquidados**, **When** em ecrã largo, **Then** as duas secções aparecem **lado a lado**; em ecrã estreito podem empilhar sem perder legibilidade.
3. **Given** **B3 informado**, **When** renderizada, **Then** ícone de informação e opções Sim / Não como botões compactos com **multi-selecção** (Sim e Não podem estar activos em simultâneo).
4. **Given** **Liquidados**, **When** renderizada, **Then** ícone de actualização/sincronização circular e opções Sim / Não como botões compactos com **multi-selecção**.
5. **Given** **Sim** e **Não** activos no mesmo grupo (B3 ou Liquidados), **When** o utilizador desactiva um deles, **Then** o outro **permanece** activo.
6. **Given** um botão compacto não seleccionado, **When** o utilizador o activa, **Then** a forma transita para **quadrada com bordas arredondadas** e fundo/contraste de estado seleccionado; ao desactivar, regressa à forma **pill** arredondada com contorno claro.
7. **Given** botões de **classe** (tamanho maior no mockup), **When** comparados aos de liquidez/B3/liquidados, **Then** os de classe são **visivelmente maiores** que os compactos, mantendo o mesmo comportamento de forma seleccionado/não seleccionado.
8. **Given** dados em que B3 informado só admite **Sim** (sem activos **Não**), **When** o painel é renderizado, **Then** a secção **B3 informado** **não** aparece.

---

### User Story 4 - Escolher vencimento até com opções derivadas dos dados (Priority: P2)

O utilizador abre o selector **Vence até** e escolhe **Qualquer vencimento** ou um mês/ano concreto; a lista de meses **só inclui vencimentos que existem** na carteira de investimentos fornecida ao painel (não uma lista genérica infinita).

**Why this priority**: Evita opções vazias e alinha o design à realidade dos dados — requisito explícito do utilizador.

**Independent Test**: Alimentar o preview com dados de demonstração contendo vencimentos em Nov/2027 e Jan/2030; abrir o menu e confirmar apenas essas entradas além de "Qualquer vencimento".

**Acceptance Scenarios**:

1. **Given** dados de demonstração com **≥1** vencimento distinto, **When** o utilizador abre **Vence até**, **Then** a secção está visível e o menu lista **Qualquer vencimento** (primeiro, pré-seleccionado por defeito) seguido dos meses nos dados, ordenados cronologicamente (ex.: "Novembro/2027").
2. **Given** nenhum vencimento nos dados, **When** o painel é construído, **Then** a secção **Vence até** **não** é exibida (FR-018e).
3. **Given** uma opção de mês seleccionada, **When** o menu fecha, **Then** o campo fechado mostra o rótulo do mês escolhido em negrito; com defeito mostra "Qualquer vencimento".
4. **Given** a secção **Vence até**, **When** renderizada, **Then** inclui ícone de calendário (24dp) e **menu / dropdown** M3 de largura total com ícone de expansão (`expand_more`), painel de menu com item seleccionado marcado (checkmark) e forma **`large`** no menu.

---

### User Story 5 - Rótulos abreviados com descrição ao focar (Priority: P2)

Quando o espaço horizontal é limitado, os botões podem mostrar **texto abreviado**; ao **passar o ponteiro** (ou equivalente de foco acessível em plataformas sem rato), o utilizador vê a **descrição completa** e amigável.

**Why this priority**: Garante legibilidade em uma linha sem sacrificar compreensão.

**Independent Test**: Instância de preview com rótulo longo forçado a abreviatura; hover/focus revela texto completo.

**Acceptance Scenarios**:

1. **Given** um botão cujo rótulo curto não cabe na largura mínima do mockup, **When** o sistema renderiza o grupo, **Then** pode usar **abreviatura** acordada (ex.: "Ação Nac." → "Ação Nacional") mantendo uma única linha.
2. **Given** rótulo abreviado visível, **When** o utilizador passa o ponteiro ou foca o controlo, **Then** aparece **tooltip** M3 (plain tooltip) com o texto completo em linguagem natural.
3. **Given** leitor de ecrã, **When** o botão tem abreviatura visual, **Then** o nome acessível anuncia a **descrição completa**, não só a abreviatura.

---

### User Story 6 - Repor filtros e reflectir estado na interface (Priority: P2)

O utilizador acciona **Resetar** e todos os critérios voltam ao **estado inicial** (nada seleccionado nos grupos de botões, subcartões ocultos, vencimento em "Qualquer vencimento").

**Why this priority**: Acção explícita no mockup; necessária para demonstrações repetíveis no preview.

**Independent Test**: Seleccionar vários critérios, clicar Resetar, verificar estado inicial.

**Acceptance Scenarios**:

1. **Given** várias classes, subtipos, liquidez, B3, liquidados e um mês de vencimento seleccionados, **When** o utilizador toca **Resetar**, **Then** todas as selecções de botões são **limpas**, a secção **Subtipos por classe** deixa de ser exibida e **Vence até** regressa a **Qualquer vencimento**.
2. **Given** estado reposto, **When** o utilizador observa o painel, **Then** nenhum botão compacto ou de classe aparece no estado seleccionado visual.

---

### User Story 7 - Opções de filtro coerentes com os dados disponíveis (Priority: P2)

Além de **Vence até**, outros grupos **só mostram opções** que existem pelo menos um investimento correspondente na lista fornecida ao painel (ex.: não mostrar "Debêntures" se não houver debênture na carteira de demonstração).

**Why this priority**: Regra transversal pedida pelo utilizador; evita UI enganosa no preview e prepara integração futura.

**Independent Test**: Dados de demo só com CDB e LCI em renda fixa; subcartão RF mostra apenas essas duas opções.

**Acceptance Scenarios**:

1. **Given** dados de carteira sem activos de categoria Fundos, **When** o painel é construído, **Then** o botão de classe **Fundos** **não** é exibido; subtipos e opções derivadas **nunca** listam valores sem correspondência nos dados.
2. **Given** dados com apenas liquidez "D+1" e "Diária", **When** a secção Liquidez é exibida, **Then** só essas duas opções aparecem; **Given** nenhuma liquidez nos dados, **Then** a secção **Liquidez** **não** é exibida.
3. **Given** classe activa sem subtipos correspondentes nos dados, **When** o painel actualiza, **Then** **não** aparece subcartão vazio para essa classe; se nenhum subcartão seria mostrado, a secção **Subtipos por classe** **não** é exibida.
4. **Given** carteira em que todos os activos têm o mesmo valor para B3 informado (ex.: só **Sim**, nunca **Não**), **When** o painel é construído, **Then** a secção **B3 informado** **não** é exibida.
5. **Given** carteira em que **Sim** e **Não** são ambos possíveis para **Liquidados**, **When** o painel é construído, **Then** a secção **Liquidados** é exibida com **dois** botões; **Given** só um dos estados existir nos dados, **Then** a secção **Liquidados** **não** é exibida.
6. **Given** alteração da lista de investimentos injectada no preview, **When** a lista é actualizada, **Then** secções e opções visíveis **actualizam** (secções sem opções **desaparecem**) sem reinício manual.

---

### Edge Cases

- **Carteira vazia**: Painel pode mostrar só cabeçalho do cartão + **Resetar**; secções **Classe**, **Subtipos**, **Liquidez**, **B3**, **Liquidados** e **Vence até** **ocultas** por falta de opções seleccionáveis (FR-018).
- **Uma única classe nos dados**: Só o botão dessa classe aparece em **Classe**; subtipos listam apenas valores presentes nos dados dessa classe.
- **Texto muito longo num botão**: Truncar ou abreviar com reticências; dica e acessibilidade com texto integral.
- **Muitos subtipos activos**: Subcartões empilham; quebra de linha dentro do subcartão sem sobrepor outros grupos.
- **Reset durante menu aberto**: Menu de vencimento fecha e estado repõe-se de forma consistente.
- **B3 / Liquidados sem escolha real**: Secção oculta (FR-018d) quando não há **Sim** e **Não** nos dados.
- **Secção sem opções**: Qualquer secção com zero botões/itens seleccionáveis após derivação dos dados **não** é renderizada (FR-018).
- **Tema escuro**: Todos os papéis de cor e contrastes DEVEM cumprir M3 em `AppThemeV2` escuro (`darkExpressiveColorScheme`); sem cores fixas do mockup.
- **Movimento reduzido**: Com preferência de sistema activa, transições de forma/cor dos toggles DEVEM ser instantâneas ou mínimas (sem animação expressiva longa).
- **Sem integração com Histórico**: Alterar filtros no preview **não** altera listas, totais ou cartões da tela de Histórico existente.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O produto DEVE disponibilizar um **widget/painel de filtros da carteira** no módulo de apresentação da app (`composeApp`), no pacote de feature de apresentação (padrão análogo a `SummaryGridWidget`), utilizável em **previews Compose** **sem** integrar nem alterar a tela de Histórico nesta entrega.
- **FR-001a**: A composição do painel (secções do mockup, subcartões por classe, estado da UI, derivação de opções a partir da carteira injectada) DEVE residir em **composeApp**, **não** como API pública monolítica do design-system-v2.
- **FR-001b**: O design-system-v2 DEVE receber **apenas** primitivos **genéricos** extraídos quando a mesma UI se repetir ou quando o primitive for claramente reutilizável noutras features (ex.: botão de filtro M3 Expressive compacto, cabeçalho de secção de filtro); **não** é obrigatório criar primitivos se o comportamento for único do painel — **priorizar** extrair só o que justifica reutilização.
- **FR-001c**: Primitivos em design-system-v2 DEVEM cumprir a secção **Material Design 3 Expressive (conformidade normativa)**; o widget em composeApp **compõe** esses primitivos com layout específico da carteira, sempre dentro de **`AppThemeV2`**.
- **FR-002**: O painel DEVE residir num **Outlined Card** M3 (ver FR-M3-001), com cabeçalho: título "Filtros da carteira", ícone `filter_list` e acção **Resetar** (FR-M3-004).
- **FR-003**: Cada secção de filtro visível DEVE ter **cabeçalho de secção** (FR-M3-013, FR-M3-016) com ícone temático e rótulo em caixa alta — **somente** se a secção tiver **≥1 opção seleccionável** (FR-018).
- **FR-018**: **Regra transversal** — **não** renderizar secção de filtro (cabeçalho, divisória associada e controlos) quando, após derivação da carteira injectada, essa secção tiver **zero** opções para marcar/seleccionar; aplica-se a **Classe**, **Subtipos por classe**, **Liquidez**, **B3 informado**, **Liquidados** e **Vence até**.
- **FR-018a**: **Classe**: secção **oculta** se **nenhuma** classe tiver activos na carteira (zero botões).
- **FR-018b**: **Subtipos por classe**: secção **oculta** se **nenhuma** classe estiver activa **ou** se nenhuma classe activa tiver **≥1** subtipo presente nos dados; subcartão de uma classe **oculto** se essa classe tiver zero subtipos derivados (não mostrar subcartão vazio).
- **FR-018c**: **Liquidez**: secção **oculta** se **nenhuma** regra de liquidez existir nos dados (zero botões).
- **FR-018d**: **B3 informado** / **Liquidados**: secção **oculta** se não houver **Sim e Não** possíveis (já em FR-008); alinhado a «sem opções para marcar».
- **FR-018e**: **Vence até**: secção **oculta** se **nenhum** mês/ano de vencimento existir nos dados (sem entradas no menu além do estado por defeito interno); com **≥1** vencimento distinto, a secção aparece com «Qualquer vencimento» + meses.
- **FR-004**: Quando a secção **Classe** é exibida, DEVE oferecer **Renda Fixa**, **Renda Variável**, **Fundos** (só classes com activos) — **multi-selecção**, botões **maiores** que os compactos.
- **FR-005**: Quando a secção **Subtipos por classe** é exibida (FR-018b), cada classe activa com subtipos nos dados tem um **subcartão** com botões compactos (catálogo RF/RV/Fundos conforme spec) e **multi-selecção**.
- **FR-006**: Ao desactivar uma classe, o subcartão correspondente DEVE **desaparecer** e o estado de subtipos dessa classe DEVE **repor-se** (não permanecer seleccionado oculto).
- **FR-007**: Quando a secção **Liquidez** é exibida, DEVE listar apenas regras presentes nos dados (ex.: D+1, Diária, No vencimento) — **multi-selecção**, botões **compactos**.
- **FR-008**: Quando exibidas, **B3 informado** e **Liquidados** usam botões compactos com **multi-selecção não exclusiva**; cada secção só é exibida com **≥1** activo **Sim** **e** **≥1** **Não** nos dados (FR-018d). Em layout largo, secções visíveis podem partilhar a mesma linha (duas colunas).
- **FR-008a**: **Todas** as secções com botões de filtro (Classe, Subtipos, Liquidez, B3 informado, Liquidados) DEVEM seguir a mesma regra de **multi-selecção não exclusiva**; **nenhuma** secção de toggles pode impor escolha exclusiva entre opções do mesmo grupo.
- **FR-009**: Todos os botões de selecção DEVEM cumprir **FR-M3-005–FR-M3-009** (ToggleButton + Button Group Expressive; morph pill → rectângulo arredondado; cores por tokens).
- **FR-010**: Botões compactos DEVEM ser **visivelmente menores** em altura/padding que os botões de classe, alinhados ao mockup.
- **FR-011**: Quando a secção **Vence até** é exibida (FR-018e), usa **menu/dropdown** M3 (FR-M3-010–FR-M3-011) de largura total, **Qualquer vencimento** + meses presentes nos dados, ordenados cronologicamente.
- **FR-012**: Opções derivadas dos dados DEVEM **omitir** valores sem correspondência; **nunca** mostrar secção ou subcartão **sem** opções seleccionáveis (reforço de FR-018).
- **FR-013**: Rótulos de botões PODEM usar **forma curta** (FR-M3-015); DEVEM expor descrição completa via **tooltip** M3 (FR-M3-019–FR-M3-020) e nome acessível.
- **FR-014**: **Resetar** DEVE limpar todas as selecções de botões, ocultar a secção **Subtipos por classe** (nenhuma classe activa) e repor **Vence até** para **Qualquer vencimento**.
- **FR-015**: O estado de todas as selecções e do vencimento DEVE ser **centralizado** no widget do painel (modelo de estado imutável + actualização num único ponto no `composeApp`), separado da marcação visual dos primitivos, de forma que a UI reaja de forma consistente a alterações e ao **Resetar** sem lógica duplicada nos componentes visuais — **sem ViewModel** nesta entrega.
- **FR-016**: O módulo **composeApp** DEVE incluir **pelo menos um** `@Preview` estático do painel completo alinhado ao mockup e **pelo menos um** `@Preview` com dados de demonstração que exercitem opções dinâmicas (vencimentos e omissão de opções ausentes), dentro de `AppThemeV2`.
- **FR-017**: Nesta entrega, o painel **NÃO DEVE** aplicar filtros à lista de histórico, posicionamento ou cartões de resumo — **apenas** UI e estado no widget/previews do **composeApp** (sem wiring no `AssetHistoryScreen`).

### Material Design 3 Expressive (conformidade normativa)

Toda a UI DEVE estar dentro de **`AppThemeV2`** com `lightExpressiveColorScheme()` / `darkExpressiveColorScheme()` e **`AppShapesV2`**. **Proibido** `Color` hex/avulso ou `fontSize` fora de `MaterialTheme.typography`.

#### Contentor e divisão (Containment — Cards, Dividers)

- **FR-M3-001**: O painel DEVE usar **Outlined Card** M3: fundo `surface`, borda `outlineVariant` via tokens de cartão, elevação 0, forma `MaterialTheme.shapes.medium` (12dp — alinhado a `SummaryCard` / `AppShapesV2.medium`).
- **FR-M3-002**: Subcartões de subtipo DEVEM usar **Outlined Card** ou superfície `surfaceContainerLow` com borda `outlineVariant` e cantos `medium`/`large` conforme hierarquia — **não** caixas com cor hex do mockup.
- **FR-M3-003**: Entre secções principais DEVE haver **Divider** horizontal (`outlineVariant`, 1dp), espaçamento na grade **8dp** entre blocos e **16dp** de padding interno do cartão (grade 4/8dp).

#### Acções (Action — Text Button, Button Groups)

- **FR-M3-004**: **Resetar** DEVE ser **Text Button** M3 (ênfase baixa), cor de conteúdo `error` / `onSurface` conforme papel de acção destrutiva neutra; área de toque mínima **48×48dp**.
- **FR-M3-005**: Selecção de filtros (classe, subtipo, liquidez, B3, liquidados) DEVE usar **Button Group conectado** M3 **Expressive** com **`ToggleButton`** e `ButtonGroupDefaults.connected*ButtonShapes()` — **não** segmented buttons legados nem chips de filtro como substituto do padrão expressivo do projeto.
- **FR-M3-006**: Forma dos toggles: **não seleccionado** → cantos **full** (pill); **selecionado** → cantos **small/medium** (rectângulo arredondado) via shapes do grupo conectado — comportamento já usado em `SegmentedControl` / histórico.
- **FR-M3-007**: Cores dos toggles DEVEM vir de `ToggleButtonDefaults.toggleButtonColors()` mapeadas a roles Expressive: seleccionado `primary`/`onPrimary` (ou `secondaryContainer`/`onSecondaryContainer` se o plano uniformizar com filtros); não seleccionado contorno `outline` e fundo `surface`.
- **FR-M3-008**: **Classe**: altura visual alvo **40dp**, tipografia **`labelLarge`**; **compactos**: altura visual alvo **~32dp**, tipografia **`labelMedium`**; em ambos, alvo de toque **≥48dp** (padding mínimo ou `minimumInteractiveComponentSize`).
- **FR-M3-009**: Espaçamento entre itens do grupo: `ButtonGroupDefaults.ConnectedSpaceBetween`; quebra de linha com `FlowRow` ou equivalente mantendo grupos conectados por segmento contíguo na mesma linha.

#### Selecção e menus (Selection — Menus; Chips não substituem toggles)

- **FR-M3-010**: **Vence até** DEVE seguir padrão **Menu / ExposedDropdownMenu** M3 (referência: `MonthYearSelector` no design-system-v2): âncora de largura total, menu com `shape` **large**, item seleccionado com **ícone de check**, tipografia `bodyLarge` nos itens.
- **FR-M3-011**: Valor fechado do selector: **`titleSmall`** com peso enfatizado (bold) para o mês activo; placeholder **Qualquer vencimento** com `onSurface`.

#### Tipografia (Typography)

- **FR-M3-012**: Título do painel: **`titleMedium`** (ou `titleSmall` emphasized) + `onSurface`; **sem** uppercase no título principal.
- **FR-M3-013**: Rótulos de secção: **`labelSmall`** + **bold** + **uppercase** + cor `primary` ou `onSurfaceVariant` (um único papel por tema, consistente em todo o painel).
- **FR-M3-014**: Títulos de subcartão por classe: **`labelMedium`** ou **`titleSmall`**, cor `primary`/`onSurface` alinhada aos rótulos de secção.
- **FR-M3-015**: Texto dos toggles: **`labelLarge`** (classe) / **`labelMedium`** (compacto); truncamento com reticências (`TextOverflow.Ellipsis`), **maxLines = 1**.

#### Ícones (Icons — Material Symbols)

- **FR-M3-016**: Ícones DEVEM ser **Material Symbols Outlined** (mesma família do projeto), **24dp** nos cabeçalhos de secção, **opsz 24**; ícones decorativos junto a rótulo de secção com **semantics** que os ocultam do leitor de ecrã (rótulo de texto é a fonte de verdade).
- **FR-M3-017**: Mapa de ícones (nomes lógicos): cabeçalho do painel `filter_list`; Classe `layers`; Subtipos `filter_list`; Liquidez `calendar_month`; B3 informado `info`; Liquidados `sync` ou `autorenew`; Vence até `calendar_month`; expansão `expand_more`.
- **FR-M3-018**: Ícones em botões seleccionados: opcional; se ausentes, só texto. Cor do ícone segue `onSurface` / `onPrimary` do estado do toggle.

#### Comunicação (Communication — Tooltips)

- **FR-M3-019**: Abreviaturas DEVEM usar **tooltip** M3 (plain tooltip) no hover/foco desktop; em toque, equivalente acessível (long-press ou tooltip após foco) no plano.
- **FR-M3-020**: Tooltip com fundo `inverseSurface` / texto `inverseOnSurface` (tokens), tipografia `bodySmall`.

#### Estados de interacção e acessibilidade

- **FR-M3-021**: Todos os controlos interactivos DEVEM expor estados M3: enabled, pressed, focused, disabled; foco visível com contraste adequado.
- **FR-M3-022**: Todos os grupos de toggles DEVEM usar semântica de **checkbox** (multi-selecção); **proibido** `Role.RadioButton` / exclusividade entre opções do mesmo grupo.
- **FR-M3-023**: Contraste de texto ≥ **4,5:1** (WCAG AA) entre pares tokenizados em temas claro e escuro nos previews.
- **FR-M3-024**: **Proibido** animação decorativa no cartão; apenas morph de forma/cor nos toggles conforme API Expressive, respeitando **preferência de movimento reduzido**.

#### Referência de implementação no repositório

- ToggleButton + ButtonGroup: `SegmentedControl` (design-system v1) e `AssetHistoryScreen` (composeApp).
- Dropdown mês/ano: `MonthYearSelector` (design-system-v2).
- Cartão e tema: `SummaryCard` + `AppThemeV2`.

### Key Entities

- **Critério de filtro**: Par identificador + rótulo curto + rótulo completo (para dica/acessibilidade); pertence a um **grupo** (classe, subtipo, liquidez, B3, liquidados, vencimento).
- **Grupo de filtro**: Conjunto nomeado de critérios com **multi-selecção não exclusiva** (secções de toggles) ou **selecção única** (**Vence até**, menu); opções visíveis **derivadas dos dados** quando aplicável.
- **Estado do painel**: Conjunto de critérios activos por grupo + mês de vencimento seleccionado (ou "qualquer"); exposto à UI de forma imutável/reactiva.
- **Carteira de referência (preview)**: Lista de investimentos de demonstração usada para calcular opções disponíveis e alimentar o preview — não persiste preferências do utilizador nesta feature.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: No preview com dataset de demo **completo** (todas as dimensões de filtro representadas), um revisor identifica as áreas do mockup presentes em **menos de 30 segundos**; secções sem opções nos dados **não** contam para o total esperado.
- **SC-002**: Em demonstração gravada ou teste manual, **100%** das transições seleccionado/não seleccionado nos botões expressivos exibem a mudança de forma pill → quadrada arredondada conforme FR-009.
- **SC-003**: Com dados de demo contendo **N** vencimentos distintos (**N ≥ 1**), a secção **Vence até** está visível e o menu lista **N + 1** entradas (incluindo "Qualquer vencimento"); com **N = 0**, a secção **não** aparece.
- **SC-009**: Em carteira de demo, **zero** secções de filtro aparecem com cabeçalho e **zero** controlos seleccionáveis (validação de FR-018).
- **SC-004**: Após **Resetar**, o painel volta ao estado inicial verificável em **uma acção** sem resíduos de selecção em qualquer grupo.
- **SC-005**: Nenhuma alteração funcional ou visual na **tela de Histórico** é exigida para considerar esta feature concluída (validação por ausência de diffs/regressões nessa tela).
- **SC-006**: Para botões com abreviatura no preview, **100%** expõem descrição completa via tooltip M3 ou nome acessível em teste de acessibilidade básico.
- **SC-007**: Revisão visual nos previews claro/escuro confirma **zero** cores ou tamanhos de texto fora de `MaterialTheme` (amostragem de todos os grupos de filtro).
- **SC-008**: **100%** dos grupos de selecção usam morph de forma pill→arredondado via ToggleButton Expressivo (FR-M3-006), verificável em inspecção ou gravação de preview.

## Assumptions

- **Escopo e módulos**: Entrega principal em **`core/presentation/composeApp`** (widget + estado local/previews), alinhada ao padrão **Summary**: primitivo visual reutilizável em **design-system-v2** só quando necessário; integração no `AssetHistoryScreen` / Histórico e **ViewModel** ficam para feature futura.
- **Referência de arquitectura**: `SummaryCard` (v2) + `SummaryGridWidget` (composeApp) — repetir a separação: **genérico no v2**, **composição e estado no composeApp**.
- **Catálogo de classes**: O catálogo de produto inclui Renda Fixa, Renda Variável e Fundos, mas o painel **só exibe** botões de classe com **≥1 activo** na carteira injectada; subtipos e demais grupos derivados seguem a mesma regra de presença nos dados.
- **Regras de selecção**: **Multi-selecção não exclusiva** em **todas** as secções de toggles (Classe, Subtipos, Liquidez, B3, Liquidados) — toque alterna cada botão, combinações livres; **Vence até** = uma escolha no menu; semântica AND/OR na filtragem real **fora** desta entrega.
- **Gestão de estado (esta entrega)**: Estado no **widget** em **composeApp** (ex.: `remember` nos previews e no composable raiz do painel, com `data class` de estado e callbacks); **não** há **ViewModel** nem dependência de Koin para este painel até integração no Histórico.
- **M3 Expressive**: Conformidade **normativa** via secção FR-M3-*; mockups são referência **qualitativa** de hierarquia e densidade — **não** paleta hex (azul/vermelho/cinza) nem medidas px fixas.
- **Ícones**: **Material Symbols Outlined**, 24dp em cabeçalhos; ver FR-M3-016–FR-M3-018.
- **Tipografia**: Exclusivamente `MaterialTheme.typography` (FR-M3-012–FR-M3-015); sem emphasized em excesso — bold/uppercase só onde a spec indica rótulos de secção.
- **Plataforma de dica**: Hover para descrição completa prioriza **desktop/web** no preview; em toque, foco longo ou tooltip equivalente será tratado no plano de acessibilidade.
- **Dados de demo**: O preview usa lista estática representativa (incl. vencimentos Nov/2027 e Jan/2030 como no mockup) até existir injecção real da carteira noutra feature.

## Out of Scope

- **ViewModel**, `StateFlow` de ecrã e registo Koin para o painel de filtros (adiado à integração com Histórico).
- Implementar o **painel completo** de filtros da carteira como componente público principal do módulo **design-system-v2** (ex.: `WalletFiltersPanel` no v2).
- Aplicar filtros à tabela ou resumos do **Histórico**.
- Persistir preferências de filtro entre sessões.
- Lógica de negócio de filtragem no domínio ou repositórios.
- Animações decorativas no cartão ou secções; morph mínimo nos toggles além do permitido em FR-M3-024.
- **Chips** de filtro M3 como substituto dos **ToggleButton** em button group (escolha explícita: toggles expressivos).
- Cores, tipografia ou formas **fora** dos tokens `MaterialTheme` / `AppThemeV2`.
- Filtros adicionais não presentes no mockup (corretora, emissor, etc.).
