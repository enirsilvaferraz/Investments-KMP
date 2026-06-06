# Feature Specification: Balanceamento de carteira

**Feature Branch**: `024-portfolio-balancing`

**Created**: 2026-06-06

**Status**: Draft

**Input**: User description: "Criar o conceito de balanceamento no projeto — alocação alvo por grupos/componentes com pesos, cálculo de valor atual vs. ideal, acionamento a partir do histórico com saída em tabela no log (sem ecrã dedicado na v1)."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Calcular desvios da alocação alvo da carteira total (Priority: P1)

Quem gere investimentos precisa saber, para o mês de referência actual, quanto dinheiro está hoje em cada **componente** da **Carteira Total** e quanto **deveria** estar segundo os pesos definidos — para decidir se compra ou vende activos desse segmento.

**Why this priority**: É o núcleo do balanceamento e responde directamente à pergunta «estou dentro ou fora da meta?» no nível mais alto.

**Independent Test**: Com posições conhecidas em renda fixa, renda variável (com e sem HASH11), fundos de previdência e outros, executar o cálculo e verificar que cada linha do relatório traz nome, regra, valor actual, peso alvo e valor ideal coerentes com o total investido do período.

**Acceptance Scenarios**:

1. **Given** posições activas (património > 0) no mês seleccionado, **When** o utilizador solicita o balanceamento, **Then** o relatório inclui uma linha por componente do Grupo «Carteira Total» com: nome, descrição da regra, soma dos valores actuais, peso alvo e valor ideal.
2. **Given** um componente com peso fixo de 50% e carteira total de R$ 100.000, **When** o cálculo é executado, **Then** o valor ideal desse componente é R$ 50.000.
3. **Given** o componente «Fundos de Previdência» com regra dinâmica, **When** o cálculo é executado, **Then** o peso alvo reflecte a participação actual desse segmento no total da carteira e o valor ideal coincide com o valor actual (desvio zero por desenho).
4. **Given** o componente «Demais investimentos» com peso 0%, **When** o cálculo é executado, **Then** o valor ideal é zero, indicando que o segmento deveria ser eliminado da carteira.
5. **Given** posições liquidadas (património zero no mês), **When** o cálculo é executado, **Then** essas posições **não** entram nas somas de valor actual.

---

### User Story 2 - Balanceamento aninhado por classe (Renda Fixa e Renda Variável) (Priority: P1)

Quem já cumpre a meta global de renda fixa ou renda variável precisa decompor **dentro** de cada classe se pós-fixados, pré-fixados, FIIs, acções nacionais, etc. estão proporcionais aos pesos internos.

**Why this priority**: Complementa a visão global com acção concreta dentro de cada classe de activo.

**Independent Test**: Com carteira só de renda fixa distribuída entre indexadores, executar o cálculo e confirmar que os componentes do Grupo «Renda Fixa» somam o total de RF e que cada valor ideal é 33,33% desse universo.

**Acceptance Scenarios**:

1. **Given** R$ 60.000 em renda fixa activa, **When** o cálculo inclui o Grupo «Renda Fixa», **Then** o componente «Pós-fixados» tem valor ideal de R$ 20.000 (33,33% de 100% da RF).
2. **Given** activos de renda variável classificados por sub-regra (nacionais, IVVB11, FIIs, outros), **When** o cálculo inclui o Grupo «Renda Variável», **Then** cada componente usa como base de referência **100% da renda variável activa** (excluindo HASH11, que pertence ao Grupo Carteira Total), e os valores ideais reflectem os pesos 50%, 10%, 30% e 10% respectivamente.
3. **Given** um activo de RV com ticker HASH11, **When** é classificado, **Then** entra no componente «Cripto Ativos» do Grupo Carteira Total e **não** entra nos componentes do Grupo Renda Variável.

---

### User Story 3 - Acionar cálculo a partir do histórico e ler resultado no log (Priority: P2)

O utilizador, na tela de histórico, precisa disparar o cálculo sem navegar para outro ecrã e ver o resultado de forma legível — por ora numa tabela formatada na saída de diagnóstico (log), não num painel visual dedicado.

**Why this priority**: Entrega o fluxo mínimo pedido (botão ao lado de importar) sem expandir o escopo para UI de balanceamento.

**Independent Test**: Abrir histórico, tocar no novo controlo, verificar que o log contém tabela alinhada com colunas nome, regra, valor actual, peso, valor ideal, agrupada por grupo de balanceamento.

**Acceptance Scenarios**:

