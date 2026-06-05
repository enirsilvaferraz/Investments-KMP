# Research: Cadastro de investimento — cards Ativo e Posicionamento

**Feature**: `020-asset-registration-form` | **Phase**: 0 | **Date**: 2026-06-05

**Diretriz do planeamento**: reuso máximo, diff mínimo, sem alterar Transações/Resumo/Excluir.

---

## R1 — Onde persistir "Isento de IR"

**Decision**: Coluna `income_tax_exempt INTEGER NOT NULL DEFAULT 0` em `fixed_income_assets`, espelhada em `FixedIncomeAsset.incomeTaxExempt: Boolean` (default `false` = "Não").

**Rationale**:
- Atributo exclusivo de renda fixa; mesma tabela 1:1 usada por `b3Identifier` (feature 004).
- `Boolean` é o tipo mais simples para Room, domínio e integração futura com IncomeTax (019).
- Registros legados recebem `0` na migração → retrocompatível com spec (interpretar como "Não").

**Alternatives considered**:

| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| `YesOrNo` no domínio | Enum já existe para UI/filtros; `Boolean` evita conversões em toda a cadeia de persistência |
| Coluna em `assets` | Nullable/morto para RV e fundos |
| Só em UiState sem DB | Viola FR-004 |

---

## R2 — Migração Room (v7 → v8)

**Decision**: Incrementar `AppDatabase.version` para **8**; `AutoMigration(from = 7, to = 8)`; exportar schema `8.json`.

**Rationale**: Padrão idêntico à feature 004 (ADD COLUMN com default). Sem `MigrationSpec` customizado.

**Alternatives considered**:

| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| Migration manual SQL | Desnecessário para coluna NOT NULL com default |
| Destructive migration | Perda de dados |

---

## R3 — UI Sim/Não para isenção

**Decision**: Reutilizar `SegmentedControl` + `SegmentedControlChoice("Sim"/"Não")` já presente em `FixedIncomeFields`; mapear para `UiState.incomeTaxExempt: Boolean` via evento `IncomeTaxExemptChanged(Boolean)`.

**Rationale**: Componente e copy já existem no ecrã; elimina `remember { mutableStateOf }` órfão (bug actual). Labels alinhadas a `YesOrNo.asLabel()` do módulo `:presentation:naming` onde fizer sentido na UI.

**Alternatives considered**:

| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| Novo composable binário | Duplica `SegmentedControl` |
| `FilterToggleGroup` (DS v2) | Formulário usa design-system v1; YAGNI |

---

## R4 — Botão Salvar único (sempre habilitado)

**Decision**:
- **Remover** `FormCardActions` de persistência nos cards ATIVO e POSICIONAMENTO (não desactivar — eliminar botão Salvar interno).
- Renomear **Concluir → Salvar** na barra inferior e ligar a `AssetManagementEvents.Save` (fluxo `onSave()` já implementado na feature 001).
- Botão **Salvar sempre habilitado** — sem lógica de enable/disable na UI (cadastro novo, edição, com ou sem alterações).
- **Sem** snapshot de comparação (`AssetFormSnapshot`, `isDirty`, `isNew` para save).

**Protecção contra double-tap**: manter guard existente em `onSave()` (`if (isSaving) return`) — interno ao ViewModel, não reflectido no `enabled` do botão.

**Rationale**: Menos código e menos estados a manter; alinhado a FR-010/FR-011 da spec (Salvar sempre ON; guard `isSaving` no ViewModel).

**Alternatives considered**:

| Alternativa | Motivo de rejeição |
|-------------|-------------------|
| Snapshot + `isDirty` (plano anterior) | Rejeitado pelo utilizador — complexidade desnecessária para esta entrega |
| Desabilitar durante `isSaving` | Rejeitado — utilizador pediu botão sempre activo |
| Dois botões Salvar (card + barra) | Viola FR-007 |

---

## R5 — Reset parcial ao trocar classe (cadastro novo)

**Decision**: No handler `AssetClassChanged`, aplicar função única `AssetManagementUiState.partialResetForAssetClass(newClass)` que:
- **Limpa**: `type`, campos específicos da classe anterior (RF/RV/Fundo), erros associados, `incomeTaxExempt` → `false`
- **Mantém**: `issuer`, `observations`, listas (`issuers`, `brokerages`), posicionamento (`owner`, `brokerage`)

**Rationale**: Lógica centralizada num único sítio; evita ramificações espalhadas no ViewModel.

---

## R6 — Card Posicionamento (titular + corretora)

**Decision**:
- **Titular**: `FormTextField` **read-only** com `ui.owner?.name` (corrigir bug actual que usa `brokerage` no dropdown Titular).
- **Corretora**: manter `AppDropdownField` wired a `BrokerageChanged` (já existe no ViewModel).

**Rationale**: Spec exige titular só leitura; reutiliza `FormTextField` com `readOnly = true` — sem novo componente.

---

## R7 — Dropdown Tipo por classe

**Decision**: Opções do dropdown **Tipo** derivadas de `assetClass`:
- `FIXED_INCOME` → `FixedIncomeAssetType.entries`
- `VARIABLE_INCOME` → `VariableIncomeAssetType.entries`
- `INVESTMENT_FUND` → `InvestmentFundAssetType.entries`

**Rationale**: Corrige bug actual (sempre `FixedIncomeAssetType`); necessário para FR-001 sem novos tipos.

---

## R8 — Escopo congelado (fora desta entrega)

**Decision**: **Não alterar** comportamento funcional de:
- Card **TRANSAÇÕES** (`TransactionFormView`, `FormCardActions` de toggle)
- Card **RESUMO** (valores estáticos/read-only)
- Botão **Excluir** (permanece desabilitado com TODO)
- Motor **IncomeTax** (019) — só persistir flag

**Rationale**: Clarificação explícita na spec; minimiza diff e risco de regressão.

---

## R9 — Testes

**Decision**: Actualizar `UpsertAssetUseCaseTest` com RF + `incomeTaxExempt = true/false`; sem novos UseCases.

**Rationale**: Princípio V — alteração em entidade + validação existente; cobertura mínima útil.
