<!--
  Relatório de impacto (sync)
  Versão: 1.3.0 → 1.4.0
  Motivo do MINOR: novo princípio IX (coerência código ↔ documentos MD ↔ .specify ↔ IA)

  Princípios alterados: adicionado IX; VIII sem mudança de título.

  Secções novas: IX Coerência entre código, documentação Markdown e configuradores

  Templates: plan-template (Constitution Check); spec-template (nota); .cursorrules

  Pendências: nenhuma
-->

# Constitution — Investments-KMP

Documento de princípios do projeto. Orienta `/speckit.*`, planos, tarefas e implementação. Em conflito com práticas soltas, **esta constitution**, **`DOMAIN.md`** (quando aplicável) e **`.cursorrules`** prevalecem.

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

O projeto compila com **`explicitApi()`** (convention KMP). Toda declaração ao nível do ficheiro (classes, interfaces, funções de topo, propriedades, etc.) **DEVE** ter **modificador de visibilidade explícito** — **NÃO DEVE** depender da visibilidade padrão implícita.

**Regra de ouro:** escolher sempre a **visibilidade mais restrita** que ainda permita o uso legítimo **no grafo de módulos** (consumo real por outros `:módulos` ou `expect`/`actual`).

| Modificador | Quando usar |
|-------------|-------------|
| **`private`** | Símbolos **só** referenciados **no mesmo ficheiro** (ex.: sub-composables, helpers locais). |
| **`internal`** | **Predefinição** para tudo o que **não** é contrato entre módulos: DAOs, entidades Room, `*RepositoryImpl`, ViewModels, mappers, maior parte de composables/estado interno ao módulo, infra `expect`/`actual` só usada dentro do módulo (ex.: motor HTTP, builder de BD). |
| **`public`** | **Apenas** para API que **outro subprojeto Gradle** precisa de **importar**: tipos de domínio e ports expostos, casos de uso e interfaces públicas, ponto de entrada (`App()`), destinos/rotas partilhados, `expect`/`actual` que constituem API real (ex.: formatação em `PlatformUtils`). |
| **`protected`** | Só quando uma **hierarquia de classes** o exige; cada uso **DEVERIA** ser intencional e raro neste projeto. |

**NÃO DEVE** marcar `public` por conveniência ou “para mais tarde”. **DEVERIA** começar por **`internal`** (ou `private` se só for usado no ficheiro) e **só elevar** a `public` quando existir **consumidor noutro módulo**.

**`expect` / `actual`:** a visibilidade **DEVE** coincidir entre declaração `expect` e todas as `actual` (ex.: ambos `internal`).

**Orientação técnica:** em revisão, perguntar “**quem fora deste módulo importa isto?**”; se a resposta for “ninguém”, o símbolo **NÃO DEVE** ser `public`.

**Orientação técnica (geral IV):** em dúvida entre duas implementações, preferir a que for **mais simples de ler, testar e alterar** sem quebrar fronteiras de módulo nem alargar API sem necessidade.

### V. Padrões de teste e evidência

- **Fonte de regras:** convenções detalhadas em **`.cursor/rules/test-patterns.mdc`** (nomenclatura, `jvmTest` / `commonTest`, asserções com delta em `Double`, MockK/corrotinas).
- **Lógica crítica:** alterações a cálculos financeiros, invariantes de domínio ou contratos de casos de uso **DEVERIAM** incluir ou atualizar testes automatizados no nível adequado (`entity`, `usecases`, `composeApp` conforme o caso).
- **Rastreio:** critérios de aceitação na spec **DEVERIAM** ser verificáveis por teste automatizado ou por passos de verificação explícitos na tarefa.

**Orientação técnica:** ao planear uma feature, identificar **o que prova** que funciona (teste unitário, verificação manual documentada); **NÃO DEVE** fundar segurança só em testes manuais ad hoc para regras de negócio repetíveis.

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
- **Artefatos gerados por comandos** (`/speckit.*`): o conteúdo entregue ao utilizador **DEVE** seguir pt-BR, salvo citações ou termos técnicos abaixo.
- **Exceções permitidas:** identificadores de código, nomes de bibliotecas e APIs externas, comandos de terminal, caminhos de ficheiro; termos técnicos sem tradução estável (ex.: *pull request*, *pipeline* quando preferido pela equipa); trechos citados de normas ou RFCs.
- **Código:** comentários e **KDoc** destinados à equipa **DEVERIAM** preferir pt-BR; comentários triviais ou gerados automaticamente podem seguir o idioma já predominante no ficheiro.
- **Interface do utilizador:** textos de UI **DEVERIAM** estar em pt-BR, alinhados ao produto.

