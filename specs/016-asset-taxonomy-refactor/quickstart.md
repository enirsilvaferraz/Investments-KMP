# Quickstart: 016-asset-taxonomy-refactor

**Branch**: `016-asset-taxonomy-refactor`

Validação manual e build **sob pedido** (constituição IX).

---

## Pré-requisitos

- Branch `016-asset-taxonomy-refactor` com implementação completa.
- (Opcional) Base v6 com ativos RF/RV/fundo para testar migração.

---

## 1. Compilar (sob pedido)

```bash
./gradlew :domain:entity:compileKotlinIosArm64 \
  :data:database:compileKotlinIosArm64 \
  :domain:usecases:compileKotlinJvm \
  :features:composeApp:compileKotlinIosArm64
```

Ou alvo único Desktop/JVM conforme ambiente.

---

## 2. Testes use cases (sob pedido)

```bash
./gradlew :domain:usecases:jvmTest
```

---

## 3. Migração de dados

1. Instalar build sobre app com DB **versão 6** e carteira populada.
2. Abrir app → Room aplica **6→7**.
3. Verificar: mesma contagem de ativos/holdings.
4. Abrir um CDB pós-fixado legado → **Indexador** = Pós-fixado, **Tipo** = CDB.

---

## 4. Cadastro (SC-002)

| Classe | Passos | Esperado |
|--------|--------|----------|
| RF | CDB + pós-fixado + salvar | Reabrir: indexador e tipo corretos |
| RV | ETF + salvar | Sem campo indexador |
| Fundo | Previdência + salvar | Sem indexador |

---

## 5. Histórico e filtros (015) — SC-003

1. Histórico do mês com RF/RV/fundo.
2. Filtro por classe renda fixa → só RF.
3. **SC-003**: na **mesma** carteira e período, comparar totais (valor investido, quantidade) antes e depois da migração — expectativa **zero divergência**; opcionalmente coberto por T031 (teste JVM) em `tasks.md`.

---

## 6. Documentação

```bash
rg 'InvestmentCategory|FixedIncomeSubType' core/domain/entity/docs/DOMAIN.md
```

Esperado: **zero** ocorrências ativas (só nota histórica opcional).

---

## 7. Schema Room (CI / sob pedido)

```bash
./gradlew :data:database:compileKotlinJvm
```

Commitar `core/data/database/schemas/.../AppDatabase/7.json` se gerado.

---

## Falhas comuns

| Sintoma | Causa provável |
|---------|----------------|
| Crash ao abrir DB | `Migration6To7` incompleta ou ordem rename RF errada |
| Compile error `FixedIncomeAssetType` ambíguo | Ordem de rename entity (ver research R1) |
| Filtro histórico vazio | `WalletHistoryFilter` ainda usa `InvestmentCategory` |
