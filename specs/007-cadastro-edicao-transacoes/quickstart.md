# Quickstart — Cadastro e edição de transações no diálogo de ativo

## Pré-requisitos

- Branch: `007-cadastro-edicao-transacao`
- Projeto sincronizado com Gradle.
- Base de dados local disponível conforme fluxo atual da aplicação.

## Passos de validação rápida

1. Abrir o diálogo de gestão de ativo com um holding existente.
2. Validar carregamento inicial:
   - Tabela exibe transações existentes ordenadas por data (mais recente primeiro).
3. Validar criação:
   - Criar uma nova transação com campos válidos.
   - Confirmar que a linha aparece imediatamente na tabela.
4. Validar edição inline:
   - Editar cada tipo de campo diretamente na célula.
   - Confirmar que edição válida é refletida na linha.
5. Validar erro de edição:
   - Informar valor inválido e finalizar edição.
   - Confirmar que valor anterior foi mantido e erro visível foi exibido.
6. Validar cancelamento:
   - Criar/editar transações e cancelar/fechar sem salvar.
   - Reabrir diálogo e confirmar que rascunho foi descartado.
7. Validar salvar final:
   - Repetir criação/edição e tocar em salvar.
   - Confirmar persistência no banco das alterações realizadas.
8. Validar escopo:
   - Confirmar ausência de ação de exclusão de transação.

## Verificação de build

Executar na raiz:

`./gradlew :features:asset-management:compileKotlinJvm`