1. **Given** o ecrã de histórico com barra de acções existente, **When** a feature é entregue, **Then** existe um controlo com ícone **adjacente** ao botão de importar B3 que dispara o cálculo de balanceamento.
2. **Given** o utilizador activa o controlo, **When** o cálculo termina com sucesso, **Then** a saída de diagnóstico apresenta **uma tabela** legível (colunas alinhadas, cabeçalhos claros, separação por grupo) com todas as linhas do relatório.
3. **Given** o mês de referência seleccionado no histórico, **When** o balanceamento é calculado, **Then** usa **as mesmas posições e valores de mercado** (valor fim de mês × quantidade) que alimentam a tabela de histórico desse período.
4. **Given** erro ao obter dados (ex.: sem posições no período), **When** o utilizador activa o controlo, **Then** o log regista mensagem compreensível; **não** falha silenciosamente.

---

### Edge Cases

- Carteira total **zero** (todas liquidadas ou sem posições) → relatório vazio ou mensagem explícita; pesos e valores ideais não produzem divisão inválida.
- Ativo que satisfaz **mais de uma** regra candidata → aplica-se a **primeira** regra mais específica na ordem de precedência definida (ticker específico > subtipo > classe > demais).
- Ativo não enquadrado em RF, RV (sem HASH11), previdência ou cripto → componente «Demais investimentos» (peso 0%).
- Soma dos pesos fixos num grupo **≠ 100%** por desenho intencional (ex.: previdência dinâmica) → o sistema **não** normaliza automaticamente; calcula cada componente segundo a sua regra documentada.
- Alteração futura da definição de grupos (hoje fixa, amanhã persistida) → a arquitectura do catálogo deve permitir acrescentar/remover grupos e componentes **sem** reescrever a lógica de cálculo (ver Assumptions).
- Posição com valor negativo (se existir) → tratada como património da posição; componentes com total negativo reflectem-no no valor actual.
- Filtros activos no histórico → o balanceamento considera **todas** as posições do mês de referência, **independentemente** dos filtros visuais da tabela (decisão de âmbito total da carteira).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE calcular um **relatório de balanceamento** para o mês de referência, contendo uma entrada por **componente** configurado, com os campos: **nome**, **regra aplicada** (texto legível), **valor actual** (soma dos patrimónios das posições enquadradas), **peso alvo** (percentagem ou indicador dinâmico) e **valor ideal** (montante que deveria estar alocado segundo o peso).
- **FR-002**: O **valor actual** de cada componente DEVE ser a soma de `valor fim de mês × quantidade` das posições activas (património > 0) enquadradas pela regra, no mês de referência.
- **FR-003**: O **total da carteira** para cálculos de primeiro nível DEVE ser a soma dos patrimónios activos de **todas** as posições no mês de referência.
- **FR-004**: Para componentes com **peso fixo** expresso em percentagem do universo de referência, o **valor ideal** DEVE ser `universo de referência × (peso ÷ 100)`.
- **FR-005**: Para o componente **Fundos de Previdência** (Grupo Carteira Total), o peso alvo DEVE ser a participação **actual** desse segmento no total da carteira (`valor actual do componente ÷ total da carteira × 100`), e o valor ideal DEVE ser igual ao valor actual.
- **FR-006**: Para componentes com peso **0%**, o valor ideal DEVE ser **zero**.
- **FR-007**: O catálogo inicial DEVE incluir os três **grupos de balanceamento** e componentes ab abaixo, com regras de enquadramento explícitas:

  **Grupo 1 — Carteira Total** (universo = total da carteira):

  | Componente | Peso alvo | Regra de enquadramento |
  |------------|-----------|------------------------|
  | Renda Fixa | 50% | Classe renda fixa |
  | Renda Variável | 40% | Classe renda variável **exceto** ticker HASH11 |
  | Fundos de Previdência | Dinâmico (FR-005) | Fundos com tipo previdência |
  | Cripto Ativos | 1% | Renda variável com ticker HASH11 |
  | Demais investimentos | 0% | Posições não enquadradas nos componentes acima |

  **Grupo 2 — Renda Fixa** (universo = total activo de renda fixa):

  | Componente | Peso alvo | Regra de enquadramento |
  |------------|-----------|------------------------|
  | Pós-fixados | 33,33% | Renda fixa com indexador pós-fixado |
  | Pré-fixado | 33,33% | Renda fixa com indexador pré-fixado |
  | Atrelado a inflação | 33,33% | Renda fixa com indexador atrelado à inflação |

  **Grupo 3 — Renda Variável** (universo = total activo de RV **sem** HASH11):

  | Componente | Peso alvo | Regra de enquadramento |
  |------------|-----------|------------------------|
  | Ações Nacionais | 50% | RV tipo ação nacional, excluindo tickers HASH11 e IVVB11 |
  | Ações Internacionais | 10% | RV com ticker IVVB11 |
  | FIIs | 30% | RV tipo fundo imobiliário |
  | Outros RV | 10% | RV activa no universo do grupo não enquadrada nos três componentes anteriores |

