# Quickstart: Cadastro — cards Ativo e Posicionamento

**Feature**: `020-asset-registration-form` | **Date**: 2026-06-05

## Pré-requisitos

- Branch `021-asset-registration-form`
- Pelo menos uma corretora cadastrada (assunção da spec)
- Artefactos: [data-model.md](./data-model.md), [contracts/ui-contracts.md](./contracts/ui-contracts.md)

## Verificação (sob pedido)

```bash
# Schema Room após migração 7→8
./gradlew :data:database:compileKotlinJvm

# Módulo UI
./gradlew :features:asset-management:compileKotlinJvm

# Testes de domínio (UpsertAssetUseCase)
./gradlew :domain:usecases:jvmTest
```

> Princípio IX: o agente **não** executa Gradle por defeito — só quando pedido ou em CI.

## Ficheiros principais (delta)

| Ficheiro | Alteração |
|----------|-----------|
| `FixedIncomeAsset.kt` | `+ incomeTaxExempt: Boolean = false` |
| `FixedIncomeAssetEntity.kt` | `+ incomeTaxExempt` |
| `AssetMappers.kt` | round-trip |
| `AppDatabase.kt` | version 8, AutoMigration 7→8 |
| `AssetManagementUiState.kt` | `+ incomeTaxExempt` |
| `AssetManagementEvents.kt` | `IncomeTaxExemptChanged` |
| `AssetManagementViewModel.kt` | reset parcial classe |
| `AssetManagementMap.kt` | mapeamento RF |
| `AssetManagementScreen.kt` | wiring Isento IR, titular RO, barra Salvar, tipo por classe |
| `Validations.kt` | confirmar cobertura ATIVO + corretora (T027) |
| `DOMAIN.md` | documentar `incomeTaxExempt` |

**Sem tocar** (salvo imports cosméticos): `TransactionFormView`, bloco RESUMO, `TransactionManagement*`.

## Cenários manuais

### 1. Novo RF — Isento de IR default

1. Abrir dialog novo investimento (FAB +).
2. Manter classe **Renda fixa**.
3. Verificar **Isento de IR = Não** antes de interagir.
4. Preencher ATIVO + corretora.
5. Clicar **Salvar** na barra inferior → dialog fecha.
6. Reabrir → isenção continua **Não**.

### 2. Isento = Sim persiste

1. Novo RF → alternar Isento para **Sim** → salvar → reabrir → **Sim**.

### 3. Isento só em RF

1. Cadastro novo → classe **Renda variável** ou **Fundo** → campo Isento **ausente**.
2. RF com Sim → trocar classe (novo) → salvar → reabrir RV → sem isenção persistida.

### 4. Botão Salvar — sempre habilitado

1. Cadastro novo → **Salvar** clicável ao abrir (mesmo com formulário vazio; validação bloqueia persistência).
2. Edição sem alterações → **Salvar** continua clicável (re-grava estado actual se válido).
3. Durante persistência → botão permanece habilitado; cliques extra ignorados no ViewModel.

### 5. Validação obrigatória (FR-009, FR-012)

1. Cadastro novo → clicar **Salvar** sem preencher → erros nos campos obrigatórios ATIVO; dialog aberto.
2. Preencher ATIVO válido sem corretora → erro em Posicionamento; nada persistido.

### 6. Posicionamento

1. Titular visível, **não** editável.
2. Salvar sem corretora → erro, dialog aberto.
3. Com corretora → holding persistido ao reabrir.

### 7. Reset parcial de classe (novo)

1. Preencher emissor + observações + campos RF.
2. Trocar para RV → tipo/campos RF limpos; emissor/observações mantidos.

### 8. Regressão — fora do escopo

1. Cards **Transações** e **Resumo** comportam-se como antes.
2. Botão **Excluir** inactivo/oculto.
3. Não há botão Salvar funcional dentro dos cards ATIVO/POSICIONAMENTO.

## Falha parcial (001)

Se asset grava e holding falha: dialog permanece aberto, dados preservados, erro em corretora — sem rollback automático.
