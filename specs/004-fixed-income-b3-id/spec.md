# Feature Specification: Identificador B3 em Renda Fixa

**Feature Branch**: `004-fixed-income-b3-id`

**Created**: 2026-05-24

**Status**: Draft

**Input**: User description: "Vamos atualizar o cadastro de renda fixa no sistema. Ela terá um novo campo chamado Identificador B3 (String nullable, opcional, qualquer tipo de texto). Tela de cadastro com campo não obrigatório. Persistência com migração dos dados existentes. Tela de Histórico com coluna à direita: ícone azul de informação com tooltip do valor quando preenchido; ícone amarelo de aviso com tooltip quando não informado. Escopo somente renda fixa; demais tipos inalterados e sem ícone no histórico."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cadastrar ou editar ativo de renda fixa com Identificador B3 opcional (Priority: P1)

O usuário abre o cadastro de um ativo de **renda fixa** (novo ou existente). Além dos campos já existentes, há um campo opcional rotulado **"Identificador B3"**, em que pode informar qualquer texto livre (letras, números, símbolos, espaços internos, etc.) ou deixar em branco. O cadastro pode ser salvo com ou sem esse valor; nenhuma validação de formato bloqueia o salvamento — apenas a ausência do campo permanece opcional.

**Why this priority**: Sem persistir e editar o identificador no cadastro, o restante da feature (histórico, integração futura com B3) não tem valor.

**Independent Test**: Criar um ativo de renda fixa informando um identificador, salvar, reabrir o cadastro e confirmar que o valor aparece. Repetir criando outro ativo sem preencher o campo e confirmar que salva normalmente e o campo permanece vazio.

**Acceptance Scenarios**:

1. **Given** o usuário está no cadastro de um **novo** ativo de renda fixa, **When** preenche "Identificador B3" com qualquer texto (ex.: `  ABC-123/XYZ  `) e salva, **Then** o ativo é gravado e, ao reabrir o cadastro, o valor exibido é o texto **após trim** (`ABC-123/XYZ`).
2. **Given** o usuário está no cadastro de um **novo** ativo de renda fixa, **When** deixa "Identificador B3" em branco e salva, **Then** o ativo é gravado sem erro e o campo continua vazio ao reabrir.
3. **Given** existe um ativo de renda fixa **sem** identificador cadastrado, **When** o usuário edita o cadastro, informa um identificador e salva, **Then** o valor passa a estar associado ao ativo.
4. **Given** existe um ativo de renda fixa **com** identificador cadastrado, **When** o usuário edita o cadastro, apaga o valor do campo e salva, **Then** o identificador deixa de estar associado ao ativo (equivalente a não informado).
5. **Given** o usuário está no cadastro de ativo de **renda variável** ou **fundo de investimento**, **When** visualiza o formulário, **Then** o campo "Identificador B3" **não** é exibido e o fluxo de salvamento permanece como hoje.

---

### User Story 2 - Dados existentes preservados após atualização do aplicativo (Priority: P1)

Usuários que já possuem ativos de renda fixa cadastrados atualizam o aplicativo para a versão que inclui esta feature. Todos os ativos e históricos existentes continuam acessíveis; ativos de renda fixa passam a suportar o novo atributo, inicialmente **sem** identificador informado até que o usuário edite e salve um valor.

**Why this priority**: Garantir continuidade operacional e evitar perda de dados em produção.

**Independent Test**: Simular base com ativos de renda fixa criados antes da atualização; após a atualização, listar cadastros e histórico — dados anteriores intactos, identificador ausente até edição manual.

**Acceptance Scenarios**:

1. **Given** existem ativos de renda fixa cadastrados **antes** da atualização, **When** o usuário abre o aplicativo na nova versão, **Then** todos os ativos permanecem listados com os mesmos dados de negócio já conhecidos (emissor, tipo, valores, etc.).
2. **Given** um ativo de renda fixa legado sem identificador, **When** o usuário abre seu cadastro após a atualização, **Then** o campo "Identificador B3" aparece vazio e o ativo pode ser salvo sem preencher o campo.
3. **Given** a atualização foi aplicada, **When** o usuário consulta o histórico de posicionamento, **Then** linhas de renda fixa exibem o indicador de identificador **não informado** (conforme User Story 3) até que o cadastro seja atualizado com um valor.

