# Feature Specification: Cartões de resumo da carteira

**Feature Branch**: `011-summary-cards`

**Created**: 2026-05-29

**Status**: Draft

**Input**: User description: "Desenvolver os componentes de cartões de resumo. O protótipo e especificações podem ser encontrados no prototype/src/App.tsx. Quero um componente reutilizável que se adapte aos 8 modelos de cards, se atente as cores, espaçamentos, fontes e tudo mais. Os cartões não terão ação ao serem tocados. Nem animações. Tudo deve ser customizável e parametrizável, incluindo as cores (divididas em Default, success, error, warning, info), ícones (serão os do material design) e textos."

## Clarifications

### Session 2026-05-29

- Q: Qual regra de variante semântica os oito cartões do Histórico devem seguir (fixo, híbrido, dinâmico)? → A: **Só componente** — não definir regra nesta feature; cada tela escolhe `status` e ícone ao integrar, sem catálogo obrigatório de regras de negócio para o Histórico.
- Q: O escopo inclui integrar os 8 cartões na tela de Histórico ou apenas o design system? → A: **Só design system** — componente(s) no módulo de UI compartilhado; **sem** alterar a tela de Histórico nesta feature.
- Q: A feature inclui preview/catálogo no design system para validar os 8 modelos? → A: **Componente + preview** — preview/catálogo no design system com os **8 exemplos** estáticos do protótipo.
- Q: As cores das variantes semânticas vêm de tokens M3 ou paleta fixa do protótipo? → A: **Tokens do tema M3** — cada status mapeia roles do `MaterialTheme` no design system (ver refinamento abaixo sobre API).
- Q: Como o ícone deve se comportar em leitores de tela? → A: **Ícone decorativo** — sempre ignorado; apenas título, valor e legenda são anunciados.
- ~~Q: Cores via cinco status nesta versão?~~ → **Substituído** pela sessão *redução de escopo — cores por status* abaixo: nesta entrega só **`Default`**; API `status` extensível para `info`/`warning`/`error`/`success` em features futuras.
- Q: A altura do cartão é mínima ou fixa? → A: **Altura mínima uniforme** entre instâncias (slots M3 + line heights), sem crescer quando opcionais faltam — **não** obrigatório igualar ~110 px do protótipo.
- Q: O layout muda quando legenda ou ícone estão ausentes? → A: **Não** — campos opcionais ausentes não reduzem altura nem alteram comportamento; slots reservados mantêm posição e espaçamento idênticos ao cartão completo.

### Session 2026-05-29 (redução de escopo — cores por status)

- Q: Quantos status e paletas de cor entram nesta entrega? → A: **Apenas `Default`** — implementação e cores só para status neutro; **fora do escopo** nesta feature: paletas `info`, `warning`, `error`, `success` e qualquer hex/cores custom do protótipo por status.
- Q: De onde vêm as cores do `Default`? → A: **Material Design 3 Expressive** — roles do `MaterialTheme.colorScheme` (superfícies container, outline, on-surface); sem paleta custom por status nesta entrega.
- Q: Como preparar status futuros? → A: API mantém parâmetro `status` e tipos extensíveis (`SummaryCardStatus`, resolvedor de cores por status); novos valores e paletas entram em **features futuras** sem quebrar o contrato do componente.
- Q: O protótipo deve ser pixel-perfect? → A: **Não** — o protótipo (`App.tsx`) é referência **qualitativa** (ideia de hierarquia tipográfica — tamanho relativo, bold, uppercase — e de espaçamento entre blocos). Implementação usa **o máximo possível** de tokens e componentes **Material Design 3 Expressive** (`MaterialTheme.typography`, `shapes`, `colorScheme` Expressive, espaçamento da grade 4/8 dp, `OutlinedCard` com `CardDefaults`).
- Q: Qual variante de cartão M3 e tema? → A: **Outlined Card** (fundo `surface`, borda `outlineVariant`, elevação 0) dentro de **`AppThemeV2`** com `lightExpressiveColorScheme()` / `darkExpressiveColorScheme()` e escala de **shape** Expressive (`medium` = 12 dp no cartão; `full` no badge circular). Motion Expressive **não** se aplica ao cartão (componente estático por requisito).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Exibir cartão de resumo padronizado (Priority: P1)

Quem desenvolve telas do produto usa o componente de **cartão de resumo** do design system para exibir uma métrica com **título** (rótulo em caixa alta), **valor principal** em destaque, **legenda** opcional, **ícone** opcional no badge e **`status`** — aparência via **M3 Expressive** (não cópia pixel a pixel do protótipo web).

