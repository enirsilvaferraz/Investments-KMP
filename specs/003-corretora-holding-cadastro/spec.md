# Especificação de feature: Corretora obrigatória e criação de posição no cadastro de investimento

**Branch da feature**: `003-corretora-holding-cadastro`  
**Criada em**: 2026-04-13  
**Estado**: Rascunho  
**Diretório da spec**: `specs/003-corretora-holding-cadastro` (alinhado ao branch Git atual).  
**Entrada do utilizador**: "Na tela de cadastro de investimentos (specs/001-cadastro-investimento-dialog) preciso adicionar um novo campo de corretora. Essa corretora se relaciona com o investimento através da posição (holding) no armazenamento de dados. Esse campo será um dropdown padrão da tela que busca os dados de corretoras do armazenamento e exibe como lista. Esse campo é obrigatório. Ao salvar o ativo, deve-se criar a posição (holding) também."

**Relacionamento com especificação existente**: Esta feature **estende** o diálogo de cadastro descrito em `specs/001-cadastro-investimento-dialog/spec.md`, acrescentando seleção de corretora e a obrigação de registar a **posição** associada ao investimento aquando o utilizador conclui o cadastro com sucesso.

**Constitution (Investments-KMP):** Incluir, quando aplicável, critérios de **qualidade**, **API/visibilidade** (**`~/.cursor/rules/explicit-api.mdc`**, explicitApi, superfície mínima entre módulos — princípio IV), **testabilidade** (incl. **`~/.cursor/rules/test-patterns.mdc`**, **inglês**, **GIVEN / WHEN / THEN** nos nomes de testes em `*Test.kt`, **KDoc** no método, comentários **`// GIVEN` / `// WHEN` / `// THEN`** no corpo quando aplicável (**linha em branco** antes de cada marcador), **MockK** para dependências externas quando aplicável, **criação de objetos no próprio teste** — **evitar** *factories* de teste (ex. `TestDataFactory`) para código novo, **testes obrigatórios** para mudanças em `core/domain/usecases/` (módulo Gradle **`:domain:usecases`**) — princípio V), **consistência de UX** (formatação, erros; **pré-visualizações `@Preview` no mesmo ficheiro que o composable** — princípio VI), **desempenho** (latência, volumes) e **coerência** código ↔ documentação ↔ `.specify` ↔ `.cursor` (princípios IV–IX); ver `.specify/memory/constitution.md`.

**Idioma:** Redigir este documento em **português do Brasil (pt-BR)** (princípio VIII).

## Clarifications

### Session 2026-04-13

- Q: Se a gravação falhar durante **Salvar**, pode ficar só o investimento persistido sem a posição (ou só a posição sem o investimento correspondente ao mesmo cadastro)? → A: **Não (tudo ou nada)** — ou o utilizador obtém **sucesso** com **investimento e posição** coerentes entre si, ou **não** há cadastro concluído; **não** é aceitável estado final com apenas um dos dois para o mesmo pedido de **Salvar**.

## Cenários de utilizador e testes *(obrigatório)*

### História de utilizador 1 — Escolher corretora ao cadastrar investimento (Prioridade: P1)

Como utilizador que está a registar um investimento no diálogo de cadastro, quero **escolher uma corretora** a partir de uma **lista** alimentada pelo **catálogo já guardado** na aplicação, para que o investimento fique associado à corretora correta através da **posição** que o produto utiliza para essa ligação.

**Por que esta prioridade**: Sem corretora e sem criação da posição, o cadastro não reflete a informação patrimonial esperada (onde o ativo está custodiado).

**Teste independente**: Abrir o cadastro, ver o campo de corretora com as opções do catálogo, tentar gravar sem selecionar e ver bloqueio com mensagem clara; selecionar uma corretora válida e concluir o fluxo de gravação com sucesso.

**Cenários de aceitação**:

