# Pesquisa — Ajuste de corretora e tabela de transações por tipo de asset

## Decisão 1: Reposicionamento da corretora

- **Decision**: Recolocar a corretora exclusivamente na primeira coluna do formulário e removê-la da seção de transações.
- **Rationale**: Mantém consistência com o fluxo esperado de preenchimento principal e evita duplicidade de campo.
- **Alternatives considered**:
  - Manter corretora na tabela: rejeitado por conflitar com requisito explícito.
  - Exibir corretora em ambas as áreas: rejeitado por risco de inconsistência de edição.

## Decisão 2: Modelo de criação de transação por linha em branco

- **Decision**: Remover formulário de transação e manter botão de adicionar que cria linha em branco editável na tabela.
- **Rationale**: Reduz fricção de navegação e concentra criação/edição no mesmo contexto da lista.
- **Alternatives considered**:
  - Formulário externo + tabela somente leitura: rejeitado por fluxo redundante.
  - Modal separado para criar linha: rejeitado por aumentar cliques e troca de contexto.

## Decisão 3: Regra para múltiplas linhas inválidas

- **Decision**: Se já houver linha inválida pendente, novo toque em adicionar não cria outra linha; foco permanece na inválida.
- **Rationale**: Evita acúmulo de inconsistências e orienta correção incremental.
- **Alternatives considered**:
  - Permitir múltiplas inválidas: rejeitado por degradar experiência de correção.
  - Bloquear botão permanentemente até reload: rejeitado por comportamento excessivamente rígido.

## Decisão 4: Colunas por tipo de asset

- **Decision**: Configurar colunas/campos da tabela dinamicamente pelo tipo de asset; ao trocar tipo, descartar dados incompatíveis e avisar para revisão.
- **Rationale**: Garante coerência semântica dos dados e evita persistência de valores sem validade no novo contexto.
- **Alternatives considered**:
  - Manter todos os campos sempre: rejeitado por ambiguidade funcional.
  - Bloquear troca de tipo com linhas preenchidas: rejeitado por reduzir flexibilidade do fluxo.

## Decisão 5: Escopo de edição inline e bloqueio de salvar

- **Decision**: Permitir edição inline para linhas novas e existentes; bloquear salvar final se qualquer linha inválida existir.
- **Rationale**: Uniformiza regras de validação na tabela e elimina persistência parcial inconsistente.
- **Alternatives considered**:
  - Validar apenas novas linhas: rejeitado por criar dupla regra de consistência.
  - Salvar parcialmente linhas válidas: rejeitado por dificuldade de rastreio e previsibilidade para o utilizador.

## Decisão 6: Reuso de componentes de input de tabela

- **Decision**: Reutilizar componentes como `TableInputMoney` do contexto de histórico e movê-los para `design-system` quando não estiverem em camada compartilhada adequada.
- **Rationale**: Mantém consistência de UX e reduz duplicação de implementação.
- **Alternatives considered**:
  - Criar novos inputs locais na feature: rejeitado por risco de divergência visual/comportamental.
  - Copiar componente sem migração: rejeitado por criar débito técnico de manutenção.
