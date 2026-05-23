# Quickstart: Importação de Dados da B3

**Feature**: `003-b3-import` | **Date**: 2026-05-23

---

## Pré-requisitos

- JDK 17+ instalado (necessário para Apache POI e javax.swing)
- Arquivo XLSX exportado da B3 disponível em disco

---

## Verificação de Build (após implementação)

```bash
# 1. Verificar módulo de entidades
./gradlew :domain:entity:compileKotlinJvm

# 2. Verificar módulo de usecases
./gradlew :domain:usecases:compileKotlinJvm

# 3. Verificar módulo filestore (inclui implementação JVM com Apache POI)
./gradlew :data:filestore:compileKotlinJvm

# 4. Verificar feature de apresentação
./gradlew :features:composeApp:compileKotlinJvm

# 5. Rodar testes dos UseCases
./gradlew :domain:usecases:jvmTest
```

---

## Testar o Fluxo no Desktop

1. Executar a aplicação Desktop: `./gradlew :apps:desktopApp:run`
2. Navegar até a tela **Posicionamento no Período** (AssetHistoryScreen)
3. Localizar o botão de importação à **esquerda** do botão de exportação (ícone `FileUpload`)
4. Clicar no botão — o diálogo nativo do SO deve abrir filtrando por `.xlsx`
5. Selecionar o arquivo exportado da B3 (ex.: `posicao-2026-05-23-12-53-16.xlsx`)
6. O botão deve ser substituído por um spinner enquanto o arquivo é processado
7. No console da IDE (ou no terminal onde a app foi iniciada), verificar a saída:

```
=== Guia: Acoes (21 linhas) ===
Cabeçalhos: [Produto, Instituição, Conta, Código de Negociação, ...]
Linha 1: [AXIA6 - AXIA ENERGIA S.A., NU INVESTIMENTOS S.A. - CTVM, 255869, AXIA6, ...]
...

=== Guia: ETF (4 linhas) ===
...

=== Guia: Fundo de Investimento (28 linhas) ===
...

=== Guia: Renda Fixa (26 linhas) ===
...

=== Guia: Tesouro Direto (4 linhas) ===
...
```

---

## Cenários de Teste Manual

| Cenário | Ação | Resultado esperado |
|---------|------|-------------------|
| Arquivo válido B3 | Selecionar `posicao-*.xlsx` | Todas as 5 guias no console; botão restaurado |
| Cancelamento | Fechar o diálogo sem selecionar | Nada acontece; botão restaurado silenciosamente |
| Arquivo não-xlsx | Selecionar `.csv` ou `.pdf` | Mensagem de rejeição; botão restaurado |
| Arquivo corrompido | Selecionar `.xlsx` com conteúdo inválido | Mensagem de erro; botão restaurado |
| Timeout | (simular com arquivo artificial muito grande) | Erro de timeout após 30 s; botão restaurado |

---

## Checklist de Aceitação (mapeado ao spec.md)

- [ ] FR-001: Botão de importação visível à esquerda do botão de exportação
- [ ] FR-002: Diálogo nativo abre ao tocar no botão
- [ ] FR-003: Diálogo filtra por `.xlsx`; arquivos inválidos rejeitados
- [ ] FR-004: Todas as guias do arquivo são identificadas
- [ ] FR-005: Linhas e colunas de cada guia são lidas
- [ ] FR-006: Conteúdo exibido no console com nome da guia e dados
- [ ] FR-007: Cancelamento sem erros
- [ ] FR-009: Spinner substitui botão durante processamento; botão restaurado ao concluir
- [ ] FR-010: Guias vazias exibidas no console com indicação "sem dados"
- [ ] FR-011: Timeout de 30 s com mensagem de erro e restauração do botão
- [ ] FR-008: Arquivo corrompido exibe mensagem de erro; app não trava
