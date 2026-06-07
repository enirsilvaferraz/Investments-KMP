# Feature Specification: Grupos balanceáveis e não balanceáveis

**Feature Branch**: `025-balanceable-groups`

**Created**: 2026-06-07

**Status**: Draft

**Input**: User description: "Alterar o conceito de balanceamento para distinguir grupo balanceável de não balanceável. Alguns investimentos não são passíveis de balanceamento (FGTS, fundo de previdência, etc., configurável). Os demais ativos são balanceáveis e seguem as regras do grupo 1. Após a adição desse conceito, o grupo 1 passa a conter somente os ativos balanceáveis. Para o grupo não balanceável, calcular dinamicamente o peso (peso dinâmico). Normalmente o grupo não balanceável terá pesos dinâmicos e os demais grupos pesos fixos, mas não é regra fixa no sistema. Apresentar no log primeiro a carteira não balanceável, depois a balanceável e por fim as demais carteiras. Possibilidade de grupo com 2 itens (balanceável e não balanceável) para saber a proporção entre eles na carteira."

## Clarifications

### Session 2026-06-07

- Q: Soma dos pesos normalizados no grupo balanceável — qual denominador? → A: **Eliminar peso normalizado** do relatório; peso dinâmico **apenas** nos grupos não balanceáveis (detalhe e resumo); grupos balanceáveis e aninhados usam somente peso fixo ou zero.
- Q: Fallback no grupo não balanceável para posições sem componente explícito? → A: Incluir componente **«Demais não balanceáveis»** (peso dinâmico) como fallback exclusivo do complemento.
- Q: Fallback no grupo balanceável para posições sem componente explícito? → A: Manter **«Demais investimentos»** (peso **zero**) como fallback exclusivo do complemento no universo balanceável.
- Q: Sinal do desvio? → A: **Desvio = valor actual − valor ideal** (positivo = acima da meta; negativo = abaixo; zero = em meta).
- Q: Critério de classificação não balanceável? → A: **Lista explícita** de tickers/tipos no catálogo (sem regras genéricas); omissão = balanceável.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Separar ativos balanceáveis dos não balanceáveis (Priority: P1)

Quem gere investimentos precisa que o sistema distinga claramente quais posições entram no cálculo de metas de alocação (balanceáveis) e quais são tratadas à parte porque não podem ou não devem ser rebalanceadas (FGTS, previdência, etc.).

**Why this priority**: Sem essa distinção, metas e desvios do grupo principal ficam distorcidos por ativos que o investidor não pretende mover.

**Independent Test**: Com carteira contendo renda fixa, renda variável, FGTS e previdência, executar o balanceamento e verificar que FGTS e previdência aparecem apenas no grupo não balanceável, enquanto RF e RV aparecem no grupo balanceável — sem sobreposição.

**Acceptance Scenarios**:

1. **Given** posições activas classificadas como não balanceáveis (ex.: FGTS, fundos de previdência) e balanceáveis (ex.: renda fixa, renda variável), **When** o cálculo é executado, **Then** cada posição activa pertence a **exactamente um** universo (balanceável ou não balanceável) conforme a configuração do catálogo.
2. **Given** um activo marcado como não balanceável no catálogo, **When** o cálculo inclui o grupo balanceável, **Then** esse activo **não** entra nas somas nem nos desvios desse grupo.
3. **Given** um activo **ausente** da lista explícita de tickers/tipos não balanceáveis no catálogo, **When** o cálculo é executado, **Then** o activo é tratado como **balanceável** (comportamento por omissão).
4. **Given** um activo **presente** na lista explícita de não balanceáveis, **When** o cálculo é executado, **Then** o activo entra no universo não balanceável e **não** no grupo balanceável.
5. **Given** alteração da lista explícita (ex.: acrescentar ticker ou tipo), **When** o catálogo é actualizado, **Then** a classificação reflecte a nova entrada sem alterar a lógica genérica de cálculo.

---

### User Story 2 - Grupo balanceável com metas fixas sobre a base balanceável (Priority: P1)

