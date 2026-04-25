# Especificação de feature: Ajuste de corretora e tabela de transações por tipo de asset

**Branch da feature**: `008-ajustar-tabela-transacoes`  
**Criada em**: 2026-04-25  
**Estado**: Rascunho  
**Entrada do utilizador**: "a corretora deve voltar para a primeira coluna. Os campos da tabela devem ser de acordo com o tipo de asset. Vamos remover o formulario de transações. Manteremos o botao de adicionar e ao tocar nele, cria-se uma nova linha em branco na tabela (essa linha representará uma nova transação). Ao salvar, deve-se validar se a linha adicionada é valida"

**Constitution (Investments-KMP):** Incluir, quando aplicável, critérios de qualidade, API/visibilidade, testabilidade, consistência de UX, desempenho e coerência entre código, documentação e artefatos de especificação.

**Idioma:** Português do Brasil (pt-BR).

## Clarificações

### Session 2026-04-25

- Q: Quando já existir uma nova linha em branco inválida, o que deve acontecer ao tocar novamente no botão adicionar? → A: Não criar nova linha e focar/indicar a linha inválida existente.
- Q: Ao mudar o tipo do asset com linhas novas já preenchidas, como tratar valores em colunas que deixam de existir? → A: Descartar os valores incompatíveis e avisar que precisam ser revisados.
- Q: No salvar final, se houver mistura de linhas válidas e inválidas, qual comportamento esperado? → A: Bloquear salvamento total até todas as linhas ficarem válidas.
- Q: Nesta feature, a edição inline deve valer para quais linhas da tabela? → A: Todas as linhas (novas e já existentes).
- Q: No salvar final, a regra de bloqueio por invalidação deve considerar quais linhas? → A: Todas as linhas inválidas (novas e existentes).

## Cenários de utilizador e testes *(obrigatório)*

### História de utilizador 1 — Reposicionar corretora no formulário principal (Prioridade: P1)

Como utilizador do diálogo de gestão de ativo, quero ver e editar a corretora novamente na primeira coluna para manter o fluxo principal de preenchimento no mesmo local esperado.

**Por que esta prioridade**: O reposicionamento restaura consistência de navegação do formulário e reduz erro de preenchimento.

**Teste independente**: Abrir o diálogo e validar que o campo corretora está na primeira coluna e não aparece no bloco de transações.

**Cenários de aceitação**:

1. **Dado** o diálogo aberto, **quando** o utilizador observa a primeira coluna, **então** o campo corretora está visível e editável nela.
2. **Dado** o diálogo aberto, **quando** o utilizador acessa a seção de transações, **então** o campo corretora não está presente nessa seção.

---

### História de utilizador 2 — Adicionar nova transação por linha em branco na tabela (Prioridade: P2)

Como utilizador, quero adicionar transações pelo botão de adicionar que insere uma linha em branco na tabela para preencher diretamente no formato da própria lista.

**Por que esta prioridade**: Remove redundância de formulário separado e torna o fluxo de cadastro de transação mais direto.

**Teste independente**: Tocar no botão adicionar e validar inserção imediata de linha em branco editável na tabela.

**Cenários de aceitação**:

1. **Dado** a tabela de transações carregada, **quando** o utilizador toca no botão adicionar, **então** uma nova linha em branco é criada e exibida na tabela.
2. **Dado** uma nova linha em branco criada, **quando** o utilizador edita seus campos, **então** os valores ficam armazenados em memória até o salvar final.

---

### História de utilizador 3 — Campos da tabela variam conforme tipo do asset e validação no salvar (Prioridade: P3)

Como utilizador, quero que a tabela apresente somente campos compatíveis com o tipo do asset e que o salvar valide se as novas linhas são válidas antes de persistir.

**Por que esta prioridade**: Evita preenchimento inválido e garante coerência entre tipo de ativo e estrutura da transação.

**Teste independente**: Trocar tipo de asset, validar colunas exibidas e tentar salvar com linha inválida para confirmar bloqueio com erro.

**Cenários de aceitação**:

1. **Dado** um tipo de asset selecionado, **quando** a tabela é exibida, **então** os campos/colunas refletem somente os dados aplicáveis para esse tipo.
2. **Dado** uma ou mais linhas novas inválidas, **quando** o utilizador tenta salvar, **então** o salvamento é bloqueado e as linhas inválidas são destacadas para correção.
3. **Dado** todas as linhas novas válidas, **quando** o utilizador salva, **então** as transações são persistidas com sucesso junto das demais alterações.

---

### Casos extremos (edge cases)

