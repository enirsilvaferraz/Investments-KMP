# Contrato de UI — Transações com criação e edição inline

## Objetivo

Definir o comportamento funcional da área de transações no diálogo de gestão de ativo, cobrindo criação, edição inline, staging em memória e persistência no salvar final.

## Entradas esperadas

- Holding em edição com identificador válido.
- Lista inicial de transações já carregadas para o holding.
- Estado de sessão para novas transações e alterações pendentes.
- Estado de salvamento do formulário principal (`isSaving`).

## Comportamentos obrigatórios

1. **Criação de transação**
   - Permite adicionar nova transação vinculada ao holding.
   - Após confirmação válida, a nova linha aparece imediatamente na tabela.
   - A transação criada permanece somente em memória até o salvar final.

2. **Edição inline na tabela**
   - Todos os campos visíveis da transação são editáveis inline.
   - Ao finalizar edição válida, o valor é aplicado na linha e marcado em memória.
   - Ao finalizar edição inválida, a alteração é rejeitada, o valor anterior é mantido e erro é exibido.

3. **Ordenação da lista**
   - A tabela exibe transações ordenadas por data da transação (mais recente primeiro).
   - A regra vale para itens carregados e itens criados/alterados na sessão.

4. **Salvar e cancelar**
   - Botão Salvar persiste em lote todas as alterações pendentes no banco.
   - Cancelar/fechar sem salvar descarta imediatamente o estado em memória.

5. **Escopo explícito**
   - Esta versão não inclui exclusão de transações.

## Regras de aceitação

- Nova transação válida aparece na tabela em até 1 segundo.
- Nenhuma alteração é persistida antes do salvar final.
- Tentativa de edição inline inválida não altera o valor armazenado.
- Após cancelar/fechar sem salvar, reabrir o diálogo não recupera o rascunho descartado.
- Ordenação por data permanece estável após criação e edição de linhas.