---

### User Story 3 - Visualizar status do Identificador B3 no Histórico (somente renda fixa) (Priority: P1)

Na tela de **Histórico** (posicionamento no período), para cada linha de **renda fixa**, a coluna mais à direita da tabela indica se o identificador B3 foi informado no cadastro do ativo:

- **Informado**: ícone de **informação** na cor azul (padrão visual já usado na coluna mais à esquerda da mesma tabela), com tooltip ao passar o cursor ou equivalente de acessibilidade mostrando o **valor** do identificador.
- **Não informado**: ícone de **aviso** na cor amarela, com tooltip informando que **não foi informado o valor para Identificador B3** (ou redação equivalente clara para o usuário).

Linhas de **renda variável** e **fundos de investimento** **não** exibem ícone nem conteúdo nesta coluna — a célula permanece visualmente neutra (sem ícone), como hoje nas colunas que não se aplicam a outros tipos.

**Why this priority**: Permite ao usuário, na rotina mensal de histórico, ver rapidamente quais títulos de renda fixa ainda carecem de identificador para conciliação com a B3.

**Independent Test**: No histórico, comparar uma linha de renda fixa com identificador preenchido (ícone azul + tooltip com valor) e outra sem (ícone amarelo + tooltip de ausência); confirmar que linhas de outros tipos não mostram ícone.

**Acceptance Scenarios**:

1. **Given** uma linha de histórico referente a ativo de **renda fixa** cujo cadastro tem identificador preenchido, **When** o usuário visualiza a tabela de histórico, **Then** na coluna mais à direita aparece o ícone azul de informação e o tooltip exibe o valor do identificador.
2. **Given** uma linha de histórico referente a ativo de **renda fixa** sem identificador no cadastro, **When** o usuário visualiza a tabela, **Then** na coluna mais à direita aparece o ícone amarelo de aviso e o tooltip comunica que o identificador B3 não foi informado.
3. **Given** uma linha de histórico de **renda variável** ou **fundo**, **When** o usuário visualiza a tabela, **Then** a coluna mais à direita **não** exibe ícone de informação nem de aviso para essa linha.
4. **Given** o usuário alterou o identificador no cadastro de um ativo de renda fixa, **When** retorna ao histórico do período que inclui esse ativo, **Then** o ícone e o tooltip refletem o estado **atual** do cadastro (preenchido ou não).

---

### Edge Cases

- **Espaços em branco**: Espaços à esquerda e à direita são removidos com `trim` ao salvar. Se, após o trim, o campo ficar vazio, trata-se como **não informado** (equivalente a vazio) no cadastro e no histórico.
- **Texto livre**: Símbolos, pontuação, acentuação e demais caracteres no miolo do texto são aceitos; o valor persistido é o texto **após trim**, sem limite artificial de tamanho na interface.
- **Múltiplas holdings do mesmo ativo**: O identificador pertence ao **ativo** de renda fixa; todas as linhas de histórico desse ativo exibem o mesmo status e valor de identificador na coluna dedicada.
- **Ativo de renda fixa recém-criado sem identificador**: No histórico, comporta-se como "não informado" (ícone amarelo) até que o usuário preencha no cadastro.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE armazenar, para cada ativo de **renda fixa**, um atributo opcional **Identificador B3** representando texto livre ou ausência de valor.
- **FR-002**: O cadastro de ativo de renda fixa DEVE exibir o campo **"Identificador B3"** como **não obrigatório**, aceitando qualquer entrada de texto ou vazio, sem restrição de formato nem limite de caracteres na digitação — no mesmo espírito do campo **"Observações gerais"** do mesmo formulário.
- **FR-003**: O sistema DEVE permitir salvar o cadastro de renda fixa com o identificador preenchido, vazio ou removido em edição posterior, sem exigir o campo para conclusão do salvamento.
- **FR-004**: O sistema DEVE aplicar `trim` ao valor do identificador antes de persistir (remover espaços à esquerda e à direita). Se o resultado do trim for vazio, o identificador é tratado como não informado (`null`). Espaços internos e demais caracteres no miolo do texto são preservados.
- **FR-005**: Após atualização do aplicativo em instalações existentes, o sistema DEVE preservar todos os dados de ativos e histórico já cadastrados; ativos de renda fixa existentes DEVEM iniciar sem identificador até edição explícita pelo usuário.
- **FR-006**: O cadastro de ativos de **renda variável** e **fundos de investimento** NÃO DEVE exibir nem alterar o campo Identificador B3.
- **FR-007**: A tela de Histórico DEVE incluir uma coluna adicional posicionada **totalmente à direita** da tabela, dedicada ao status do Identificador B3.
- **FR-008**: Para linhas de **renda fixa** com identificador informado, a coluna do histórico DEVE exibir ícone de informação **azul** e tooltip com o **valor** do identificador, no mesmo padrão de interação (tooltip ao focar/hover) já usado na coluna informativa mais à esquerda da tabela.
- **FR-009**: Para linhas de **renda fixa** sem identificador informado, a coluna DEVE exibir ícone de **aviso amarelo** e tooltip informando explicitamente que o Identificador B3 não foi informado.
- **FR-010**: Para linhas de **renda variável** e **fundos**, a coluna mais à direita NÃO DEVE exibir ícone de informação nem de aviso; a célula permanece **vazia** (coluna presente na tabela para alinhamento, sem conteúdo visual).
- **FR-011**: O estado exibido no histórico DEVE refletir o valor atual do cadastro do ativo após cada salvamento no formulário de renda fixa.

