# Contrato de UI — Alinhamento da tabela de transações por tipo de asset

## Objetivo

Definir o comportamento funcional do diálogo de gestão de ativo para:
- corretora na primeira coluna,
- criação de transação por linha em branco na tabela,
- edição inline para linhas novas e existentes,
- validação global no salvar final.

## Entradas esperadas

- Tipo de asset selecionado no formulário.
- Corretora selecionada no formulário principal.
- Linhas existentes de transação carregadas.
- Estado da sessão de edição em memória.

## Comportamentos obrigatórios

1. **Corretora**
   - Exibida apenas na primeira coluna.
   - Não aparece na seção da tabela de transações.

2. **Adição de linha**
   - Botão adicionar cria nova linha em branco na tabela.
   - Se houver linha inválida pendente, não cria nova linha e destaca a inválida.

3. **Edição inline**
   - Válida para linhas novas e existentes.
   - Usa componentes de input de tabela já consolidados (ex.: `TableInputMoney`).
   - Se necessário, componente deve ser migrado para `design-system`.

4. **Colunas por tipo**
   - Colunas variam conforme tipo do asset.
   - Ao trocar tipo, campos incompatíveis são descartados e utilizador recebe aviso de revisão.

5. **Salvar final**
   - Bloqueado se qualquer linha inválida existir (nova ou existente).
   - Persistência em lote somente quando todas as linhas estiverem válidas.

## Regras de aceitação

- Corretora aparece somente na coluna principal em 100% dos fluxos.
- Nova linha em branco aparece em até 1 segundo quando permitido.
- Edição inline mantém consistência visual com inputs de tabela já usados no histórico de ativos.
- Salvar é bloqueado sempre que existir qualquer linha inválida.
- Troca de tipo remove campos incompatíveis e sinaliza revisão ao utilizador.