Quem rebalanceia a carteira investível precisa que o grupo principal (anterior «Grupo 1 — Carteira Total») contenha **somente** activos balanceáveis, com pesos fixos que somam 100% **da base balanceável** (soma dos patrimónios balanceáveis), e desvios coerentes com essa base.

**Why this priority**: É o núcleo do rebalanceamento — metas aplicadas apenas ao que o investidor pode mover.

**Independent Test**: Carteira R$ 100.000 com R$ 20.000 não balanceáveis e R$ 80.000 balanceáveis (RF 50%, RV 49%, cripto 1% no catálogo). Verificar que ideais e desvios usam base R$ 80.000, não R$ 100.000.

**Acceptance Scenarios**:

1. **Given** património total R$ T com R$ N não balanceável e R$ B balanceável (B = T − N), **When** o grupo balanceável é calculado, **Then** a **base balanceável** = B e os pesos fixos aplicam-se sobre B.
2. **Given** componentes balanceáveis com pesos fixos configurados, **When** o grupo balanceável é calculado, **Then** a soma dos pesos fixos (+ zero, se houver) = **100% ± 0,01%** da base balanceável.
3. **Given** carteira só com activos balanceáveis, **When** o cálculo é executado, **Then** base balanceável = total da carteira e o comportamento equivale ao grupo 1 anterior (excepto exclusão explícita de não balanceáveis).
4. **Given** carteira só com activos não balanceáveis, **When** o grupo balanceável é calculado, **Then** base balanceável = zero; valores actuais, ideais e desvios dos componentes balanceáveis = zero; pesos configurados fixos permanecem visíveis.
5. **Given** posição balanceável que **não** corresponde a RF, RV, cripto nem outro componente explícito, **When** o cálculo inclui o grupo balanceável, **Then** a posição entra no componente fallback **«Demais investimentos»** (peso zero; valor ideal = zero).

---

### User Story 3 - Grupo não balanceável com peso dinâmico (Priority: P1)

Quem monitoriza FGTS, previdência e similares precisa ver a participação actual desses activos na carteira **sem** meta de realocação — peso dinâmico (participação real) e valor ideal igual ao valor actual (desvio zero por desenho).

**Why this priority**: Complementa a separação do universo balanceável com visibilidade correcta do que fica fora do rebalanceamento.

**Independent Test**: Carteira com previdência R$ 15.000 e FGTS R$ 5.000 num total R$ 100.000. Verificar grupo não balanceável com pesos dinâmicos 15% e 5%, ideais = actuais, desvios zero.

**Acceptance Scenarios**:

1. **Given** componentes no grupo não balanceável com peso **dinâmico**, **When** o cálculo é executado, **Then** peso configurado = «dinâmico»; valor ideal = valor actual; desvio = zero; participação no total inferível por `valor actual ÷ total da carteira`.
2. **Given** múltiplos componentes não balanceáveis (ex.: previdência, FGTS), **When** o cálculo agrega o grupo, **Then** a soma dos valores actuais = total não balanceável; cada posição activa classificada aparece em exactamente um componente.
3. **Given** um componente no grupo não balanceável configurado com peso **fixo** ou **zero** (em vez de dinâmico), **When** o cálculo é executado, **Then** o sistema aplica a regra do tipo de peso configurado — **sem** impor dinâmico a todo o grupo (flexibilidade de catálogo).
4. **Given** posição classificada como não balanceável que **não** corresponde a previdência, FGTS nem outro componente explícito, **When** o cálculo inclui o grupo não balanceável, **Then** a posição entra no componente fallback **«Demais não balanceáveis»** (peso dinâmico).
5. **Given** posição não balanceável com património zero, **When** o cálculo é executado, **Then** a posição não entra nas somas (mesma regra de posições liquidadas da feature anterior).

---

### User Story 4 - Ordem de apresentação no log e grupo de proporção (Priority: P2)

Quem lê o relatório no log precisa ver **em primeiro lugar** a secção **Carteira Total** com dois itens («Não balanceável» e «Balanceável») que mostra a proporção entre os dois universos na carteira total. A seguir: o detalhe do que **não** rebalanceia, depois o universo balanceável com metas, e por fim os grupos aninhados (renda fixa, renda variável, FIIs, etc.).

