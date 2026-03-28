<!--
  Relatório de impacto (sync)
  Versão: 1.12.2 → 1.12.3
  Motivo do PATCH: redução de redundância (intro vs. precedência; IV visibilidade; V testes + `.mdc`);
  secção «Orientação de decisões técnicas» condensada; sem alteração normativa de fundo.

  Princípios alterados: IV, V (redação)

  Templates: nenhum (texto anterior já alinhado)

  Pendências: nenhuma
-->

# Constitution — Investments-KMP

Documento de princípios do projeto; orienta `/speckit.*`, planos, tarefas e implementação. Ordem de precedência entre fontes normativas: secção **Governança** (abaixo).

**Normas:** **DEVE** / **NÃO DEVE** são obrigatórios. **DEVERIA** é recomendação forte; desvios **DEVERIAM** ser justificados no plano (Complexity Tracking) ou no PR.

---

## Core Principles

### I. Produto e domínio

Aplicação de **carteira de investimentos**. O modelo em **`core/domain/entity/docs/DOMAIN.md`** é a referência de vocabulário e invariantes; alterações no domínio **DEVE** manter código e documentação alinhados.

### II. Arquitetura (Kotlin Multiplatform)

**DEVE** respeitar módulos **`entity`**, **`usecases`**, **`data`**, **`presentation`**. Dependências **apontam para dentro**; o domínio **NÃO DEVE** depender de UI nem de detalhes de infraestrutura.

### III. Build e verificação contínua

Após alterações em código ou Gradle que possam afetar compilação, **DEVE** validar com `./gradlew :<módulo>:compileKotlinJvm` no módulo tocado, conforme **`.cursorrules`**. Isto **DEVERIA** ser considerado antes de concluir uma tarefa de implementação.

### IV. Qualidade de código e manutenibilidade

- **Legibilidade:** nomes e estrutura **DEVERIAM** refletir o domínio (`DOMAIN.md`); evitar abreviações opacas e “código inteligente” sem necessidade.
- **Complexidade:** novas abstrações **DEVERIAM** ter justificativa (menor duplicação, testabilidade, limite de módulo). **NÃO DEVE** introduzir padrões só por hábito externo ao projeto.
- **Consistência:** **DEVERIA** seguir estilo e organização já presentes no módulo (pacotes `com.eferraz.*`, padrões Compose existentes).

#### Visibilidade e `explicitApi()`

O projeto compila com **`explicitApi()`** (convention KMP). Toda declaração ao nível do ficheiro (classes, interfaces, funções de topo, propriedades, etc.) **DEVE** ter **modificador de visibilidade explícito**.

Escolher a **visibilidade mais restrita** que ainda permita uso legítimo **no grafo de módulos** (outros `:módulos`, `expect`/`actual`).

| Modificador | Quando usar |
|-------------|-------------|
| **`private`** | Símbolos **só** referenciados **no mesmo ficheiro** (ex.: sub-composables, helpers locais). |
| **`internal`** | **Predefinição** para o que **não** é contrato entre módulos: DAOs, entidades Room, `*RepositoryImpl`, ViewModels, mappers, maior parte de composables/estado interno ao módulo, infra `expect`/`actual` só usada dentro do módulo (ex.: motor HTTP, builder de BD). |
| **`public`** | **Apenas** para API que **outro subprojeto Gradle** precisa de **importar**: tipos de domínio e ports expostos, casos de uso e interfaces públicas, ponto de entrada (`App()`), destinos/rotas partilhados, `expect`/`actual` que constituem API real (ex.: formatação em `PlatformUtils`). |
| **`protected`** | Só quando uma **hierarquia de classes** o exige; cada uso **DEVERIA** ser intencional e raro neste projeto. |

**Não** marcar `public` por conveniência; começar por `internal` ou `private` e só elevar quando houver consumidor noutro módulo. Em revisão: *«quem fora deste módulo importa isto?»* — se ninguém, **não** `public`.

**`expect` / `actual`:** mesma visibilidade na declaração `expect` e em **todas** as `actual`.

