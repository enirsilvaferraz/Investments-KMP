# Especificação de feature: Diálogo de cadastro de investimento

**Branch da feature**: `001-cadastro-investimento-dialog`  
**Criada em**: 2026-04-09  
**Estado**: Rascunho  
**Entrada do utilizador**: "Vamos criar uma tela que abrirá como uma dialog sobre o sistema. Essa é uma tela de cadastro de investimentos, ou seja, um
formulário. Considere que temos 3 tipos de investimentos (Asset no domínio: renda fixa, renda variável, fundo de investimento). O primeiro campo da
tela é um dropdown que posso informar o tipo de asset (InvestmentCategory), o seu valor padrão pode ser renda fixa. Os demais campos serão dispostos
na tela de acordo com a seleção da categoria do investimento. Esses campos devem representar todos os campos dos 3 tipos de investimentos. Porém se eu
selecionar uma categoria somente campos correlacionados a essa categoria ficarão visíveis. No final da tela/dialog, teremos 2 botões: salvar e
cancelar. No topo direito teremos um botão de X para fechar a tela."

**Constitution (Investments-KMP):** Incluir, quando aplicável, critérios de **qualidade**, **API/visibilidade** (**`~/.cursor/rules/explicit-api.mdc`
**, explicitApi, superfície mínima entre módulos — princípio IV), **testabilidade** (incl. **`~/.cursor/rules/test-patterns.mdc`**, **inglês**, *
*GIVEN / WHEN / THEN** nos nomes de testes em `*Test.kt`, **KDoc** no método, comentários **`// GIVEN` / `// WHEN` / `// THEN`** no corpo quando
aplicável (**linha em branco** antes de cada marcador), **MockK** para dependências externas quando aplicável, **criação de objetos no próprio teste
** — **evitar** *factories* de teste (ex. `TestDataFactory`) para código novo, **testes obrigatórios** para mudanças em `core/domain/usecases/` —
princípio V), **consistência de UX** (formatação, erros), **desempenho** (latência, volumes) e **coerência** código ↔ documentação ↔ `.specify` ↔
`.cursor` (princípios IV–IX); ver `.specify/memory/constitution.md`.

**Idioma:** Redigir este documento em **português do Brasil (pt-BR)** (princípio VIII).

## Clarifications

### Session 2026-04-09

- Q: No diálogo de cadastro, como o utilizador deve indicar o emissor? → A: Apenas seleção de um emissor já existente no catálogo; sem criar emissor
  neste diálogo (opção A).
- Q: Ao tocar em "Cancelar" ou "X" com formulário já alterado, deve haver confirmação antes de fechar? → A: Sim, mas só quando existirem alterações
  face ao estado inicial ao abrir o diálogo; sem alterações, fechar de imediato (opção B).
- Q: O que é o "estado inicial" do formulário ao abrir o diálogo (referência para alterado e confirmação)? → A: Sempre o mesmo: categoria "Renda fixa"
  e restantes campos vazios/omissão neutra, sem pré-preenchimento a partir do contexto (opção A).
- Q: Se "Salvar" falhar por causa do sistema (ex.: indisponibilidade, erro ao gravar), o que o utilizador deve ver? → A: Manter o diálogo aberto,
  mensagem de erro clara, dados preservados, possibilidade de tentar "Salvar" de novo ou sair com Cancelar/X (opção B).
- Q: Enquanto o sistema está a processar o "Salvar", como deve ser o comportamento? → A: Impedir nova submissão até haver resultado, com feedback
  perceptível de que está em curso (opção A).

## Cenários de utilizador e testes *(obrigatório)*

### História de utilizador 1 — Abrir e preencher cadastro por categoria (Prioridade: P1)

Como utilizador, quero abrir um diálogo de cadastro de investimento, escolher a categoria (renda fixa, renda variável ou fundo de investimento) e ver
apenas os campos que fazem sentido para essa categoria, para não me confundir com informações irrelevantes.