**Why this priority**: É o entregável central desta feature — o bloco visual reutilizável que futuras telas (ex.: Histórico) poderão compor em grade.

**Independent Test**: Renderizar instâncias isoladas (e preview de catálogo com oito exemplos de **layout**, todos com `status` **Default**) no design system; validar hierarquia tipográfica e aparência M3 Expressive do status Default, sem depender de nenhuma tela de feature.

**Acceptance Scenarios**:

1. **Given** parâmetros de título, valor, legenda e ícone, **When** o componente é renderizado, **Then** exibe a hierarquia visual (rótulo → valor → legenda) e disposição (título/ícone na mesma linha) inspirada no protótipo, usando **tokens M3** para tipografia, forma, cor e espaçamento — **sem** obrigatoriedade de medidas px do mock.
2. **Given** qualquer instância do cartão, **When** o usuário apenas visualiza (sem interagir), **Then** o cartão **não** responde a toque ou clique como ação navegável — é elemento **somente leitura**.
3. **Given** qualquer instância do cartão, **When** o usuário passa o cursor ou toca na superfície, **Then** **não** há animação de escala, brilho, sombra pulsante ou transição de movimento associada ao cartão.

---

### User Story 2 - Diferenciar métricas por significado semântico (Priority: P1) — **FORA DO ESCOPO desta entrega**

Paletas **info**, **warning**, **error** e **success** ficam para feature futura. Nesta entrega só **`Default`** (neutro, M3 Expressive). A API `status` permanece para extensão sem reimplementar o componente.

---

### User Story 3 - Validar os oito modelos no catálogo do design system (Priority: P2)

O desenvolvedor ou revisor de produto abre o **preview/catálogo** do design system e vê os **oito cartões de referência** do protótipo dispostos como exemplos estáticos (títulos, valores, legendas e ícones do mock), cada um demonstrando a composição esperada sem depender da tela de Histórico.

**Why this priority**: Valida composição e conteúdos de exemplo antes da integração em telas futuras; **não** exige paridade pixel-perfect com o mock.

**Independent Test**: Abrir o preview do design system e conferir que existem exatamente oito instâncias alinhadas à tabela FR-008, com dados estáticos de demonstração.

**Acceptance Scenarios**:

1. **Given** o preview do design system, **When** o revisor acessa a seção de cartões de resumo, **Then** são exibidos **exatamente oito** exemplos correspondentes ao catálogo FR-008 (Valor Anterior até Valorização).
2. **Given** os exemplos estáticos do preview, **When** revistos, **Then** cada um exibe título, valor, legenda e ícone da tabela FR-008 com `status` **Default** — estrutura reconhecível em relação ao protótipo, com liberdade de divergir em medidas M3.

---

### User Story 4 - Parametrizar conteúdo e status sem expor cores (Priority: P2)

Quem integra a tela define **título**, **valor** (texto já formatado), **legenda** opcional, **ícone** (Material Design) e **`status`** (nesta entrega: apenas **`Default`** válido). Aparência cromática do Default via **M3 Expressive**; sem parâmetros de cor avulsos.

**Why this priority**: Mantém API estável para conteúdo; `status` preparado para extensão futura.

**Independent Test**: Instanciar cartões com conteúdos distintos e `status` **Default**; confirmar que cores vêm do tema M3 Expressive sem `Color` na API.

**Acceptance Scenarios**:

1. **Given** cartão com `status` **Default**, **When** exibido dentro de `AppThemeV2`, **Then** usa roles M3 Expressive do `MaterialTheme.colorScheme` (superfícies container, outline, on-surface).
2. **Given** tentativa de integração com cores avulsas, **When** avaliada nesta feature, **Then** fica **fora do contrato**.
3. **Given** um ícone Material Design escolhido pelo integrador, **When** o cartão é exibido, **Then** o ícone aparece no slot decorativo (canto superior direito): **24.dp** de glifo em contentor circular **40.dp** com shape **full** e fundo `surfaceContainerHigh` — não é o componente `Badge` de notificação M3.
4. **Given** legenda omitida, **When** o cartão é exibido lado a lado com outro **com** legenda, **Then** **altura mínima uniforme**, posição do título/valor e espaçamentos internos são **visualmente idênticos** — a área da legenda permanece reservada (vazia/invisível), sem o valor “subir” ou o cartão encolher.
5. **Given** ícone omitido, **When** comparado a cartão **com** ícone, **Then** o slot do badge no canto superior direito **permanece** (mesmo tamanho e posição); o título não expande para ocupar esse espaço de forma diferente.
6. **Given** legenda **e** ícone omitidos no mesmo cartão, **When** exibido na grade, **Then** o layout é indistinguível em estrutura (altura, padding, alinhamentos) de um cartão completo — apenas o conteúdo dos slots opcionais não aparece.
7. **Given** título ou valor com texto longo, **When** o espaço horizontal é limitado, **Then** o texto é **truncado** com reticências sem sobrepor o slot do ícone.

