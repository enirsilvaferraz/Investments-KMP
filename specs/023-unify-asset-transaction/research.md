# Research: Feature 023 — Modelo unificado de transações

**Data**: 2026-06-06

---

## R1 — Representação de domínio (tipo único vs hierarquia)

**Decision**: Substituir `sealed interface AssetTransaction` + três `data class` (`FixedIncomeTransaction`, `VariableIncomeTransaction`, `FundsTransaction`) por **um único** `data class AssetTransaction` com `quantity`, `unitPrice` e `totalValue` derivado (`get() = quantity * unitPrice`).

**Rationale**: A spec (FR-001, clarificação 2026-06-06) exige tipo concreto único; elimina ramificações `when (transaction)` em mappers, `TransactionBalance` e testes. A classe do ativo continua em `Asset`/`AssetHolding`, não na transação.

**Alternatives considered**:
- Manter sealed interface com subtipos homogéneos → rejeitado; duplica campos e mantém branching desnecessário.
- Interface + implementação única → rejeitado; `data class` directo é mais simples (princípio X).

---

## R2 — Esquema Room (tabelas satélite vs colunas na tabela base)

**Decision**: **Achatar** o modelo persistido: adicionar `quantity` e `unitPrice` em `asset_transactions`; **remover** tabelas `fixed_income_transactions`, `variable_income_transactions` e `funds_transactions`; **remover** colunas `observations` e `asset_class` de `asset_transactions` (incluindo o índice em `asset_class`).

**Rationale**: FR-007 exige persistir só quantidade e preço unitário; FR-009 coloca a classe do ativo na posição/ativo, não na transação. Três tabelas 1:1 e `asset_class` eram artefactos do discriminador polimórfico — nenhum DAO filtra por `asset_transactions.asset_class` e o mapper actual nunca lê a coluna na reconstrução do domínio. Transações carregam via `@Relation` em `AssetHoldingWithDetails`, com `assets.asset_class` sempre disponível.

**Alternatives considered**:
- Manter tabelas satélite com schema idêntico (qty+price em todas) → rejeitado; complexidade sem benefício.
- Manter `asset_class` denormalizado na transação → rejeitado; dado duplicado sem consumidor; contradiz FR-009.

---

## R3 — Estratégia de migração 9 → 10

**Decision**: `AutoMigration(from = 9, to = 10, spec = Migration9To10::class)` com:
1. Room adiciona colunas `quantity` (REAL, default 1) e `unitPrice` (REAL, default 0).
2. `onPostMigrate`: copiar dados das três tabelas satélite para `asset_transactions` (RV: qty/price existentes; RF/Fundos: qty=1, unitPrice=totalValue legado).
3. `@DeleteTable` para as três tabelas satélite; `@DeleteColumn` para `observations` e `asset_class` em `asset_transactions`.
4. Exportar schema `10.json` em `schemas/`.

**Rationale**: Padrão já usado em `Migration6To7` (`onPostMigrate` + `AutoMigrationSpec`). SQL explícito garante integridade numérica antes de dropar tabelas legadas (FR-008, SC-003).

**Alternatives considered**:
- Migração manual sem AutoMigration → rejeitado; inconsistente com `AppDatabase` actual.
- Destructive migration (reset DB) → rejeitado; perda de dados do utilizador.

---

## R4 — Regras de formulário inline (feature 022)

**Decision**:
- **Todos** os campos visíveis: quantidade, preço unitário, valor total (FR-003).
- **Valor total**: sempre `readOnly`; recalculado em qualquer alteração de qty ou price (`syncTotal()` generalizado — não só RV).
- **RF e fundos**: quantidade fixa `"1"`, campo desabilitado; preço unitário editável (FR-005).
- **Renda variável**: quantidade editável com `KeyboardType.Number` (sem decimal); preço unitário editável (FR-006).
- **Observações**: remover de `TransactionDraftUi` e UI (FR-002).
- **Validação**: remover erros de qty/price/total; manter apenas parse de data em `toDomainTransaction` (retorna `null` se data inválida — fluxo de save existente). Zero/negativos **não** bloqueiam (refinamento spec).

**Rationale**: Alinha UI actual (`TransactionManagementView.kt`) que hoje inverte editabilidade RF vs RV; a spec revoga validação numérica.

**Alternatives considered**:
- Manter `totalValue` editável em RF → contradiz FR-004/FR-007.
- Validar inteiro positivo em RV no save → contradiz refinamento “sem validação”.

---

## R5 — UI legada em `composeApp` (`features/transactions/`)

**Decision**: Pacote **já removido** no branch antes da implementação 023 (`TransactionForm`, `TransactionPanel`, `TransactionRow`, `TransactionState`, `TransactionTable`, `TransactionViewModel`, `TransactionIntent`). Navegação de histórico usa `AssetManagementRouting` (feature 022). Na implementação 023, **não** recriar nem adaptar estes ficheiros — apenas limpar artefactos residuais (ex.: entradas no `detekt-baseline.xml`).

**Rationale**: FR-011 limita alteração de UI ao formulário inline; remover o legado morto reduz superfície de mudança vs. “ajuste mínimo para compilar”. `AssetHistoryScreen` continua a listar transações via `AssetTransaction` do domínio (sem depender do pacote removido).

**Alternatives considered**:
- Manter legado com diff mínimo → obsoleto após remoção efectiva.
- Refactor completo do diálogo legado → fora de escopo e já substituído por 022.

---

## R6 — `TransactionBalance` e leitura downstream

**Decision**: Simplificar `calculateTransactionValue` para `transaction.totalValue` (já derivado no domínio). Actualizar testes em `:domain:entity` e `:domain:usecases` que instanciam subtipos.

**Rationale**: FR-009 proíbe branching por subtipo; `TransactionBalance` é o caso canónico.

---

## R7 — Precisão numérica

**Decision**: Persistir `quantity` e `unitPrice` como `Double` em Room/domínio **sem arredondamento** na gravação; `totalValue` nunca persistido. Cálculo derivado usa multiplicação directa (`*`).

**Rationale**: Clarificação e FR-012; evita divergência entre total exibido e dados gravados.

**Alternatives considered**:
- `BigDecimal` → rejeitado; YAGNI, resto do domínio usa `Double`.
- Arredondar a 2 casas → revogado na spec.

---

## R8 — Documentação

**Decision**: Actualizar no mesmo PR de implementação (princípio VII):
- `core/domain/entity/docs/DOMAIN.md` §9.3 — diagrama e entidade de domínio unificada.
- `docs/Modelagem do Banco de Dados.md` — diagrama ER (mermaid), secção “Transações de ativos”, DDL de `asset_transactions`, remoção de `fixed_income_transactions` / `variable_income_transactions` / `funds_transactions`, índices e tabela de FKs/CASCADE.

**Rationale**: O ficheiro em `docs/` é a referência canónica do esquema SQL; ainda descreve Table per Subclass, `category`, `observations` e as três tabelas satélite — ficará inconsistente após migração 9→10.

**Alternatives considered**:
- Só actualizar `DOMAIN.md` → rejeitado; duas fontes de verdade divergentes.
