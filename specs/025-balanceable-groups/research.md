# Research: Grupos balanceáveis e não balanceáveis

**Feature**: `025-balanceable-groups` | **Date**: 2026-06-07

## R1 — Partição explícita balanceável / não balanceável

**Decision**: Introduzir `NonBalanceableAssetList` (object no catálogo) como **único ponto de configuração** — lista de predicados explícitos (tipo de fundo previdência, observação FGTS, tickers futuros). Função `BalancingMatchers.isNonBalanceable(entry)` consulta a lista; omissão = balanceável (FR-001, FR-007).

**Rationale**: A spec proíbe heurísticas genéricas; a lista explícita alinha com OCP (SC-006) — nova entrada no catálogo sem alterar fórmulas. Reutiliza matchers existentes (`isPensionFund`, `isFGTSFund`) como entradas iniciais.

**Alternatives considered**:

- Flag em `AssetHolding` / Room — fora de escopo (sem persistência).
- Regras por `AssetClass` — rejeitado (spec: sem regras genéricas).
- Manter previdência/FGTS só como componentes Dynamic no G1 — rejeitado (universos sobrepostos distorcem metas).

---

## R2 — Estrutura de grupos e ordem no log

**Decision**: Substituir o monolito `PORTFOLIO_TOTAL` por **árvore única** com raiz **`carteiraTotalNode`**. Filhos directos: `nonBalanceableNode`, `balanceableNode`; RF/RV/Cripto/etc. são descendentes. **Um tipo** `BalancingTreeNode` (contentor vs terminal); **sem** `BalancingLeaf`, **sem** overlay separado.

**Rationale**: Tudo deriva do Grupo 1; configuração e cálculo homogéneos.

**Alternatives considered**:

- Duas raízes + overlay — rejeitado (utilizador: árvore única).
- Leaf + Node — rejeitado (filhos do nó = linhas; terminais = nós sem filhos).

---

## R2 — Ordem e forma do log

**Decision**: Log **segue a árvore** (pre-order DFS). Cada nó **contentor** → secção `=== displayName ===`; **filhos directos** → linhas da tabela; linha **Total**; recursão nos filhos contentores. Nó terminal → só linha no pai, nunca secção própria.

**Rationale**: Uma regra de apresentação; formatter = iterar `sections` já na ordem do engine — sem `flattenForDisplay` customizado.

**Alinhamento spec** (resolvido 2026-06-07): 1.ª secção = **Carteira Total** (raiz; filhos Balanceável + Não balanceável) — `spec.md` actualizado.

**Alternatives considered**:

- Ordem customizada desacoplada da árvore — rejeitado (feedback utilizador).

---

## R3 — Base balanceável e pesos fixos

**Decision**: `balanceableBase = Σ património(balanceáveis)`. Filhos **directos** de `balanceableNode` usam `balanceableBase` como `referenceBase`. A 1.ª secção (`carteiraTotalNode`) mostra filhos com peso Dynamic (ideal = actual) — substitui overlay «Composição».

**Rationale**: Herda intenção da feature 024 (`balanceableBase = total − não balanceável`) com cálculo explícito por partição em vez de subtrair componentes Dynamic do mesmo grupo.

**Alternatives considered**:

- Base = total sempre — viola FR-003 quando existem não balanceáveis.
- Normalizar pesos fixos proporcionalmente — rejeitado na clarificação (eliminar peso normalizado).

---

## R4 — Remoção de peso normalizado

**Decision**: Remover `normalizedWeightDisplay`, `normalizedWeightPercent` de `PortfolioBalancingReportLine` e toda a coluna «Peso normalizado» / «Percentual actual» condicional de `FormatPortfolioBalancingReport`. Log expõe **cinco** colunas: nome, valor actual, peso configurado, valor ideal, desvio (FR-009).

**Rationale**: Clarificação 2026-06-07; simplifica modelo e formatter (princípio X). Participação inferível por `actual ÷ total` quando necessário, sem coluna dedicada.

**Alternatives considered**:

- Manter normalizado só no resumo — rejeitado (spec: sem coluna normalizado em todo o relatório).
- Manter «Percentual actual» — rejeitado (não pedido na spec 025; reduz ruído).

---

## R5 — Sinal do desvio

**Decision**: `deviation = actualValue − idealValue` em todo o motor (positivo = acima da meta; negativo = abaixo; zero = em meta ou componente dinâmico). Corrigir implementação 024 que usa `ideal − actual` em `PortfolioBalancingEngine.toReportLine`.