- **FR-008**: A definição de grupos e componentes DEVE residir num **catálogo central** (inicialmente fixo no código) estruturado de forma a permitir **acrescentar, remover ou alterar** entradas com alteração localizada — preparado para persistência futura sem mudar o contrato do relatório.
- **FR-009**: O cálculo DEVE estar disponível como **operação reutilizável** no domínio (entrada: posições/valores do período; saída: lista tipada de linhas do relatório), testável de forma isolada.
- **FR-010**: Na tela de histórico, DEVE existir um **controlo dedicado** (ícone) imediatamente **ao lado** do botão de importar B3 que invoca o cálculo e emite o relatório formatado como **tabela na saída de diagnóstico**.
- **FR-011**: Esta entrega **NÃO** inclui ecrã, gráfico ou painel visual de balanceamento — apenas o relatório em log.
- **FR-012**: O relatório em log DEVE incluir cabeçalhos de coluna, alinhamento consistente e separação visual entre grupos para leitura rápida.
- **FR-013**: A feature DEVE incluir testes automatizados que cubram: peso fixo, peso dinâmico de previdência, peso zero, exclusão de HASH11 da RV, enquadramento por indexador RF, e carteira vazia.

### Key Entities

- **Grupo de balanceamento**: Agrupador lógico (Carteira Total, Renda Fixa, Renda Variável) que define o **universo de referência** para os pesos dos seus componentes.
- **Componente de balanceamento**: Unidade acionável do relatório — nome, regra de classificação de posições, peso alvo (fixo, zero ou dinâmico) e resultado calculado.
- **Regra de enquadramento**: Critério determinístico que decide se uma posição pertence a um componente (classe de activo, subtipo, indexador, ticker, ou fallback «demais»).
- **Linha do relatório de balanceamento**: Registo com nome, regra, valor actual, peso alvo e valor ideal; opcionalmente identificação do grupo pai para apresentação.
- **Catálogo de balanceamento**: Conjunto configurável (v1: estático) de grupos, componentes, pesos e regras.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em cenários de teste com carteira fixa documentada, **100%** das linhas do relatório apresentam valor ideal coerente com a fórmula de peso fixo ou dinâmico definida em FR-004/FR-005/FR-006.
- **SC-002**: O utilizador consegue, a partir do histórico, obter o relatório completo dos **11 componentes** configurados em **menos de 5 segundos** após o toque no controlo (inclui cálculo e escrita no log), em carteira típica até **200 posições activas**.
- **SC-003**: A tabela no log permite identificar, sem ferramentas adicionais, qual componente está **acima ou abaixo** do ideal em **100%** dos casos de teste de aceitação (colunas legíveis e diferença inferível entre valor actual e ideal).
- **SC-004**: Acrescentar um novo componente ao catálogo (em teste de extensibilidade) exige alteração em **um único ponto de configuração** do catálogo, sem modificar a fórmula genérica de cálculo.

## Assumptions

- **Base de valor**: Património de posição = valor fim de mês × quantidade, alinhado ao histórico mensal existente.
- **Período**: Mês de referência actualmente seleccionado no histórico; posições liquidadas (património zero) ficam de fora das somas.
- **Âmbito**: Balanceamento sobre a carteira **completa** do período, ignorando filtros visuais do histórico.
- **Previdência dinâmica**: Meta «manter como está» — não força percentagem fixa; desvio sempre zero por construção.
- **Outros RV (10%)**: Componente explícito para completar os 100% do Grupo Renda Variável; cobre ETFs e RV não enquadrados em nacionais, IVVB11 ou FIIs.
- **Precedência de regras**: Ticker específico (HASH11, IVVB11) prevalece sobre classificação genérica por subtipo.
- **Persistência futura**: Catálogo em código na v1; modelo de dados do relatório e do catálogo estável para migração posterior para base de dados.
- **Sem acções automáticas**: O relatório **informa** desvios; não executa ordens de compra/venda.
- **Fora de âmbito**: Ecrã de balanceamento, edição de pesos pelo utilizador, recomendações de quantidade/ticker a negociar, impostos, corretagem e simulação Monte Carlo.
