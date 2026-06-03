# Feature Specification: Filtragem de histórico por critérios unificados (incl. corretora)

**Feature Branch**: `018-holding-history-filter`

**Created**: 2026-06-03

**Status**: Draft

**Input**: User description: "Criar um use case capaz de receber uma lista de registos de histórico mensal de posição (`HoldingHistoryEntry`) e filtrar os itens com base em `WalletHistoryFilterCriteria`. Atualizar o critério de filtro para incluir corretora com as mesmas regras dos demais grupos. Não haverá alteração visual nesta feature."

## Clarifications

### Session 2026-06-03

- Q: Ao derivar facetas/opções do painel de filtros, a corretora seleccionada deve restringir o universo (como hoje) ou ignorar-se? → A: **Como hoje** — facetas calculam-se sobre activos já filtrados pela corretora seleccionada; sem corretora → todas as corretoras do período.
- Q: Ao mudar o período (mês/ano), a selecção de corretora deve manter-se ou limpar? → B: **Reset total** — repor painel ao defeito **e** limpar corretora (equivalente a «todas»), alinhado ao reset de filtros da feature 015.
- Q: Como determinar «Liquidados» ao filtrar um único registo mensal? → A: **Património do mês do registo** — liquidado quando valor fim de mês × quantidade == 0 nesse registo (sem mês anterior).
- Q: Como integrar o novo filtro em `GetHistoryTableDataUseCase`? → C: **Migração completa** — após merge, filtrar entradas do mês de referência via novo use case; mapear só as que passam; remover filtro paralelo em `HoldingHistoryResult` e parâmetro de corretora à parte.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Filtrar registos de histórico por critérios já usados na carteira (Priority: P1)

Quem analisa a carteira precisa que a mesma lógica de filtros do histórico (classe, subtipo, liquidez, B3, liquidados, vencimento) possa ser aplicada **directamente** a uma lista de **registos de histórico mensal** de posições, sem depender de transformações intermediárias só para filtrar — e que a tabela de histórico use **esse** caminho como única fonte de verdade para critérios do painel. A dimensão **corretora** no mesmo pacote de critérios é objectivo da **User Story 2** (unificação com o selector existente).

**Why this priority**: Entrega o núcleo reutilizável pedido — filtragem sobre o modelo de histórico — e desbloqueia consolidar regras num único ponto.

**Independent Test**: Dada uma lista fixa de registos de histório com perfis variados, aplicar critérios conhecidos (ex.: só «Não liquidado») e verificar que o conjunto devolvido coincide com as regras OR/AND já documentadas na feature de filtros do histórico.

**Acceptance Scenarios**:

1. **Given** uma lista com posições activas e liquidadas no mesmo período, **When** o critério activa apenas «Não liquidado» em Liquidados, **Then** o resultado contém **somente** registos de posições não liquidadas.
2. **Given** uma lista mista por classe de ativo, **When** o critério activa apenas «Renda Fixa» em Classe, **Then** o resultado contém **somente** registos cuja posição é de renda fixa.
3. **Given** critérios vazios em todos os grupos (estado totalmente inactivo), **When** a filtragem é executada, **Then** o resultado é **igual** à lista de entrada (nenhum registo removido por critério).

---

### User Story 2 - Incluir corretora no mesmo modelo de critérios (Priority: P1)

O utilizador que já filtra o histórico por corretora espera que essa dimensão faça parte do **mesmo pacote de critérios** que os restantes grupos do painel de filtros da carteira — com **OR** entre corretoras seleccionadas no grupo (no domínio) e **AND** com os outros grupos activos — em vez de um canal paralelo só na camada de carregamento da tabela.

**Seleção de corretora (UI)**: permanece **single selection** — no máximo **uma** corretora activa de cada vez, com desmarcar para voltar a «todas»; não há multi-selecção de corretoras nesta feature nem no painel de filtros da carteira.

**Why this priority**: Alinha corretora ao contrato de filtros existente e elimina duplicação de regras entre «corretora à parte» e «critérios da carteira».

**Independent Test**: Com lista contendo a mesma posição em corretoras distintas, activar critério de uma corretora e confirmar que só entram registos dessa corretora; activar duas corretoras no critério e confirmar união (OR); combinar com classe e confirmar intersecção (AND).

**Acceptance Scenarios**:

1. **Given** registos em corretoras A e B, **When** o critério inclui **apenas** a corretora A no grupo Corretora, **Then** o resultado contém **somente** registos cuja posição está na corretora A.
2. **Given** registos em corretoras A, B e C, **When** o critério inclui A **e** B no grupo Corretora (e nenhum outro grupo activo), **Then** o resultado inclui registos de A **ou** B (OR intra-grupo).
3. **Given** registos RF na corretora A e RV na corretora B, **When** o critério inclui corretora A **e** classe «Renda Fixa», **Then** o resultado inclui **apenas** registos RF na corretora A (AND inter-grupo).
4. **Given** o grupo Corretora **sem** selecção activa, **When** a filtragem é executada, **Then** a dimensão corretora **não** exclui nenhum registo.

---

