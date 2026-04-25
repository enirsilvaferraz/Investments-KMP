# Contrato de UI — Dialog de asset em duas colunas

## Objetivo

Definir o contrato funcional da interface de cadastro/edição de asset com duas colunas, sem alterar regras de negócio existentes.

## Entradas esperadas

- Estado atual do formulário de asset/holding.
- Lista de corretoras disponíveis.
- Lista de transações da holding atual (pode ser vazia).
- Estado de salvamento (`isSaving`) para habilitar/desabilitar ações.

## Estrutura obrigatória da UI

1. **Coluna esquerda (formulário)**
   - Contém os campos atualmente existentes.
   - Não contém o campo corretora.

2. **Separador entre colunas**
   - Divisão visual suave e contínua.

3. **Coluna direita (contexto de holding)**
   - Campo corretora no topo.
   - Tabela de transações abaixo do campo corretora.
   - Estado vazio com mensagem "Histórico disponível após salvar a holding" quando não houver dados.
   - Rolagem interna da tabela para listas longas.

4. **Ações do dialog**
   - Botões Cancelar e Salvar preservados.
   - Comportamento de habilitação segue estado já existente.

## Regras de aceitação

- O layout renderiza duas colunas em 100% das aberturas do dialog.
- O campo corretora não aparece na coluna esquerda.
- O campo corretora aparece antes da tabela na coluna direita.
- O estado vazio é textual e orientativo.
- A altura externa do dialog permanece estável com histórico extenso.
