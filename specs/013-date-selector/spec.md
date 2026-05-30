# Feature Specification: Seletor de mês/ano

**Feature Branch**: `013-date-selector`

**Created**: 2026-05-29

**Status**: Draft

**Input**: User description: "vamos desenvoler o seletor de data. Esse componente encontra-se no prototype/src/App.tsx. Esse componente é um dropdown menu que ao ser tocado abre uma lista de meses/ano. No mock nao aparece o ano mas o ideal é que o label fique 'Maio de 2026'. O menu flutuante deve ser similar ao do cadastro de assets. Não há necessida de colocar a seta. Quero um componente completamente novo e ele deve ser armazenado em core/presentation/design-system-v2. Escopo: somente componente de visualização."

## Clarifications

### Session 2026-05-29

- Q: Como o integrador deve passar o período seleccionado e a lista de opções? → A: **Híbrido** — valor estruturado (mês + ano) para identidade e selecção; rótulo por opção via callback `itemLabel` opcional (padrão do dropdown de cadastro); formatação predefinida **"{Mês} de {Ano}"** em pt-BR quando o integrador não customiza o rótulo.
- Q: Comportamento quando a lista de opções está vazia? → A: **Gatilho desactivado** — continua a mostrar o rótulo do valor seleccionado (se existir), **não** abre menu.
- Q: Ordem dos itens no menu flutuante? → A: **Ordem do integrador** — o componente **não** reordena; exibe na sequência recebida.
- Q: Ao abrir o menu com lista longa, o scroll deve posicionar o item seleccionado? → A: **Auto-scroll** — ao abrir, posiciona o item seleccionado visível (centrado ou no topo visível).
- Q: O preview/catálogo no design system v2 deve demonstrar tema claro, escuro ou ambos? → A: **Claro e escuro** — dois exemplos (ou toggle) no catálogo.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Identificar o período selecionado (Priority: P1)

Quem usa a aplicação vê, numa barra de filtros ou cabeçalho, um **seletor compacto em forma de pílula** que indica claramente o **mês e ano de referência** atualmente escolhidos — por exemplo, **"Maio de 2026"** — acompanhado de um **ícone de calendário** à esquerda do texto.

**Why this priority**: O gatilho visível é a base do componente; sem ele não há contexto temporal para o utilizador nem ponto de interação para abrir o menu.

**Independent Test**: Renderizar o componente isolado com um período selecionado e validar aparência do gatilho (formato do rótulo, ícone, ausência de seta) sem depender de nenhuma tela de feature.

**Acceptance Scenarios**:

1. **Given** um período **maio de 2026** selecionado, **When** o componente é exibido, **Then** o gatilho mostra o texto **"Maio de 2026"** (mês por extenso em português + ano numérico de quatro dígitos).
2. **Given** o gatilho em repouso, **When** o utilizador apenas observa, **Then** vê um **ícone de calendário** à esquerda do rótulo e **não** vê seta, chevron ou indicador de expansão à direita.
3. **Given** o gatilho renderizado, **When** comparado ao protótipo web de referência, **Then** a forma **compacta e arredondada (pílula)**, o contraste texto/fundo e a hierarquia ícone + rótulo são **reconhecíveis** como o mesmo padrão visual — sem exigir paridade pixel a pixel.

---

### User Story 2 - Escolher outro mês/ano no menu flutuante (Priority: P1)

Quem filtra dados por período **toca ou clica** no gatilho, abre um **menu flutuante** com a lista de meses/anos disponíveis, seleciona uma opção e o gatilho passa a refletir a nova escolha; o menu fecha.

**Why this priority**: É a interação central do dropdown — sem seleção funcional o componente não cumpre o propósito do protótipo.

**Independent Test**: Simular abertura do menu, seleção de um item diferente do atual e verificar fecho do menu + atualização do rótulo do gatilho, usando lista de opções estática de demonstração.

**Acceptance Scenarios**:

