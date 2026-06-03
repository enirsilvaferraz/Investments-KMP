# Feature Specification: Filtros da carteira no histórico

**Feature Branch**: `015-history-wallet-filters`

**Created**: 2026-06-02

**Status**: Draft

**Input**: User description: "Integrar o painel de filtros da carteira (`WalletFiltersPanel`) à listagem de ativos no histórico. Regras: nenhuma selecção → sem filtro; uma selecção → filtrar; várias opções no mesmo grupo → OR; opções em grupos diferentes → AND. Substituir os controlos legados de categoria, liquidez e meta financeira. A lógica de filtragem deve viver na camada de domínio (caso de uso que alimenta a tabela de histórico), não na UI."

## Clarifications

### Session 2026-06-02

- Q: O critério «Vence até» aplica-se a todas as classes ou só a renda fixa? → A: **Só renda fixa** — activos de renda variável e fundos **não** são avaliados nem excluídos por vencimento; o grupo «Vence até» só restringe activos de renda fixa cujo vencimento é **até** o mês seleccionado (inclusive).
- Q: O critério «Liquidez» aplica-se a todas as classes ou só a renda fixa? → A: **Só renda fixa** — RV e fundos **não** são avaliados nem excluídos por liquidez; o grupo «Liquidez» só restringe activos de renda fixa cuja liquidez corresponde às opções seleccionadas.
- Q: Ao mudar o período (mês/ano), o que acontece às selecções de filtros? → A: **Reset ao estado por defeito** — todas as selecções são limpas **excepto** «Não liquidado» pré-seleccionado em Liquidados; a listagem reflecte o filtro por defeito do novo mês.
- Q: Com filtros activos, aportes/resgates no sumário reflectem só activos filtrados ou todo o período? → A: **Só activos filtrados** — património, aportes, resgates e valorização agregam **apenas** as linhas visíveis na tabela após filtrar.
- Q: Posições com património zero (anterior e actual) — manter exclusão implícita ou incluir na listagem? → A: **Via filtro Liquidados** — deixa de haver exclusão implícita separada; posições liquidadas/zeradas são controladas pelo grupo **Liquidados**. Estado inicial por defeito: **«Não liquidado»** pré-seleccionado (oculta liquidados por defeito, paridade com o comportamento actual da tabela).
- Q: Onde vive o estado dos filtros no histórico? → A: **HistoryViewModel** — estado exposto via `StateFlow`; o ecrã envia intents ao alterar selecções; recarregamento de dados coordenado pelo ViewModel.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ver listagem por defeito (posições activas) (Priority: P1)

Quem consulta o histórico de investimentos abre o ecrã e vê, por defeito, **posições não liquidadas** do período — o painel inicia com **«Não liquidado»** activo em Liquidados, ocultando posições liquidadas (incluindo as de património zero anterior e actual). «Resetar» repõe este estado por defeito.

**Why this priority**: Preserva o comportamento actual da tabela (sem posições zeradas/liquidadas por defeito) de forma explícita via filtro, não via regra oculta.

**Independent Test**: Abrir histórico com mix de posições activas e liquidadas; confirmar que só activas aparecem; tocar «Resetar» após alterar filtros e confirmar repetição do estado por defeito.

**Acceptance Scenarios**:

1. **Given** o painel no **estado por defeito** («Não liquidado» seleccionado; restantes grupos inactivos; vencimento «Qualquer vencimento»), **When** o histórico carrega, **Then** a tabela exibe **posições não liquidadas** do período e **não** exibe posições liquidadas com património zero anterior e actual.
2. **Given** filtros alterados pelo utilizador, **When** toca «Resetar», **Then** o painel repõe o **estado por defeito** (incluindo «Não liquidado» activo) **e** a tabela e o sumário reflectem essa listagem por defeito.
3. **Given** o estado por defeito, **When** o utilizador **desactiva** «Não liquidado» sem activar «Sim», **Then** o grupo Liquidados fica inactivo **e** a listagem inclui também posições liquidadas/zeradas.

---

### User Story 2 - Filtrar por um único critério (Priority: P1)

O utilizador activa **uma** opção num grupo de filtros (ex.: apenas «Renda Fixa» em Classe, ou apenas «Sim» em B3 informado) e a listagem mostra **somente** activos que satisfazem esse critério.

**Why this priority**: Cobre o caso mais simples e frequente de refinamento da carteira.

**Independent Test**: Com carteira mista, activar só «Renda Fixa» e verificar que nenhuma linha de RV ou Fundos permanece; repetir com um único subtipo (ex.: CDB) e confirmar correspondência.

**Acceptance Scenarios**:

