# Feature Specification: Redesenho do Dialog de Transações com Lista em Draft

**Feature Branch**: `002-transaction-form-redesign`

**Created**: 2026-05-19

**Status**: Draft

**Input**: User description: "Atualizar TransactionFormDialog (TransactionManagementView.kt) trocando o layout do TransactionTable pelo layout prototipado em TransactionManagementScreen.kt (NewTransactionsTable). Manter a regra que diferencia campos por categoria. Não usar UiTableV3. Transações buscadas por holding, ordenadas por data (ordenação apenas ao buscar do banco). Botões Adicionar e Salvar abaixo da tabela. Botão X para excluir em cada linha. Save habilitado apenas se a lista exibida diferir da lista inicial (registros e conteúdo). Sistema de draft: só persiste no Save. Todos os campos editáveis. Replicar o padrão de dialog do AssetManagementScreen. Implementação simples."

## Clarifications

### Session 2026-05-19

- Q: O que acontece quando o Salvar falha (parcial ou total)? → A: Em qualquer falha, o dialog permanece aberto, sai do estado de "salvando" e mantém o rascunho atual para o utilizador tentar de novo; sem mensagem de erro dedicada nesta feature.
- Q: Como é feito o pareamento ao comparar "lista exibida" com "lista inicial" para habilitar o Salvar? → A: Por posição — duas listas são iguais sse têm o mesmo número de linhas e, para cada índice i, os campos comparáveis (data, tipo, quantidade, valor unitário, valor total — aplicáveis à categoria) coincidem exatamente.
- Q: Como o campo `observations` deve ser tratado no dialog redesenhado? → A: Não visível, não editável e não conta para a comparação de diff. Para transações existentes, o valor original é preservado intacto no round-trip. Para novas transações criadas via "Adicionar", `observations` fica vazio/nulo.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Visualizar e editar transações existentes de uma holding (Priority: P1)

O usuário abre o dialog de transações para uma holding específica (a partir de outro ponto da aplicação que invoca `TransactionFormDialog`). O dialog carrega todas as transações associadas à holding, exibidas como linhas em uma tabela ordenadas por data (mais antiga primeiro), com campos editáveis em cada coluna. O conjunto de colunas exibido depende da categoria do ativo da holding: para renda variável, são mostradas as colunas Data, Tipo de Transação, Quantidade, Valor Unitário, Valor Total e a ação de remover; para renda fixa e fundos, são mostradas Data, Tipo de Transação, Valor Total e a ação de remover. Os dados são apresentados em um layout de linhas (Row/Column) sem usar componentes de tabela complexos.

**Why this priority**: Fluxo principal — sem visualização e edição inline, o dialog não cumpre sua função de gerenciar as transações da holding.

**Independent Test**: Abrir o dialog para uma holding com transações já cadastradas e validar que (a) todas as transações daquela holding aparecem, (b) estão ordenadas por data crescente, (c) as colunas exibidas correspondem à categoria do ativo, (d) cada campo é editável, (e) o layout usado é baseado em linhas e não no componente de tabela legado.

**Acceptance Scenarios**:

1. **Given** existem transações cadastradas para a holding selecionada, **When** o dialog é aberto, **Then** todas as transações da holding são exibidas como linhas na tabela, ordenadas por data em ordem crescente.
2. **Given** a holding pertence a um ativo de renda variável, **When** o dialog é aberto, **Then** são exibidas as colunas Data, Transação, Quantidade, Valor Unitário, Valor Total e ação de remover.
3. **Given** a holding pertence a um ativo de renda fixa ou fundo de investimento, **When** o dialog é aberto, **Then** são exibidas as colunas Data, Transação, Valor Total e ação de remover (sem Quantidade e Valor Unitário).
4. **Given** o dialog está aberto com transações carregadas, **When** o usuário altera qualquer valor de qualquer campo de qualquer linha, **Then** a alteração é refletida na lista exibida em memória, sem persistência imediata.
5. **Given** o usuário alterou ordem/conteúdo das linhas durante a edição (ex.: mudar uma data para um valor anterior), **When** a tela continua aberta, **Then** a ordem das linhas exibidas NÃO é recalculada automaticamente — a ordenação ocorre exclusivamente no momento do carregamento inicial a partir do banco.

---

### User Story 2 - Adicionar uma nova transação como rascunho (Priority: P1)

Abaixo da tabela existe sempre um botão "Adicionar" (ou equivalente). Ao clicar, uma nova linha em branco aparece ao final da tabela, pronta para preenchimento. Os campos seguem as regras da categoria do ativo da holding. A nova transação é parte do rascunho e só é persistida quando o usuário clica em "Salvar".

**Why this priority**: Adicionar transações é parte essencial do gerenciamento; sem ela, o dialog é somente leitura.