1. **Given** o gatilho fechado, **When** o utilizador o ativa, **Then** abre um **menu flutuante** abaixo (ou adjacente) ao gatilho, com cantos arredondados, fundo claro e sombra suave que o distingue do conteúdo por trás.
2. **Given** o menu aberto com três ou mais opções, **When** o utilizador seleciona **"Abril de 2026"**, **Then** o menu **fecha**, o gatilho passa a exibir **"Abril de 2026"** e a opção escolhida fica registada para quem integrou o componente.
3. **Given** o menu aberto, **When** o utilizador ativa fora do menu ou no gatilho novamente, **Then** o menu **fecha** sem alterar a seleção (exceto se tiver escolhido um item no passo anterior).
4. **Given** a opção atualmente selecionada presente na lista, **When** o menu está aberto, **Then** esse item aparece **destacado** com fundo de realce (tom lavanda/roxo claro) e texto contrastante — padrão visual **equivalente** ao dropdown do cadastro de ativos.
5. **Given** cada item do menu, **When** exibido, **Then** usa o **mesmo formato de rótulo** que o gatilho: **"{Mês} de {Ano}"** (ex.: "Junho de 2025") — ou rótulo customizado via `itemLabel` quando aplicável.
6. **Given** uma lista com ordem definida pelo integrador (ex.: Jun/2026, Mar/2026, Dez/2025), **When** o menu abre, **Then** os itens aparecem **exactamente nessa ordem**, sem reordenação cronológica interna.
7. **Given** uma lista longa em que o item seleccionado não está no topo, **When** o utilizador abre o menu, **Then** a lista **rola automaticamente** até tornar o item seleccionado **visível** (centrado ou no topo da área visível).

---

### User Story 3 - Validar o componente no catálogo do design system (Priority: P2)

Quem desenvolve ou revê produto abre o **preview/catálogo** do design system v2 e encontra o seletor de mês/ano como **exemplo isolado**, com lista estática de períodos e um período inicial de demonstração.

**Why this priority**: Garante revisão visual e de interação antes de integrar em telas como Histórico de Posicionamento.

**Independent Test**: Abrir o preview do design system v2, localizar o seletor e executar abrir → selecionar → verificar rótulo, sem aceder a nenhuma tela de negócio.

**Acceptance Scenarios**:

1. **Given** o catálogo/preview do design system v2, **When** o revisor acede à secção do seletor de mês/ano, **Then** vê exemplos funcionais em **tema claro e tema escuro** (dois blocos ou toggle equivalente).
2. **Given** o exemplo do catálogo, **When** o revisor interage, **Then** consegue abrir o menu, mudar a seleção e observar o realce do item activo — comportamento idêntico ao contrato do componente reutilizável, em **ambos** os temas.

---

### User Story 4 - Integrar o seletor com dados externos (Priority: P2)

Quem compõe uma tela futura fornece a **lista de períodos disponíveis**, o **período seleccionado** e recebe notificação quando o utilizador escolhe outro — **sem** o componente calcular sozinho quais meses existem nos dados da carteira.

**Why this priority**: Mantém o componente reutilizável e desacoplado da lógica de negócio do Histórico.

**Independent Test**: Instanciar o componente com listas distintas (2 itens vs. 12 itens) e callbacks de seleção; confirmar que apenas renderiza o que recebe.

**Acceptance Scenarios**:

1. **Given** uma lista **vazia** de opções, **When** o componente é renderizado, **Then** o gatilho fica **desactivado** (aparência atenuada), **continua** a exibir o rótulo do valor seleccionado se existir, e **não** abre menu ao ser activado — **sem** crash ou estado inconsistente.
2. **Given** uma lista com um único período, **When** o menu é aberto, **Then** exibe **apenas** esse item, destacado como seleccionado.
3. **Given** o integrador altera programaticamente o período seleccionado, **When** o componente recompõe, **Then** o gatilho reflecte imediatamente o novo valor **sem** exigir nova interacção do utilizador.
4. **Given** opções como valores estruturados mês/ano **sem** `itemLabel` customizado, **When** o componente renderiza gatilho e menu, **Then** ambos exibem **"{Mês} de {Ano}"** formatado em pt-BR.
5. **Given** o integrador fornece `itemLabel` customizado para uma opção (ex.: "Todos os meses"), **When** essa opção aparece no menu ou no gatilho seleccionado, **Then** o rótulo customizado é exibido **no lugar** da formatação predefinida — a identidade da opção permanece o par mês/ano (ou valor sentinela acordado pelo integrador).