---

### Edge Cases

- **Valores zero**: Exibem "R$ 0,00" ou "0,00%" conforme formatação fornecida; o cartão mantém **altura mínima uniforme** derivada de slots M3 (não px do protótipo).
- **Altura uniforme na grade**: Em preview ou telas com vários cartões, todos os itens respeitam a **mesma altura mínima** (slots M3) — o layout não estica nem encolhe cartões com legenda/ícone ausentes ou texto curto.
- **Campos opcionais ausentes**: **Legenda** e **ícone** são opcionais na API, mas o layout interno **não** se reorganiza: reserva-se o mesmo espaço (slots) que quando presentes; nenhum elemento restante muda de tamanho, peso visual ou alinhamento por causa da omissão.
- **Valores negativos**: O sinal faz parte do texto do valor (ex.: "-R$ 73.375,43"); nesta entrega o `status` permanece **`Default`** (cores semânticas por sinal ficam para feature futura).
- **Prefixo explícito**: Valores como lucro podem incluir "+" no texto fornecido (ex.: "+R$ 940,05"); o componente não altera o texto.
- **Retiradas**: O protótipo exibe retiradas como valor negativo na UI; a formatação negativa é responsabilidade de quem fornece o texto do valor.
- **Combinações opcionais**: Qualquer combinação de presença/ausência de legenda e ícone produz o **mesmo esqueleto** de layout; apenas o texto/ícone deixa de ser desenhado nos slots, sem reflow.
- **Acessibilidade**: Título e legenda devem permanecer legíveis no contraste mínimo entre tokens de texto e fundos de cada variante; o ícone é **sempre decorativo** (sem descrição anunciada); o título é o rótulo principal da métrica.
- **Tema claro/escuro**: `Default` deve permanecer legível em tema claro e escuro via `MaterialTheme.colorScheme` Expressive.
- **Status nesta entrega**: Apenas **`Default`** implementado; outros valores de `status` ficam para feature futura (código preparado, não expostos no enum até lá).
- **Grade responsiva**: Em larguras estreitas, a grade pode empilhar para uma coluna **sem** perder padding interno nem raio de canto; comportamento exato de breakpoint fica a critério do layout pai, desde que cada cartão preserve proporções internas.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O produto DEVE oferecer um **único componente reutilizável** de cartão de resumo que aceite: título, valor principal (texto), legenda opcional, ícone opcional (Material Design) e **`status`** — nesta entrega apenas **`Default`**; API e resolvedor de cores **preparados** para novos status sem alterar a assinatura pública do cartão.
- **FR-002**: Para **`Default`**, as cores DEVEM derivar de **tokens Material Design 3 Expressive** via `MaterialTheme.colorScheme` e **`CardDefaults.outlinedCardColors()`** / **`CardDefaults.outlinedCardBorder()`** para o contentor: fundo **`surface`**, borda **`outlineVariant`**, conteúdo **`onSurface`** / **`onSurfaceVariant`**; badge decorativo com **`surfaceContainerHigh`** — **não** paleta custom hex do protótipo.
- **FR-002a**: **Fora do escopo desta entrega**: definição de cores para `info`, `warning`, `error`, `success`.
- **FR-003**: O componente **NÃO DEVE** aceitar parâmetros de cor individuais; aparência cromática via `status` (somente Default implementado).
- **FR-003a**: Extensões de status futuras adicionam valor ao enum (ou equivalente) + ramo no resolvedor de cores; **não** parâmetros de cor ad hoc por instância.
- **FR-004**: A estrutura visual DEVE usar **componentes e tokens M3 Expressive**: **`OutlinedCard`** (`enabled = false`), forma **`MaterialTheme.shapes.medium`** (12 dp — token de cartão M3), padding **`16.dp`**, espaçamento interno **`8.dp`** (grade 4/8 dp); disposição inspirada no protótipo (linha título+ícone; valor; legenda) — **sem** obrigação de medidas px do mock.
- **FR-004d**: O tema **`AppThemeV2`** DEVE aplicar **`lightExpressiveColorScheme()`** / **`darkExpressiveColorScheme()`** (API Expressive Material3) e **`Shapes`** com escala Expressive documentada em `research.md` — **não** reutilizar tema v1.
- **FR-004a**: O cartão DEVE ter **altura mínima uniforme** calculada a partir de slots reservados (line heights M3 + espaçamento tema), não crescendo quando opcionais ausentes; **não** é obrigatório igualar ~110 px do protótipo.
- **FR-004b**: Para **legenda** e **ícone** (únicos campos opcionais), a ausência **NÃO DEVE** alterar o comportamento do layout: mantêm-se **slots reservados** com as mesmas dimensões e posições que quando preenchidos; o título e o valor **não** deslocam-se, **não** redimensionam e **não** ganham área extra por omissão de opcionais.
- **FR-004c**: O espaço interno distribui-se de forma **idêntica** entre cartões com todos os campos, só legenda, só ícone ou nenhum opcional — equivalente a um esqueleto fixo com conteúdo condicionalmente visível nos slots.
- **FR-005**: Tipografia DEVE usar **`MaterialTheme.typography`** exclusivamente: título `labelSmall` + bold + uppercase; valor `titleLarge`; legenda `bodySmall` — hierarquia alinhada à **ideia** do protótipo (rótulo pequeno, valor grande, legenda menor), **sem** `fontSize` avulso nem cópia de px do mock.
- **FR-006**: O cartão DEVE ser **não interativo**: sem ação ao toque, sem estado pressionado, sem cursor de link e sem feedback de clique.
- **FR-007**: O cartão DEVE ser **estático**: sem animações de entrada, hover, escala ou transição de sombra.
- **FR-008**: O produto DEVE documentar (via catálogo de referência **não normativo**) os **oito modelos** ilustrados no protótipo/mock, com título, legenda e ícone sugerido — **sem** prescrever variante semântica obrigatória por cartão:

| # | Título | Valor (exemplo mock) | Legenda | Ícone sugerido |
|---|--------|----------------------|---------|----------------|
| 1 | Valor Anterior | R$ 126.248,76 | Soma dos valores anteriores | maleta / portfólio |
| 2 | Valor Atual | R$ 71.182,11 | Soma do valor atualizado | carteira |
| 3 | Aportes | R$ 0,00 | Soma das transações (compras) | adicionar (+) |
| 4 | Retiradas | -R$ 73.375,43 | Soma das transações (vendas) | fechar (×) |
| 5 | Crescimento | -R$ 72.435,38 | Aportes − Retiradas + Valorização | camadas |
| 6 | % Crescimento | -57,38% | % em relação ao valor anterior | gráfico de barras |
| 7 | Lucro | +R$ 940,05 | Rendimento dos investimentos | tendência de alta |
| 8 | Valorização | 1,32% | % em relação ao valor final | gráfico de barras |

- **FR-008a**: No preview, todos os oito exemplos usam `status` **Default**; diferenciação cromática por métrica fica para features futuras.
- **FR-012**: Esta feature **NÃO DEVE** alterar a tela de Histórico de posicionamento nem substituir o painel de resumo existente; entrega limita-se ao **design system** (e documentação/preview associados ao componente).
- **FR-013**: O design system DEVE incluir **preview/catálogo** exibindo os **oito cartões de referência** (FR-008) com **dados estáticos** de demonstração (valores e textos do mock), em disposição que permita revisão visual conjunta (ex.: grade 2×4 ou lista equivalente no preview).
- **FR-014**: O preview/catálogo é **somente demonstração** — não persiste dados, não reage a filtros de carteira e não substitui telas de produção.
- **FR-015**: O **ícone** do cartão DEVE ser tratado como **puramente decorativo** para acessibilidade: leitores de tela **não** o anunciam; a compreensão da métrica vem do **título**, do **valor** e da **legenda** (quando presente).

- **FR-009**: O cartão DEVE ser composable em layouts externos (ex.: grade 2 colunas com espaçamento **`8.dp`** entre itens — grade M3); **não** é obrigatório nesta feature entregar um layout de grade pronto — a composição em 2×4 fica a cargo das telas consumidoras (ex.: Histórico, em feature futura).
- **FR-010**: Textos longos em título, valor ou legenda DEVEM truncar sem sobreposição ao ícone.
- **FR-011**: O escopo desta feature é o **componente de apresentação** e seu catálogo visual; **cálculo** dos agregados (aportes, crescimento, percentuais) permanece na camada de negócio existente — apenas o texto formatado é passado ao cartão.

### Key Entities

