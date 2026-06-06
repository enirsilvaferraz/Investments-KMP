# Research: Balanceamento de carteira

**Feature**: `024-portfolio-balancing` | **Date**: 2026-06-06

## R1 — Onde colocar tipos e lógica (camada)

**Decision**: Tipos de relatório, catálogo, classificação, cálculo e formatação de log residem em `:domain:usecases`, pacote `com.eferraz.usecases.balancing`. Sem novos módulos Gradle; sem alterações em `:domain:entity` na v1.

**Rationale**: O catálogo é configuração estática em código (FR-008); o relatório é saída de caso de uso, não persistência. Padrão alinhado a `WalletHistoryFilter` e `HoldingHistoryRow` em usecases. Entidades de ativo (`AssetClass`, `YieldIndexer`, tickers) já existem em `:domain:entity`.

**Alternatives considered**:

- Novo módulo `:domain:balancing` — rejeitado (princípio X: superfície mínima).
- Tipos em `:domain:entity` — rejeitado (catálogo/regras são lógica de negócio, não modelo persistente na v1).

---

## R2 — Estrutura do catálogo e pesos alvo

**Decision**: Catálogo como `object PortfolioBalancingCatalog` com `TargetWeight`: `Fixed(percent)`, `Zero`, `Residual`. Pesos fixos+zero do G1 somam 100% da **base balanceável** (`total − previdência actual`).

**Rationale**: Previdência calculada primeiro; fixos aplicam-se ao remanescente; duas colunas de peso (configurado vs normalizado = `ideal ÷ total`).

**Alternatives considered**:

- Normalização proporcional só na exibição — rejeitado; preferida mudança do universo de cálculo.
- YAML/JSON externo — fora de escopo v1.

---

## R3 — Classificação e partição de posições

**Decision**: Cada componente expõe `matches: (HoldingHistoryEntry) -> Boolean`. Classificador percorre componentes **na ordem do catálogo**; fallback («Demais investimentos», «Outros RV») é o **último** de cada grupo. Posições com património ≤ 0 são **excluídas** antes da classificação (FR-002). Testes de partição (FR-007a) validam exclusividade e exaustividade com fixtures sintéticas — **sem** resolução por precedência em runtime para conflitos (conflito = falha de teste/catálogo).

**Rationale**: Regras determinísticas e disjuntas por desenho; fallbacks fecham o complemento. Ordem só importa para fallbacks, não para «primeira regra ganha» entre regras principais.

**Alternatives considered**:

- Mapa posição→componente com prioridade runtime — rejeitado (proibido na spec).
- Double-count com flags — rejeitado (FR-007a).

---

## R4 — Cálculo de valor ideal e desvio

**Decision**:

- Património: `endOfMonthValue * endOfMonthQuantity`.
- Grupo 1: `balanceableBase = total − actual(previdência)`; fixos → `ideal = balanceableBase × (peso ÷ 100)`; `Residual` → ideal = actual; `Zero` → 0.
- Peso normalizado: `ideal / totalCarteira × 100`.
- Grupos 2–3: `ideal = pesoInterno × idealDoPaiNoGrupo1`.
- `totalCarteira == 0` → ideais e normalizados = 0.
- `desvio = actual − ideal`.

**Rationale**: Alinha clarificações da spec (ideal aninhado sobre pai no Grupo 1, não sobre actual da classe).

**Alternatives considered**:

- Ideal aninhado sobre actual da classe — rejeitado (contradiz FR-004b).

---

## R5 — Fonte de dados e âmbito (histórico)

**Decision**: `HistoryViewModel` obtém entradas via `GetHoldingHistoriesUseCase` / `CreateHistoryUseCase` para o `referenceDate` seleccionado, **sem** `FilterHoldingHistoryUseCase` (FR-015 âmbito total). Reutiliza o mesmo pipeline de carregamento que `loadInitialData`, mas ramo dedicado no intent de balanceamento.

**Rationale**: Spec exige carteira completa independente de filtros visuais. Evita duplicar repositório dentro do UC de cálculo (FR-009 mantém UC puro: entrada = lista de entradas).

**Alternatives considered**:

- UC orquestrador com fetch interno — possível mas acopla UC a repositório; VM já tem os use cases.

---

## R6 — Apresentação em log (sem ecrã dedicado)

**Decision**: Função pura `formatPortfolioBalancingReport(report): String`. Colunas: nome, valor actual, **peso configurado**, **peso normalizado**, valor ideal, desvio; **linha `Total`** por grupo.

**Rationale**: FR-010/FR-011/FR-012; padrão existente de `println` no histórico (sync, export, import). Sem Snackbar nem estado `isBalancing` — botão permanece activo (clarificação spec).

**Alternatives considered**:

- `SharedFlow` de eventos de log — over-engineering para v1.
- Desactivar botão durante cálculo — rejeitado (spec).

---

## R7 — Ícone e posição na barra de acções

**Decision**: `IconButton` com `Icons.Default.Balance` (ou `AccountBalance` se indisponível no target), inserido **imediatamente após** o botão de importar B3 na função `Actions` de `AssetHistoryScreen`. `contentDescription = "Balanceamento de carteira"`.

**Rationale**: FR-010 — adjacente ao import; layout actual: Sync → Import → **[novo]** → Export.

**Alternatives considered**:

- Ícone à esquerda do import — menos explícito na ordem actual da barra.

---

## R8 — Constantes de ticker e mapeamento de domínio

**Decision**: Constantes `HASH11` e `IVVB11` em `PortfolioBalancingCatalog` (ou ficheiro `BalancingTicker.kt` no mesmo pacote). Classificação RV usa `VariableIncomeAsset.ticker` normalizado (`uppercase()`). Fundos previdência: `InvestmentFundAsset` + `InvestmentFundAssetType.PENSION`. Demais fundos → «Demais investimentos».

**Rationale**: Não há referências pré-existentes a HASH11/IVVB11 no código; taxonomia 016 cobre indexadores RF e tipos RV/Fundos.

**Alternatives considered**:

- Usar só `VariableIncomeAssetType.INTERNATIONAL_STOCK` para IVVB11 — insuficiente (spec exige ticker IVVB11 explicitamente).

---

## R9 — Testes obrigatórios

**Decision**: `CalculatePortfolioBalancingUseCaseTest` em `:domain:usecases:jvmTest` com cenários FR-014; teste de partição separado `PortfolioBalancingPartitionTest` que itera todas as combinações de fixture e asserta exactamente um componente por posição activa por grupo.

**Rationale**: Princípio V; cenários de aceitação da spec são determinísticos e não precisam de Room.

**Alternatives considered**:

- Testes só de integração com DB — mais lentos e desnecessários para motor puro.

