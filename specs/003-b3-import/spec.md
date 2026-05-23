# Feature Specification: Importação de Dados da B3

**Feature Branch**: `003-b3-import`

**Created**: 2026-05-23

**Status**: Draft

**Input**: User description: "vamos implementar a importação de dados da B3. A feature inicará a partir de um botão de upload de arquivo localizado a esquerda do botão de exportação na tela de AssetHistoryScreen. Ao tocar no botão abrirá uma caixa de diálogo padrão do sistema para escolher um arquivo. Serão aceitos somente arquivos com extensão xlsx. A feature deve ler esse arquivo que contém uma tabela e apresentar o resultado no console da IDE por enquanto. Esse arquivo possui guias e cada guia tem uma tabela diferente."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Selecionar e Importar Arquivo XLSX da B3 (Priority: P1)

O investidor acessa a tela de histórico de ativos e deseja importar dados exportados pela B3. Ele toca no botão de importação (localizado à esquerda do botão de exportação), seleciona um arquivo XLSX do sistema de arquivos do dispositivo e o sistema lê as cinco guias B3 conhecidas presentes no arquivo (`Acoes`, `ETF`, `Fundo de Investimento`, `Renda Fixa`, `Tesouro Direto`), apresentando o conteúdo de cada uma no console de desenvolvimento; demais guias são ignoradas sem log nem erro.

**Why this priority**: É o fluxo central da feature — sem ele a importação não existe. Entrega valor imediato ao permitir que o investidor carregue dados reais da B3 para o aplicativo.

**Independent Test**: Pode ser testado completamente ao selecionar um arquivo XLSX válido exportado pela B3 e verificar que o conteúdo das cinco guias conhecidas aparece no console da IDE.

**Acceptance Scenarios**:

1. **Given** o investidor está na tela de histórico de ativos, **When** ele toca no botão de importação, **Then** uma caixa de diálogo padrão do sistema operacional é exibida para seleção de arquivo.
2. **Given** a caixa de diálogo de seleção está aberta, **When** o investidor seleciona um arquivo com extensão `.xlsx`, **Then** o sistema aceita o arquivo e inicia a leitura.
3. **Given** um arquivo XLSX válido foi selecionado, **When** o sistema inicia o processamento, **Then** o botão de importação desaparece e é substituído por um spinner do mesmo tamanho na mesma posição; ao concluir, o spinner desaparece, o botão retorna e o conteúdo de cada guia é apresentado no console da IDE.
4. **Given** um arquivo XLSX com múltiplas guias foi selecionado, **When** o sistema processa o arquivo, **Then** cada uma das cinco guias B3 conhecidas presentes no arquivo é processada individualmente e seus dados são exibidos separadamente no console; guias com outros nomes não geram saída no console.

---

### User Story 2 - Restrição de Tipo de Arquivo na Seleção (Priority: P2)

O investidor abre a caixa de diálogo de seleção de arquivo. O sistema configura o diálogo para exibir e permitir a seleção somente de arquivos `.xlsx`, impedindo preventivamente a escolha de formatos inválidos. Caso o sistema operacional não suporte filtragem nativa, o aplicativo rejeita o arquivo selecionado após a escolha e regista a rejeição no console da IDE (sem mensagem na UI nesta fase).

**Why this priority**: A restrição preventiva na própria caixa de diálogo oferece melhor experiência ao usuário do que permitir a seleção e depois apresentar um erro; garante também a integridade do processo de importação.

**Independent Test**: Pode ser testado verificando que, ao abrir a caixa de diálogo, arquivos de outros formatos não aparecem disponíveis para seleção (ou, em SOs sem suporte a filtro, que uma tentativa de selecionar arquivo não-.xlsx resulta em registo de rejeição no console da IDE).

**Acceptance Scenarios**:

1. **Given** o investidor toca no botão de importação, **When** a caixa de diálogo nativa é exibida, **Then** o filtro de tipo de arquivo está configurado para exibir somente arquivos `.xlsx`, ocultando ou desabilitando demais formatos.
2. **Given** o sistema operacional não suporta filtragem por tipo de arquivo na caixa de diálogo, **When** o investidor seleciona um arquivo com extensão diferente de `.xlsx`, **Then** o sistema rejeita o arquivo, restaura o botão de importação e regista no console da IDE que apenas arquivos XLSX são aceitos (sem mensagem na UI).

---

### User Story 3 - Cancelamento da Seleção de Arquivo (Priority: P3)

O investidor abre a caixa de diálogo de seleção de arquivo mas decide cancelar a operação sem selecionar nenhum arquivo.

