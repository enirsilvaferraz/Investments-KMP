# Quickstart — Ajuste de corretora e tabela de transações por tipo de asset

## Pré-requisitos

- Branch: `008-ajustar-tabela-transacoes`
- Projeto sincronizado com Gradle.
- Fluxo de gestão de ativo acessível na aplicação.

## Passos de validação rápida

1. Abrir diálogo de gestão de ativo.
2. Validar corretora:
   - Campo aparece na primeira coluna.
   - Campo não aparece na seção de transações.
3. Validar adição por linha:
   - Tocar em adicionar e confirmar nova linha em branco na tabela.
4. Validar bloqueio de múltiplas inválidas:
   - Deixar uma linha inválida e tocar em adicionar novamente.
   - Confirmar que não cria nova linha e destaca pendente.
5. Validar colunas por tipo:
   - Trocar tipo de asset.
   - Confirmar ajuste de colunas e aviso de revisão de dados incompatíveis.
6. Validar edição inline:
   - Editar linhas novas e existentes com os inputs de tabela compartilhados (`TableInputDate`, `TableInputMoney`, `TableInputSelect`, `TableInputText`).
7. Validar salvar:
   - Com qualquer linha inválida (nova ou existente), salvar deve bloquear.
   - Com todas válidas, salvar deve persistir em lote.

## Verificação de build

Executar na raiz:

`./gradlew :features:asset-management:compileKotlinJvm`