---

### Edge Cases

- **Formatação do mês**: Nomes de mês em **português do Brasil** com capitalização do mês (ex.: "Maio", não "maio" nem "MAIO"); ano sempre com quatro dígitos.
- **Ordem da lista**: O componente **preserva** a ordem das opções fornecidas pelo integrador; **não** aplica ordenação cronológica automática.
- **Lista longa**: Com muitos meses (ex.: 24+ entradas), o menu deve permanecer **rolável** dentro do ecrã sem cortar itens de forma inacessível; ao **abrir**, deve **auto-scroll** até o item seleccionado ficar visível.
- **Texto longo em ecrãs estreitos**: Rótulos no gatilho devem **truncar com reticências** antes de sobrepor o ícone ou sair da pílula.
- **Lista vazia**: Com **zero** opções, o gatilho entra automaticamente em estado **desactivado** (equivalente visual a FR-009), mantém o rótulo do valor seleccionado se fornecido, e **não** abre menu.
- **Estado desactivado**: Quando desactivado **explicitamente** pelo integrador **ou** por lista vazia, o gatilho **não** abre o menu e apresenta aparência atenuada coerente com outros controlos do design system.
- **Tema claro e escuro**: Gatilho, ícone, texto e realce do item seleccionado permanecem **legíveis** em ambos os modos de aparência.
- **Item seleccionado ausente da lista**: Se o valor seleccionado não constar nas opções fornecidas, o gatilho **continua** a mostrar o rótulo formatado desse valor; o menu pode não realçar nenhum item até haver correspondência.
- **Sem opção "Todos"**: Esta entrega **não** impõe opção agregada "Todos os meses"; se o integrador a incluir na lista, é tratada como qualquer outro rótulo.
- **Acessibilidade**: O gatilho deve ser activável por teclado/leitor de ecrã como controlo de seleção; o ícone de calendário é **decorativo** (rótulo textual anunciado como valor seleccionado).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O produto DEVE oferecer um **componente reutilizável** de seletor de mês/ano no **design system v2**, independente de qualquer tela de feature existente.
- **FR-002**: O gatilho DEVE ter forma **compacta em pílula**, ícone de **calendário** à esquerda e rótulo no formato **"{Mês} de {Ano}"** em português do Brasil (ex.: "Maio de 2026").
- **FR-003**: O gatilho **NÃO DEVE** exibir seta, chevron ou ícone de expansão.
- **FR-004**: Ao activar o gatilho, DEVE abrir um **menu flutuante** com cantos arredondados, fundo claro, sombra suave e lista vertical de opções — aparência **equivalente** ao dropdown do **cadastro de ativos** (realce lavanda/roxo claro no item seleccionado, espaçamento respirável entre itens).
- **FR-005**: Cada opção do menu DEVE usar o **mesmo formato de rótulo** que o gatilho (**"{Mês} de {Ano}"**), salvo quando o integrador fornece `itemLabel` customizado para essa opção.
- **FR-006**: Ao seleccionar uma opção, o menu DEVE **fechar** e o integrador DEVE ser **notificado** da nova escolha; o gatilho DEVE actualizar o rótulo reflectindo a selecção.
- **FR-007**: O item correspondente ao valor seleccionado DEVE aparecer **visualmente destacado** enquanto o menu estiver aberto.
- **FR-007a**: Ao abrir o menu com lista rolável, o componente DEVE **auto-scroll** até o item seleccionado ficar **visível** na área do menu (centrado ou no topo visível).
- **FR-008**: A lista de períodos disponíveis e o valor seleccionado DEVEM ser **fornecidos pelo integrador** como **valores estruturados** (mês calendário + ano); o componente **NÃO DEVE** inferir meses a partir de dados de carteira ou persistência.
- **FR-008a**: O componente DEVE expor callback **`itemLabel`** opcional por opção (mesmo padrão do dropdown de cadastro de ativos); **omitido**, o rótulo predefinido é **"{Mês} de {Ano}"** em pt-BR; **fornecido**, o integrador controla o texto exibido no gatilho e no menu para essa opção.
- **FR-008b**: A **identidade** da selecção (comparação de item activo, callback de mudança) DEVE basear-se no **valor estruturado**, não no texto do rótulo — dois rótulos distintos não podem colidir se representarem o mesmo mês/ano.
- **FR-008c**: Com lista de opções **vazia**, o componente DEVE **desactivar automaticamente** o gatilho (sem abrir menu), mantendo visível o rótulo do valor seleccionado quando fornecido.
- **FR-008d**: O componente **NÃO DEVE** reordenar as opções recebidas; a sequência no menu DEVE coincidir com a ordem da lista fornecida pelo integrador.
- **FR-009**: O componente DEVE permitir estado **desactivado** explícito (prop do integrador) **ou** implícito (lista vazia), em ambos os casos sem abertura de menu.
- **FR-010**: **Fora do escopo desta entrega**: integração na tela de Histórico de Posicionamento, filtros de dados, persistência de preferência, opção fixa "Todos os meses", navegação por ano separada, seletor de dia ou intervalo de datas.
- **FR-011**: O design system v2 DEVE incluir **preview/catálogo** demonstrando o seletor com dados estáticos de exemplo (lista de meses/anos fictícia e interacção completa abrir → seleccionar → fechar) em **tema claro e tema escuro** (dois exemplos ou toggle no catálogo).

