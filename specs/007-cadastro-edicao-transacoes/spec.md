# Especificação de feature: Cadastro e edição de transações no diálogo de ativo

**Branch da feature**: `007-cadastro-edicao-transacao`  
**Criada em**: 2026-04-25  
**Estado**: Rascunho  
**Entrada do utilizador**: "Quero adicionar a capacidade de criar e editar transações dentro do AssetManagementFormView. As transações já são carregadas. Agora eu quero a opção de criar uma transação associada ao holding e salvá-la no banco. Devemos guardar as transações criadas em memória para salvar tudo no banco no final ao tocar em salvar. Devemos usar a forma de edição dentro da tabela para editar as transações; ao terminar a edição devemos salvar em memória para salvar ao tocar em salvar."

**Constitution (Investments-KMP):** Incluir, quando aplicável, critérios de qualidade, API/visibilidade, testabilidade, consistência de UX, desempenho e coerência entre código, documentação e artefatos de especificação.

**Idioma:** Português do Brasil (pt-BR).

## Clarificações

### Session 2026-04-25

- Q: Esta feature deve incluir exclusão de transações? → A: Não permitir exclusão nesta feature; apenas criar e editar.
- Q: Quando o utilizador fecha/cancela o diálogo sem salvar, como tratar as alterações em memória? → A: Descartar imediatamente todas as alterações não salvas.
- Q: Na edição inline da célula, como tratar valor inválido ao finalizar edição? → A: Rejeitar alteração inválida, manter valor anterior e exibir erro.
- Q: Quais campos devem ser editáveis inline nesta feature? → A: Todos os campos da transação.
- Q: Na tabela, como ordenar transações em memória versus transações já carregadas? → A: Ordenar por data da transação (mais recente primeiro).

## Cenários de utilizador e testes *(obrigatório)*

### História de utilizador 1 — Criar transação associada ao holding (Prioridade: P1)

Como utilizador que está editando um ativo, quero incluir uma nova transação vinculada ao holding atual para registrar movimentações que ainda não existem na lista carregada.

**Por que esta prioridade**: Sem criação de nova transação, o fluxo de manutenção fica incompleto e impede o registro de operações recentes.

**Teste independente**: Abrir o diálogo de gestão de ativo, adicionar uma transação nova e confirmar que ela aparece na tabela como pendente de persistência até o salvamento final.

**Cenários de aceitação**:

1. **Dado** que o diálogo está aberto com um holding válido, **quando** o utilizador adiciona uma nova transação preenchendo os campos obrigatórios, **então** a transação é exibida na tabela e mantida em memória.
2. **Dado** que a transação nova está em memória, **quando** o utilizador toca em salvar no formulário principal, **então** a transação é persistida junto com as demais alterações do ativo.

---

### História de utilizador 2 — Editar transação na própria tabela (Prioridade: P2)

Como utilizador, quero editar valores da transação diretamente na célula da tabela para ajustar rapidamente informações sem sair do contexto da lista.

**Por que esta prioridade**: A edição inline reduz fricção no fluxo e torna o ajuste de múltiplas transações mais eficiente.

**Teste independente**: Editar um campo de uma transação já exibida na tabela e concluir a edição; validar que o novo valor fica refletido na linha e guardado em memória.

**Cenários de aceitação**:

1. **Dado** uma transação visível na tabela, **quando** o utilizador altera um campo editável na célula e finaliza a edição, **então** o novo valor é aplicado na tabela e armazenado em memória para persistência futura.
2. **Dado** múltiplas edições realizadas em linhas diferentes, **quando** o utilizador ainda não salvou o formulário principal, **então** todas as alterações permanecem disponíveis no estado em memória.

---

### História de utilizador 3 — Persistir alterações apenas no salvar final (Prioridade: P3)

Como utilizador, quero que criação e edição de transações só sejam gravadas no banco ao salvar o formulário principal para ter controle do lote final de mudanças.

**Por que esta prioridade**: O comportamento transacional do fluxo evita gravações parciais e mantém previsibilidade para o utilizador.

**Teste independente**: Criar e editar transações, fechar sem salvar e validar que nenhuma alteração foi persistida; repetir com salvar e validar persistência.

**Cenários de aceitação**:

1. **Dado** que há transações criadas e editadas em memória, **quando** o utilizador cancela ou sai sem salvar, **então** nenhuma dessas alterações é persistida no banco.
2. **Dado** que há alterações pendentes em memória, **quando** o utilizador confirma salvar, **então** todas as alterações válidas são persistidas em uma única confirmação do fluxo.

---

### Casos extremos (edge cases)

- Inclusão de transação com campos obrigatórios ausentes deve impedir a confirmação da inclusão e orientar o utilizador a corrigir os dados.
- Edição inline com valor inválido deve preservar o valor anterior, impedir confirmação da alteração inválida e exibir feedback de erro.
- Se houver falha ao salvar no banco no momento final, as alterações em memória devem permanecer disponíveis para nova tentativa.
- Conflito entre dados carregados originalmente e alterações em memória deve priorizar o estado mais recente confirmado pelo utilizador no diálogo.

## Requisitos *(obrigatório)*

### Requisitos funcionais

- **RF-001**: O sistema DEVE permitir adicionar nova transação associada ao holding em edição dentro do diálogo de gestão do ativo.
- **RF-002**: O sistema DEVE exibir imediatamente na tabela toda transação criada durante a sessão do diálogo.
- **RF-003**: O sistema DEVE permitir editar campos de transações diretamente nas células editáveis da tabela.
- **RF-004**: O sistema DEVE aplicar e manter em memória cada edição confirmada pelo utilizador na tabela.
- **RF-005**: O sistema DEVE manter em memória, até o salvamento final, todas as transações criadas e alteradas durante a sessão.
- **RF-006**: O sistema DEVE persistir no banco, ao salvar o formulário principal, o conjunto completo de transações criadas e editadas.
- **RF-007**: O sistema NÃO DEVE persistir alterações de transação quando o utilizador encerrar o diálogo sem salvar.
- **RF-008**: O sistema DEVE informar o utilizador quando existir erro de persistência no salvamento final e manter os dados pendentes para nova tentativa.
- **RF-009**: O sistema NÃO DEVE oferecer ação de exclusão de transações nesta feature.
- **RF-010**: O sistema DEVE descartar imediatamente as alterações em memória quando o utilizador encerrar ou cancelar o diálogo sem salvar.
- **RF-011**: O sistema DEVE rejeitar edição inline inválida, preservar o valor anterior da célula e informar o utilizador sobre o erro de validação.
- **RF-012**: O sistema DEVE permitir edição inline em todos os campos da transação exibidos na tabela.
- **RF-013**: O sistema DEVE ordenar a tabela de transações por data da transação, exibindo primeiro as mais recentes, independentemente de terem sido carregadas inicialmente ou alteradas na sessão.

### Entidades principais *(incluir se a feature envolver dados)*

- **Holding**: Agregado principal do ativo em edição, ao qual as transações pertencem.
- **Transação**: Registro de movimentação financeira associado ao holding, com campos editáveis apresentados na tabela.
- **Sessão de edição de transações**: Conjunto temporário em memória contendo inclusões e alterações realizadas até a confirmação de salvar.

## Critérios de sucesso *(obrigatório)*

### Resultados mensuráveis

- **CS-001**: 100% das transações criadas durante a edição aparecem na tabela em até 1 segundo após confirmação da inclusão.
- **CS-002**: Pelo menos 95% das edições inline confirmadas refletem o novo valor na tabela sem necessidade de nova ação do utilizador.
- **CS-003**: Em testes de aceitação do fluxo principal, 100% das alterações de transações só são persistidas após o utilizador executar o salvar final.
- **CS-004**: Em cenário de erro de salvamento final, 100% das alterações pendentes permanecem disponíveis para nova tentativa dentro da mesma sessão.

## Premissas

- O utilizador já possui permissão para editar o ativo e suas transações no diálogo atual.
- As transações existentes continuam sendo carregadas no início da sessão de edição, conforme comportamento atual.
- Fora de escopo desta versão: exclusão de transações, criação de novos tipos de transação, mudanças em regras de cálculo de negócio e edição em lote fora da tabela do diálogo.
- O salvamento final do formulário principal já existe e será estendido para incluir o lote de transações pendentes.
- Encerrar/cancelar sem salvar sempre invalida o rascunho em memória da sessão.
