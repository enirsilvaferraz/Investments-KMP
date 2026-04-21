# Especificação de feature: Editar investimento a partir do histórico

**Branch da feature**: `004-edit-investment`  
**Criada em**: 2026-04-21  
**Estado**: Rascunho  
**Entrada do utilizador**: "Na tela de cadastro de investimentos (dialog), quero ter a opção de editar o mesmo. Ao clicar em uma dos itens da lista principal na tela de historico, quero que abra o dialog de cadastro com todos os campos preenchidos com aquele investimento que eu selecionei. Ao tocar no botao de salvar o investimento deve ser atualizado no banco com as novas informações. No modo de edição, o campo categoria fica desabilitado."

**Constitution (Investments-KMP):** Incluir, quando aplicável, critérios de **qualidade**, **API/visibilidade** (**`~/.cursor/rules/explicit-api.mdc`**, explicitApi, superfície mínima entre módulos — princípio IV), **testabilidade** (incl. **`~/.cursor/rules/test-patterns.mdc`**, **inglês**, **GIVEN / WHEN / THEN** nos nomes de testes em `*Test.kt`, **KDoc** no método, comentários **`// GIVEN` / `// WHEN` / `// THEN`** no corpo quando aplicável (**linha em branco** antes de cada marcador), **MockK** para dependências externas quando aplicável, **criação de objetos no próprio teste** — **evitar** *factories* de teste (ex. `TestDataFactory`) para código novo, **testes obrigatórios** para mudanças em `core/domain/usecases/` (módulo Gradle **`:domain:usecases`**) — princípio V), **consistência de UX** (formatação, erros; **pré-visualizações `@Preview` no mesmo ficheiro que o composable** — princípio VI), **desempenho** (latência, volumes) e **coerência** código ↔ documentação ↔ `.specify` ↔ `.cursor` (princípios IV–IX); ver `.specify/memory/constitution.md`.

**Idioma:** Redigir este documento em **português do Brasil (pt-BR)** (princípio VIII).

## Clarifications

### Session 2026-04-21

- Q: Se o mesmo investimento for alterado noutro fluxo ou por sincronização depois de o diálogo de edição ter sido aberto, o que deve acontecer ao tocar em **Salvar**? → A: **Bloquear a gravação**, informar o utilizador de forma clara de que os dados mudaram desde a abertura do diálogo, **não** aplicar as alterações do formulário à persistência nessa tentativa; o utilizador **fecha** o diálogo e **volta a abrir** a edição (ou equivalente na app) para trabalhar com dados atualizados.

## Relação com especificações existentes

Esta feature **estende** o diálogo de cadastro de investimento descrito em `specs/001-cadastro-investimento-dialog/spec.md` (mesmo formulário e regras de validação), acrescentando **modo edição** aberto a partir do **ecrã de histórico** e persistência como **atualização** do investimento existente. Comportamentos não contraditos aqui (confirmação ao fechar com alterações, feedback de gravação, campos por categoria) **mantêm-se** salvo onde esta especificação define excepção (categoria bloqueada em edição).

## Cenários de utilizador e testes *(obrigatório)*

### História de utilizador 1 — Abrir edição a partir da lista do histórico (Prioridade: P1)

Como utilizador, no ecrã de histórico onde vejo a lista principal dos investimentos, quero **tocar num item** e ver o **mesmo diálogo de cadastro** a abrir com **todos os campos preenchidos** com os dados desse investimento, para corrigir ou completar informação sem recriar o registo.

**Por que esta prioridade**: Sem este fluxo não há edição; é o valor central da feature.

**Teste independente**: Com lista populada, tocar numa linha/item e verificar que o diálogo abre e que cada campo visível para a categoria desse investimento reflete os dados guardados desse investimento (incluindo categoria mostrada corretamente).

**Cenários de aceitação**:

1. **Dado** o ecrã de histórico com pelo menos um investimento na lista principal, **quando** o utilizador toca num desse investimentos, **então** o diálogo de cadastro abre em **modo edição** desse investimento.
2. **Dado** o diálogo em modo edição, **quando** o utilizador observa o formulário, **então** os campos aplicáveis à categoria desse investimento aparecem **preenchidos** de forma coerente com o investimento selecionado (equivalente ao que já estava guardado para o utilizador).
3. **Dado** o diálogo em modo edição, **quando** o utilizador observa o controlo de categoria do investimento, **então** esse controlo está **desabilitado** (não é possível alterar a categoria neste modo).

