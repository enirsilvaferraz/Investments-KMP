# Quickstart: 019-holding-income-tax

**Branch**: `019-holding-income-tax`

Validação **sob pedido** (constituição IX). Implementação mínima conforme [plan.md](./plan.md) e [contracts/IncomeTaxContract.md](./contracts/IncomeTaxContract.md).

---

## Pré-requisitos

- Branch com `IncomeTax.kt` em `holdings/`.
- [data-model.md](./data-model.md) e [research.md](./research.md) alinhados.

---

## 1. Verificação estática (sem Gradle)

```bash
rg 'class IncomeTax' core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/holdings/
rg 'earliestPurchaseDate' core/domain/entity/
rg 'IncomeTax' core/domain/usecases/ core/data/ core/presentation/
```

Esperado: só `IncomeTax` em `holdings/`; **sem** `earliestPurchaseDate`; **sem** referências em usecases/data/features na v1.

---

## 2. Testes de entidade (sob pedido)

```bash
./gradlew :domain:entity:jvmTest --tests '*IncomeTax*'
```

---

## 3. Cenários manuais (revisão de código)

| # | Entrada | Esperado |
|---|---------|----------|
| 1 | Lucro 1000, 181 dias | taxRate 20%, taxValue 200 |
| 2 | Lucro 500, 180 dias | taxRate 22,5%, taxValue 112,50 |
| 3 | Lucro 800, 361 dias | taxRate 17,5%, taxValue 140 |
| 4 | Lucro 2000, 721 dias | taxRate 15%, taxValue 300 |
| 5 | Lucro -10, qualquer faixa | taxValue 0 |
| 6 | Compra 2024-06-01, ref 2024-01-01 | exceção |

---

## 4. Documentação

- `core/domain/entity/docs/DOMAIN.md` — secção `IncomeTax` (após implementação).

---

## 5. Entrega

| Artefacto |
|-----------|
| `IncomeTax.kt` + `IncomeTaxTest.kt` |
