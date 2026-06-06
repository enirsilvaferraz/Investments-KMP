# Quickstart: Balanceamento de carteira

**Feature**: `024-portfolio-balancing` | **Date**: 2026-06-06

Guia de validação manual e automática. Detalhes de modelo e contratos: [data-model.md](data-model.md), [contracts/PortfolioBalancingContract.md](contracts/PortfolioBalancingContract.md).

---

## Pré-requisitos

- Branch `024-portfolio-balancing` com implementação concluída.
- Carteira de teste com posições em RF, RV (incl. FII), fundo previdência, e opcionalmente HASH11.
- IDE com consola visível (log via `println`).

---

## Validação automática (sob pedido)

```bash
./gradlew :domain:usecases:jvmTest --tests "*PortfolioBalancing*"
```

**Resultado esperado**: todos os testes `CalculatePortfolioBalancingUseCaseTest` e `PortfolioBalancingPartitionTest` passam.

---

## Cenário 1 — Relatório completo no histórico (US3)

1. Abrir ecrã de histórico com mês que tenha posições activas.
2. Tocar no ícone de balanceamento (ao lado de «Importar B3»).
3. Verificar na consola tabela com:
   - Cabeçalhos: nome, valor actual, peso alvo, valor ideal, desvio
   - Três secções (Carteira Total, Renda Fixa, Renda Variável)
   - 11 linhas de componentes no total
   - **Sem** coluna de regra de enquadramento

---

## Cenário 2 — Peso fixo 50% (US1)

**Dado**: carteira total R$ 100.000 com RF actual = R$ 100.000 (simplificado).

**Esperado**: linha «Renda Fixa» (Grupo 1) com valor ideal R$ 50.000, desvio R$ 50.000.

---

## Cenário 3 — Previdência dinâmica (US1)

**Dado**: previdência com R$ 10.000 num total de R$ 100.000.

**Esperado**: peso alvo «dinâmico (10,00%)», valor ideal = valor actual, desvio = 0.

---

## Cenário 4 — HASH11 em Cripto (US2)

**Dado**: posição RV com ticker HASH11.

**Esperado**:
- Grupo 1: linha «Cripto Ativos» com património da posição
- Grupo 3: posição **não** contada em componentes RV

---

## Cenário 5 — Ideal aninhado com RF actual zero (US2)

**Dado**: total R$ 100.000, RF actual R$ 0, peso RF 50%.

**Esperado**:
- Grupo 2 presente com actuais zero
- «Pós-fixados» ideal = R$ 16.667 (33,33% × R$ 50.000)

---

## Cenário 6 — Carteira total zero (edge case)

**Dado**: mês sem posições activas (todas liquidadas ou vazias).

**Esperado**: tabela completa; actual, ideal e desvio = 0 em todas as linhas; sem excepção no log.

---

## Cenário 7 — Filtros activos ignorados (edge case)

1. Activar filtro de corretora ou classe no painel de filtros.
2. Disparar balanceamento.

**Esperado**: relatório reflecte **toda** a carteira do mês, não só linhas visíveis na tabela (UC não usa `FilterHoldingHistoryUseCase`).

## Cenário 7b — Período = mês corrente

1. Seleccionar um mês passado no selector do histórico.
2. Disparar balanceamento.

**Esperado**: relatório usa posições do **mês corrente** (`DateProvider`), não o mês visível na tabela.

---

## Cenário 8 — Toques repetidos (US3)

1. Tocar balanceamento duas vezes seguidas.

**Esperado**: botão permanece activo; duas tabelas no log (uma por execução).

---

## Cenário 9 — Erro de carregamento

Simular falha (ex.: período inválido em teste unitário do ViewModel, se existir).

**Esperado**: linha `Balanceamento: <mensagem>` no log; sem falha silenciosa.