### Key Entities

- **Ativo de renda fixa**: Investimento de categoria renda fixa; ganha atributo opcional **identificador B3** (texto livre ou ausente), independente de corretora/emissor já modelados.
- **Linha de histórico de posicionamento**: Representação tabular de uma posição no período; para renda fixa, inclui indicação derivada do cadastro do ativo sobre preenchimento do identificador B3 e valor para tooltip quando aplicável.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em teste de aceitação, 100% dos fluxos de cadastro de renda fixa (com identificador, sem identificador, edição para adicionar e para remover) concluem salvamento com sucesso na primeira tentativa, sem erro de validação indevida.
- **SC-002**: Após atualização em ambiente com base pré-existente, 100% dos ativos de renda fixa legados permanecem consultáveis com os mesmos dados de negócio observados antes da atualização.
- **SC-003**: Em revisão da tela de Histórico com amostra mista (renda fixa com e sem identificador, renda variável, fundos), o usuário identifica corretamente o status do identificador em **todas** as linhas de renda fixa em menos de 5 segundos por linha, sem ambiguidade entre ícone azul (preenchido) e amarelo (ausente).
- **SC-004**: Nenhuma linha de renda variável ou fundo exibe ícone na coluna do Identificador B3 em testes de regressão da tabela de histórico.

## Assumptions

- **Escopo fechado**: Apenas **renda fixa** recebe o campo e a coluna de histórico; nenhuma outra categoria de investimento é alterada nesta entrega.
- **Texto livre**: Não há validação de formato nem limite de caracteres imposto na interface; o usuário pode informar textos longos como no campo de observações.
- **Vazio vs. preenchido**: Campo em branco ou que fique vazio após `trim` conta como **não informado** para ícone amarelo e ausência de valor no tooltip.
- **Paridade com observações**: Comportamento de entrada e persistência alinhado ao campo **"Observações gerais"** (texto opcional, sem teto artificial de tamanho no formulário).
- **Tooltip e acessibilidade**: O padrão de tooltip da coluna esquerda do histórico (ícone + texto descritivo ao interagir) serve de referência de comportamento para a nova coluna.
- **Propriedade do dado**: O identificador é propriedade do **ativo**, não da holding ou da entrada mensal de histórico isoladamente — todas as linhas do mesmo ativo compartilham o mesmo indicador.
- **Integração B3**: Esta feature **não** inclui importação automática nem validação contra APIs da B3; apenas cadastro manual e visualização no histórico.

## Out of Scope

- Alteração de cadastro ou histórico para renda variável, fundos ou outras categorias.
- Importação, sincronização ou conciliação automática com extratos B3 usando o identificador.
- Validação de unicidade ou formato oficial do identificador junto à bolsa.
- Exportação CSV ou relatórios incluindo a nova coluna (salvo já existente export de renda fixa — o identificador pode ser tratado em feature futura).