**Why this priority**: Comportamento esperado de qualidade mínima — o aplicativo não deve travar ou apresentar erro ao cancelar.

**Independent Test**: Pode ser testado ao abrir a caixa de diálogo e fechar sem selecionar arquivo, verificando que o aplicativo permanece estável e na mesma tela.

**Acceptance Scenarios**:

1. **Given** a caixa de diálogo de seleção está aberta, **When** o investidor cancela ou fecha a caixa de diálogo sem selecionar arquivo, **Then** o aplicativo retorna ao estado anterior sem erros e sem modificações.

---

### Edge Cases

- ~~O que acontece quando o arquivo XLSX está corrompido ou malformado?~~ → Resolvido: registar falha no console da IDE; restaurar botão; sem mensagem na UI (FR-008, FR-014).
- ~~O que acontece quando o arquivo XLSX contém guias com nomes fora das cinco guias B3 conhecidas?~~ → Resolvido: ignorar silenciosamente (FR-012).
- ~~O que acontece quando o arquivo XLSX não contém nenhuma guia B3 conhecida ou está vazio?~~ → Resolvido: concluir sem erro e sem saída no console (FR-013).
- ~~O que acontece quando uma guia do arquivo não contém nenhuma linha de dados?~~ → Resolvido: exibir no console com indicação de vazia (FR-010).
- ~~Como o sistema lida com arquivos XLSX muito grandes?~~ → Resolvido: timeout de 30 segundos com cancelamento e registo no console (FR-011).
- ~~O que acontece se o dispositivo não tiver permissão de acesso ao armazenamento?~~ → Resolvido: em Desktop, o diálogo nativo concede permissão implícita ao selecionar; `AccessDeniedException`/`SecurityException` são capturadas e o motivo é registado no console da IDE (FR-014), sem mensagem na UI.
- ~~O que acontece quando uma guia B3 conhecida tem colunas obrigatórias ausentes?~~ → Resolvido: falhar a importação inteira; registar erro no console; restaurar botão (FR-015).
- Arquivo XLSX com **0 bytes** ou ilegível como workbook (antes de identificar guias): registar falha no console (`EMPTY_FILE` ou equivalente), restaurar botão; sem mensagem na UI (alinhado a FR-008/FR-014). **Distinto** de FR-013 (workbook válido sem guias B3 conhecidas → sucesso silencioso sem console).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE exibir um botão de importação de arquivo na tela de histórico de ativos, posicionado à esquerda do botão de exportação existente.
- **FR-002**: Ao tocar no botão de importação, o sistema DEVE abrir a caixa de diálogo padrão do sistema operacional para seleção de arquivo.
- **FR-003**: A caixa de diálogo de seleção DEVE ser configurada para exibir somente arquivos `.xlsx`, impedindo preventivamente a seleção de outros formatos; nos sistemas operacionais que não suportarem filtragem nativa, o sistema DEVE rejeitar o arquivo após a seleção e registar no console da IDE o motivo da rejeição (sem mensagem na UI nesta fase).
- **FR-004**: O sistema DEVE ler o arquivo XLSX selecionado e processar somente as guias cujos nomes correspondam exatamente a uma das cinco guias B3 conhecidas: `Acoes`, `ETF`, `Fundo de Investimento`, `Renda Fixa`, `Tesouro Direto`.
- **FR-005**: Para cada guia B3 conhecida presente no arquivo, o sistema DEVE ler a tabela de dados correspondente (linhas e colunas).
- **FR-006**: O sistema DEVE apresentar o conteúdo lido de cada guia B3 conhecida no console da IDE (saída de log de desenvolvimento), identificando o nome da guia e seus dados tabulares.
- **FR-012**: Guias do arquivo XLSX cujo nome não corresponda a nenhuma das cinco guias B3 conhecidas DEVEM ser ignoradas silenciosamente — sem log no console, sem mensagem de erro e sem falha da importação.
- **FR-013**: Se o arquivo XLSX não contiver nenhuma das cinco guias B3 conhecidas (arquivo sem guias ou apenas com guias de nomes desconhecidos), o sistema DEVE concluir a importação sem erro ao usuário e sem qualquer saída no console; o spinner DEVE desaparecer e o botão de importação DEVE ser restaurado. **Não** se aplica a arquivo com 0 bytes ou sem estrutura XLSX legível (ver Edge Cases — `EMPTY_FILE`).
- **FR-007**: O sistema DEVE tratar graciosamente o cancelamento da seleção de arquivo, sem erros ou mudanças de estado indesejadas.
- **FR-009**: Durante o processamento do arquivo XLSX, o botão de importação DEVE ser substituído visualmente por um spinner do mesmo tamanho e na mesma posição; ao concluir (com sucesso ou erro), o spinner DEVE desaparecer e o botão de importação DEVE retornar ao seu lugar.
- **FR-010**: Guias do arquivo XLSX que estejam vazias (sem linhas de dados) ou contenham apenas cabeçalho DEVEM ser exibidas no console identificadas pelo nome, com indicação de que não possuem dados.
- **FR-011**: O processamento do arquivo XLSX DEVE ser cancelado automaticamente se atingir ou exceder **30 000 ms**; nesse caso, o sistema DEVE registar a falha no console da IDE, remover o spinner e restaurar o botão de importação (sem mensagem na UI nesta fase). O limite é **30 000 ms** inclusive para cancelamento: importações que não concluam dentro desse prazo são canceladas; conclusão com sucesso exige duração **estritamente inferior** a 30 000 ms (alinhado a SC-001).
- **FR-011a**: O registo no console da IDE em caso de timeout DEVE ocorrer no `ImportB3FileUseCase` ao capturar `TimeoutCancellationException`, com mensagem identificável (ex.: `TIMEOUT` ou `Processamento cancelado: tempo limite de 30 s excedido`), antes de propagar a falha ao chamador. Nenhuma linha de dados de guias B3 pode ser impressa após o cancelamento (Fase B não executada).
- **FR-008**: Caso o arquivo XLSX esteja corrompido ou não possa ser lido, o sistema DEVE registar a falha no console da IDE, remover o spinner e restaurar o botão de importação (sem mensagem na UI nesta fase).
- **FR-014**: Nesta fase, falhas de importação (formato inválido, arquivo ilegível, timeout, permissão negada, colunas obrigatórias ausentes) DEVEM ser comunicadas exclusivamente via console da IDE; nenhum Snackbar, diálogo modal ou texto de erro na UI é exigido. O sucesso da importação também não exige feedback na UI além da restauração do botão após o spinner.
- **FR-016**: Quando a importação concluir com sucesso (dados de ao menos uma guia B3 conhecida no console), o sistema DEVE apenas remover o spinner e restaurar o botão de importação, sem Snackbar, diálogo ou outro indicador de sucesso na UI.
- **FR-015**: Se qualquer guia B3 conhecida **presente no arquivo** estiver com colunas obrigatórias ausentes para o mapeamento tipado (cabeçalhos definidos pelos `@ColumnName` de cada DTO em [data-model.md](./data-model.md)), a importação DEVE falhar por completo: registar no console da IDE o nome da guia e o motivo (`MISSING_COLUMNS` ou equivalente), **não** apresentar dados de nenhuma guia no console (validação Fase A antes de qualquer `println` de dados — Fase B), remover o spinner e restaurar o botão de importação.