**Independent Test**: Abrir o dialog, clicar em "Adicionar", verificar que uma nova linha aparece, preencher os dados, fechar o dialog sem salvar e reabrir — a transação adicionada não deve aparecer (não foi persistida).

**Acceptance Scenarios**:

1. **Given** o dialog está aberto, **When** o usuário clica no botão "Adicionar" abaixo da tabela, **Then** uma nova linha é adicionada ao final da lista com a data preenchida com a data atual e campos editáveis.
2. **Given** o usuário adicionou uma nova linha e preencheu os campos, **When** o dialog é fechado sem clicar em "Salvar", **Then** ao reabrir o dialog para a mesma holding, a nova linha não aparece (nada foi persistido).
3. **Given** o usuário adicionou e preencheu uma nova linha, **When** o usuário clica em "Salvar", **Then** a nova transação é persistida no banco vinculada à holding.

---

### User Story 3 - Excluir uma transação como rascunho (Priority: P1)

Cada linha exibe um botão "X" para remoção. Ao clicar, a linha é removida da lista exibida na tela, mas a exclusão no banco só ocorre se o usuário clicar em "Salvar". Se o usuário fechar o dialog sem salvar, a transação permanece no banco.

**Why this priority**: Necessário para correções; precisa ser consistente com o sistema de draft (não pode excluir imediatamente do banco).

**Independent Test**: Abrir o dialog, remover uma transação existente clicando no X, fechar sem salvar, reabrir o dialog — a transação removida deve continuar presente. Repetir o teste salvando — a transação removida não deve aparecer.

**Acceptance Scenarios**:

1. **Given** uma transação existente está visível na tabela, **When** o usuário clica no botão X dessa linha, **Then** a linha é removida da lista exibida, mas nenhuma alteração é persistida no banco.
2. **Given** o usuário removeu transações da lista, **When** o usuário fecha o dialog sem clicar em "Salvar", **Then** ao reabrir o dialog as transações removidas voltam a aparecer (foram restauradas do banco).
3. **Given** o usuário removeu transações da lista, **When** o usuário clica em "Salvar", **Then** as transações removidas são apagadas do banco e não aparecem ao reabrir o dialog.

---

### User Story 4 - Botão Salvar habilitado apenas quando a lista difere da original (Priority: P1)

Abaixo da tabela existe sempre um botão "Salvar". O botão fica desabilitado quando a lista exibida na tela é idêntica à lista carregada inicialmente do banco (mesmo número de registros e mesmos valores em cada campo de cada registro). Qualquer divergência — adição, remoção, ou edição de qualquer campo — habilita o botão. Ao clicar em "Salvar", todas as alterações da lista (adições, edições e remoções) são persistidas em conjunto e o dialog fecha.

**Why this priority**: Evita que o usuário acione persistência desnecessária e dá feedback visual claro de que há alterações pendentes.

**Independent Test**: Abrir o dialog para uma holding com transações; sem fazer alterações o botão "Salvar" está desabilitado; ao alterar um campo, adicionar ou remover uma linha, o botão "Salvar" fica habilitado; reverter manualmente todas as alterações para os valores iniciais e o botão volta a ficar desabilitado.

**Acceptance Scenarios**:

1. **Given** o dialog acabou de carregar e nenhuma alteração foi feita, **When** o usuário olha o botão "Salvar", **Then** o botão está desabilitado.
2. **Given** o usuário alterou o valor de um campo em qualquer linha, **When** a alteração é refletida na tela, **Then** o botão "Salvar" fica habilitado.
3. **Given** o usuário adicionou uma nova linha (mesmo em branco), **When** a linha está visível, **Then** o botão "Salvar" fica habilitado.
4. **Given** o usuário removeu uma linha existente, **When** a remoção é refletida na tela, **Then** o botão "Salvar" fica habilitado.
5. **Given** o usuário fez alterações e depois reverteu manualmente todos os campos para os valores originais (incluindo reinserir linhas removidas e remover linhas adicionadas), **When** a lista exibida coincide com a inicial em número de registros e conteúdo dos campos comparáveis, **Then** o botão "Salvar" volta a ficar desabilitado.
6. **Given** o botão "Salvar" está habilitado, **When** o usuário clica em "Salvar", **Then** todas as alterações da lista são persistidas no banco e o dialog é fechado.

---

### User Story 5 - Padrão visual e estrutural alinhado ao dialog de Asset Management (Priority: P2)

O dialog de transações segue o mesmo padrão visual e estrutural do dialog `AssetManagementDialog` (`AssetManagementScreen.kt`): cabeçalho com título e fechar, corpo com o formulário (no caso, a lista de transações) e área de ações ao final com o botão "Salvar" alinhado ao mesmo padrão visual.

