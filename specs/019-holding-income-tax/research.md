# Research: Imposto de renda regressivo (019)

**Feature**: `019-holding-income-tax` | **Phase**: 0 | **Date**: 2026-06-04

**Diretriz**: **simplicidade**, **menos é mais** — só `IncomeTax.calculate`; sem use case, UI, `:data`, nem `earliestPurchaseDate`.

---

## R1 — Onde vive a regra (só `:domain:entity`)

**Decision**: Toda a lógica em `:domain:entity`, espelhando `Growth.kt`:

- `IncomeTax` — `data class` com construtor `private`, fábrica `companion object.calculate(...)`.
- Propriedades: `taxRate` (alíquota da faixa, ex. 22.5) e `taxValue` (reais).

**Rationale**: FR-009 e constituição II — regra de negócio no domínio, sem framework; KISS — não criar use case só para delegar três parâmetros.

**Alternatives considered**:

| Alternativa | Rejeitada porque |
|-------------|------------------|
| `CalculateIncomeTaxUseCase` | Camada extra sem ports nem I/O |
| Tabela em JSON/config | YAGNI; quatro faixas fixas em código |
| Enum de faixas + serviço | Mais tipos que o necessário para v1 |

---

## R2 — Dias investidos (`kotlinx-datetime` 0.8)

**Decision**: `val daysInvested = purchaseDate.daysUntil(referenceDate)` (`LocalDate` → `Int`, não negativo quando `referenceDate >= purchaseDate`).

- Mesmo dia → `0` → faixa até 180 dias (22,5%).
- `purchaseDate > referenceDate` → `require` / `IllegalArgumentException` (FR-008).

**Rationale**: API oficial do projeto (`libs.versions.toml`); evita `DatePeriod` manual.

**Alternatives considered**:

| Alternativa | Rejeitada porque |
|-------------|------------------|
| `toEpochDays()` diferença | Equivalente, menos legível |
| Meses aproximados | Viola spec (dias corridos) |

---

## R3 — Tabela regressiva (implementação mínima)

**Decision**: `when` monotónico sobre `daysInvested` (sem estrutura de dados intermédia):

| Condição | `taxRate` |
|----------|-----------|
| `<= 180` | 22.5 |
| `<= 360` | 20.0 |
| `<= 720` | 17.5 |
| `else` | 15.0 |

`taxValue = if (profit > 0) profit * taxRate / 100 else 0.0` (FR-005, FR-006). Domínio **não** arredonda monetariamente; testes com delta `0.01`.

**Rationale**: Quatro ramos; legível; cobre fronteiras 180/181/360/361/720/721 da spec.

---

## R4 — `earliestPurchaseDate` (fora de escopo)

**Decision**: **Não implementar** extensão em `List<AssetTransaction>`. `purchaseDate` é sempre argumento de `calculate`.

**Rationale**: Pedido explícito do utilizador; YAGNI — integração com transações fica para use case/feature futura.

**Alternatives considered**:

| Alternativa | Rejeitada porque |
|-------------|------------------|
| Extensão paralela no entity | Removida do escopo |
| `IncomeTax.calculate(holding)` | Acopla agregado ao motor puro |

---

## R5 — Testes

**Decision**: Apenas `IncomeTaxTest.kt` em `entity/src/jvmTest/.../holdings/` — faixas, fronteiras, lucro ≤ 0, data inválida.

**Rationale**: `GrowthTest` + `test-patterns.mdc`. Constituição IX — escrever testes, Gradle sob pedido.

---

## R6 — Fora de escopo (v1)

**Decision**: Sem `:domain:usecases`, `:data`, `:features`; `DOMAIN.md` com secção curta em `holdings`.

**Rationale**: YAGNI; UI, DARF, derivação de data de compra — fora desta entrega.