1. **Dado** o diálogo de cadastro aberto e existindo **pelo menos uma** corretora no catálogo, **quando** o utilizador abre o controlo de seleção de corretora, **então** vê **todas** as corretoras disponíveis no catálogo, apresentadas de forma legível e coerente com o restante formulário.
2. **Dado** o formulário preenchido de acordo com as regras da categoria de investimento **exceto** a corretora (ainda não selecionada), **quando** o utilizador solicita **Salvar**, **então** o sistema **não** conclui o cadastro e indica que a **corretora é obrigatória**.
3. **Dado** o catálogo de corretoras **vazio**, **quando** o utilizador interage com o campo de corretora ou tenta **Salvar**, **então** o sistema comunica de forma clara que **não há corretoras disponíveis** e **não** conclui o cadastro até existir pelo menos uma corretora selecionável (alinhado às regras globais do produto para dados em falta).
4. **Dado** uma corretora selecionada e os demais requisitos do formulário válidos para a categoria, **quando** o utilizador solicita **Salvar** e a gravação tem **sucesso**, **então** o investimento fica registado **e** é criada a **posição** que associa esse investimento à corretora escolhida, de modo que a relação possa ser recuperada nas visualizações e relatórios que dependem dessa ligação.
5. **Dado** um formulário válido e o utilizador iniciou **Salvar**, **quando** ocorre falha que impede persistir **conjuntamente** o investimento e a posição com a corretora escolhida, **então** o sistema comunica falha de forma clara, mantém o diálogo aberto com os dados preservados conforme `001`, e **não** deixa como resultado final **só** o investimento **ou** **só** a posição desse cadastro (alinhado a **RF-007**).

---

### História de utilizador 2 — Coerência com cancelamento e alterações (Prioridade: P2)

Como utilizador, quero que o campo de corretora se comporte como os restantes campos do formulário relativamente a **alterações**, **cancelamento** e **fecho com confirmação**, para não perder ou guardar dados de forma inconsistente.

**Por que esta prioridade**: Evita surpresas quando o utilizador descarta o diálogo ou altera a categoria do investimento.

**Teste independente**: Abrir o diálogo, selecionar corretora, cancelar com confirmação quando aplicável (conforme `001`) e verificar que nada foi persistido; alterar categoria após escolher corretora e verificar que a seleção de corretora continua válida para o envio se ainda aplicável.

**Cenários de aceitação**:

1. **Dado** o estado inicial do formulário ao abrir o diálogo (conforme `001`), **quando** nenhuma corretora foi selecionada, **então** o campo de corretora conta como **não preenchido** para efeitos de deteção de alterações e validação.
2. **Dado** uma corretora selecionada, **quando** o utilizador fecha sem gravar segundo as regras de `001` (incluindo confirmação quando há alterações), **então** **nenhum** investimento novo **nem** posição nova é criada.
3. **Dado** o utilizador altera apenas a **categoria** do investimento no primeiro campo, **quando** já existia corretora selecionada, **então** a seleção de corretora **mantém-se** até o utilizador a alterar explicitamente (salvo regra de negócio futura que invalide combinações específicas — fora de âmbito se não existir essa regra).

---

### Casos extremos (edge cases)

- **Catálogo vazio**: não deve ser possível concluir o cadastro; a mensagem deve orientar o utilizador (por exemplo, necessidade de registar corretoras antes), sem linguagem técnica interna.
- **Falha ao gravar** (indisponibilidade ou erro ao persistir): manter o diálogo aberto, dados preservados e possibilidade de nova tentativa, **em linha** com a história P2 de `001`; o resultado para o utilizador **não** pode ser “cadastro concluído” se **investimento** e **posição** não ficarem **ambos** persistidos de forma coerente — em caso de falha a meio, **não** deve persistir apenas um dos dois (ver **RF-007** e clarificação de sessão).
- **Lista muito longa**: o controlo de seleção deve permanecer utilizável (por exemplo, através de padrões de pesquisa ou ordenação já adotados noutros dropdowns da aplicação, quando existirem).
- **Corretora deixa de existir** entre abrir o diálogo e gravar: o sistema deve tratar o erro de forma compreensível e não deixar o utilizador sem feedback.

## Requisitos *(obrigatório)*

### Requisitos funcionais

