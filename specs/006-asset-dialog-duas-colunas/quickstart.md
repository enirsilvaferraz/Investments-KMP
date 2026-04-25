# Quickstart — Dialog de asset em duas colunas

## Pré-requisitos

- Branch: `006-asset-dialog-duas-colunas`
- Projeto sincronizado com Gradle.

## Passos de validação rápida

1. Abrir o fluxo de cadastro/edição de asset.
2. Confirmar que o dialog exibe duas colunas com separador visual suave.
3. Validar coluna esquerda:
   - Mantém campos atuais do formulário.
   - Não exibe o campo corretora.
4. Validar coluna direita:
   - Exibe corretora no topo.
   - Exibe tabela de transações abaixo.
5. Validar estado vazio:
   - Quando sem transações, mostrar "Histórico disponível após salvar a holding".
6. Validar lista extensa:
   - Tabela rola internamente sem aumentar altura externa do dialog.
7. Validar ações:
   - Cancelar e Salvar permanecem funcionais conforme estado de salvamento.

## Verificação de build

Executar na raiz:

`./gradlew :features:asset-management:compileKotlinJvm`

## Resultado da implementação (2026-04-25)

- Build do módulo `:features:asset-management` concluído com sucesso.
- Restam validações visuais manuais do dialog em execução da aplicação (passos 1 a 7).