**Cursor:** **`.cursor/rules/explicit-api.mdc`** (IX); **resto de IV:** em dúvida, preferir a solução **mais simples de ler, testar e alterar** sem alargar API nem quebrar módulos.

### V. Padrões de teste e evidência

**Referência operacional:** **`.cursor/rules/test-patterns.mdc`** (nomenclatura, KDoc, `// GIVEN`/`// WHEN`/`// THEN`, source sets, `Double`, corrotinas, MockK, dados inline). Testes **novos ou alterados** **DEVEM** seguir esse ficheiro; desvios **NÃO DEVEM** ocorrer sem justificativa no PR; mudança permanente **DEVE** atualizar o `.mdc` (IX).

- **Idioma (inglês):** em `*Test.kt`, texto descritivo do teste (classe, métodos em *backticks*, comentários de cenário, asserções **definidas no teste**) **DEVE** ser **inglês**. Mensagens de exceção de **produção** **não** estão cobertas. Legado em PT **DEVERIA** migrar ao tocar no ficheiro.
- **Nomes GIVEN / WHEN / THEN:** métodos **novos ou alterados** **DEVEM** usar **GIVEN**, **WHEN**, **THEN** em maiúsculas no nome; **GIVEN … WHEN … THEN …** ou **GIVEN … THEN …** conforme haja passo intermediário. **Não** usar estilo `should …` / `when …` em testes novos ou alterados; legado **DEVERIA** migrar ao tocar no ficheiro.
- **KDoc e corpo:** **DEVERIA** KDoc em inglês acima de `@Test`; **DEVERIA** `// GIVEN` / `// WHEN` / `// THEN` (ou só GIVEN/THEN) com **linha em branco** antes de cada marcador. Testes triviais: mantém KDoc; comentários de secção **podem** omitir-se se só duplicarem o nome.
- **MockK:** para colaboradores externos à unidade sob teste (ports, repos, *clients*, etc.) **DEVE** usar-se **MockK**, salvo o teste exercitar só funções puras, valores de domínio ou código sem colaboradores substituíveis. **Não** acoplar a infra real (rede, BD, FS).
- **Dados no teste:** **DEVERIA** evitar *factories* centralizadas (`TestDataFactory`, etc.) e *helpers* `private` no ficheiro; **NÃO DEVE** acrescentar **novos** métodos a factories existentes para código novo; exceções mínimas (ex.: `@BeforeTest`) **DEVERIAM** constar no PR.
- **Módulo `usecases` (`core/domain/usecases/`):** **toda** alteração a ficheiros **`.kt`** ou **`.kts`** sob esse caminho que **modifique código executável** (lógica de casos de uso, interfaces de repositório, DTOs de aplicação, DI, etc.) **DEVE** ser acompanhada, **no mesmo PR**, de **testes unitários** novos ou de **testes existentes atualizados** no módulo Gradle **`:usecases`** (`src/jvmTest/`), em conformidade com `.cursor/rules/test-patterns.mdc`. **NÃO DEVE** integrar-se código em `usecases` sem cobertura de teste apropriada quando a mudança for **testável** (regra geral: se compila e comporta-se diferente, há teste).
- **Exceções ao parágrafo anterior:** alterações **apenas** a comentários/KDoc **sem** mudar código executável; alterações **apenas** a `build.gradle.kts` do módulo **sem** mudar dependências de teste ou fontes; renomeações mecânicas que **exijam** só atualização espelhada nos testes — neste último caso **DEVE** atualizar-se **também** os ficheiros `*Test.kt` afetados no mesmo PR.
- **Lógica crítica:** alterações a cálculos financeiros, invariantes de domínio ou contratos de casos de uso **DEVERIAM** incluir ou atualizar testes automatizados no nível adequado (`entity`, `usecases`, `composeApp` conforme o caso), **sempre** respeitando os parágrafos anteriores quando houver teste.
- **Rastreio:** critérios de aceitação na spec **DEVERIAM** ser verificáveis por teste automatizado ou por passos de verificação explícitos na tarefa.