### Key Entities

- **ArquivoB3**: Representa o arquivo XLSX exportado pela B3; possui um nome de arquivo, uma data de exportação implícita e uma ou mais guias.
- **GuiaArquivo**: Representa uma aba/guia individual dentro do arquivo XLSX; possui um nome e uma tabela de dados (linhas e colunas).
- **TabelaDados**: Representa os dados tabulares de uma guia; composta por cabeçalhos de coluna e linhas de dados.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O investidor consegue concluir a importação com sucesso (dados no console) em **menos de 30 000 ms** desde o toque no botão até o fim da Fase B; processamentos que atinjam ou excedam **30 000 ms** são cancelados automaticamente com registo da falha no console da IDE (FR-011).
- **SC-002**: 100% das guias B3 conhecidas presentes em um arquivo XLSX válido são lidas e apresentadas no console, sem omissão de dados; guias com outros nomes não afetam o sucesso da importação.
- **SC-007**: Se qualquer guia B3 conhecida tiver colunas obrigatórias ausentes, a importação falha atomicamente em 100% das execuções — sem dados de guias no console e com erro registado no console da IDE.
- **SC-006**: Arquivos XLSX válidos mas sem nenhuma guia B3 conhecida concluem a importação sem mensagem de erro e sem travamento em 100% das execuções.
- **SC-008**: Importações bem-sucedidas não exibem feedback de sucesso na UI em 100% das execuções; o investidor confirma o resultado apenas pelo console da IDE.
- **SC-003**: Arquivos com extensão diferente de `.xlsx` são rejeitados em 100% das tentativas, com motivo registado no console da IDE.
- **SC-004**: O cancelamento da seleção de arquivo não resulta em nenhum erro ou estado inconsistente em 100% das execuções.
- **SC-005**: Arquivos XLSX corrompidos ou ilegíveis resultam em registo de erro no console da IDE e restauração do botão de importação, sem travar o aplicativo e sem mensagem na UI.