### User Story 3 - Comportamento inalterado na interface (Priority: P2)

O utilizador continua a ver e a usar os **mesmos** controlos de histórico que já existem (incluindo a selecção de corretora actual); apenas o **cálculo** por detrás passa a usar o critério unificado.

**Why this priority**: Limita o risco de regressão de UX e cumpre o pedido explícito de não alterar o visual nesta entrega.

**Independent Test**: Comparar ecrã de histórico antes/depois em fluxos típicos (abrir, mudar corretora, mudar filtros do painel, mudar mês) — layout, rótulos e controlos permanecem iguais; apenas os dados reflectidos podem mudar se a unificação corrigir inconsistências internas.

**Acceptance Scenarios**:

1. **Given** o ecrã de histórico renderizado, **When** a feature é entregue, **Then** **não** são adicionados, removidos nem redesenhados componentes visuais (painel de filtros da carteira, selector de corretora, tabela, sumário).
2. **Given** o utilizador selecciona uma corretora como hoje (selecção única com desmarcar), **When** a listagem é recalculada, **Then** o efeito na tabela é **equivalente** ao comportamento anterior de filtrar por essa corretora.
3. **Given** uma corretora seleccionada no segment control, **When** o sistema deriva opções do painel de filtros (facetas), **Then** as secções mostram **apenas** dimensões presentes nos activos dessa corretora no período (paridade com o fluxo actual de facetas).
4. **Given** uma corretora seleccionada, **When** o utilizador muda o período (mês/ano), **Then** a corretora é **limpa** (todas as corretoras), o painel repõe o estado por defeito e a listagem/facetas reflectem o novo período sem filtro de corretora.

---

### Edge Cases

- Lista de entrada **vazia** → resultado vazio, sem erro.
- Critério com **todas** as corretoras representadas na lista seleccionadas no grupo Corretora → efeito equivalente a grupo inactivo (não excluir por corretora), alinhado à regra de grupo saturado já usada em B3/Liquidados.
- Corretora no critério referencia identificador que **não** existe em nenhum registo da lista → resultado vazio quando esse grupo está activo.
- Combinação de corretora com grupos **só RF** (liquidez, vencimento): RV/fundos ignoram liquidez/vencimento mas **são** avaliados por corretora quando esse grupo está activo.
- Registos com a mesma posição lógica em corretoras diferentes são tratados como entradas distintas (filtro pela corretora da posição em cada registo).
- Critérios `defaultForHistory()` continuam a excluir liquidados por defeito **sem** exigir selecção de corretora.
- Derivação de facetas com corretora activa e lista sem activos dessa corretora → painel sem opções aplicáveis (secções vazias ou equivalente actual), sem erro bloqueante.
- Mudança de período com corretora activa → reset de corretora e filtros; facetas passam a reflectir **todas** as corretoras do novo período.
- Registo com valor ou quantidade negativos e produto zero → tratado como liquidado (património zero), alinhado à regra actual da tabela.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE disponibilizar um **caso de uso dedicado** que recebe uma **lista de registos de histórico mensal de posição** (`HoldingHistoryEntry`) e **critérios de filtro da carteira**, e devolve a **sublista** que satisfaz esses critérios (reutilizando `matchesWalletHistoryFilter` sobre candidato derivado do registo).
- **FR-002**: A avaliação DEVE reutilizar as **mesmas regras** já definidas para o histórico: nenhuma selecção num grupo → grupo inactivo; uma ou mais opções no mesmo grupo → **OR**; grupos diferentes activos → **AND**; grupos saturados (todas as opções relevantes seleccionadas) → equivalente a inactivo para essa dimensão.
- **FR-003**: Os critérios de filtro DEVE incluir o grupo **Corretora** como conjunto de identificadores de corretora seleccionados, com semântica OR intra-grupo e AND com os restantes grupos. Na interface do histórico, a origem desse conjunto é **seleção única**: 0 corretoras (grupo inactivo) ou exactamente 1 corretora activa — nunca várias corretoras seleccionadas pelo utilizador nesta entrega.
- **FR-004**: Cada registo de histórico DEVE ser avaliado a partir dos atributos da **posição** associada (classe, subtipo, liquidez, B3, estado liquidado, vencimento, **corretora**) no mês de referência do registo. **Liquidado** no mês: património do registo (`valor fim de mês × quantidade`) **igual a zero** — sem exigir registo do mês anterior.
- **FR-005**: O carregamento da tabela de histórico DEVE, após `MergeHistoryUseCase`, aplicar o **novo caso de uso** sobre as **entradas do mês de referência** (`currentEntry` de cada par), com critérios unificados (incl. corretora), e **mapear para linhas só os pares cuja entrada actual passa** — removendo `filter` paralelo em `HoldingHistoryResult`, `toWalletHistoryFilterCandidate()` nesse fluxo e o parâmetro `brokerage` separado em `Param`.
- **FR-006**: O mapeamento desde o estado actual do ecrã de histórico (selector de corretora em **single selection** + estado do painel de filtros) DEVE popular o grupo Corretora nos critérios com **no máximo um** identificador: sem selecção → conjunto vazio (grupo inactivo); com selecção → conjunto de um elemento; alternar para a mesma corretora desmarca (equivalente a grupo inactivo).
- **FR-007**: Esta entrega **NÃO** DEVE alterar layout, componentes, textos nem fluxos visuais do histórico ou do painel de filtros da carteira.
- **FR-008**: A feature DEVE incluir testes automatizados que cubram filtragem por corretora (isolada e combinada com pelo menos um outro grupo) e regressão dos cenários contratuais já existentes para critérios sem corretora.
- **FR-009**: A derivação de **facetas e opções do painel** DEVE usar critérios com grupos do painel inactivos (sem classe, subtipo, liquidez, B3, liquidados, vencimento) **mas** com o grupo **Corretora** igual à selecção do utilizador: com corretora seleccionada → universo restrito a essa corretora; sem selecção → todas as corretoras do período. Os critérios completos do painel aplicam-se **só** à listagem e ao sumário da tabela.
- **FR-010**: Ao **mudar o período** no histórico, o sistema DEVE repor o painel de filtros ao estado por defeito (`defaultForHistory()`) **e** limpar a selecção de corretora (grupo Corretora inactivo), em linha com o reset de filtros da feature 015.
- **FR-011**: A filtragem na tabela DEVE avaliar **apenas o registo do mês de referência** de cada posição; o registo do mês anterior permanece disponível **só** para cálculo de colunas (património anterior, valorização), não para critérios de filtro.