---

### História de utilizador 2 — Guardar alterações do investimento (Prioridade: P1)

Como utilizador, quero alterar campos editáveis no diálogo (exceto categoria) e, ao tocar em **Salvar**, quero que o sistema **atualize** o investimento existente com as novas informações, para que a lista e o restante da aplicação reflitam os dados corrigidos.

**Por que esta prioridade**: A persistência como atualização é o resultado de negócio pedido explicitamente.

**Teste independente**: Abrir edição, alterar um ou mais campos permitidos, Salvar, fechar o diálogo e verificar no histórico (e noutros pontos relevantes da app) que o mesmo investimento mostra os valores atualizados, sem duplicar entradas.

**Cenários de aceitação**:

1. **Dado** o diálogo em modo edição com dados válidos após alterações nos campos permitidos, **quando** o utilizador toca em **Salvar**, **então** o sistema **atualiza** o investimento existente (não cria um investimento novo em substituição desse fluxo) e o feedback de sucesso ou fecho do diálogo é coerente com o restante da aplicação.
2. **Dado** dados inválidos ou obrigatórios em falta, **quando** o utilizador toca em **Salvar**, **então** o sistema **não** conclui a atualização e comunica o que falta ou está incorreto, de forma compreensível.
3. **Dado** uma tentativa de gravação que **falha** por motivo do sistema (indisponibilidade ou erro ao persistir), **quando** o utilizador tinha dados no formulário, **então** o diálogo **permanece aberto** com os dados preservados, mensagem de erro clara, e é possível voltar a **Salvar** ou sair conforme as regras habituais de cancelamento/fecho.
4. **Dado** o diálogo em modo edição aberto com dados carregados, **quando** o investimento foi **alterado noutro fluxo ou por sincronização** antes de **Salvar** e o utilizador toca em **Salvar**, **então** o sistema **não** aplica a atualização, explica que os dados mudaram desde a abertura do diálogo e o utilizador pode **fechar** e **reabrir** a edição para continuar com informação atualizada.

---

### História de utilizador 3 — Cancelar ou fechar sem perder controlo (Prioridade: P2)

Como utilizador, quero **Cancelar** ou fechar pelo **X** com o mesmo tipo de proteção já definido para o cadastro (confirmar descarte só quando há alterações em relação ao estado ao abrir), para não perder dados por engano nem ser incomodado sem necessidade.

**Por que esta prioridade**: Consistência de UX com o cadastro e prevenção de erros.

**Teste independente**: Abrir edição; sem alterações, fechar deve ser imediato; com alteração num campo editável, fechar deve pedir confirmação; confirmar descarte não deve atualizar o investimento.

**Cenários de aceitação**:

1. **Dado** o diálogo em modo edição **sem alterações** face ao estado ao abrir, **quando** o utilizador toca em **Cancelar** ou **X**, **então** o diálogo fecha sem confirmação intermédia e **nenhuma** atualização é aplicada ao investimento.
2. **Dado** o diálogo em modo edição **com alterações** face ao estado ao abrir, **quando** o utilizador toca em **Cancelar** ou **X**, **então** o sistema pede confirmação para descartar; se o utilizador confirmar, o diálogo fecha sem persistir alterações; se revogar, o diálogo mantém-se com os dados intactos.

---

### Casos extremos (edge cases)

- **Investimento já não existe** (removido por outro fluxo enquanto o diálogo estava aberto ou antes de gravar): o sistema deve comunicar de forma clara e não deixar o utilizador em estado ambíguo (ex.: impossível guardar; opção de fechar).
- **Lista vazia**: não deve ser possível abrir edição a partir de um item inexistente; comportamento da lista permanece coerente.
- **Gravação em curso**: o utilizador não deve conseguir submeter **Salvar** em duplicado até haver resultado, com indicação perceptível de progresso (alinhado ao cadastro).
- **Tentativa de alterar categoria**: com o controlo desabilitado, não há caminho normal de mudança; qualquer manipulação indirecta não deve contornar a regra de negócio de **categoria imutável** em modo edição.
- **Dados desatualizados no diálogo** (o investimento mudou na persistência depois da abertura do diálogo): ao **Salvar**, o sistema **bloqueia** a gravação, comunica a divergência de forma clara e o utilizador deve **fechar e reabrir** a edição para alinhar com o estado atual (sem sobrescrever silenciosamente alterações externas).

## Requisitos *(obrigatório)*

### Requisitos funcionais