**Orientação técnica:** ao planear uma feature, identificar **o que prova** que funciona (teste unitário alinhado a `test-patterns.mdc`, ou verificação manual documentada); **NÃO DEVE** fundar segurança só em testes manuais ad hoc para regras de negócio repetíveis. Para `usecases`, validar com `./gradlew :usecases:jvmTest` antes de concluir.

### VI. Consistência da experiência do utilizador

- **Dados financeiros:** formatação de valores, percentagens e datas **DEVERIA** reutilizar componentes e helpers existentes (inputs, tabelas, tema) para evitar formatos contraditórios entre ecrãs.
- **Estados:** carregamento, vazio e erro **DEVERIAM** ser tratados de forma previsível; mensagens **DEVERIAM** ser compreensíveis para o utilizador final (sem detalhes técnicos internos em diálogos).
- **Acessibilidade:** **DEVERIA** manter contraste e alvos tocáveis adequados ao padrão já usado no Compose; novos componentes críticos **DEVERIAM** seguir o mesmo nível de cuidado.

**Orientação técnica:** antes de criar um componente novo de formulário ou lista, **DEVERIA** verificar-se reutilização no *design system* ou em ecrãs semelhantes; desvios **DEVERIAM** ser raros e documentados.

### VII. Desempenho e responsividade

- **UI:** listas e ecrãs densos **DEVERIAM** evitar trabalho desnecessário no *main thread*; preferir padrões já usados (paginação, estado derivado, recomposição contida) quando aplicável.
- **Dados:** consultas e agregações **DEVERIAM** ser adequadas ao volume esperado da app (carteira pessoal); evitar N+1 ou recomputações repetidas sem necessidade quando o plano identificar risco.
- **Rede:** chamadas **DEVERIAM** ser assíncronas e não bloquear a UI; timeouts e falhas **DEVERIAM** degradar com mensagem utilizador, não *crash* silencioso.

**Orientação técnica:** o plano **DEVERIA** registar objetivos de desempenho quando a spec impor latência, listas grandes ou sincronização; a implementação **DEVERIA** alinhar-se a esses números ou documentar desvio.

### VIII. Idioma dos documentos (português do Brasil)

- **Texto corrido:** toda documentação **produzida** para este repositório — incluindo **esta constitution**, **`DOMAIN.md`**, especificações em `specs/`, planos (`plan.md`), listas de tarefas (`tasks.md`), pesquisas, modelos de dados, checklists, **templates em `.specify/templates/` usados aqui**, `docs/` e **`.cursorrules`** — **DEVE** ser redigida em **português do Brasil (pt-BR)** (ortografia e vocabulário brasileiros).
- **Exceção — testes unitários:** ficheiros **`*.kt` de teste** (`*Test.kt`, `jvmTest` / `commonTest`) seguem o **princípio V** (inglês); o princípio VIII **não** aplica pt-BR ao texto descritivo dos testes.
- **Artefatos gerados por comandos** (`/speckit.*`): o conteúdo entregue ao utilizador **DEVE** seguir pt-BR, salvo citações ou termos técnicos abaixo.
- **Exceções permitidas:** identificadores de código, nomes de bibliotecas e APIs externas, comandos de terminal, caminhos de ficheiro; termos técnicos sem tradução estável (ex.: *pull request*, *pipeline* quando preferido pela equipa); trechos citados de normas ou RFCs.
- **Código (não teste):** comentários e **KDoc** em código de produção **DEVERIAM** preferir pt-BR; comentários triviais ou gerados automaticamente podem seguir o idioma já predominante no ficheiro.
- **Interface do utilizador:** textos de UI **DEVERIAM** estar em pt-BR, alinhados ao produto.

**Orientação técnica:** em PRs ou revisões de documentação, tratar **mistura desnecessária** de inglês em documentos normativos como **dívida** a corrigir.

### IX. Coerência entre código, documentação Markdown e configuradores (Specify / IA)

Alterações de código **NÃO DEVEM** deixar o repositório em estado em que **documentação**, **Spec Kit** e **regras de IA** **contradigam** o comportamento ou a governança real, dentro do âmbito abaixo.