### Key Entities

- **Registo de histórico de posição**: Snapshot mensal de uma posição (valor, quantidade, mês de referência) ligado à posição que traz ativo, corretora e demais atributos de filtragem.
- **Critérios de filtro da carteira (histórico)**: Pacote de dimensões seleccionáveis — classe, subtipo, liquidez, B3, liquidados, vencimento até, **corretoras** — com regras OR/AND partilhadas. O grupo **corretoras** é alimentado pelo histórico em **single selection** (0 ou 1 id); os restantes grupos do painel mantêm multi-selecção conforme a feature 014/015.
- **Candidato a filtro**: Projeção normalizada dos atributos filtráveis de um registo/posição usada pela função de correspondência aos critérios; flag **liquidado** derivada do património zero no próprio registo (FR-004).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em cenários de teste com lista fixa de pelo menos 10 registos heterogéneos, **100%** dos casos contratados de corretora (isolada, OR de duas corretoras, AND com classe, grupo inactivo) produzem o subconjunto esperado.
- **SC-002**: Para os **9+** cenários de regressão já definidos para critérios sem corretora, o resultado da nova operação permanece **idêntico** ao comportamento actual após migração.
- **SC-003**: Com a mesma selecção de corretora e período (sem mudança de período entretanto), a listagem pós-entrega coincide com a listagem **pré-entrega** em **100%** dos casos de aceitação da história de utilizador 3; após mudança de período, corretora limpa e filtros por defeito conforme FR-010.
- **SC-004**: A filtragem de uma lista de até **500** registos com todos os grupos activos completa em tempo imperceptível para o utilizador (subsegundo em dispositivo de referência do produto) — medido por testes de desempenho ou cenários representativos, não por métricas de infraestrutura.

## Assumptions

- As regras OR/AND, grupos só-RF (liquidez, vencimento), liquidados e `defaultForHistory()` permanecem como na feature **015-history-wallet-filters**; esta feature **estende** o modelo, não o redefine.
- **Corretora = single selection** (contrato desta feature): o control segmentado do histórico permite 0 ou 1 corretora; o critério representa isso como conjunto vazio ou singleton. Multi-selecção de corretoras na UI fica **fora de âmbito** — o modelo de domínio pode aceitar vários ids (OR) para testes e extensão futura, mas o produto não expõe mais de uma corretora activa ao utilizador.
- Identificação de corretora nos critérios usa o **identificador estável** da corretora (não apenas o nome exibido).
- Listas passadas à operação já reflectem o período desejado; a operação **não** altera ordenação nem agrega sumários — apenas filtra.
- **Facetas do painel**: calculadas sobre o subconjunto já filtrado pela **corretora** (critério de facetas com painel inactivo + corretora conforme FR-009), não sobre a carteira completa do período quando há corretora seleccionada.
- O sumário da tabela reflecte os critérios **completos** (painel + corretora); o pipeline da tabela filtra **só** via o novo caso de uso sobre entradas do mês (FR-005/011), sem duplicar regras em `HoldingHistoryResult`.
- **Mudança de período**: reset de corretora é **comportamento novo** face ao código actual (antes a corretora mantinha-se); adoptado por decisão de produto (clarificação B).
- **Liquidados**: mesma regra que a tabela actual — património zero no mês do registo, não comparação com mês anterior.

## Dependencies

- Feature **015-history-wallet-filters**: contrato de critérios, correspondência (`matches`) e integração com tabela de histórico.
- Feature **017-holding-transactions**: modelo de posição com `holding` em cada registo de histórico (acesso a corretora e transações via posição).
- Entidade **corretora** já existente no domínio de posições.