**Why this priority**: Consistência de experiência em todo o app.

**Independent Test**: Abrir lado a lado o dialog de Asset Management e o dialog de Transações e validar que ambos compartilham o mesmo enquadramento (mesmo container de dialog, mesmo posicionamento do título, mesmo padrão de área de ações com botão Salvar).

**Acceptance Scenarios**:

1. **Given** o dialog de transações está aberto, **When** comparado ao `AssetManagementDialog`, **Then** o container de dialog, o título e a região de ações (com botão Salvar) seguem o mesmo padrão estrutural.
2. **Given** o dialog de transações está aberto, **When** o usuário clica em fechar (X) no topo do dialog, **Then** o dialog é fechado e quaisquer alterações em rascunho são descartadas sem persistência.

---

### Edge Cases

- **Holding sem transações cadastradas**: O dialog exibe apenas o cabeçalho da tabela (sem linhas), o botão "Adicionar" e o botão "Salvar" desabilitado. Adicionar e salvar transações é o fluxo natural.
- **Campos com valores inválidos no momento de Salvar**: O comportamento segue o mesmo do dialog atual — linhas inválidas exibem indicação de erro nos campos correspondentes; o processo de salvamento ignora linhas que não podem ser convertidas em transação válida.
- **Falha de persistência ao Salvar (parcial ou total)**: O dialog permanece aberto, sai do estado de "salvando" e mantém o rascunho atual exatamente como estava antes de tentar salvar, permitindo nova tentativa pelo utilizador. Não há mensagem de erro dedicada nesta feature.
- **Usuário fecha o dialog (botão X do topo) com alterações pendentes**: Todas as alterações em draft são descartadas sem confirmação adicional, replicando o comportamento do `AssetManagementDialog`.
- **Edição muda a data de uma linha para uma data anterior a outras**: A ordem visual na tela não é recalculada durante a edição; a reordenação só ocorre na próxima carga a partir do banco.
- **Usuário adiciona uma linha e remove a mesma linha sem salvar**: A lista volta ao estado inicial e o botão "Salvar" fica desabilitado.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O dialog DEVE exibir as transações da holding identificada na invocação, carregando-as do banco ao abrir.
- **FR-002**: O dialog DEVE ordenar a lista de transações por data em ordem crescente APENAS no momento do carregamento inicial a partir do banco. Edições subsequentes (inclusive de data) NÃO devem reordenar visualmente as linhas.
- **FR-003**: O dialog DEVE renderizar a tabela usando um layout baseado em linhas/colunas equivalente ao prototipado em `NewTransactionsTable` (`TransactionManagementScreen.kt`), SEM utilizar o componente `UiTableV3` (`design-system/.../UiTableV3.kt`).
- **FR-004**: O conjunto de colunas exibido DEVE depender da categoria do ativo da holding:
  - Renda variável: Data, Transação (tipo), Quantidade, Valor Unitário, Valor Total, ação de remover.
  - Renda fixa e fundos de investimento: Data, Transação (tipo), Valor Total, ação de remover.
- **FR-005**: Todos os campos exibidos em cada linha DEVEM ser editáveis pelo usuário.
- **FR-005a**: O campo `observations` da transação NÃO DEVE ser exibido nem editável no dialog. Para transações existentes carregadas do banco, o valor original DEVE ser preservado intacto no round-trip (não pode ser apagado ao salvar). Para novas transações criadas via "Adicionar", `observations` DEVE ficar vazio/nulo. `observations` NÃO DEVE participar da comparação de diff descrita em FR-010.
- **FR-006**: O dialog DEVE exibir SEMPRE, abaixo da tabela, um botão "Adicionar" que insere uma nova linha em branco ao final da lista, com a data preenchida com a data atual e demais campos prontos para edição.
- **FR-007**: O dialog DEVE exibir SEMPRE, abaixo da tabela, um botão "Salvar" que, ao ser acionado, persiste todas as alterações (adições, edições e remoções) da lista exibida e fecha o dialog após sucesso.
- **FR-008**: Cada linha da tabela DEVE exibir um botão "X" que remove a linha da lista exibida em rascunho. A remoção NÃO deve persistir alteração no banco até que o usuário clique em "Salvar".
- **FR-009**: O sistema DEVE manter as alterações (adições, edições, remoções) exclusivamente em memória (rascunho) até o usuário acionar "Salvar". Fechar o dialog sem salvar DEVE descartar todas as alterações.
- **FR-010**: O botão "Salvar" DEVE estar habilitado SOMENTE quando a lista exibida diferir da lista carregada inicialmente do banco. A comparação DEVE usar pareamento **por posição** e DEVE considerar:
  - O número de registros (listas com tamanhos diferentes contam como diferentes), e
  - Para cada índice `i`, a igualdade exata dos campos editáveis comparáveis aplicáveis à categoria (data, tipo, quantidade, valor unitário, valor total). Diferença em qualquer campo, em qualquer posição, marca a lista como diferente.
