# Quickstart — Redesenho do Dialog de Transações

**Feature**: 002-transaction-form-redesign
**Target audience**: implementador da feature; revisor de PR.

Este guia mostra como validar manualmente o novo dialog após a implementação, e quais comandos de build/teste rodar.

---

## 1. Verificação de build (obrigatória)

A partir da raiz do repositório:

```bash
# Compila o módulo onde a feature é implementada.
./gradlew :features:asset-management:compileKotlinJvm

# Compila o shell que consome o dialog (descobre quebras de contrato público).
./gradlew :apps:umbrellaApp:compileKotlinJvm
```

Ambos devem terminar com `BUILD SUCCESSFUL`. Os nomes exatos dos accessors estão em `settings.gradle.kts`.

> Nenhum teste em `:domain:usecases` é exigido por esta feature (a constitution só obriga testes quando há alteração em `:domain:usecases`, e esta entrega não toca o domínio).

---

## 2. Verificação visual via Preview Compose

`TransactionManagementView.kt` expõe `TransactionFormViewPreview` parametrizado por categoria. Abrir o ficheiro no Android Studio e validar:

- **FIXED_INCOME**: colunas Data, Transação, Valor Total, X.
- **VARIABLE_INCOME**: colunas Data, Transação, Quantidade, Unitário, Valor Total, X.
- **INVESTMENT_FUND**: colunas Data, Transação, Valor Total, X.
- **Vazio** (`TransactionManagementUiState()`): cabeçalho + botões Adicionar/Salvar (sem linhas; Salvar desabilitado).

> `NewTransactionsTable` (placeholder) deixa de ser invocado por `AssetManagementScreen` e pode ser removido se nenhum chamador remanescer.

---

## 3. Verificação funcional (build instalado/desktop)

1. **Rodar a app desktop**:
   ```bash
   ./gradlew :apps:umbrellaApp:run
   ```
2. Navegar até a tela que abre o dialog de Transações para uma holding com transações cadastradas.
3. **Cenário 1 — Carga**:
   - Verificar que as transações aparecem ordenadas por data ascendente.
   - Botão **Salvar** está **desabilitado**.
4. **Cenário 2 — Editar campo**:
   - Alterar `totalValue` em uma linha.
   - Botão **Salvar** fica **habilitado**.
   - Reverter o valor original do campo (mesma string).
   - Botão **Salvar** volta a ficar **desabilitado**.
5. **Cenário 3 — Adicionar**:
   - Clicar em **Adicionar**.
   - Surge uma nova linha no final com a data de hoje.
   - Botão **Salvar** fica **habilitado**.
6. **Cenário 4 — Remover sem salvar**:
   - Clicar no X de uma linha existente.
   - Linha desaparece da UI.
   - Botão **Salvar** fica **habilitado**.
   - Fechar o dialog pelo X do topo.
   - Reabrir o dialog: a linha **continua** presente no banco (não foi excluída).
7. **Cenário 5 — Salvar adições + remoções**:
   - Adicionar uma linha válida, remover outra existente, editar uma terceira.
   - Clicar em **Salvar**.
   - Dialog **fecha** automaticamente em ≤ 2 s.
   - Reabrir: as alterações refletem o estado persistido.
8. **Cenário 6 — Preservação de observations**:
   - Em uma transação com `observations` existente no banco, editar somente outros campos.
   - Salvar.
   - Reabrir e (via banco/Room ou inspector) confirmar que `observations` permaneceu intacto. Validar também que linhas adicionadas via "Adicionar" têm `observations` `null`/vazio.
9. **Cenário 7 — Cancelar via X**:
   - Fazer várias edições, adições e remoções; clicar no X do topo do dialog.
   - Reabrir: estado idêntico ao inicial (nada foi persistido).
10. **Cenário 8 — Falha de save (smoke test opcional)**:
    - Forçar uma falha (ex.: durante desenvolvimento, lançar `IllegalStateException` em `SaveTransactionUseCase` ou simular via Room exception).
    - Confirmar que o dialog **não fecha**, o botão Salvar volta ao estado habilitado (se ainda houver `isDirty`), e nenhum toast/mensagem de erro é exibido (paridade com clarification).

---

## 4. Checklist final do revisor

- [ ] `UiTableV3` não aparece em nenhum ficheiro de `core/presentation/asset-management/.../transactions/`.
- [ ] `TableInputDate`, `TableInputSelect` e `TableInputMoney` **não** aparecem em nenhum ficheiro de `core/presentation/asset-management/.../transactions/` — todas as células usam `FormTextField` (helper local), espelhando `NewTransactionsTable`.
- [ ] `TransactionManagementView.kt` renderiza com `Column`/`Row` e botões abaixo da tabela.
- [ ] Botão Salvar habilitado somente quando `isDirty && !isSaving`.
- [ ] `DraftTransactionDeleteClicked` **não** invoca `DeleteTransactionUseCase` no ViewModel.
- [ ] `onSave` aplica delete + upsert calculados a partir de `initialSnapshot`.
- [ ] `initialSnapshot` é populado **apenas** em `loadInitialState`.
- [ ] `DraftTransactionObservationChanged` foi removido do `sealed class`.
- [ ] `NewTransactionsTable()` não é mais invocado por `AssetManagementScreen.kt` (e o ficheiro `TransactionManagementScreen.kt` foi reduzido/removido se aplicável).
- [ ] `./gradlew :features:asset-management:compileKotlinJvm` e `./gradlew :apps:umbrellaApp:compileKotlinJvm` passam.