**Why this priority**: O resumo responde de imediato «quanto da carteira está fora do rebalanceamento?»; a ordem seguinte aprofunda do agregado para o detalhe.

**Independent Test**: Executar balanceamento com ambos os universos preenchidos; confirmar que **Carteira Total** é a **primeira** secção do log e que os valores «Balanceável» + «Não balanceável» somam o total da carteira.

**Acceptance Scenarios**:

1. **Given** relatório completo com raiz, grupos não balanceável, balanceável e aninhados, **When** o log é emitido, **Then** a **primeira** secção é **Carteira Total** (filhos directos «Não balanceável» e «Balanceável»), seguida em pre-order DFS por: (2) **Carteira Não Balanceável** (detalhe), (3) **Carteira Balanceável** (grupo principal), (4) **demais grupos** (aninhados) na ordem definida pela árvore do catálogo.
2. **Given** nó raiz «Carteira Total» com filhos «Não balanceável» e «Balanceável» (peso dinâmico), **When** o cálculo é executado, **Then** cada linha mostra valor actual, peso configurado «dinâmico», ideal = actual e desvio zero; soma dos valores actuais = total da carteira quando T > 0.
3. **Given** carteira total zero, **When** o log é emitido, **Then** todas as secções aparecem na mesma ordem, com valores zero, sem erro.
4. **Given** acionamento a partir do histórico (feature 024), **When** o utilizador solicita balanceamento, **Then** mantém-se tabela formatada com colunas **nome**, **valor actual**, **peso configurado**, **valor ideal** e **desvio** — **sem** coluna peso normalizado e **sem** coluna de regra de enquadramento; linha **Total** por grupo.

---

### Edge Cases

