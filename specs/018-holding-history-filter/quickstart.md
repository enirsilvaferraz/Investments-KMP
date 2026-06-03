# Quickstart: 018-holding-history-filter

**Branch**: `018-holding-history-filter`

Validação manual e build **sob pedido** (constituição IX).

---

## Pré-requisitos

- Branch com implementação conforme [plan.md](./plan.md) e [contracts/HoldingHistoryFilterContract.md](./contracts/HoldingHistoryFilterContract.md).
- Carteira com posições em **várias corretoras** e histórico no mês seleccionado.

---

## 1. Verificação estática (sem Gradle)

```bash
rg 'val brokerage:' core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/GetHistoryTableDataUseCase.kt
rg 'FilterHoldingHistoryEntriesUseCase' core/
rg 'brokerageIds' core/domain/usecases/
rg 'toWalletHistoryFilterCriteria\(' core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/
```

Esperado:
- `GetHistoryTableDataUseCase.Param` **sem** campo `brokerage`.
- UC de filtro referenciado no pipeline da tabela e testado em `jvmTest`.
- Mapper de critérios aceita corretora seleccionada.

---

## 2. Testes use cases (sob pedido)

```bash
./gradlew :domain:usecases:jvmTest --tests '*WalletHistoryFilter*' --tests '*FilterHoldingHistory*' --tests '*GetHistoryTableData*'
```

Foco: corretora isolada/OR/AND; regressão cenários 015; Param migrado.

---

## 3. Cenários manuais (UI inalterada)

| # | Passo | Esperado |
|---|--------|----------|
| 1 | Abrir Histórico | Layout igual (painel, segment corretora, tabela, sumário) |
| 2 | Seleccionar corretora A | Só linhas da corretora A; facetas só dimensões de A |
| 3 | Desmarcar corretora (toggle) | Todas as corretoras do período; facetas globais do mês |
| 4 | Corretora A + filtro «Renda Fixa» | Intersecção RF em A |
| 5 | Mudar mês/ano | Painel repõe default («Não liquidado»); **corretora limpa** (FR-010); listagem do novo mês sem filtro de corretora |
| 6 | Facetas com corretora A | Opções do painel só dimensões presentes em A (FR-009; critério sem `defaultForHistory()`) |
| 6 | Posição liquidada no mês (património 0) | Excluída com default; incluída se activar «Liquidado» |

---

## 4. Regressão visual

Comparar capturas ou inspecção lado a lado **antes/depois** no mesmo build: zero componentes novos/removidos no ecrã Histórico (FR-007).

---

## Artefatos desta feature

| Ficheiro | Conteúdo |
|----------|----------|
| [plan.md](./plan.md) | Plano e ondas de implementação |
| [research.md](./research.md) | Decisões de desenho |
| [data-model.md](./data-model.md) | Critérios, candidato, fluxos |
| [contracts/HoldingHistoryFilterContract.md](./contracts/HoldingHistoryFilterContract.md) | Contrato técnico |
| [tasks.md](./tasks.md) | Gerado por `/speckit.tasks` |