### Key Entities

- **Período (mês/ano)**: Unidade seleccionável representada por mês calendário (1–12) e ano (inteiro); identidade usada na selecção e no callback. **Apresentação predefinida**: rótulo localizado **"{Mês} de {Ano}"**; **opcionalmente** substituível via `itemLabel` por opção.
- **Opção do menu**: Entrada seleccionável na lista flutuante; uma opção está **activa** quando corresponde ao período seleccionado.
- **Gatilho**: Superfície compacta em pílula que mostra o período actual e inicia a abertura do menu.
- **Menu flutuante**: Painel temporário com lista de opções, realce do item activo e encerramento ao seleccionar ou dismissar.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Num teste de usabilidade com **5 utilizadores**, **100%** identificam o período seleccionado no gatilho em **menos de 2 segundos** sem instruções adicionais.
- **SC-002**: Num fluxo de seleção (abrir menu → escolher outro mês → menu fechar), **95%** dos utilizadores completam a acção em **menos de 5 segundos** com lista de até **12** opções visíveis ou roláveis.
- **SC-003**: Revisores de produto confirmam em **uma sessão de preview** que o menu flutuante e o realce do item seleccionado são **visualmente consistentes** com o dropdown do cadastro de ativos (escala de 1–5, média **≥ 4** em consistência visual).
- **SC-004**: O componente é demonstrável **isoladamente** no catálogo do design system v2 **sem** dependência de ecrãs de negócio — critério verificado por checklist de preview antes de merge, incluindo validação em **tema claro e escuro**.
- **SC-005**: Com lista de **24** períodos, todos os itens permanecem **acessíveis** via scroll do menu em viewports móveis típicos (largura ≥ 320 px) sem sobreposição ilegível com o gatilho; ao abrir, o item **seleccionado** fica **visível sem scroll manual** na maioria dos casos.

## Assumptions

- O protótipo em `prototype/src/App.tsx` (filtro "Mês de Referência" na barra superior) é referência **qualitativa** para o gatilho em pílula; o formato de rótulo evolui de só mês ("Maio") para **mês + ano** ("Maio de 2026") conforme pedido.
- O dropdown do **cadastro de ativos** existente é referência **qualitativa** para o menu flutuante (cantos arredondados, realce do item activo, sombra) — **sem** reutilizar ou alterar esse componente; o seletor de mês/ano é **novo** no design system v2.
- Localização fixa **pt-BR** para nomes de mês nesta entrega; outros locales ficam para feature futura.
- O integrador fornece **valores estruturados** mês/ano; formatação predefinida **"{Mês} de {Ano}"** é responsabilidade do componente. Rótulos customizados via `itemLabel` são opt-in (ex.: "Todos os meses") e não alteram a regra de identidade por mês/ano.
- Interacção segue padrões M3 Expressive do design system v2 (tema claro/escuro via tema partilhado da biblioteca).
- "Somente componente de visualização" significa: entrega limitada ao **componente + preview** no design system v2, **sem** ligar a ViewModels, repositórios ou ecrãs de Histórico nesta feature.