**Por que esta prioridade**: Entrega o valor central do formulário dinâmico e reduz erros de preenchimento.

**Teste independente**: Abrir o diálogo, alternar cada categoria e verificar que só aparecem os conjuntos de campos esperados; submeter dados válidos
e confirmar que o cadastro é aceite.

**Cenários de aceitação**:

1. **Dado** o diálogo aberto, **quando** o utilizador seleciona "Renda fixa", **então** são apresentados apenas os campos de renda fixa (e campos
   comuns a todos os tipos, se existirem na mesma superfície de formulário) e nenhum campo exclusivo das outras categorias fica visível.
2. **Dado** o diálogo aberto, **quando** o utilizador seleciona "Renda variável", **então** são apresentados apenas os campos de renda variável e
   campos comuns aplicáveis, sem campos exclusivos de renda fixa ou de fundo.
3. **Dado** o diálogo aberto, **quando** o utilizador seleciona "Fundo de investimento", **então** são apresentados apenas os campos de fundo e campos
   comuns aplicáveis, sem campos exclusivos das outras categorias.
4. **Dado** o diálogo recém-aberto, **quando** nenhuma acção foi tomada pelo utilizador além da abertura, **então** a categoria pré-selecionada é
   renda fixa **e** todos os outros campos estão vazios ou nos valores por omissão neutros do formulário, **sem** dados trazidos do ecrã ou fluxo que
   abriu o diálogo.

---

### História de utilizador 2 — Salvar ou abandonar o cadastro (Prioridade: P2)

Como utilizador, quero confirmar o cadastro com "Salvar", ou sair sem guardar com "Cancelar" ou com o "X", para controlar se o investimento é criado
ou não.

**Por que esta prioridade**: Conclui o fluxo e evita perda involuntária ou retenção indevida de rascunhos.

**Teste independente**: Sem alterações ao formulário, Cancelar/X fecham de imediato sem investimento novo; com alterações, verificar pedido de
confirmação e que cancelar a confirmação mantém o diálogo; após confirmar descarte, nenhum investimento novo persistido; preencher válido, Salvar, e
verificar persistência conforme regras do produto; simular falha de gravação após dados válidos e verificar diálogo aberto, dados intactos, mensagem
compreensível e nova tentativa de "Salvar" possível; durante gravação em curso, verificar que não é possível disparar segunda submissão e que existe
indicação perceptível de progresso.

**Cenários de aceitação**:

1. **Dado** um formulário válido para a categoria escolhida, **quando** o utilizador toca em "Salvar", **então** o sistema regista o investimento de
   acordo com as regras de negócio existentes e o diálogo fecha ou reflete sucesso de forma coerente com o restante da aplicação.
2. **Dado** o diálogo aberto e o formulário **sem qualquer alteração** face ao estado inicial, **quando** o utilizador toca em "Cancelar" ou "X", *
   *então** o diálogo fecha de imediato, sem ecrã intermédio de confirmação, e nenhum investimento novo é persistido.
3. **Dado** o diálogo aberto e o formulário **já alterado** (qualquer campo em relação ao estado inicial), **quando** o utilizador toca em "Cancelar"
   ou "X", **então** o sistema solicita confirmação de que as alterações serão descartadas; **se** o utilizador confirma, o diálogo fecha sem
   persistir investimento novo; **se** o utilizador revoga, o diálogo permanece aberto com os dados intactos.
4. **Dado** dados inválidos ou incompletos obrigatórios, **quando** o utilizador toca em "Salvar", **então** o sistema não conclui o cadastro e
   comunica o que falta ou está incorreto, de forma compreensível.
5. **Dado** um formulário válido para a categoria escolhida, **quando** a operação de gravação falha por causa do sistema (por exemplo
   indisponibilidade ou erro ao persistir), **então** o diálogo permanece aberto com todos os dados introduzidos preservados, o sistema apresenta
   feedback claro de que a gravação não foi concluída e o utilizador pode voltar a tocar em "Salvar" ou sair via "Cancelar"/"X" segundo as regras
   habituais.
