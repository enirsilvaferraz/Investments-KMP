# Feature Specification: Balanceamento de carteira

**Feature Branch**: `024-portfolio-balancing`

**Created**: 2026-06-06

**Status**: Draft

**Input**: User description: "Criar o conceito de balanceamento no projeto — alocação alvo por grupos/componentes com pesos, cálculo de valor atual vs. ideal, acionamento a partir do histórico com saída em tabela no log (sem ecrã dedicado na v1)."

## Clarifications

### Session 2026-06-06

- Q: Onde enquadrar fundos de investimento não-previdência (ex.: fundo de ações, multimercado) no Grupo Carteira Total? → A: **Demais investimentos** (peso 0%).
- Q: Como calcular valores ideais em grupos aninhados quando a classe tem valor actual zero mas peso alvo no Grupo 1? → A: Valor **actual** aninhado = zero (sem activos na classe); valor **ideal** aninhado = peso interno × **valor ideal do componente-pai no Grupo 1** (ex.: Pós-fixados ideal = 33,33% × ideal RF; ideal RF = 50% × total carteira).
- Q: A tabela no log deve incluir coluna explícita de desvio? → A: **Sim** — coluna «desvio» = valor actual − valor ideal.
- Q: Desactivar o botão de balanceamento durante o cálculo? → A: **Não** — permitir múltiplos toques; cada acção gera nova tabela no log.
- Q: Classificação exclusiva vs. sobreposição de regras por grupo? → A: Regras **desenhadas** para **não conflituar** nem deixar posições de fora — partição **mutuamente exclusiva e exaustiva** por grupo (cada posição activa em exactamente um componente; **sem** double-count; **sem** resolução por «primeira regra ganha»).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Calcular desvios da alocação alvo da carteira total (Priority: P1)

Quem gere investimentos precisa saber, para o mês de referência actual, quanto dinheiro está hoje em cada **componente** da **Carteira Total** e quanto **deveria** estar segundo os pesos definidos — para decidir se compra ou vende activos desse segmento.

**Why this priority**: É o núcleo do balanceamento e responde directamente à pergunta «estou dentro ou fora da meta?» no nível mais alto.

**Independent Test**: Com posições conhecidas em renda fixa, renda variável (com e sem HASH11), fundos de previdência e outros, executar o cálculo e verificar que cada linha do relatório traz nome, valor actual, peso alvo, valor ideal e desvio coerentes com o total investido do período.

**Acceptance Scenarios**:

1. **Given** posições activas (património > 0) no mês seleccionado, **When** o utilizador solicita o balanceamento, **Then** o relatório inclui uma linha por componente do Grupo «Carteira Total» com: nome, soma dos valores actuais, peso alvo, valor ideal e **desvio** (actual − ideal).
2. **Given** um componente com peso fixo de 50% e carteira total de R$ 100.000, **When** o cálculo é executado, **Then** o valor ideal desse componente é R$ 50.000.
3. **Given** o componente «Fundos de Previdência» com regra dinâmica, **When** o cálculo é executado, **Then** o peso alvo reflecte a participação actual desse segmento no total da carteira e o valor ideal coincide com o valor actual (desvio zero por desenho).
4. **Given** o componente «Demais investimentos» com peso 0%, **When** o cálculo é executado, **Then** o valor ideal é zero, indicando que o segmento deveria ser eliminado da carteira.
5. **Given** posições liquidadas (património zero no mês), **When** o cálculo é executado, **Then** essas posições **não** entram nas somas de valor actual.
6. **Given** todas as posições activas do mês, **When** o cálculo agrega o Grupo «Carteira Total», **Then** a soma dos valores actuais dos componentes = total da carteira (cada posição activa contada **exactamente uma vez** — regras sem sobreposição).

---

### User Story 2 - Balanceamento aninhado por classe (Renda Fixa e Renda Variável) (Priority: P1)

Quem já cumpre a meta global de renda fixa ou renda variável precisa decompor **dentro** de cada classe se pós-fixados, pré-fixados, FIIs, acções nacionais, etc. estão proporcionais aos pesos internos.

**Why this priority**: Complementa a visão global com acção concreta dentro de cada classe de activo.