## Clarifications

### Session 2026-05-23

- Q: O que a interface deve mostrar enquanto o arquivo está sendo processado? → A: O botão de importação é substituído visualmente por um spinner do mesmo tamanho; ao concluir (com sucesso ou erro), o spinner desaparece e o botão de importação volta ao seu lugar.
- Q: O que fazer com guias vazias (sem dados além do cabeçalho, ou completamente vazias)? → A: Exibir no console identificando a guia pelo nome e informando que está vazia ou sem dados.
- Q: O sistema deve reconhecer nomes específicos de guias da B3 ou processar todas genericamente? → A: Processar somente as cinco guias B3 conhecidas pelo nome exato (`Acoes`, `ETF`, `Fundo de Investimento`, `Renda Fixa`, `Tesouro Direto`); demais guias são ignoradas silenciosamente.
- Q: Se o XLSX tiver uma aba que não seja uma das cinco guias B3 conhecidas, o que o sistema deve fazer? → A: Processar somente as cinco guias conhecidas; ignorar silenciosamente as demais.
- Q: Se o arquivo XLSX não tiver nenhuma das cinco guias B3 conhecidas (ou estiver sem guias / vazio), o que o sistema deve fazer? → A: Concluir sem erro e sem saída no console (importação vazia bem-sucedida).
- Q: Como tratar Android e iOS nesta fase? → A: Bypass (stub sem comportamento) — a implementação completa é exclusiva para Desktop; Android e iOS ficam fora do escopo desta entrega.
- Q: Como lidar com arquivos XLSX muito grandes? → A: Processar sem limite de tamanho, mas aplicar timeout de 30 segundos; se ultrapassado, cancelar o processamento e registar a falha no console da IDE.
- Q: Como exibir mensagens de erro na AssetHistoryScreen nesta fase? → A: Apenas log no console da IDE; sem UI de erro (Snackbar, diálogo ou texto inline).
- Q: Se uma guia B3 conhecida tiver colunas obrigatórias ausentes, o que o sistema deve fazer? → A: Falhar a importação inteira; registar erro no console; restaurar botão.
- Q: Quando a importação conclui com sucesso, há feedback na UI além do spinner que desaparece? → A: Sem feedback na UI — apenas dados no console; spinner some e botão volta.
- Q: Comportamento no limite exacto de 30 s? → A: Cancelamento em `>= 30_000 ms`; sucesso apenas se concluir antes de 30_000 ms.

## Assumptions

- O formato do arquivo exportado pela B3 é XLSX padrão (compatível com a especificação Open XML).
- A feature, nesta fase inicial, limita-se a apresentar dados e falhas de importação no console da IDE; nenhum dado é persistido no banco de dados do aplicativo e nenhum feedback de sucesso ou erro é exibido na UI.
- O botão de exportação já existe na tela de histórico de ativos (`AssetHistoryScreen`) e serve como referência de posicionamento para o novo botão de importação.
- A implementação completa da feature (seleção de arquivo, leitura do XLSX e exibição no console) é feita exclusivamente para **Desktop** nesta fase.
- Nas plataformas **Android e iOS**, a feature será implementada como **bypass** (stub sem comportamento): o botão de importação pode estar presente, mas não executa nenhuma ação. A implementação dessas plataformas está fora do escopo desta entrega.
- A caixa de diálogo de seleção de arquivo é a nativa do sistema operacional Desktop, sem customização de UI.
- Arquivos com múltiplas guias são o caso de uso principal da B3; arquivos com uma única guia também devem funcionar.
- O escopo desta feature não inclui validação semântica dos dados importados (ex.: verificar se os dados correspondem a ativos conhecidos); apenas a leitura e exibição bruta dos dados é requerida.
- O sistema processa somente as cinco guias B3 conhecidas pelo nome exato da aba no arquivo; guias adicionais são ignoradas sem log nem erro nesta fase.
- Arquivo sem nenhuma guia B3 conhecida não é tratado como falha: a importação termina em silêncio, sem log no console.
- A importação é atómica em relação a falhas de esquema: colunas ausentes numa guia B3 conhecida invalidam toda a importação, sem saída parcial no console.