6. **Dado** um formulário válido e **após** o utilizador ter iniciado "Salvar", **quando** o pedido de gravação ainda está em curso, **então** o
   sistema impede nova submissão de gravação até existir resultado (sucesso ou falha) e apresenta indicação perceptível de que a gravação está em
   progresso.

---

### História de utilizador 3 — Alternar categoria após edição (Prioridade: P3)

Como utilizador, quero mudar a categoria no primeiro campo mesmo depois de começar a preencher, sabendo que o que só valia para a categoria anterior
deixa de contar para o envio.

**Por que esta prioridade**: Evita dados inconsistentes entre categorias quando o utilizador corrige a escolha.

**Teste independente**: Preencher campos específicos de uma categoria, mudar para outra, e verificar que valores só da categoria anterior não são
enviados no guardar.

**Cenários de aceitação**:

1. **Dado** campos específicos de uma categoria preenchidos, **quando** o utilizador altera a categoria no primeiro campo, **então** os campos
   apresentados atualizam-se conforme a nova categoria e valores introduzidos em campos que deixaram de ser aplicáveis não entram no cadastro final.
2. **Dado** seleção de emissor no catálogo e texto de observações preenchidos, **quando** o utilizador altera apenas a categoria no primeiro campo, *
   *então** a seleção de emissor e as observações mantêm-se; apenas os campos específicos da categoria anterior deixam de aplicar-se ao envio.

---

### Casos extremos (edge cases)

- O que acontece quando o utilizador submete com campos obrigatórios vazios da categoria atual? O sistema deve impedir o envio e indicar os campos em
  falta.
