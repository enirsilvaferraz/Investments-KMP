# Pesquisa — Dialog de asset em duas colunas

## Decisão 1: Estrutura de layout em duas colunas

- **Decision**: Usar container horizontal com duas colunas de largura equilibrada e separador visual suave entre elas.
- **Rationale**: Atende ao requisito funcional principal (formulário + histórico simultâneos) e reduz salto de contexto para edição.
- **Alternatives considered**:
  - Manter layout único em grade: rejeitado por não exibir histórico de forma estruturada.
  - Abrir histórico em modal secundário: rejeitado por aumentar passos da tarefa.

## Decisão 2: Posição do campo corretora

- **Decision**: Mover o campo corretora para o topo da coluna direita, antes da tabela de transações.
- **Rationale**: Agrupa metadado de holding junto ao histórico, coerente com a leitura contextual do usuário.
- **Alternatives considered**:
  - Manter corretora na coluna esquerda: rejeitado por contrariar requisito explícito.
  - Colocar corretora no rodapé da coluna direita: rejeitado por baixa descoberta.

## Decisão 3: Comportamento para histórico vazio

- **Decision**: Exibir estado vazio com mensagem explícita: "Histórico disponível após salvar a holding".
- **Rationale**: Evita ambiguidade em cadastro novo e orienta próximo passo sem bloquear o fluxo.
- **Alternatives considered**:
  - Tabela vazia sem mensagem: rejeitado por baixa clareza.
  - Ocultar tabela até salvar: rejeitado por quebra de consistência do layout em duas colunas.

## Decisão 4: Escalabilidade visual para histórico extenso

- **Decision**: Aplicar rolagem interna na tabela da coluna direita, mantendo altura do dialog estável.
- **Rationale**: Preserva ações primárias (Salvar/Cancelar) sempre previsíveis e evita crescimento abrupto do dialog.
- **Alternatives considered**:
  - Expandir dialog até limite da tela: rejeitado por variabilidade de comportamento.
  - Limitar linhas e exigir navegação externa: rejeitado por aumentar fricção do fluxo.

## Decisão 5: Estratégia de validação

- **Decision**: Validar por cenários funcionais da spec + compilação do módulo `:features:asset-management`.
- **Rationale**: Mudança é predominantemente de UI, com critérios objetivos de posicionamento e comportamento.
- **Alternatives considered**:
  - Cobertura apenas manual ad hoc: rejeitado por risco de regressão não rastreável.