- **Documentos Markdown (`*.md`):** quando a alteração **mudar** modelo de domínio, invariantes, regras de negócio, fluxos ou decisões já descritas, **DEVE** atualizar-se o **Markdown aplicável** na **mesma alteração** ou no **mesmo PR** (preferencialmente): em especial **`DOMAIN.md`**, ficheiros em **`docs/`**, e specs/planos/tarefas em **`specs/`** que estejam ativos para a feature. Se não couber no mesmo PR, **DEVE** existir **PR de seguimento imediato** só para documentação, referenciado no original.
- **Specify (`.specify/`):** quando a alteração **mudar** princípios de governança, gates de plano, formato de artefatos ou convenções que os **templates** ou **`memory`** formalizam, **DEVE** atualizar-se **`.specify/memory/constitution.md`** e, se necessário, **`.specify/templates/**`, scripts em **`.specify/scripts/`** e **`init-options.json`**, para que comandos `/speckit.*` e novas features **não herdem** texto obsoleto.
- **Configuração da IA (Cursor e afins):** quando a alteração **mudar** regras operacionais já espelhadas em **`.cursorrules`**, **`.cursor/rules/*.mdc`** (ex.: **`explicit-api.mdc`**, **`test-patterns.mdc`**) ou comandos **`.cursor/commands/`** personalizados ao projeto (build, testes, domínio), **DEVE** atualizar-se o **ficheiro correspondente** na **mesma alteração** ou no **mesmo PR**, de modo que agentes e revisores **leiam** a mesma regra que o código exige.

**Âmbito:** alterações **puramente internas** (refactor sem mudança de comportamento, correção de *typo*, formatação) **NÃO EXIGEM** atualização de documentos nem de configuradores **salvo** se corrigirem informação **explicitamente falsa** na documentação.

**Orientação técnica:** no fecho de uma tarefa, verificar o checklist: *“O que li em MD / `.specify` / `.cursor` continua verdadeiro após este código?”*

---

## Orientação de decisões técnicas e de implementação

1. **Spec e plano:** decisões relevantes **DEVERIAM** ser legíveis à luz desta constitution (domínio, qualidade, testes, UX, desempenho, idioma).
2. **Trade-offs:** sacrificar qualidade ou testes **DEVERIA** ficar explícito no plano ou PR (risco, follow-up).
3. **Implementação:** **`.cursorrules`** (build); **`.cursor/rules/explicit-api.mdc`** (IV); **`.cursor/rules/test-patterns.mdc`** (V); resto conforme princípios II–IV.
4. **Features financeiras:** cruzar **`DOMAIN.md`** e documentação quando cálculos ou persistência mudarem.
5. **Coerência (IX):** `*.md` aplicável, `.specify/` e `.cursor*` no mesmo ciclo quando o código o exigir.

---

## Governança

### Precedência

1. Constitution  
2. **`DOMAIN.md`** (modelo de domínio)  
3. **`.cursorrules`**  
4. **`.cursor/rules/explicit-api.mdc`** (visibilidade e `explicitApi`)  
5. **`.cursor/rules/test-patterns.mdc`** (detalhe de testes)  
6. **`.specify/memory/`** e templates (governança Spec Kit)  
7. Acordos de equipa em PRs  

### Versionamento desta constitution

- **MAJOR:** alteração incompatível a um princípio obrigatório (DEVE/NÃO DEVE).
- **MINOR:** novo princípio, nova secção material ou expansão de obrigações.
- **PATCH:** clarificações sem mudar o sentido normativo.

### Emendas e revisão

- Alterações **DEVERIAM** atualizar **Version** e **Last Amended** (data ISO **YYYY-MM-DD**).
- PRs que toquem domínio, **`usecases`**, API entre módulos, **testes**, UX crítica, desempenho, **documentação**, **`.specify/`** ou **regras de IA** **DEVERIAM** referir os princípios tocados (incl. **IV**–**V**, **IX**, **VIII** quando aplicável).

**Version**: 1.12.3 | **Ratified**: 2026-03-28 | **Last Amended**: 2026-03-28