- O botão adicionar acionado repetidamente deve criar múltiplas linhas em branco sem sobrescrever linhas já preenchidas.
- Alteração do tipo de asset com linhas novas em edição deve descartar valores incompatíveis e avisar o utilizador para revisão.
- Linha nova sem campos obrigatórios no momento do salvar deve impedir persistência e manter os dados já digitados para correção.
- Em caso de falha de persistência no salvar final, os dados das linhas adicionadas devem permanecer em memória para nova tentativa.
- Se já houver linha nova inválida, novo toque em adicionar deve impedir criação de outra linha e destacar a linha pendente para correção.

## Requisitos *(obrigatório)*

### Requisitos funcionais

- **RF-001**: O sistema DEVE exibir e editar a corretora na primeira coluna do formulário principal.
- **RF-002**: O sistema NÃO DEVE exibir o campo corretora na seção de transações.
- **RF-003**: O sistema DEVE remover o formulário separado de criação de transações.
- **RF-004**: O sistema DEVE manter um botão de adicionar transação na seção da tabela.
- **RF-005**: O sistema DEVE criar uma nova linha em branco na tabela ao tocar no botão de adicionar.
- **RF-006**: O sistema DEVE permitir edição dos dados da nova linha diretamente na tabela.
- **RF-007**: O sistema DEVE manter em memória as linhas novas e alterações até o salvar final.
- **RF-008**: O sistema DEVE exibir colunas/campos da tabela conforme o tipo de asset selecionado.
- **RF-009**: O sistema DEVE validar linhas novas no momento do salvar final.
- **RF-010**: O sistema NÃO DEVE salvar quando houver linha nova inválida, devendo sinalizar os erros ao utilizador.
- **RF-011**: O sistema DEVE salvar as novas linhas quando todas forem válidas.
- **RF-012**: O sistema NÃO DEVE criar nova linha quando já existir linha nova inválida, devendo direcionar o utilizador para corrigir a linha pendente.
- **RF-013**: O sistema DEVE descartar valores de colunas incompatíveis ao trocar o tipo de asset e DEVE avisar o utilizador sobre a necessidade de revisão.
- **RF-014**: O sistema DEVE bloquear todo o salvamento final quando existir ao menos uma linha nova inválida.
- **RF-015**: O sistema DEVE permitir edição inline tanto para linhas novas quanto para linhas já existentes na tabela.
- **RF-016**: O sistema DEVE reutilizar os componentes de entrada em tabela já usados em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt` (ex.: `TableInputMoney`) para edição inline dos campos de transação.
- **RF-017**: O sistema DEVE manter esses componentes em uma camada compartilhada de UI; se o componente necessário não estiver no `design-system`, DEVE ser movido para lá antes do uso na feature.
- **RF-018**: O sistema DEVE bloquear o salvar final se qualquer linha inválida existir na tabela, independentemente de a linha ser nova ou já existente.

### Entidades principais *(incluir se a feature envolver dados)*

- **Asset em edição**: Ativo principal que define o tipo e as regras aplicáveis aos campos de transação.
- **Linha de transação em memória**: Registro temporário criado pelo botão adicionar e preenchido diretamente na tabela.
- **Conjunto de linhas pendentes**: Coleção de linhas novas/alteradas aguardando validação e persistência no salvar final.

## Critérios de sucesso *(obrigatório)*

### Resultados mensuráveis

- **CS-001**: Em 100% das aberturas do diálogo, o campo corretora aparece somente na primeira coluna.
- **CS-002**: Em pelo menos 95% das tentativas de adicionar transação, a nova linha em branco é exibida na tabela em até 1 segundo.
- **CS-003**: Em testes de aceitação, 100% das tentativas de salvar com qualquer linha inválida (nova ou existente) são bloqueadas integralmente com indicação clara de erro.
- **CS-004**: Em testes de aceitação, 100% das linhas válidas adicionadas na tabela são persistidas com sucesso no salvar final.
- **CS-005**: Em testes de consistência visual/funcional, os campos inline de transação reutilizam os mesmos padrões de entrada em tabela já adotados no histórico de ativos.

## Premissas

- O tipo de asset já está definido no fluxo atual e pode ser usado como referência para configurar colunas da tabela.
- O comportamento de salvar final do diálogo já existe e será reutilizado para validar e persistir linhas novas.
- Fora de escopo desta versão: exclusão de linhas de transação e mudanças nas regras de negócio do cálculo financeiro de cada tipo de asset.
- Os componentes de entrada em tabela existentes no histórico de ativos são considerados referência de experiência e comportamento para esta feature.