**Independent Test**: Com carteira só de renda fixa distribuída entre indexadores, executar o cálculo e confirmar que os valores **actuais** dos componentes do Grupo «Renda Fixa» somam o total actual de RF e que cada **valor ideal** é o peso interno aplicado sobre o **ideal** de «Renda Fixa» no Grupo 1 (não sobre o total actual de RF quando divergir).

**Acceptance Scenarios**:

1. **Given** carteira total R$ 100.000 com RF actual **zero** e peso RF 50% no Grupo 1, **When** o cálculo inclui o Grupo «Renda Fixa», **Then** valor actual total de RF = zero, ideal de «Pós-fixados» = 33,33% × R$ 50.000 = R$ 16.667 (33,33% do **ideal** RF, não do actual).
2. **Given** carteira total R$ 100.000 com RF actual R$ 60.000, **When** o cálculo inclui o Grupo «Renda Fixa», **Then** valor actual de «Pós-fixados» = soma real dos pós-fixados; valor ideal de «Pós-fixados» = 33,33% × **ideal RF (R$ 50.000)** = R$ 16.667 — independentemente do actual RF ser R$ 60.000.
3. **Given** activos de renda variável classificados por sub-regra (nacionais, IVVB11, FIIs, outros), **When** o cálculo inclui o Grupo «Renda Variável», **Then** valores **ideais** usam como base o **ideal** de «Renda Variável» no Grupo 1 (40% × total carteira, excl. HASH11 na classificação), e valores **actuais** reflectem a soma real por componente.
4. **Given** um activo de RV com ticker HASH11, **When** é classificado, **Then** entra no componente «Cripto Ativos» do Grupo Carteira Total e **não** entra nos componentes do Grupo Renda Variável.

---

### User Story 3 - Acionar cálculo a partir do histórico e ler resultado no log (Priority: P2)

O utilizador, na tela de histórico, precisa disparar o cálculo sem navegar para outro ecrã e ver o resultado de forma legível — por ora numa tabela formatada na saída de diagnóstico (log), não num painel visual dedicado.

**Why this priority**: Entrega o fluxo mínimo pedido (botão ao lado de importar) sem expandir o escopo para UI de balanceamento.

**Independent Test**: Abrir histórico, tocar no novo controlo, verificar que o log contém tabela alinhada com colunas nome, valor actual, peso alvo, valor ideal e desvio, agrupada por grupo de balanceamento.

**Acceptance Scenarios**:

1. **Given** o ecrã de histórico com barra de acções existente, **When** a feature é entregue, **Then** existe um controlo com ícone **adjacente** ao botão de importar B3 que dispara o cálculo de balanceamento.
2. **Given** o utilizador activa o controlo, **When** o cálculo termina com sucesso, **Then** a saída de diagnóstico apresenta **uma tabela** legível (colunas alinhadas, cabeçalhos claros, separação por grupo) com todas as linhas do relatório — incluindo coluna **desvio** e **sem** coluna de regra de enquadramento.
3. **Given** o mês corrente obtido via `DateProvider.getCurrentYearMonth()`, **When** o balanceamento é calculado, **Then** usa as posições activas e valores de mercado (valor fim de mês × quantidade) do mês corrente — **independentemente** do mês seleccionado no selector visual do histórico (decisão de escopo; ver `research.md` R5 e `plan.md` Constraints).
4. **Given** carteira total zero (sem posições activas ou todas liquidadas), **When** o utilizador activa o controlo, **Then** o log apresenta a **mesma tabela estrutural** com **todos** os grupos e componentes, valores actuais, ideais e desvios a **zero**, pesos alvo conforme configurados (dinâmico → 0%), **sem** erro nem divisão inválida.
5. **Given** falha ao obter dados do período (erro técnico), **When** o utilizador activa o controlo, **Then** o log regista mensagem compreensível; **não** falha silenciosamente.
6. **Given** um cálculo de balanceamento **em curso**, **When** o utilizador activa o controlo novamente, **Then** o botão **permanece activo** e dispara **novo** cálculo (nova tabela no log quando concluir); **não** bloqueia toques repetidos.

---

### Edge Cases