**Orientação técnica:** em PRs ou revisões de documentação, tratar **mistura desnecessária** de inglês em documentos normativos como **dívida** a corrigir.

### IX. Coerência entre código, documentação Markdown e configuradores (Specify / IA)

Alterações de código **NÃO DEVEM** deixar o repositório em estado em que **documentação**, **Spec Kit** e **regras de IA** **contradigam** o comportamento ou a governança real, dentro do âmbito abaixo.

- **Documentos Markdown (`*.md`):** quando a alteração **mudar** modelo de domínio, invariantes, regras de negócio, fluxos ou decisões já descritas, **DEVE** atualizar-se o **Markdown aplicável** na **mesma alteração** ou no **mesmo PR** (preferencialmente): em especial **`DOMAIN.md`**, ficheiros em **`docs/`**, e specs/planos/tarefas em **`specs/`** que estejam ativos para a feature. Se não couber no mesmo PR, **DEVE** existir **PR de seguimento imediato** só para documentação, referenciado no original.
- **Specify (`.specify/`):** quando a alteração **mudar** princípios de governança, gates de plano, formato de artefatos ou convenções que os **templates** ou **`memory`** formalizam, **DEVE** atualizar-se **`.specify/memory/constitution.md`** e, se necessário, **`.specify/templates/**`, scripts em **`.specify/scripts/`** e **`init-options.json`**, para que comandos `/speckit.*` e novas features **não herdem** texto obsoleto.
- **Configuração da IA (Cursor e afins):** quando a alteração **mudar** regras operacionais já espelhadas em **`.cursorrules`**, **`.cursor/rules/*.mdc`** ou comandos **`.cursor/commands/`** personalizados ao projeto (build, testes, domínio), **DEVE** atualizar-se o **ficheiro correspondente** na **mesma alteração** ou no **mesmo PR**, de modo que agentes e revisores **leiam** a mesma regra que o código exige.

**Âmbito:** alterações **puramente internas** (refactor sem mudança de comportamento, correção de *typo*, formatação) **NÃO EXIGEM** atualização de documentos nem de configuradores **salvo** se corrigirem informação **explicitamente falsa** na documentação.

**Orientação técnica:** no fecho de uma tarefa, verificar o checklist: *“O que li em MD / `.specify` / `.cursor` continua verdadeiro após este código?”*

---

## Orientação de decisões técnicas e de implementação

Estas regras **ligam** os princípios às escolhas do dia a dia:

1. **Spec e plano:** cada decisão relevante (biblioteca nova, camada extra, formato de API) **DEVERIA** poder ser lida à luz desta constitution (domínio, qualidade, testes, UX, desempenho, **idioma**).
2. **Trade-offs:** se qualidade de código ou testes forem sacrificados por prazo, o plano ou o PR **DEVERIAM** registar o risco e o follow-up (dívida explícita).
3. **Implementação:** priorizar código que a equipa consiga **rever e estender**; alinhar com **`.cursorrules`** para build, com **`.cursor/rules/test-patterns.mdc`** para testes, e com **visibilidade mínima** (`explicitApi`) para superfície pública entre módulos.
4. **Features financeiras:** decisões que alterem cálculos ou persistência **DEVERIAM** cruzar **`DOMAIN.md`** e atualizar documentação quando o modelo mudar.
5. **Coerência global:** cumprimento do **princípio IX** — código, `*.md` relevantes, `.specify/` e regras de IA **alinhados** no mesmo ciclo de entrega quando houver impacto.

---

## Governança

### Precedência

1. Constitution  
2. **`DOMAIN.md`** (modelo de domínio)  
3. **`.cursorrules`**  
4. **`.cursor/rules/test-patterns.mdc`** (detalhe de testes)  
5. **`.specify/memory/`** e templates (governança Spec Kit)  
6. Acordos de equipa em PRs  

### Versionamento desta constitution

- **MAJOR:** alteração incompatível a um princípio obrigatório (DEVE/NÃO DEVE).
- **MINOR:** novo princípio, nova secção material ou expansão de obrigações.
- **PATCH:** clarificações sem mudar o sentido normativo.

### Emendas e revisão

- Alterações **DEVERIAM** atualizar **Version** e **Last Amended** (data ISO **YYYY-MM-DD**).
- PRs que toquem domínio, **API pública entre módulos**, UX crítica, desempenho, **documentação**, **`.specify/`** ou **regras de IA** **DEVERIAM** referir aderência aos princípios relevantes (incl. **IX**, visibilidade e **pt-BR**).

**Version**: 1.4.0 | **Ratified**: 2026-03-28 | **Last Amended**: 2026-03-28
