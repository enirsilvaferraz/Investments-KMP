# Research: Identificador B3 em Renda Fixa

**Feature**: `004-fixed-income-b3-id` | **Phase**: 0 | **Date**: 2026-05-24

---

## R1 — Onde persistir o identificador

**Decision**: Coluna nullable `b3_identifier TEXT` na tabela `fixed_income_assets` (Room), espelhada em `FixedIncomeAsset.b3Identifier: String?` no domínio.

**Rationale**:
- O atributo é exclusivo de renda fixa; a tabela `fixed_income_assets` já modela extensão 1:1 do ativo RF.
- Evita coluna morta em `assets` para linhas de RV e fundos.
- Migração trivial: `ALTER TABLE` via `AutoMigration(from = 5, to = 6)` — valores existentes ficam `NULL` (equivalente a “não informado”).

**Alternatives considered**:
| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| Coluna em `assets` | Nullable em todas as categorias; semântica confusa e risco de escrita indevida em RV/fundos |
| Tabela separada | YAGNI para um único campo opcional |
| Só em memória / prefs | Viola FR-001 e FR-005 (persistência e migração) |

---

## R2 — Migração de banco (Room 3)

**Decision**: Incrementar `AppDatabase.version` de **5** para **6**; registrar `AutoMigration(from = 5, to = 6)`; gerar schema export `schemas/.../6.json` com `./gradlew :data:database:compileKotlinJvm` (ou task Room do módulo).

**Rationale**: Padrão já usado no projeto (`AutoMigration` 1→2→3→4→5 em `AppDatabase.kt`). Sem `MigrationSpec` customizado — adição de coluna nullable é suportada automaticamente.

**Alternatives considered**:
| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| Migration manual SQL | Desnecessário para ADD COLUMN nullable |
| Destructive migration | Perda de dados; proibido pela spec |

---

## R3 — Normalização ao salvar

**Decision**: Aplicar `b3Identifier?.trim()?.ifBlank { null }` ao gravar (`buildFixedIncomeAsset`, `AssetMappers.toEntity`) e ao derivar estado “informado” no histórico (`takeIf { it.isNotBlank() }` sobre valor já trimado ou trim na leitura).

**Rationale**: Pedido explícito do utilizador — remover espaços às bordas; `"  ABC-123  "` persiste como `"ABC-123"`; `"   "` ou vazio → `null` (não informado).

**Alternatives considered**:
| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| Persistir sem trim | Rejeitado pelo utilizador |
| `trim()` + persistir `""` | `ifBlank { null }` unifica vazio e só-espaços |

---

## R4 — Coluna no Histórico para todas as categorias

**Decision**: Uma coluna **sempre presente** à direita da tabela (`UiTableV3` em `AssetHistoryScreen`). Conteúdo por linha:
- **Renda fixa**: ícone Info (azul) + tooltip com valor **ou** ícone Warning (amarelo) + tooltip de ausência.
- **Renda variável / fundos**: célula **vazia** (sem ícone, sem tooltip) — alinhamento de colunas mantido.

**Rationale**: Clarificação do utilizador no `/speckit-plan`: “os demais tipos têm uma coluna vazia no lugar dos ícones”. Melhora alinhamento visual da grelha em tabelas mistas (SC-004).

**Alternatives considered**:
| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| Ocultar coluna quando filtro ≠ RF | Quebra alinhamento ao mudar filtro de categoria |
| Mesma coluna só para RF | Não atende pedido explícito de coluna vazia para outros tipos |

---

## R5 — Posição do campo no cadastro

**Decision**: `FormTextField` **"Identificador B3"** dentro de `FixedIncomeFields` (bloco específico RF), não na linha compartilhada de “Observações gerais”.

**Rationale**: Observações é comum a todas as categorias na área compartilhada; identificador B3 só existe para RF (FR-006). Paridade de componente com observações (`FormTextField`, sem `maxLength`).

---

## R6 — Ícones e tooltips no histórico

**Decision**: Novo composable em `:presentation:naming` (ex.: `B3IdentifierStatusCell`) usando `TooltipBox` + `PlainTooltip` como `TableIcons.kt` (`InvestmentCategory.BuildIcon`). Cores: `getInfoColor()` (preenchido), `getWarningColor()` (ausente). Ícones Material: `Icons.Default.Info` e `Icons.Default.Warning`.

**Rationale**: Reutiliza padrão existente da coluna esquerda; cores já usadas na tabela (ex. saldo de transações).

**Textos de tooltip (fixos)**:
- Preenchido: valor literal do identificador.
- Ausente: `Identificador B3 não informado.`

---

## R7 — Testes

**Decision**:
- Atualizar `UpsertAssetUseCaseTest` com RF + `b3Identifier` (persistência round-trip via mock do repositório).
- Teste opcional de mapper em `:data:database` se já existir padrão; caso contrário, cobertura via UseCase é suficiente (constituição V).

**Rationale**: Nenhum UseCase novo; alteração em entidade + fluxo existente `UpsertAssetUseCase` / `GetHistoryTableDataUseCase` (mapeamento apenas).