- Carteira total **zero** (todas liquidadas ou sem posições) → apresentar tabela completa com **todos** os grupos e componentes; valor actual, valor ideal e **desvio** **zero** em cada linha; pesos alvo fixos mantêm-se visíveis; peso dinâmico (previdência) → **0%**; sem divisão inválida.
- Classe com valor **actual zero** mas peso alvo no Grupo 1 (ex.: RF 0 actual, 50% alvo) → grupo aninhado **sempre presente**; valores actuais zero; valores ideais calculados sobre o **ideal do componente-pai** no Grupo 1 (ex.: Pós-fixados ideal = 33,33% × 50% × total carteira).
- **Partição de regras** (por grupo): regras do catálogo DEVEM ser **mutuamente exclusivas e exaustivas** — nenhuma posição activa em mais de um componente; nenhuma posição activa sem componente. «Demais investimentos» / «Outros RV» actuam como fallback **exclusivo** do complemento. **Proibido** double-count; **proibido** resolver conflitos em runtime por ordem de precedência — conflitos indicam defeito no catálogo.
- Ativo não enquadrado em RF, RV (sem HASH11), previdência ou cripto → componente «Demais investimentos» (peso 0%), **incluindo** fundos de investimento cujo tipo **não** é previdência.
- Soma dos pesos fixos num grupo **≠ 100%** por desenho intencional (ex.: previdência dinâmica) → o sistema **não** normaliza automaticamente; calcula cada componente segundo a sua regra documentada.
- Alteração futura da definição de grupos (hoje fixa, amanhã persistida) → a arquitectura do catálogo deve permitir acrescentar/remover grupos e componentes **sem** reescrever a lógica de cálculo (ver Assumptions).
- Filtros activos no histórico → o balanceamento usa o mês corrente via `DateProvider` e considera **todas** as posições activas desse mês, **independentemente** dos filtros visuais da tabela e do mês seleccionado no selector (decisão de âmbito total da carteira; ver `plan.md` Constraints).
- Toques repetidos durante cálculo em curso → **permitidos**; cada acção enfileira/dispara novo cálculo e emite nova tabela no log (sem desactivar o botão).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE calcular um **relatório de balanceamento** para o mês de referência, contendo uma entrada por **componente** configurado. Cada componente possui internamente uma **regra de enquadramento** (critério para filtrar/classificar posições); essa regra **não** faz parte da saída apresentada ao utilizador. O relatório expõe: **nome**, **valor actual** (soma dos patrimónios enquadrados), **peso alvo** (percentagem ou indicador dinâmico), **valor ideal** (montante que deveria estar alocado segundo o peso) e **desvio** (`valor actual − valor ideal`).
- **FR-002**: O **valor actual** de cada componente DEVE ser a soma de `valor fim de mês × quantidade` das posições activas (património > 0) enquadradas pela regra, no mês de referência.
- **FR-003**: O **total da carteira** para cálculos de primeiro nível DEVE ser a soma dos patrimónios activos de **todas** as posições no mês de referência.
- **FR-004**: Para componentes com **peso fixo** expresso em percentagem do universo de referência, o **valor ideal** DEVE ser `universo de referência × (peso ÷ 100)`.
- **FR-004a**: No **Grupo 1 (Carteira Total)**, o universo de referência para valor ideal é o **total da carteira** (FR-003).
- **FR-004b**: Nos **grupos aninhados (2 e 3)**, o **valor actual** DEVE ser a soma real dos patrimónios enquadrados; o **valor ideal** DEVE usar como universo o **valor ideal do componente-pai homólogo no Grupo 1** — «Renda Fixa» para o Grupo 2, «Renda Variável» para o Grupo 3 — **não** o total actual da classe. Exemplo: RF actual zero, peso RF 50%, carteira R$ 100.000 → ideal Pós-fixados = 33,33% × R$ 50.000.
- **FR-004c**: Quando a **carteira total** for zero (FR-013), todos os valores ideais aninhados DEVEM ser zero (excepção à FR-004b).
- **FR-005**: Para o componente **Fundos de Previdência** (Grupo Carteira Total), o peso alvo DEVE ser a participação **actual** desse segmento no total da carteira (`valor actual do componente ÷ total da carteira × 100`), e o valor ideal DEVE ser igual ao valor actual.
- **FR-006**: Para componentes com peso **0%**, o valor ideal DEVE ser **zero**.
- **FR-007**: O catálogo inicial DEVE incluir os três **grupos de balanceamento** e componentes ab abaixo, com regras de enquadramento **mutuamente exclusivas e exaustivas** dentro de cada grupo (FR-007a):

  **Grupo 1 — Carteira Total** (universo = total da carteira):

  | Componente | Peso alvo | Regra de enquadramento |
  |------------|-----------|------------------------|
  | Renda Fixa | 50% | Classe renda fixa |
  | Renda Variável | 40% | Classe renda variável **exceto** ticker HASH11 |
  | Fundos de Previdência | Dinâmico (FR-005) | Fundos com tipo previdência |
  | Cripto Ativos | 1% | Renda variável com ticker HASH11 |
  | Demais investimentos | 0% | Posições não enquadradas nos componentes acima (inclui fundos de investimento **não** previdência, ex.: fundo de ações e multimercado) |

  **Grupo 2 — Renda Fixa** (actual: total activo RF; ideal: peso interno × **ideal** de «Renda Fixa» no Grupo 1):

  | Componente | Peso alvo | Regra de enquadramento |
  |------------|-----------|------------------------|
  | Pós-fixados | 33,33% | Renda fixa com indexador pós-fixado |
  | Pré-fixado | 33,33% | Renda fixa com indexador pré-fixado |
  | Atrelado a inflação | 33,33% | Renda fixa com indexador atrelado à inflação |

  **Grupo 3 — Renda Variável** (actual: total activo RV sem HASH11; ideal: peso interno × **ideal** de «Renda Variável» no Grupo 1):

  | Componente | Peso alvo | Regra de enquadramento |
  |------------|-----------|------------------------|
  | Ações Nacionais | 50% | RV tipo ação nacional, excluindo tickers HASH11 e IVVB11 |
  | Ações Internacionais | 10% | RV com ticker IVVB11 |
  | FIIs | 30% | RV tipo fundo imobiliário |
  | Outros RV | 10% | RV activa no universo do grupo não enquadrada nos três componentes anteriores |