1. **Given** activos de várias classes no período, **When** o utilizador activa apenas «Renda Fixa» em Classe, **Then** a tabela mostra **apenas** activos de renda fixa.
2. **Given** activos com e sem identificador B3, **When** o utilizador activa apenas «Sim» em B3 informado, **Then** a tabela mostra **apenas** activos informados na B3.
3. **Given** um mês de vencimento concreto seleccionado em «Vence até», **When** nenhum outro filtro está activo, **Then** a tabela inclui activos de **renda fixa** cujo vencimento é **até** esse mês (inclusive) **e** mantém activos de **renda variável e fundos** (não sujeitos a este critério).

---

### User Story 3 - Combinar critérios com OR dentro do grupo e AND entre grupos (Priority: P1)

O utilizador combina várias selecções para refinar a carteira: opções **no mesmo grupo** alargam o resultado (união — «ou»); opções **em grupos diferentes** restringem em conjunto (intersecção — «e»).

**Why this priority**: É a regra de negócio central pedida para substituir os controlos legados de selecção única.

**Independent Test**: Seleccionar «Renda Fixa» **e** «Renda Variável» → ver ambas as classes; acrescentar «Sim» em B3 → ver apenas RF/RV **e** com B3 informada; seleccionar CDB **e** LCI → ver activos que sejam CDB **ou** LCI.

**Acceptance Scenarios**:

1. **Given** activos RF e RV na carteira, **When** o utilizador activa «Renda Fixa» **e** «Renda Variável» em Classe, **Then** a tabela inclui activos de **qualquer** uma das duas classes (OR intra-grupo).
2. **Given** activos RF com e sem B3 e activos RV, **When** o utilizador activa «Renda Fixa» em Classe **e** «Sim» em B3 informado, **Then** a tabela inclui **apenas** activos de renda fixa **com** B3 informada (AND inter-grupo).
3. **Given** vários subtipos de RF (ex.: CDB, LCI), **When** o utilizador activa CDB **e** LCI no grupo Subtipo, **Then** a tabela inclui activos que sejam CDB **ou** LCI (OR intra-grupo Subtipo).
4. **Given** filtros activos em Classe, Subtipo, Liquidez, B3, Liquidados e Vence até em simultâneo, **When** a listagem é calculada, **Then** cada activo deve satisfazer **todos** os grupos que têm pelo menos uma selecção activa (AND entre grupos), respeitando OR dentro de cada grupo; **excepto** «Liquidez» e «Vence até», que **só** avaliam activos de renda fixa (RV e fundos ignoram esses grupos).
5. **Given** «Renda Fixa» e «Renda Variável» activas em Classe **e** um mês concreto em «Vence até», **When** a listagem é calculada, **Then** entram activos RV que passam nos restantes grupos **e** activos RF cujo vencimento é até o mês seleccionado.

---

### User Story 4 - Substituir controlos legados pelo painel unificado (Priority: P2)

O utilizador deixa de ver os controlos antigos de **categoria**, **liquidez** e **meta financeira** (selecção segmentada única) e passa a usar **apenas** o painel de filtros da carteira já desenhado, integrado ao fluxo de carregamento do histórico.

**Why this priority**: Unifica a experiência de filtragem e evita regras contraditórias entre dois conjuntos de controlos.

**Independent Test**: Abrir histórico e confirmar ausência dos três controlos segmentados; alterar filtros no painel e observar actualização da tabela e sumário sem recorrer aos controlos removidos.

**Acceptance Scenarios**:

1. **Given** o ecrã de histórico, **When** renderizado, **Then** **não** são exibidos os controlos segmentados legados de categoria, liquidez e meta financeira.
2. **Given** o painel de filtros da carteira visível, **When** o utilizador altera qualquer selecção, **Then** o estado centralizado do histórico reflecte a alteração, a listagem e o sumário são recalculados, e a UI actualiza a partir desse estado.
3. **Given** opções derivadas dos dados do período (conforme feature de filtros da carteira), **When** o histórico carrega, **Then** o painel mostra **apenas** opções aplicáveis aos activos daquele mês — sem secções vazias.

---

### User Story 5 - Sumário coerente com a listagem filtrada (Priority: P2)

O utilizador confia que totais de património, aportes, resgates e valorização no sumário correspondem **exactamente** ao conjunto de linhas visíveis na tabela após filtrar — **incluindo** aportes e resgates calculados só a partir dos activos filtrados.

**Why this priority**: Evita discrepâncias que minam confiança nos números.

**Independent Test**: Aplicar filtro que reduz a lista a metade dos activos; comparar manualmente soma dos valores da tabela com totais do sumário.

**Acceptance Scenarios**:

1. **Given** filtros que excluem parte dos activos, **When** o sumário é exibido, **Then** património, aportes, resgates e valorização reflectem **somente** os activos que passaram nos filtros activos (sem incluir transacções de activos ocultos).
2. **Given** filtros que excluem **todos** os activos, **When** a tabela fica vazia, **Then** o sumário apresenta totais coerentes com lista vazia (zeros ou equivalente definido pelo produto).