- Ordem de secções no log: **Carteira Total (raiz) sempre primeiro**; depois não balanceável (detalhe) → balanceável → aninhados — pre-order DFS; mesmo com carteira só balanceável ou só não balanceável.
- Carteira **só balanceável** ou **só não balanceável** → grupos vazios apresentam zeros coerentes; secção Carteira Total continua primeiro.
- Activos não balanceáveis **não** entram em grupos aninhados (RF, RV, etc.) — universo aninhado restrito a activos balanceáveis enquadrados na respectiva classe.
- **Partição no grupo não balanceável**: componentes explícitos + **«Demais não balanceáveis»** (fallback exclusivo, peso dinâmico) — nenhuma posição não balanceável activa sem componente.
- **Partição no grupo balanceável**: componentes explícitos (RF, RV, cripto, etc.) + **«Demais investimentos»** (fallback exclusivo, peso zero) — nenhuma posição balanceável activa sem componente.
- **Partição nos grupos aninhados**: regras mutuamente exclusivas e exaustivas dentro do universo elegível balanceável; activos não balanceáveis excluídos.
- Grupos **balanceáveis e aninhados** usam **somente** peso fixo ou zero — **sem** peso dinâmico nem coluna peso normalizado.
- Componente não balanceável com peso fixo ou zero no catálogo → permitido; ideal calculado sobre universo de referência definido para esse grupo.
- Soma de pesos fixos no grupo balanceável ≠ 100% → falha de validação do catálogo (mesmo critério ± 0,01% da feature 024).
- Filtros activos no histórico → balanceamento continua a usar mês corrente e **todas** as posições activas (herança feature 024).
- Posição **fora** da lista explícita de não balanceáveis → **sempre balanceável**, independentemente da classe de activo.
- HASH11, indexadores RF, carteira zero, toques repetidos → comportamentos da feature 024 preservados no universo balanceável.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE classificar cada posição activa como **balanceável** ou **não balanceável** mediante **lista explícita** de tickers e/ou tipos de activo no catálogo — **sem** regras genéricas ou heurísticas; omissão na lista = **balanceável**. Entrada inicial inclui, no mínimo, fundos de previdência e FGTS.
- **FR-001a**: Posições no universo não balanceável DEVEM ser **particionadas** pelos componentes do grupo não balanceável; entradas da lista sem componente dedicado caem em **«Demais não balanceáveis»**.
- **FR-002**: O **grupo balanceável** (sucessor do Grupo 1 «Carteira Total») DEVE conter **apenas** posições balanceáveis, com componentes explícitos (RF, RV, cripto, etc.) e fallback **«Demais investimentos»** (peso zero) para posições balanceáveis não enquadradas — fechando a partição do universo balanceável; activos não balanceáveis **não** entram nas somas, pesos nem desvios desse grupo.
- **FR-003**: A **base balanceável** DEVE ser a soma dos patrimónios activos balanceáveis; pesos **fixos** do grupo balanceável aplicam-se sobre essa base (não sobre o total da carteira quando existirem activos não balanceáveis).
- **FR-004**: O sistema DEVE incluir um **grupo não balanceável** dedicado aos activos não passíveis de rebalanceamento, com componentes configuráveis (ex.: «Fundos de Previdência», «Fundos do FGTS») e componente fallback **«Demais não balanceáveis»** (peso dinâmico) para posições não balanceáveis não enquadradas nos componentes explícitos — fechando a partição do universo não balanceável.
- **FR-005**: Para componentes com peso **dinâmico** (grupos não balanceáveis e resumo), o sistema DEVE: peso configurado = «dinâmico»; valor ideal = valor actual; **desvio = zero** (`actual − ideal`).
- **FR-005a**: Para componentes com peso **fixo** ou **zero**, o **desvio** DEVE ser **`valor actual − valor ideal`** — positivo indica posição acima da meta; negativo indica abaixo; zero indica em meta.
- **FR-006**: Grupos **balanceáveis e aninhados** DEVEM usar **somente** peso fixo ou zero. Peso **dinâmico** restringe-se aos filhos directos de **Carteira Total** (resumo Balanceável / Não balanceável), ao grupo **não balanceável** (detalhe) e, excepcionalmente, a componentes individuais não balanceáveis com peso fixo/zero se configurado. O catálogo **não** impede fixo/zero em não balanceáveis nem bloqueia tipos além desta convenção nos grupos balanceáveis; o validador **rejeita** `Dynamic` na subárvore balanceável.
- **FR-007**: O catálogo DEVE expor a **lista explícita** de tickers/tipos não balanceáveis como ponto único de configuração (preparado para persistência futura); acrescentar entrada **não** exige alterar a lógica de cálculo.
- **FR-008**: Grupos **aninhados** (renda fixa, renda variável, FIIs, acções, etc.) DEVEM operar sobre o universo **balanceável** da respectiva classe; valores ideais continuam derivados do **ideal do componente-pai no grupo balanceável** (herança feature 024).
- **FR-009**: O relatório em log DEVE seguir **pre-order DFS** da árvore do catálogo, começando por **Carteira Total** (raiz) com filhos «Não balanceável» e «Balanceável», depois Carteira Não Balanceável (detalhe), Carteira Balanceável e demais grupos aninhados — cada secção com linha **Total** e colunas **nome**, **valor actual**, **peso configurado**, **valor ideal**, **desvio** — **sem** coluna peso normalizado.
- **FR-010**: O catálogo DEVE definir **Carteira Total** como nó raiz (`carteiraTotalNode`) — **primeira secção no log** — com dois filhos directos: «Não balanceável» (agregado de todos os activos não balanceáveis) e «Balanceável» (agregado de todos os balanceáveis), para expor a proporção entre os dois universos; pesos dinâmicos nos filhos da raiz; a subárvore completa (detalhe, metas, aninhados) segue como descendentes na mesma árvore.
- **FR-011**: A soma dos pesos fixos (+ zero) no grupo balanceável DEVE ser **100% ± 0,01%** da base balanceável — validação de catálogo.
- **FR-012**: Com carteira total zero, o sistema DEVE devolver relatório estrutural completo (todas as secções, zeros, sem divisão inválida) — herança FR-013 da feature 024.
- **FR-013**: Acionamento, período (mês corrente), ausência de ecrã dedicado, botão no histórico e testes automatizados DEVEM permanecer conforme feature 024, **actualizados** para cobrir separação de universos, base balanceável, ordem DFS (Carteira Total primeiro), resumo na raiz e **remoção de peso normalizado**.
- **FR-014**: Regras de partição (mutuamente exclusivas e exaustivas por grupo) DEVEM ser mantidas e validadas; activos não balanceáveis excluídos do universo dos grupos balanceáveis **não** contam como lacuna nesses grupos.