- **FR-007a**: As regras de cada grupo DEVEM formar uma **partição** das posições activas elegíveis: **exactamente uma** regra por posição; soma dos valores actuais dos componentes = universo do grupo (Grupo 1 → total carteira; Grupo 2 → total actual RF; Grupo 3 → total actual RV sem HASH11). Testes DEVEM falhar se o catálogo introduzir sobreposição ou lacuna.
- **FR-008**: A definição de grupos e componentes DEVE residir num **catálogo central** (inicialmente fixo no código) estruturado de forma a permitir **acrescentar, remover ou alterar** entradas com alteração localizada — preparado para persistência futura sem mudar o contrato do relatório.
- **FR-009**: O cálculo DEVE estar disponível como **operação reutilizável** no domínio (entrada: posições/valores do período; saída: lista tipada de linhas do relatório), testável de forma isolada.
- **FR-010**: Na tela de histórico, DEVE existir um **controlo dedicado** (ícone) imediatamente **ao lado** do botão de importar B3 que invoca o cálculo e emite o relatório formatado como **tabela na saída de diagnóstico**. O controlo **não** deve ser desactivado durante o cálculo — toques sucessivos disparam novos cálculos independentes.
- **FR-011**: Esta entrega **NÃO** inclui ecrã, gráfico ou painel visual de balanceamento — apenas o relatório em log.
- **FR-012**: O relatório em log DEVE incluir cabeçalhos de coluna (**nome**, **valor actual**, **peso alvo**, **valor ideal**, **desvio**), alinhamento consistente e separação visual entre grupos para leitura rápida — **sem** coluna de regra de enquadramento. Desvio positivo = actual acima do ideal; negativo = abaixo.
- **FR-013**: Com carteira total zero, o sistema DEVE devolver relatório completo (todos os grupos e componentes) com valores actuais e ideais a zero, conforme edge case documentado.
- **FR-014**: A feature DEVE incluir testes automatizados que cubram: peso fixo, peso dinâmico de previdência, peso zero, exclusão de HASH11 da RV, enquadramento por indexador RF, carteira total zero com tabela zerada, grupo aninhado com classe actual zero (FR-004b), **partição** de regras sem sobreposição nem lacunas (FR-007a), e **falha ao obter dados do período** (UC propaga ou regista mensagem de erro compreensível — ver US3 AC5).
- **FR-015**: A implementação DEVE seguir princípios **SOLID**: responsabilidade única por papel (catálogo, classificação, cálculo, formatação do log); extensão do catálogo sem alterar o motor de cálculo (aberto/fechado); dependência de abstrações no domínio, não de detalhes concretos de UI ou persistência.

