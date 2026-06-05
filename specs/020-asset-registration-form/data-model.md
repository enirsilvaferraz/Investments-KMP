# Data Model: Cadastro — cards Ativo e Posicionamento

**Feature**: `020-asset-registration-form` | **Phase**: 1 | **Date**: 2026-06-05

---

## Decisões arquitecturais

| Decisão | Escolha |
|---------|---------|
| Isenção IR | `fixed_income_assets.income_tax_exempt` (`BOOLEAN`, default `false`) |
| Domínio | `FixedIncomeAsset.incomeTaxExempt: Boolean = false` |
| Migração | Room DB v7 → v8, `AutoMigration` |
| Legado | `NULL`/ausente na migração → `false` ("Não") |
| Botão Salvar | **Sempre habilitado** — sem snapshot/`isDirty` |
| Persistência | Reutilizar `UpsertAssetUseCase` + `UpsertAssetHoldingUseCase` (001) |
| Fora do escopo | Transações, Resumo, Excluir, IncomeTax engine |

---

## Entidade de domínio

### `FixedIncomeAsset` (`:domain:entity`)

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `incomeTaxExempt` | `Boolean` | Sim (default `false`) | Só RF; `true` = "Sim", `false` = "Não" |

Demais campos inalterados. RV e fundos **não** ganham o atributo.

**Documentação**: actualizar `core/domain/entity/docs/DOMAIN.md` (§5 + ER `FixedIncomeAsset`).

---

## Persistência Room (`:data:database`)

### `FixedIncomeAssetEntity`

```kotlin
@ColumnInfo(name = "income_tax_exempt")
val incomeTaxExempt: Boolean = false,
```

### Migração 7 → 8

| Item | Valor |
|------|--------|
| `AppDatabase.version` | `8` |
| `autoMigrations` | `AutoMigration(from = 7, to = 8)` |
| Efeito em dados legados | Todas as linhas RF com `income_tax_exempt = 0` |
| Schema export | `schemas/com.eferraz.database.core.AppDatabase/8.json` |

### Mapeamento (`AssetMappers.kt`)

- `toDomain()` → `incomeTaxExempt = fixedIncome.incomeTaxExempt`
- `toEntity()` → `incomeTaxExempt = incomeTaxExempt` (sem transformação)

---

## Modelo de apresentação

### `AssetManagementUiState` (campos novos/alterados)

| Campo | Tipo | Notas |
|-------|------|-------|
| `incomeTaxExempt` | `Boolean` | Default `false`; visível só em RF |

### `AssetManagementEvents` (novo)

```kotlin
data class IncomeTaxExemptChanged(val exempt: Boolean) : AssetManagementEvents()
```

### Fluxo de salvamento (inalterado na essência — feature 001)

```
Barra inferior [Salvar]
  → checkErros() (ATIVO + Posicionamento)
  → buildAsset() + build holding
  → UpsertAssetUseCase → UpsertAssetHoldingUseCase
  → isCompleted = true → LaunchedEffect fecha dialog
```

---

## Validação

| Camada | O que valida |
|--------|--------------|
| UI (`Validations.kt`) | Campos obrigatórios ATIVO + corretora (existente) |
| `UpsertAssetUseCase` | Regras de negócio por subtipo (existente; sem nova regra para isenção) |

Isenção IR **não** é campo obrigatório — default satisfaz FR-003.

---

## Transições de estado

### Troca de classe (cadastro novo)

```text
AssetClassChanged(newClass)
  → partialResetForAssetClass(newClass)
  → type = null, campos específicos limpos
  → issuer, observations preservados
  → incomeTaxExempt = false (se voltar a RF)
```

### Edição existente

- `assetClass` imutável (`enabled = ui.asset == null` — já implementado)
- `incomeTaxExempt` editável em RF

---

## Entidades fora do modelo desta feature

| Entidade / secção | Acção |
|-------------------|-------|
| `AssetTransaction` / card Transações | **Sem alteração** |
| Resumo (valores mock) | **Sem alteração** |
| `Owner` | Leitura via `GetOwnerUseCase` (existente) |
| `Brokerage` | Seleção + persistência no holding (existente) |