- **RF-001**: O diálogo de cadastro de investimento **DEVE** apresentar um campo **obrigatório** para **seleção de corretora**, com o mesmo padrão de interação dos **outros dropdowns** desse ecrã.
- **RF-002**: As opções desse campo **DEVEM** corresponder às corretoras **persistidas** no catálogo do utilizador (fonte única de verdade para a lista).
- **RF-003**: O sistema **NÃO DEVE** permitir concluir o cadastro (**Salvar** com sucesso) sem uma corretora **explicitamente** selecionada.
- **RF-004**: Quando o cadastro for concluído com sucesso, o sistema **DEVE** criar a **posição** que associa o investimento recém-criado à corretora selecionada, de forma que a associação fique disponível para consultas posteriores.
- **RF-005**: Se não existir nenhuma corretora no catálogo, o sistema **DEVE** impedir a conclusão do cadastro e **DEVE** comunicar o motivo de forma clara ao utilizador.
- **RF-006**: O campo de corretora **DEVE** integrar-se às regras já definidas em `001` para validação à submissão, feedback de erro, estado de carregamento durante **Salvar**, e fecho com **Cancelar** / **X** (incluindo confirmação quando há alterações).
- **RF-007**: Para um único pedido de **Salvar** neste diálogo, o sistema **NÃO DEVE** deixar o utilizador com **investimento** persistido **sem** a **posição** correspondente à corretora escolhida, **nem** **posição** persistida **sem** o **investimento** desse cadastro; falhas a meio **DEVEM** ser tratadas de modo que o estado final visível ao utilizador seja falha (com mensagem clara) ou sucesso **completo** (ambos os registos), **nunca** sucesso parcial dessa ligação.

### Entidades principais *(incluir se a feature envolver dados)*

- **Corretora**: registo do catálogo que identifica a instituição onde o investimento está (ou estará) custodiado; é a origem das opções do dropdown.
- **Investimento (ativo)**: o registo principal criado pelo diálogo de cadastro, conforme `001`.
- **Posição (holding)**: registo que **liga** um investimento a uma corretora no modelo de dados do produto; **deve** ser criado quando o cadastro do investimento é bem-sucedido, com a corretora escolhida no formulário.

## Critérios de sucesso *(obrigatório)*

### Resultados mensuráveis

- **CS-001**: Em testes de aceitação do fluxo feliz, **100%** dos novos investimentos criados através deste diálogo **incluem** uma corretora selecionada e resultam numa **posição** consultável que referencia essa corretora.
- **CS-002**: Em cenários de validação, **100%** das tentativas de **Salvar** sem corretora selecionada são **bloqueadas** com mensagem compreensível **antes** de qualquer conclusão apresentada como sucesso ao utilizador.
- **CS-003**: O tempo percebido pelo utilizador para ver a lista de corretoras após abrir o controlo **não** deve degradar a experiência face aos outros dropdowns do mesmo ecrã (lista disponível no momento esperado da interação, sem atraso anómalo face ao restante formulário).
- **CS-004**: **Não** há discrepância entre o nome da corretora mostrada na lista e o vínculo guardado na posição (o que o utilizador escolhe é o que fica associado após sucesso).
- **CS-005**: Em cenários de **falha** durante **Salvar**, **0%** dos resultados finais aceites pelo produto para esse pedido são estados em que exista **só** investimento **ou** **só** posição daquele cadastro — ou falha comunicada ou sucesso com **par** investimento+posição coerente.

## Premissas

- Já existe (ou existirá em paralelo) um mecanismo no produto para **manter o catálogo de corretoras**; este diálogo **consome** esse catálogo, **não** substitui a gestão completa de corretoras.
- A criação do investimento e da posição associada segue a política **tudo ou nada** para um dado **Salvar** (ver **RF-007**, **CS-005** e clarificação de sessão): o utilizador **só** deve ver **cadastro concluído** quando **ambos** existem de forma coerente; falha a meio **não** deve produzir só um dos dois registos.
- Comportamentos de **edição** de investimento já existente **não** são obrigatórios nesta especificação salvo já forem parte do mesmo diálogo; se apenas o **cadastro novo** estiver no âmbito de `001`, esta feature aplica-se **no mínimo** a esse fluxo.