- **Cartão de resumo**: Unidade de UI somente leitura com título, valor (obrigatórios), slots opcionais de legenda e ícone (layout reservado mesmo vazios) e `status` visual.
- **Status do cartão**: Enumerado extensível; nesta entrega só **`Default`**, com cores via M3 Expressive; demais status reservados para evolução.
- **Exemplo de catálogo (referência)**: Conjunto ilustrativo dos oito cartões do protótipo; uso opcional por telas futuras, sem binding nesta feature.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: No preview com oito exemplos, **100%** dos revisores reconhecem a **mesma hierarquia e disposição** que o protótipo (título, ícone, valor, legenda), aceitando diferenças de medida por tokens M3.
- **SC-001a**: O preview expõe **exatamente oito** instâncias nomeadas conforme o catálogo FR-008, sem omissão nem duplicata de modelo.
- **SC-002**: Em teste de interação no componente isolado, **0%** das instâncias disparam navegação ou ação ao toque/clique.
- **SC-003**: Em teste de movimento no componente isolado, **0%** das instâncias exibem animação de hover, escala ou transição ao focar ou passar o ponteiro.
- **SC-004**: Em teste com `status` **Default**, **100%** das instâncias aplicam cores coerentes com roles M3 Expressive do tema, sem parâmetros de cor avulsos na API.
- **SC-005**: Em teste de truncamento com título e valor 40% mais longos que o mock, **100%** dos casos mantêm slot do ícone, **altura mínima uniforme** inalterada e sem sobreposição de texto.
- **SC-005a**: Em grade com oito cartões (preview), **100%** dos itens exibem a **mesma altura** (desvio visual zero entre cartões com e sem legenda).
- **SC-005b**: Em teste A/B com o mesmo título e valor, comparando cartão **completo** vs. **sem legenda**, **sem ícone** e **sem ambos**, **100%** dos pares mantêm alinhamento vertical do título e do valor na mesma posição (tolerância zero de reflow perceptível). **Validação**: preview `SummaryCard_OptionalSlots_preview` (quatro instâncias lado a lado em `SummaryCard.kt`); revisor confirma no checklist da PR — sem automação nesta feature.
- **SC-006**: Usuário identifica corretamente a métrica (ex.: aportes vs. retiradas) em **menos de 3 segundos** por cartão em cenário com os oito cartões visíveis, sem ler a legenda completa. **Validação**: QA manual opcional ao abrir `SummaryCard_Catalog8_preview` (checklist quickstart); não bloqueia merge técnico.
- **SC-007**: Em teste com leitor de tela no componente isolado, **100%** das instâncias anunciam título e valor (e legenda, se houver) e **0%** anunciam o ícone como elemento separado.

## Assumptions

- **Referência visual (protótipo)**: Inspiração **qualitativa** — ordem dos blocos, ideia de bold/uppercase no rótulo, valor em destaque, espaçamento relativo entre título/valor/legenda. **Não** pixel-perfect.
- **Referência visual (implementação)**: **M3 Expressive** em primeiro lugar — `colorScheme` Expressive, `shapes` (medium 12 dp em cartões), `typography`, espaçamento 4/8 dp, **Outlined Card** oficial (sem elevação/sombra no repouso).
- **Motion Expressive**: Springs e micro-interações Expressive **não** se aplicam a este cartão (FR-007); motion fica para componentes interativos futuros no v2.
- **Contraste (WCAG AA)**: Texto sobre `surface` / `surfaceContainerHigh` deve respeitar **≥ 4,5:1** com `onSurface` / `onSurfaceVariant` nos temas light e dark Expressive. Verificação na PR: previews light/dark em `SummaryCard.kt` + confirmação manual no checklist `quickstart.md` (T025).
- **Ícones**: Uso da família **Material Design** (Material Symbols / equivalente na app); nomes exatos dos glifos ficam para a fase de plano, desde que semanticamente alinhados à tabela FR-008.
- **Formatação monetária e percentual**: Valores chegam ao componente já formatados em pt-BR (ex.: `R$ 126.248,76`, `-57,38%`); o cartão não recalcula nem reformata números.
- **Status por tela**: Não há regra de negócio centralizada nesta feature para mapear métrica ou sinal → `status`; protótipo e mock são exemplos visuais.
- **API de cor**: Integrador passa apenas `status`; objetos de configuração de cor por status vivem no design system e podem evoluir com o tema M3 sem mudar a assinatura do cartão.
- **Escopo**: Design system — componente, **status Default** (M3 Expressive), preview 8 exemplos (layout), código extensível. **Fora do escopo**: paletas info/warning/error/success, cores custom por parâmetro, Histórico, agregados.
- **Preview**: Oito exemplos com dados do mock e `status` **Default** em todos.
- **Elevação**: **Outlined Card** sem sombra no repouso (elevação 0); hover/sombra do protótipo **não** deve ser replicado.
- **Layout estável**: Opcionais ausentes usam slots invisíveis/reservados, não “colapso” de linhas — padrão comum em UI de densidade fixa para grades homogêneas.