- **FR-011**: Quando a lista exibida volta a ser igual à inicial (por reversão manual das alterações), o botão "Salvar" DEVE voltar ao estado desabilitado.
- **FR-012**: O dialog DEVE seguir o mesmo padrão estrutural e visual do `AssetManagementDialog`: container de dialog, cabeçalho com título e botão de fechar (X) no topo, corpo com a tabela, e área de ações ao final com o botão "Salvar".
- **FR-013**: O botão de fechar (X) no topo do dialog DEVE encerrar o dialog imediatamente, descartando todas as alterações em rascunho sem confirmação adicional.
- **FR-014**: Após salvamento bem-sucedido, o dialog DEVE fechar automaticamente.
- **FR-015**: Em caso de falha de persistência (parcial ou total) ao acionar "Salvar", o dialog DEVE permanecer aberto, sair do estado de "salvando" e manter o rascunho atual inalterado para nova tentativa pelo utilizador. Esta feature NÃO introduz mensagem de erro dedicada para falhas de persistência.
- **FR-016**: A feature DEVE ser implementada de forma simples, sem introduzir comportamentos não declarados nestes requisitos.

### Key Entities

- **TransactionDraft (rascunho de transação)**: Representação editável de uma transação em memória; contém os campos editáveis (data, tipo, quantidade, valor unitário, valor total — conforme aplicáveis à categoria), o valor original de `observations` (preservado mas não editável nem visível), e a categoria herdada da holding. Marcada como existente (vinda do banco) ou nova (criada pelo usuário).
- **InitialTransactionSnapshot (snapshot inicial)**: Cópia imutável da lista de transações carregada do banco na abertura do dialog, usada para detectar diferenças (habilitar/desabilitar Salvar).
- **AssetHolding (holding do ativo)**: Identifica a holding alvo do dialog; sua categoria de ativo determina o conjunto de colunas exibidas.
- **AssetTransaction (transação persistida)**: Entidade persistida em banco vinculada à holding; tem variantes por categoria (renda fixa, renda variável, fundos).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das holdings com transações cadastradas conseguem ser abertas no dialog redesenhado exibindo todas as transações ordenadas por data crescente na carga inicial.
- **SC-002**: O botão "Salvar" permanece desabilitado em 100% dos casos em que o usuário não tenha realizado nenhuma alteração na lista após a carga.
- **SC-003**: O botão "Salvar" fica habilitado em 100% dos casos em que ao menos um dos seguintes ocorre: alteração de campo, adição de linha ou remoção de linha.
- **SC-004**: Em 100% dos casos em que o usuário fecha o dialog (via X) sem clicar em "Salvar", o estado persistido em banco para aquela holding permanece idêntico ao estado antes de abrir o dialog.
- **SC-005**: Em 100% dos casos em que o usuário clica em "Salvar" com sucesso, o dialog é fechado em até 2 segundos após a persistência das alterações.
- **SC-006**: 0 (zero) ocorrências do componente `UiTableV3` na implementação do dialog de transações após a entrega.
- **SC-007**: A categoria do ativo determina corretamente o conjunto de colunas em 100% dos casos de teste (renda variável vs. renda fixa/fundo).

## Assumptions

- O `TransactionFormDialog` continuará sendo invocado pelos mesmos pontos atuais da aplicação, recebendo SEMPRE o identificador de uma holding válida e existente. Cenários de holding nula ou inválida estão fora do escopo desta feature.
- Os UseCases de buscar transações por holding, salvar transação e remover transação já existem e podem ser reutilizados; apenas o orquestramento muda (não persistir até o Save).
- O padrão de dialog `AppContentDialog` usado por `AssetManagementDialog` será o mesmo container utilizado pelo novo `TransactionFormDialog`.
- A ordem de inserção de novas linhas é "ao final da lista exibida", sem reordenação automática por data durante a edição.
- A comparação de "lista igual" é feita por **posição** (índice a índice), sobre apenas os campos editáveis exibidos (data, tipo, quantidade, valor unitário, valor total) conforme aplicáveis à categoria do ativo da holding, e a quantidade de registros. Não há suporte a reordenação manual de linhas.
- A feature mantém as validações de campos existentes (sinalização de erro por campo) sem introduzir novas regras de bloqueio de Save; o Save é controlado exclusivamente pela diferença em relação à lista inicial.
- O componente prototípico `NewTransactionsTable` em `TransactionManagementScreen.kt` serve apenas como referência de layout (Row/Column/FormTextField) e seu conteúdo estático será substituído pela ligação aos dados/eventos do ViewModel existente.