### Key Entities

- **Classificação balanceável**: Determinada por **lista explícita** no catálogo — presente na lista = não balanceável; ausente = balanceável.
- **Lista explícita não balanceáveis**: Conjunto configurável de tickers e/ou tipos de activo fora do rebalanceamento; sem regras genéricas.
- **Grupo balanceável**: Agrupador principal de metas — só activos balanceáveis; componentes explícitos + **«Demais investimentos»** (peso zero); base = soma dos patrimónios balanceáveis.
- **Grupo não balanceável**: Agrupador de activos fora do rebalanceamento (FGTS, previdência, **Demais não balanceáveis**, etc.); componentes explícitos + fallback dinâmico para partição exaustiva.
- **Carteira Total (raiz)**: Primeira secção do relatório; nó contentor com dois filhos directos (Não balanceável / Balanceável) sobre o total da carteira; peso dinâmico nos filhos da raiz.
- **Tipo de peso**: Fixo (meta percentual sobre universo de referência) ou zero (meta de eliminação) nos grupos balanceáveis e aninhados; dinâmico (ideal = actual) nos grupos não balanceáveis e resumo.
- **Base balanceável**: Soma dos patrimónios activos classificados como balanceáveis; universo de referência para metas fixas do grupo balanceável.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em cenários de teste com mix balanceável e não balanceável documentados, **100%** das posições activas aparecem no universo correcto (balanceável ou não balanceável), sem double-count entre grupos principais.
- **SC-002**: Em **100%** dos casos de teste de aceitação, valores ideais do grupo balanceável usam a base balanceável (não o total da carteira) quando existem activos não balanceáveis.
- **SC-003**: A secção **Carteira Total** é sempre a **primeira** do log; as restantes seguem pre-order DFS (não balanceável detalhado → balanceável → aninhados) — em **100%** das execuções de teste.
- **SC-004**: Os filhos da raiz reflectem a partição da carteira: valor actual «Balanceável» + valor actual «Não balanceável» = **total da carteira** (± arredondamento) quando património total > 0.
- **SC-005**: Em **100%** dos casos de teste, a coluna **desvio** segue `actual − ideal` — positivo quando actual > ideal, negativo quando actual < ideal, zero em meta (incl. componentes dinâmicos).
- **SC-006**: Acrescentar um activo não balanceável exige **uma entrada** na lista explícita do catálogo, sem modificar a fórmula genérica de cálculo.
- **SC-007**: Relatório completo obtido a partir do histórico em **menos de 5 segundos** para carteira típica até **200 posições activas** (herança feature 024).

## Assumptions

- **Herança feature 024**: Motor de balanceamento, catálogo extensível, acionamento via histórico, saída em log, sem ecrã dedicado, sem persistência de pesos pelo utilizador, sem recomendações de negociação.
- **Não balanceáveis iniciais**: Fundos de previdência e FGTS na **lista explícita** — alinhado ao catálogo actual; expansão = nova entrada na lista.
- **Grupo balanceável**: Sucessor do «Grupo 1 — Carteira Total» com a mesma decomposição por classe (RF, RV, cripto, demais), mas **excluindo** não balanceáveis; **somente** pesos fixos ou zero sobre a base balanceável.
- **Peso dinâmico**: Exclusivo dos filhos de **Carteira Total** (resumo) e do grupo não balanceável (detalhe); **sem** coluna peso normalizado no log (evolução em relação à feature 024).
- **Carteira Total (raiz)**: Obrigatória nesta entrega; **primeira secção** no log, com filhos Balanceável e Não balanceável, antes de qualquer subárvore de detalhe.
- **Ordem aninhados**: Demais grupos mantêm ordem relativa do catálogo actual após as duas secções principais.
- **Fora de âmbito**: UI para marcar activos como balanceáveis; persistência em base de dados; alteração de pesos pelo utilizador; ecrã de balanceamento.

## Dependencies

- Feature **024-portfolio-balancing** (motor, catálogo, log, histórico) — esta feature **refina** o modelo de grupos e universos, não substitui o fluxo de acionamento.