**Rationale**: Clarificação FR-005a; alinha com texto da spec e testes de aceitação SC-005.

**Alternatives considered**:

- Manter sinal invertido da 024 — rejeitado (contradiz spec 025).

---

## R6 — Fallbacks por universo

**Decision**: Fallback = **filho terminal** catch-all (`demaisInvestimentosNode`, `demaisNonBalanceableNode`) com `matches` exclusivo do complemento. Mesmo padrão em qualquer nó contentor.

---

## R7 — Grupos aninhados e filtros de universo

**Decision**: Subárvores = **`children`** na mesma árvore. Universo herdado do pai (R1); escopo via `matches`. Sem tipo «aninhado» separado.

**Rationale**: Não há «grupo aninhado» como conceito separado — só nós mais profundos na mesma árvore.

---

## R8 — Componentes dinâmicos fora dos grupos balanceáveis

**Decision**: `TargetWeight.Dynamic` permitido nos filhos directos de `carteiraTotalNode` (raiz), no grupo não balanceável (detalhe) e, excepcionalmente, em componentes individuais não balanceáveis com peso fixo/zero se configurado no catálogo (FR-006). Subárvore balanceável e aninhados: **somente** Fixed/Zero; validator rejeita Dynamic nesses nós.

**Rationale**: Flexibilidade de catálogo sem impor dinâmico a todo o grupo não balanceável.

**Alternatives considered**:

- Proibir fixo/zero em não balanceáveis — rejeitado (FR-006 permite).

---

## R9 — Impacto na UI e orquestração

**Decision**: **Sem** alterações em `HistoryViewModel` / `AssetHistoryScreen` além de possível ajuste de testes se assinatura pública mudar. `CalculatePortfolioBalancingUseCase(Unit)` permanece autossuficiente (herança FR-013).

**Rationale**: Refino de domínio; acionamento e período inalterados.

**Alternatives considered**:

- Novo parâmetro no UC — rejeitado (fora de escopo).

---

## R10 — Índice de universos pré-filtrados

**Decision**: `BalancingUniverseIndex`: DFS; `byNodeId[child] = byNodeId[parent].filter(child.matches)` — listas **monotonicamente menores** da raiz às folhas.

---

## R11 — Cálculo unificado (fórmula única)

**Decision**: `BalancingIdealCalculator.computeIdeal` + `computeAndEmitNode` recursivo. **R3 única**: `referenceBase = idealByNodeId[pai.id]`; bootstrap `idealByNodeId[root.id] = totalPortfolioValue`. **Sem** `BalancingReferenceBase`, **sem** bases especiais `balanceableBase` / `nonBalanceableTotal` no motor (derivadas como `actual` dos nós respectivos).

---

## R12 — Log espelha árvore (secção = nó; linhas = filhos)

**Decision**: Pre-order DFS (R6). Formatter itera `sections` sem reordenar.

---

## R13 — Regras iguais para todos os nós

**Decision**: Sete regras R1–R7 aplicam-se a **qualquer** nó sem `when (nodeId)`. Contentor vs terminal = só `children.isEmpty()`. Validator usa a mesma `validateNode` recursiva. Diferenças balanceável / não balanceável / RF são **conteúdo do catálogo** (pesos Dynamic vs Fixed), não ramos de código.

**Rationale**: Feedback utilizador; FR-003 e FR-008 emergem de R3+R4 (ideal do pai como referência) sem casos especiais.

**Alternatives considered**:

- Tabela `referenceBase` por tipo de pai — rejeitado.
- `balanceableBase` hardcoded para filhos de Balanceável — rejeitado (propaga via `idealByNodeId[balanceable]`).

---

## R14 — Universo progressivo via `matches` (sem `universeFilter`)

**Decision**: R1 = `byNodeId[filho] = byNodeId[pai].filter(filho.matches)`. **Todos** os nós têm `matches` (raiz = `always`). Lista estreia da base às folhas; filho nunca relê `activeEntries` directamente.

**Rationale**: Utilizador — lista avança filtrada nível a nível; herança «mesma referência» (R14 draft anterior) **não** cobria isso. `matches` unifica estreia de universo + classificação terminal.

**Alternatives considered**:

- Mesma lista + matchers só no somatório — rejeitado (não materializa filtragem progressiva).
- `universeFilter` separado de `matches` — rejeitado (duplicação).