---

### Edge Cases

- Quando **todos** os grupos estão inactivos (incluindo Liquidados sem selecção e vencimento «Qualquer»), a listagem inclui **todas** as posições do período, **incluindo** liquidadas/zeradas.
- Posições com património zero anterior **e** actual **não** são excluídas por regra implícita — só pelo grupo **Liquidados** (alinhado a `isLiquidado` / posição encerrada no mês).
- Quando um grupo tem **todas** as opções possíveis seleccionadas (ex.: Sim **e** Não em B3), o efeito desse grupo deve ser equivalente a **não** aplicar filtro nessa dimensão — o resultado não deve ficar vazio por contradição interna.
- Ao **desactivar** uma classe, subtipos dessa classe deixam de influenciar o filtro (estado limpo ou ignorado), mesmo que ainda visíveis momentaneamente na UI até actualização.
- Activos **liquidados** vs **não liquidados**: filtro «Liquidados» deve alinhar-se ao significado já usado na visualização de histórico (posição encerrada no mês de referência).
- **Liquidez** com opções activas: **só** activos de **renda fixa** são avaliados; RV e fundos **nunca** são excluídos por este grupo.
- **Vence até** com mês concreto: **só** activos de **renda fixa** são avaliados; RV e fundos **nunca** são excluídos por este grupo (mesmo sem data de vencimento).
- Activos RF cujo vencimento é **posterior** ao mês seleccionado em «Vence até» são **excluídos** quando esse grupo está activo.
- Alteração do **período** (mês/ano) **DEVE** repor o painel ao **estado por defeito** (reset com «Não liquidado» pré-seleccionado) **e** recalcular opções derivadas dos dados do novo mês.
- Lista filtrada vazia: utilizador vê tabela vazia com sumário coerente, **sem** erro bloqueante.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema **DEVE** aplicar filtros do painel da carteira à listagem de activos do histórico do período seleccionado.
- **FR-002**: O **estado por defeito** do painel **DEVE** ter **«Não liquidado»** pré-seleccionado em Liquidados (restantes grupos inactivos; vencimento «Qualquer vencimento»); a listagem inicial **DEVE** exibir **posições não liquidadas** do período.
- **FR-002a**: **Não** **DEVE** existir exclusão implícita de posições com património zero — visibilidade de liquidados/zerados **DEVE** ser controlada **apenas** pelo grupo **Liquidados**.
- **FR-003**: Quando existe **exactamente uma** opção activa num grupo, o sistema **DEVE** restringir a listagem aos activos que satisfazem essa opção.
- **FR-004**: Quando existem **múltiplas** opções activas **no mesmo grupo**, o sistema **DEVE** incluir activos que satisfaçam **qualquer** uma delas (operador lógico **OR** intra-grupo).
- **FR-005**: Quando existem opções activas **em grupos diferentes**, o sistema **DEVE** incluir **apenas** activos que satisfaçam **simultaneamente** todos os grupos com selecção activa (operador lógico **AND** inter-grupo).
- **FR-006**: Os **grupos de filtro** reconhecidos para OR/AND **DEVEM** ser: **Classe**, **Subtipo**, **Liquidez**, **B3 informado**, **Liquidados** e **Vence até** (activo apenas quando um mês concreto está seleccionado, não em «Qualquer vencimento»).
- **FR-006a**: Os grupos **Liquidez** e **Vence até** **DEVEM** aplicar-se **exclusivamente** a activos de **renda fixa**; activos de renda variável e fundos **NÃO DEVEM** ser filtrados nem excluídos por liquidez ou vencimento.
- **FR-007**: A lógica de filtragem **DEVE** ser **centralizada** num único ponto responsável por produzir a listagem de histórico, **testável independentemente** da interface, **sem** duplicar regras OR/AND na apresentação.
- **FR-008**: A interface **DEVE** remover os controlos segmentados legados de **categoria**, **liquidez** e **meta financeira** do ecrã de histórico.
- **FR-009**: O painel de filtros da carteira **DEVE** permanecer a única superfície de filtragem por classe, subtipo, liquidez, B3, liquidados e vencimento no histórico.
- **FR-010**: Alterações no painel **DEVEM** provocar recálculo da listagem e do sumário com base nos mesmos critérios; o estado dos filtros **DEVE** ser **centralizado** no fluxo do histórico (fonte única de verdade), orquestrando o recarregamento **sem** duplicar selecções na UI.
- **FR-010a**: O sumário **DEVE** agregar património, aportes, resgates e valorização **exclusivamente** a partir dos activos visíveis na tabela filtrada — **não** do período completo quando filtros estão activos.
- **FR-011**: A acção «Resetar» **DEVE** repor o **estado por defeito** do painel (incluindo «Não liquidado» activo) e a listagem correspondente.
- **FR-011a**: Ao **mudar o período** (mês/ano), o sistema **DEVE** repor o **estado por defeito** do painel antes de carregar os dados do novo período.
- **FR-012**: As opções visíveis no painel **DEVEM** continuar **derivadas dos activos presentes** no período (ocultar grupos/opções sem variación ou sem dados), conforme já especificado para o painel de filtros da carteira.
- **FR-013**: Ao desactivar uma **classe**, quaisquer **subtipos** dessa classe **NÃO DEVEM** afectar o resultado do filtro.
- **FR-014**: O sistema **DEVE** incluir **testes automatizados** da lógica de filtragem que cubram combinações representativas de OR intra-grupo, AND inter-grupo, estado vazio e reset lógico equivalente.

