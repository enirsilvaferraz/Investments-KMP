# Quickstart: Validação — Feature 023 (modelo unificado de transações)

Guia de verificação manual e por testes. **Não** inclui implementação — ver `plan.md` e `tasks.md`.

**Pré-requisitos**: branch `023-unify-asset-transaction`; build local quando o utilizador quiser validar (princípio IX).

---

## 1. Testes unitários (obrigatórios na implementação)

Executar **sob pedido** do utilizador:

```bash
./gradlew :domain:entity:jvmTest :domain:usecases:jvmTest
```

**Suites críticas** (actualizar/criar):

| Teste | Verifica |
|-------|----------|
| `AssetTransactionContractTest` | Tipo único; sem `holding`; sem subtipos |
| `TransactionBalanceTest` | Soma via `totalValue` derivado |
| `SaveTransactionUseCaseTest` | Persistência qty + unitPrice |
| `SaveAssetWithTransactionsUseCaseTest` | Save atómico com tipo unificado |

Cenários GIVEN/WHEN/THEN sugeridos:
- RV: qty=100, price=28.5 → totalValue=2850.0
- RF migrado: qty=1, price=5000 → totalValue=5000.0
- Venda (SALE) incluída no balanço

---

## 2. Migração de base de dados

**Cenário**: instalar build com DB v9 contendo transações RF, RV e Fundos.

**Passos**:
1. Abrir app na versão anterior (ou fixture v9).
2. Actualizar para build com migração 9→10.
3. Abrir posição com transações legadas.

**Resultado esperado** (SC-003):
- RF/Fundos: qty=1, preço unitário = valor total antigo, total derivado idêntico.
- RV: qty e preço preservados; total = produto.
- Observações não visíveis em lado nenhum.

**Inspecção SQL** (Android Studio Database Inspector ou `adb`):

Ver queries em [unified-transaction-domain.md](contracts/unified-transaction-domain.md).

---

## 3. Formulário inline — cadastro (User Story 1)

**Ecrã**: Gestão de ativos → card Transações (`AssetManagementScreen`).

### Renda variável

1. Adicionar linha; preencher data, tipo Compra, qty `100`, preço `28,50`.
2. Confirmar valor total **2850** (ou equivalente) e campo **desabilitado**.
3. Salvar posição; reabrir.
4. Valores persistidos coerentes.

### Renda fixa / Fundos

1. Adicionar linha.
2. Confirmar quantidade **1** e **desabilitada**.
3. Informar preço unitário; total actualiza automaticamente.
4. Salvar e reabrir.

### Sem validação numérica

1. Informar qty `0` ou preço negativo em RV.
2. Salvar **não** deve ser bloqueado por validação de qty/price (spec refinamento).

---

## 4. Formulário inline — edição legado (User Story 2)

1. Carregar posição com transações pré-migração.
2. Editar preço unitário em RF; total actualiza para `1 × novo preço`.
3. Salvar; releitura coerente.
4. Confirmar ausência de campo observações.

---

## 5. Leitura downstream

1. Abrir **histórico** da carteira — transações listadas sem erro (consumidores usam `totalValue` derivado).
2. Verificar que totais investidos / balanços não regrediram vs pré-migração (amostra manual).

---

## 6. Documentação

Confirmado na implementação (feature 023):
- `core/domain/entity/docs/DOMAIN.md` §9.3 — tipo único `AssetTransaction` (sem subtipos)
- `docs/Modelagem do Banco de Dados.md` — ER, DDL `asset_transactions` achatada (Room v10), sem tabelas satélite
- `AGENTS.md` — secção transações actualizada (tipo único, Room v10, UI em asset-management)

---

## Checklist rápido

- [x] Tipo único `AssetTransaction` no domínio (`data class`: qty, unitPrice, totalValue derivado)
- [x] DB v10 sem tabelas satélite, sem colunas `observations` nem `asset_class` em `asset_transactions`
- [x] Formulário inline: layout uniforme, total read-only (`TransactionManagementView` em asset-management)
- [x] RF/Fundos: qty=1 fixa (read-only na UI)
- [x] Testes `:entity` escritos (`AssetTransactionContractTest`, `TransactionBalanceTest`); `:usecases` usa `AssetTransaction` unificado
- [x] Legado `composeApp/features/transactions/` removido; `detekt-baseline.xml` limpo (entradas órfãs TransactionForm/ViewModel/Panel/Table/Row)
