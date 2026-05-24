# Research: Destaque visual de investimentos liquidados no Histórico

**Feature**: `005-liquidated-history-style` | **Phase**: 0 | **Date**: 2026-05-24

---

## 1. Onde encapsular a regra `valor atual == 0`

**Decision**: Propriedade derivada `isLiquidated` em `HoldingHistoryView` (`:domain:usecases`), calculada como `currentValue == 0.0`.

**Rationale**:
- A spec define liquidação como estado derivado do valor atual no período — já presente na view.
- UI (`:features:composeApp`) e filtros futuros consomem um único ponto de verdade, sem duplicar `currentValue == 0` na camada de apresentação.
- Alinha com pedido explícito do utilizador no `/speckit-plan`.
- Sem migração Room nem alteração em `:domain:entity`.

**Alternatives considered**:
| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| Regra só em `AssetHistoryScreen` | Duplicação; filtro futuro exigiria refactor |
| Campo persistido `is_liquidated` | YAGNI; valor atual já define o estado |
| Use case `IsHoldingLiquidatedUseCase` | Over-engineering para comparação trivial |

---

## 2. Forma da propriedade (`val` vs `fun`)

**Decision**: `public val isLiquidated: Boolean` com getter no corpo da classe (`get() = currentValue == 0.0`).

**Rationale**: Propriedade imutável coerente com `data class`; sempre sincronizada com `currentValue` na mesma instância; API legível para `filter { it.isLiquidated }`.

**Alternatives considered**:
| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| Parâmetro no construtor primário | Redundante com `currentValue`; risco de inconsistência |
| `fun isLiquidated(): Boolean` | Utilizador pediu propriedade |

---

## 3. Cor cinza de referência

**Decision**: Extrair constante/helper `historyMutedTextColor()` em `:presentation:design-system` (ou reutilizar tom existente `Color.Gray.copy(alpha = 0.5f)` já usado em Valorização zero e “Adicionar”).

**Rationale**: FR-002 exige o mesmo padrão visual da valorização zerada; DRY evita três cópias do literal em `AssetHistoryScreen`.

**Alternatives considered**:
| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| `MaterialTheme.colorScheme.onSurfaceVariant` | Tom diferente do já usado na tabela de histórico |
| Cor por tema sem centralizar | Inconsistência entre colunas |

---

## 4. Precedência Valorização / Transações vs liquidado

**Decision**: Em colunas **Valorização** e **Transações**, **não** aplicar `isLiquidated` à escolha de cor — manter blocos `when` atuais intactos.

**Rationale**: Clarificação da spec (sessão 2026-05-24, revisão): regras semânticas dessas colunas têm prioridade.

**Alternatives considered**:
| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| Cinza em toda a linha | Revertido pelo utilizador |

---

## 5. Colunas gerais sem `content` customizado

**Decision**: Fornecer `content` explícito nas colunas Corretora, Display Name, Observação e Valor Anterior com `Text(..., color = row.resolveLiquidatedTextColor())`; helper de extensão ou função `@Composable` no módulo de histórico ou `naming`.

**Rationale**: `UiTableDataColumn` default usa `Text(comparable(it).toString())` sem cor condicional.

---

## 6. `TableInputMoney` (Valor Atual)

**Decision**: Acrescentar parâmetro opcional `textColor: Color? = null` em `TableInputMoney`; quando `row.isLiquidated`, passar `historyMutedTextColor()`.

**Rationale**: Cor do texto hoje é `onSurface` / `onSurfaceVariant` por `enabled`; liquidado exige cinza mesmo com campo habilitado.

**Alternatives considered**:
| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| `enabled = false` para liquidado | Altera interação; spec permite edição para “des-liquidar” |

**Atualização (pós-analyze)**: A coluna Valor Atual do Histórico usa `TableInputMoney` do módulo **composeApp** (`core/presentation/composeApp/.../presentation/design_system/components/inputs/TableInputMoney.kt`), não o de `:presentation:design-system`. O parâmetro `textColor` aplica-se apenas à cópia do composeApp.

---

## 7. Testes

**Decision**: Novo ficheiro `HoldingHistoryViewTest.kt` em `:domain:usecases:jvmTest` com casos `currentValue == 0.0`, `> 0`, `< 0`.

**Rationale**: Princípio V da constituição — alteração em usecases/entities da camada de domínio com teste unitário focado na regra encapsulada.

---

## NEEDS CLARIFICATION

Nenhum pendente após input do utilizador e clarificações na spec.