### Key Entities

- **Critério de filtro da carteira**: Dimensão avaliável de um activo no histórico — classe, subtipo, liquidez (**só RF**), informação B3, estado liquidado, vencimento até (**só RF**).
- **Grupo de filtros**: Conjunto de opções mutuamente combináveis com OR (ex.: todas as opções de «Classe» pertencem ao mesmo grupo).
- **Selecção de filtros**: Conjunto de opções activas por grupo mais vencimento seleccionado; mantido no **estado centralizado do histórico** e passado ao cálculo da listagem.
- **Linha de histórico**: Representação de um activo-posição no mês de referência, com atributos mapeáveis aos critérios de filtro.
- **Facetas do activo para filtro**: Projeção normalizada dos atributos de cada linha usada para comparar com os critérios (classe, subtipo, liquidez em RF, B3, liquidado, mês de vencimento em RF).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em testes de aceitação com carteira de demonstração (≥3 classes, ≥2 subtipos por classe, mix B3 e liquidados), **100%** dos cenários documentados nas user stories 1–3 produzem a listagem esperada.
- **SC-002**: Após aplicar filtros, o utilizador vê a tabela actualizada em **menos de 1 segundo** em condições normais de uso (carteira pessoal típica), sem bloqueio perceptível da interface.
- **SC-003**: Os totais do sumário coincidem com a agregação manual das linhas visíveis em **100%** dos casos de teste definidos para a user story 5.
- **SC-004**: **Zero** ocorrências dos controlos segmentados legados (categoria, liquidez, meta financeira) no ecrã de histórico após entrega.
- **SC-005**: A suíte de testes automatizados da filtragem cobre **pelo menos** 9 cenários de contrato (T1–T9) mais OR de subtipo — ver `contracts/HistoryWalletFiltersContract.md` (vazio, single, OR, AND, vencimento, liquidados, default/reset, grupo saturado Sim+Não).

## Assumptions

- O painel visual de filtros da carteira (feature `014-wallet-filters`) **já existe**; esta feature **integra-o** ao histórico e **não** redesenha o painel.
- O filtro por **meta financeira** deixa de estar disponível no histórico nesta entrega — não há grupo equivalente no painel; utilizadores que dependiam desse critério passam a usar outras dimensões ou funcionalidade futura.
- Filtro por **corretora** permanece fora do painel da carteira; se existir noutro local da app, **não** é alterado nesta feature salvo conflito técnico detectado na implementação.
- Ao **mudar o período**, o painel repõe o **estado por defeito** («Não liquidado» activo); opções derivadas reflectem os activos do novo mês.
- Posições liquidadas/zeradas **não** são ocultadas por regra implícita — só via grupo **Liquidados**; por defeito «Não liquidado» está activo.
- «Liquidez» e «Vence até» **só afectam renda fixa**; RV e fundos tratam esses grupos como **transparentes** (sempre passam nessas dimensões).
- «Vence até» com **Qualquer vencimento** equivale a **grupo inactivo** para efeitos de AND/OR.
- Seleccionar **Sim** e **Não** no mesmo grupo binário (B3 ou Liquidados) **não restringe** essa dimensão — equivalente a nenhuma selecção efectiva nesse grupo.
- Derivação de opções e comportamento multi-selecção do painel mantêm-se alinhados à especificação `014-wallet-filters`.
- O estado dos filtros **vive no HistoryViewModel** (`StateFlow`); o ecrã **não** mantém estado local autónomo de filtros (`remember` só para UI efémera, se necessário).
- **Critérios de filtro no domínio**: `WalletHistoryFilterCriteria` em `:domain:usecases`; mapeamento `WalletFiltersUiState → criteria` no pacote `history` do `composeApp` (ver `plan.md` e `data-model.md`).

## Dependencies

- Feature **014-wallet-filters** (painel, estado UI, derivação de opções, mapeamento activo → facetas).
- Serviço existente que agrega histórico mensal por activo-posição (fonte actual da tabela de histórico).
- Ecrã de histórico e sumário já consumidores dessa listagem.