- Se não existir nenhum emissor no catálogo (lista vazia), o sistema DEVE impedir a conclusão válida do cadastro (incluindo desactivar ou bloquear "
  Salvar" se aplicável) e DEVE indicar claramente que é necessário registar um emissor noutro fluxo da aplicação antes de concluir este formulário.
- Como o sistema trata formatação de identificadores opcionais (por exemplo documento da empresa com ou sem máscara)? Deve aceitar entradas válidas
  equivalentes e normalizar ou validar de forma clara para o utilizador.
- Para atributos definidos no domínio como fixos para uma categoria (por exemplo regras de liquidez que não são escolha do utilizador em renda
  variável), o formulário não deve pedir esses valores como input editável.
- Alterações à **categoria** ou a **qualquer outro campo** contam como estado “alterado” para efeito de confirmação ao fechar com Cancelar/X, sempre
  comparando com o estado inicial fixo definido em **RF-014**.
- Invocar o diálogo a partir de contextos diferentes **não** altera o estado inicial de referência (continua renda fixa + campos vazios).
- **Falha ao gravar** após validação bem-sucedida: o diálogo não deve fechar por esse motivo sozinho; o utilizador deve conseguir corrigir (retry) ou
  abandonar com as mesmas regras de Cancelar/X (**RF-015**).
- **Gravação em curso**: toques repetidos em "Salvar" não devem originar múltiplos pedidos de criação em paralelo antes do primeiro concluir (**RF-016
  **).

## Requisitos *(obrigatório)*

### Requisitos funcionais

- **RF-001**: O sistema DEVE apresentar o cadastro de investimento num diálogo sobreposto ao ecrã atual da aplicação.
- **RF-002**: O primeiro controlo do formulário DEVE ser uma lista suspensa (dropdown) para a categoria do investimento, com as opções: renda fixa,
  renda variável e fundo de investimento, correspondendo ao agrupamento de tipos de ativo do domínio.
- **RF-003**: O valor inicial da lista de categorias DEVE ser "Renda fixa".
- **RF-004**: Para cada categoria selecionada, o sistema DEVE mostrar todos os campos de entrada necessários para construir o tipo de ativo
  correspondente no domínio e DEVE ocultar todos os campos que pertencem apenas a outras categorias.
- **RF-005**: Para **renda fixa**, o formulário DEVE permitir indicar: tipo de cálculo de rendimento; subtipo do instrumento; data de vencimento;
  rentabilidade contratada; rentabilidade relativa a referência de mercado opcional, quando aplicável ao modelo; regra de liquidez; seleção de um
  emissor existente no catálogo; observações opcionais (alinhado ao modelo de domínio de renda fixa, sem pedir atributos que o domínio não expõe para
  este tipo).
- **RF-006**: Para **renda variável**, o formulário DEVE permitir indicar: nome do ativo; tipo (ação, fundo imobiliário, etc., conforme classificação
  do domínio); código de negociação (ticker); identificação fiscal da empresa do ativo, quando opcional no domínio; seleção de um emissor existente no
  catálogo; observações opcionais; e NÃO DEVE tratar como editáveis pelo utilizador os atributos de liquidez que o domínio fixa para este tipo de
  ativo.
- **RF-007**: Para **fundo de investimento**, o formulário DEVE permitir indicar: nome do fundo; categoria do fundo conforme classificação do domínio;
  regra de liquidez; dias para resgate quando aplicável; data de encerramento ou vencimento, quando opcional no domínio; seleção de um emissor
  existente no catálogo; observações opcionais.
- **RF-008**: O rodapé do diálogo DEVE expor dois botões de ação primária/secundária: "Salvar" e "Cancelar".
- **RF-009**: No canto superior direito do diálogo DEVE existir um controlo "X" para fechar; o fluxo de fecho por "X" DEVE ser o mesmo de "Cancelar" (
  incluindo confirmação quando aplicável, conforme **RF-013**).
- **RF-010**: "Salvar" DEVE validar os campos obrigatórios da categoria corrente antes de tentar persistir; **após** validação bem-sucedida, o sistema
  DEVE tentar registar o investimento segundo as regras já estabelecidas do produto; quando a persistência **for concluída com sucesso**, o diálogo
  fecha ou o resultado é comunicado de forma coerente com o restante da aplicação.
- **RF-015**: Se a **persistência falhar** após dados válidos (erro de sistema, indisponibilidade, falha ao gravar, etc.), o sistema DEVE **manter o
  diálogo aberto**, **preservar** todos os valores introduzidos pelo utilizador, apresentar **mensagem compreensível** de que o cadastro não foi
  guardado por falha do sistema e permitir **nova tentativa** de "Salvar"; "Cancelar" e "X" continuam a seguir **RF-013** e **RF-011**.
- **RF-011**: Após concluído o fecho por "Cancelar" ou "X" (seja de imediato ou após confirmação de descarte, conforme **RF-013**), o sistema NÃO DEVE
  persistir um novo investimento como resultado dessa ação.
- **RF-012**: O emissor DEVE ser indicado unicamente por **seleção** de um registo já existente no **catálogo de emissores**; este diálogo **NÃO DEVE
  ** permitir criar, importar ou substituir por texto livre um novo emissor.
- **RF-013**: Se o formulário **tiver sido alterado** em relação ao **estado inicial** definido em **RF-014**, "Cancelar" e "X" DEVEM pedir *
  *confirmação explícita** antes de descartar e fechar; se **não** houver alterações em relação a esse estado inicial, DEVEM fechar **sem** passo de
  confirmação. O utilizador DEVE poder **cancelar** essa confirmação e permanecer no formulário com os dados intactos.
- **RF-014**: Em cada abertura do diálogo, o **estado inicial** de referência DEVE ser **sempre** o mesmo: categoria **Renda fixa** e todos os *
  *outros** campos nos respectivos valores **vazios** ou **por omissão neutros** (incluindo ausência de emissor seleccionado e observações em branco),
  **sem** pré-preenchimento com dados provenientes do ecrã subjacente, de navegação anterior ou de parâmetros de invocação nesta versão da feature.
- **RF-016**: Entre o início do pedido de gravação disparado por "Salvar" (após validação bem-sucedida) e a conclusão desse pedido (sucesso ou falha),
  o sistema DEVE **impedir** que o utilizador inicie **outra** submissão de gravação pelo mesmo fluxo e DEVE apresentar **indicação perceptível** de
  que a gravação está em curso; quando o pedido terminar, os controlos voltam ao estado adequado (permitir novo "Salvar" se aplicável, conforme *
  *RF-010**/**RF-015**).

### Entidades principais *(incluir se a feature envolver dados)*

- **Categoria de investimento**: Classificação de alto nível (renda fixa, renda variável, fundo de investimento) que determina qual variante de ativo
  está a ser cadastrada.
- **Ativo (visão formulário)**: Conjunto de atributos apresentados ao utilizador que, em conjunto com a categoria escolhida, descrevem o investimento
  a criar; inclui vínculo com **emissor** (entidade emissora) e **observações** opcionais quando comuns às variantes.
- **Emissor**: Entidade que emite o título ou administra o fundo; neste fluxo o utilizador **só** associa o investimento a um emissor **já cadastrado
  **, escolhido a partir do catálogo da aplicação.

## Critérios de sucesso *(obrigatório)*

### Resultados mensuráveis

- **CS-001**: Em teste com utilizadores ou avaliadores internos, **100%** das três categorias podem ser cadastradas com sucesso quando todos os campos
  obrigatórios são preenchidos corretamente, sem campos de outras categorias visíveis durante o preenchimento.
- **CS-002**: Pelo menos **90%** dos participantes num teste de usabilidade informal identificam corretamente "Salvar", "Cancelar" e "X" e relatam que
  compreendem qual categoria estão a cadastrar após ler só o primeiro campo e o rótulo da lista.
- **CS-003**: Em cenários de validação, **100%** das tentativas de salvar com campos obrigatórios em falha são bloqueadas com mensagem ou destaque
  compreensível, e nenhum registo novo inválido é concluído.
- **CS-004**: O fluxo médio de cadastro completo (abrir diálogo até salvar com sucesso) permanece abaixo de **3 minutos** para um caso típico sem
  erros, em condições normais de uso do dispositivo.
- **CS-005**: Em cenários de ensaio em que a gravação falha após formulário válido, em **100%** dos casos o utilizador mantém o diálogo aberto,
  conserva os dados preenchidos, recebe indicação clara de falha na gravação e consegue tentar guardar novamente sem ter de reintroduzir tudo do zero.
- **CS-006**: Em ensaios em que o utilizador tenta acionar "Salvar" repetidamente durante uma gravação já em curso, **não** surgem múltiplos cadastros
  inadvertidos por esse comportamento e o utilizador percebe que a operação anterior ainda está em progresso.

## Premissas

- O modelo conceitual de campos por tipo de ativo alinha-se ao módulo de entidades do domínio (três variantes: renda fixa, renda variável, fundo de
  investimento), mantendo coerência com a documentação de domínio existente.
- O catálogo de emissores é mantido **fora** deste diálogo; o cadastro de investimento depende da existência de pelo menos um emissor quando a regra
  de negócio exigir emissor obrigatório, caso contrário aplicam-se as validações e mensagens descritas nos requisitos e casos extremos.
- Comportamento após "Salvar" bem-sucedido (mensagem, navegação, actualização de listas) segue o padrão já usado noutros ecrãs da aplicação, desde que
  o utilizador perceba claramente que o cadastro foi concluído.
- **Fora do âmbito** desta feature: pré-preencher o formulário com dados vindos do contexto de invocação; cada abertura parte do estado inicial único
  descrito em **RF-014**.
- Alterar categoria após edição descarta valores apenas dos campos específicos que deixam de ser aplicáveis; a **seleção de emissor** no catálogo e o
  texto de **observações** mantêm-se quando o utilizador altera só a categoria.
