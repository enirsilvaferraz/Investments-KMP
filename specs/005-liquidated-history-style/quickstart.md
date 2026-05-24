# Quickstart: Destaque visual de investimentos liquidados no Histórico

**Feature**: `005-liquidated-history-style` | **Date**: 2026-05-24

---

## Pré-requisitos

- Branch `005-liquidated-history-style`
- App com dados de histórico em pelo menos um período
- Pelo menos uma posição com **valor atual = 0** e outra com **valor atual > 0**

---

## Build e testes unitários

```bash
./gradlew :domain:usecases:jvmTest --tests "com.eferraz.usecases.entities.HoldingHistoryViewTest"
./gradlew :features:composeApp:compileKotlinJvm
```

---

## Checklist manual — `HoldingHistoryView`

1. Colocar breakpoint ou log temporário: para linha com valor atual 0, `isLiquidated == true`.
2. Linha com valor atual positivo: `isLiquidated == false`.

---

## Checklist manual — Tabela de histórico

### Colunas gerais (liquidado)

- [ ] Corretora, nome, observação, valor anterior em **cinza muted**
- [ ] Valor atual (campo editável) com texto em **cinza muted** (campo ainda editável se RF/fundo)

### Colunas com prioridade semântica (liquidado)

- [ ] **Valorização** positiva → verde (não forçar muted)
- [ ] **Valorização** negativa → vermelho
- [ ] **Valorização** zero → cinza da regra da coluna
- [ ] **Transações** com saldo > 0 → verde; < 0 → alerta; 0 → “Adicionar” cinza

### Exceções

- [ ] Ícones de categoria/liquidez/B3 com cores habituais (ex.: aviso B3 **amarelo** com valor atual 0)
- [ ] Tooltip B3 em linha liquidada: abrir tooltip (hover/long-press) — texto **não** usa cinza muted das células

### Transição de estado (FR-005 / SC-004)

- [ ] Editar **Valor Atual** de `0` para valor positivo na tabela → após persistência (debounce/foco), linha deixa muted **sem reiniciar o app**
- [ ] Editar **Valor Atual** para `0` e salvar → linha passa a muted nas colunas gerais
- [ ] Confirmar fluxo: `TableInputMoney` → `onValueChange` → `HistoryIntent.UpdateEntryValue` → `HistoryViewModel.updateEntryValue` → lista `tableData` atualizada na UI

### Regressão

- [ ] Linha **não** liquidada: cores idênticas ao comportamento anterior
- [ ] Painel de resumo (`Summary`) inalterado

---

## Cenários sugeridos de dados

| Cenário | Valor atual | Valorização | Saldo transações | Esperado |
|---------|-------------|-------------|------------------|----------|
| A | 0 | +5% | +1000 | Geral muted; Valorização verde; Transações verde |
| B | 0 | -2% | 0 | Geral muted; Valorização vermelha; “Adicionar” cinza |
| C | 1000 | +3% | 0 | Sem muted geral; cores normais |

---

## Filtro futuro (não testar nesta feature)

Documentado para referência: `data.filter { it.isLiquidated }` — implementação em feature separada.
