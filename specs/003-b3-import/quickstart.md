# Quickstart: Importação de Dados da B3

**Feature**: `003-b3-import` | **Date**: 2026-05-23

---

## Pré-requisitos

- JDK 17+ (Desktop JVM)
- Arquivo XLSX exportado pela B3 em disco (ex.: `posicao-YYYY-MM-DD-*.xlsx`)
- IDE ou terminal com saída visível para o **console** da aplicação Desktop

> **Escopo**: validação manual completa apenas em **Desktop**. Android/iOS: bypass — botão sem ação, sem picker, sem log.

---

## Verificação de Build (após implementação)

```bash
# Módulos tocados pela feature (paths Gradle → físicos em core/)
./gradlew :domain:entity:compileKotlinJvm
./gradlew :domain:usecases:compileKotlinJvm
./gradlew :data:filestore:compileKotlinJvm
./gradlew :features:composeApp:compileKotlinJvm
./gradlew :domain:usecases:jvmTest
```

---

## Testar o Fluxo no Desktop

1. Executar: `./gradlew :apps:desktopApp:run`
2. Abrir **Posicionamento no Período** (`AssetHistoryScreen`)
3. Botão de importação à **esquerda** do export (ícone upload)
4. Clicar — diálogo nativo filtrando `.xlsx`
5. Selecionar `posicao-*.xlsx` válido
6. Desde o toque até o fim do fluxo: `CircularProgressIndicator` no mesmo slot do botão de importação (mesmo `Modifier.size` que o `IconButton`, FR-009) — inclui tempo com o diálogo aberto
7. No **console da IDE/terminal**, verificar saída por guia (apenas as cinco B3 **presentes** no ficheiro):

```
=== Acoes — header ===
Produto | Instituição | ...
=== Acoes (N registros) ===
...
=== Acoes — totais calculados ===
...
```

Repetir para `ETF`, `Fundo de Investimento`, `Renda Fixa`, `Tesouro Direto` quando existirem no arquivo.

8. Ao concluir: spinner some, botão volta — **sem** Snackbar nem mensagem de sucesso na UI (FR-016, SC-008)

---

## Cenários de Teste Manual

| Cenário | Ação | Resultado esperado |
|---------|------|-------------------|
| Arquivo válido B3 | Selecionar `posicao-*.xlsx` com as 5 guias | Dados das guias presentes no **console**; botão restaurado; sem UI de sucesso |
| Apenas subset de guias | Arquivo com 1–4 guias B3 conhecidas | Só essas guias no console; demais nomes ignorados sem log (FR-012) |
| Sem guias B3 conhecidas | XLSX só com abas desconhecidas ou vazio de abas B3 | Sem saída no console; botão restaurado; sem erro na UI (FR-013) |
| Cancelamento | Fechar diálogo sem selecionar | Sem log; botão restaurado (FR-007) |
| Arquivo não-xlsx | Selecionar `.csv` (ou SO sem filtro nativo) | Ficheiro não importado; motivo no **console** (rejeição pós-seleção se necessário); botão restaurado; **sem** Snackbar (FR-003, SC-003) |
| Arquivo corrompido | `.xlsx` inválido | Erro no **console**; app estável; botão restaurado (FR-008, SC-005) |
| Guia vazia / só cabeçalho | Guia B3 conhecida sem linhas de dados | Console identifica guia como sem dados (FR-010) |
| Colunas ausentes | Guia B3 com cabeçalho incompleto | **Nenhum** dado de guias no console; só erro `MISSING_COLUMNS`; botão restaurado (FR-015, SC-007) |
| Timeout | Fluxo completo (diálogo + parse) &gt; 30 s desde o toque | Mensagem de timeout no **console** (UseCase, FR-011a); botão restaurado (FR-011) |
| Permissão negada | Ficheiro inacessível após seleção | Motivo no **console**; botão restaurado (FR-008, FR-014) |

---

## Checklist de Aceitação (mapeado ao spec.md)

- [ ] FR-001: Botão de importação à esquerda do export
- [ ] FR-002: Diálogo nativo ao tocar
- [ ] FR-003: Filtro `.xlsx`; rejeição pós-seleção registada no **console** se necessário
- [ ] FR-004: Somente cinco guias pelo nome exato
- [ ] FR-005 / FR-006: Tabelas lidas e exibidas no **console** com nome da guia
- [ ] FR-007: Cancelamento sem erro nem estado inconsistente
- [ ] FR-008 / FR-011: Corrupção e timeout → **console** + botão restaurado
- [ ] FR-009: `CircularProgressIndicator` no slot do botão desde o toque até fim do fluxo (mesmo `Modifier.size`)
- [ ] FR-010: Guias vazias indicadas no console
- [ ] FR-012: Guias desconhecidas ignoradas sem log
- [ ] FR-013: Sem guias B3 → sucesso silencioso sem console
- [ ] FR-014 / FR-016: Sem Snackbar nem feedback de sucesso/erro na UI
- [ ] FR-015: Falha atómica em `MISSING_COLUMNS`
- [ ] SC-001 a SC-008: conforme cenários acima

---

## Android / iOS (bypass)

1. Executar app mobile (se aplicável)
2. Se o botão de import estiver visível, tocar — **nada** deve acontecer (sem diálogo, sem spinner, sem log)
3. Confirmar que export/sync continuam normais