- **RF-001**: O sistema DEVE permitir abrir o diálogo de cadastro de investimento em **modo edição** a partir de um item da **lista principal** do ecrã de histórico de investimentos.
- **RF-002**: Em modo edição, o sistema DEVE pré-preencher **todos os campos do formulário** que se aplicam à **categoria** daquele investimento com os valores atualmente associados a esse investimento.
- **RF-003**: Em modo edição, o sistema DEVE **desabilitar** a alteração da **categoria** do investimento (o utilizador não pode escolher outra categoria através desse controlo).
- **RF-004**: Em modo edição, ao confirmar **Salvar** com dados válidos, o sistema DEVE **atualizar** o investimento selecionado com as novas informações e **NÃO DEVE** criar um novo investimento como resultado desse fluxo.
- **RF-005**: O sistema DEVE aplicar as **mesmas regras de validação** do cadastro aos campos editáveis em modo edição, excetuando a impossibilidade de mudar categoria.
- **RF-006**: O sistema DEVE manter comportamento de **fecho/cancelamento** e **falha de gravação** alinhado ao definido para o diálogo de cadastro (confirmar descarte só com alterações; em falha de gravação, manter diálogo e dados; impedir submissão duplicada durante gravação), com **estado inicial** em modo edição correspondente aos **dados carregados** do investimento ao abrir o diálogo.
- **RF-007**: Na tentativa de **Salvar** em modo edição, se o investimento tiver sido **alterado na persistência** desde a abertura do diálogo (outro fluxo ou sincronização), o sistema **NÃO DEVE** aplicar as alterações do formulário; **DEVE** informar o utilizador de forma compreensível; o utilizador **DEVE** poder **fechar** o diálogo e **voltar a abrir** a edição do mesmo investimento para trabalhar com dados atualizados.

### Entidades principais

- **Investimento (cadastro existente)**: registo já persistido que o utilizador seleciona no histórico; possui categoria (renda fixa, renda variável ou fundo) e conjunto de atributos dependente da categoria, conforme modelo de produto já usado no cadastro.
- **Item da lista do histórico**: representação na lista principal que identifica de forma inequívoca qual investimento será editado ao ser tocado.

## Critérios de sucesso *(obrigatório)*

### Resultados mensuráveis

- **CS-001**: Em teste com utilizador ou avaliação interna, **100%** das tentativas válidas de “abrir item → alterar campo permitido → Salvar” resultam no **mesmo** investimento a mostrar os valores atualizados na lista de histórico após fechar o diálogo, **sem** novo investimento duplicado pelo mesmo fluxo.
- **CS-002**: Em modo edição, **zero** alterações de categoria reportadas como bem-sucedidas através do formulário (controlo desabilitado e categoria inalterada após Salvar).
- **CS-003**: O tempo para o utilizador perceber que o diálogo abriu **completamente preenchido** (campos visíveis coerentes com o item) é **percebido como imediato** na utilização normal (sem listas de espera longas); falhas de carga comunicam-se claramente em vez de formulário vazio enganador.
- **CS-004**: Taxa de conclusão do fluxo de edição (abrir → guardar válido → ver resultado) na **primeira tentativa** igual ou superior ao fluxo de cadastro equivalente, medida em sessões de teste comparáveis.
- **CS-005**: Em ensaios onde o investimento é alterado externamente após abrir o diálogo, **100%** das tentativas de **Salvar** resultam em **bloqueio** com mensagem clara e **zero** atualizações aplicadas que **ignorem** essa divergência.

## Premissas

- O ecrã de histórico referido é o que apresenta a **lista principal** de investimentos/posições ao utilizador no contexto de “histórico” já existente na aplicação; cada linha corresponde a um investimento editável por esta feature.
- A **identidade** do investimento (qual registo atualizar) é a do item selecionado; não se prevê fusão de vários investimentos neste fluxo.
- **Fora de âmbito** explícito nesta especificação: criar novo investimento a partir do histórico (cadastro em branco continua nos fluxos já existentes); alterar categoria por outro canal; edição em massa.
- Reutilização do diálogo implica que **campos por categoria** e **validações** seguem o produto já especificado no cadastro, salvo a excepção da **categoria bloqueada** em edição.
- O sistema dispõe de meio de **comparar** o estado do investimento à abertura do diálogo com o estado na persistência no momento de **Salvar**, de forma a cumprir **RF-007** sem sobrescrever alterações entretanto aplicadas por outros meios.