### Key Entities

- **Grupo de balanceamento**: Agrupador lógico (Carteira Total, Renda Fixa, Renda Variável). No Grupo 1 define universo = total carteira; nos grupos 2 e 3 separa universo de **actual** (soma real) vs **ideal** (componente-pai no Grupo 1).
- **Componente de balanceamento**: Unidade acionável do relatório — nome, regra interna de classificação, peso alvo (fixo, zero ou dinâmico) e resultado calculado.
- **Regra de enquadramento**: Critério determinístico **interno** ao componente, **disjunto** dos restantes no mesmo grupo, que decide se uma posição pertence a ele; em conjunto com as outras regras do grupo cobre **100%** das posições elegíveis sem overlap.
- **Linha do relatório de balanceamento**: Registo exposto ao utilizador com nome, valor actual, peso alvo, valor ideal e desvio; identificação do grupo pai para apresentação tabular.
- **Catálogo de balanceamento**: Conjunto configurável (v1: estático) de grupos, componentes, pesos e regras.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em cenários de teste com carteira fixa documentada, **100%** das linhas do relatório apresentam valor ideal coerente com FR-004/FR-004a/FR-004b/FR-004c/FR-005/FR-006.
- **SC-002**: O utilizador consegue, a partir do histórico, obter o relatório completo dos **11 componentes** configurados em **menos de 5 segundos** após o toque no controlo (inclui cálculo e escrita no log), em carteira típica até **200 posições activas**.
- **SC-003**: A coluna **desvio** no log identifica directamente, em **100%** dos casos de teste de aceitação, se o componente está acima do ideal (desvio > 0), abaixo (desvio < 0) ou em meta (desvio = 0).
- **SC-004**: Acrescentar um novo componente ao catálogo (em teste de extensibilidade) exige alteração em **um único ponto de configuração** do catálogo, sem modificar a fórmula genérica de cálculo — evidenciando conformidade com aberto/fechado (SOLID).
- **SC-005**: Com carteira total zero, **100%** dos componentes configurados aparecem na tabela do log com valor actual, valor ideal e desvio iguais a zero.

## Assumptions

- **Base de valor**: Património de posição = valor fim de mês × quantidade, alinhado ao histórico mensal existente; património **nunca** é negativo no domínio (posições com valor negativo não existem).
- **Regra vs. relatório**: Regras de enquadramento existem no catálogo/componente para classificar posições; no log apresentam-se **nome, valor actual, peso alvo, valor ideal e desvio**.
- **Qualidade de desenho**: Implementação orientada a **SOLID** — separação clara entre definição de grupos (catálogo), classificação de posições (regras), agregação/cálculo (domínio) e apresentação tabular (log/UI).
- **Período**: Mês de referência actualmente seleccionado no histórico; posições liquidadas (património zero) ficam de fora das somas.
- **Âmbito**: Balanceamento sobre a carteira **completa** do período, ignorando filtros visuais do histórico.
- **Fundos não-previdência**: Fundos de ações e multimercado enquadram-se em «Demais investimentos» (peso 0%), não em previdência nem RV.
- **Previdência dinâmica**: Meta «manter como está» — não força percentagem fixa; desvio sempre zero por construção.
- **Ideal aninhado**: Grupos 2 e 3 calculam valor ideal sobre o **ideal do pai** no Grupo 1, permitindo metas internas mesmo quando a classe actual está vazia ou sub/sobre-alocada.
- **Outros RV (10%)**: Componente explícito para completar os 100% do Grupo Renda Variável; cobre ETFs e RV não enquadrados em nacionais, IVVB11 ou FIIs.
- **Partição de regras**: Cada grupo define regras **sem conflito** e **sem lacunas**; fallbacks («Demais», «Outros RV») fecham o complemento. Alterações ao catálogo exigem validar a partição (testes FR-007a).
- **Persistência futura**: Catálogo em código na v1; modelo de dados do relatório e do catálogo estável para migração posterior para base de dados.
- **Sem acções automáticas**: O relatório **informa** desvios; não executa ordens de compra/venda.
- **Fora de âmbito**: Ecrã de balanceamento, edição de pesos pelo utilizador, recomendações de quantidade/ticker a negociar, impostos, corretagem e simulação Monte Carlo.
