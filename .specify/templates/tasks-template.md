---

description: "Modelo de lista de tarefas para implementação de features (Investments-KMP)"
---

# Tarefas: [NOME DA FEATURE]

**Entrada:** Documentos de desenho em `/specs/[###-feature-name]/`  
**Pré-requisitos:** plan.md (obrigatório), spec.md (obrigatório para histórias), research.md, data-model.md, contracts/

**Idioma:** Redigir `tasks.md` gerado em **português do Brasil (pt-BR)**, conforme `.specify/memory/constitution.md` princípio VIII.

**Coerência:** Se uma tarefa alterar código com impacto em domínio ou convenções, incluir subtarefas ou critérios de conclusão para atualizar `*.md`, `.specify/` ou `.cursor*` quando aplicável (princípio IX).

**Usecases:** Tarefas que alterem **`core/domain/usecases/`** **devem** incluir subtarefas de teste em `:usecases` (`jvmTest`) no mesmo lote de trabalho (princípio V).

**Kotlin (`explicitApi`):** Tarefas que adicionem ou alterem ficheiros **`.kt`** em módulos com convenção **explicitApi** **devem** respeitar **`~/.cursor/rules/explicit-api.mdc`** (modificadores explícitos; visibilidade mínima — princípio IV).

**Testes:** Quando existirem, **devem** seguir **`~/.cursor/rules/test-patterns.mdc`** (inglês; nomes **GIVEN / WHEN / THEN**; **KDoc**; **`// GIVEN` / `// WHEN` / `// THEN`** no corpo quando aplicável (**linha em branco** antes de cada marcador); **MockK** para colaboradores externos quando aplicável; **objetos criados no próprio teste** — **evitar** *factories* de teste para código novo). Os exemplos abaixo incluem tarefas de teste. Testes são **opcionais** — incluir apenas se a especificação da feature pedir ou a constitution exigir evidência para a lógica.

**Organização:** Tarefas agrupadas por história de utilizador para permitir implementação e teste independentes.

## Formato: `[ID] [P?] [História] Descrição`

- **[P]**: Pode executar em paralelo (ficheiros diferentes, sem dependências)
- **[História]**: História de utilizador (ex.: US1, US2, US3)
- Incluir caminhos de ficheiro exatos nas descrições

## Convenções de caminhos

- **Investments-KMP (KMP):** `core/domain/entity/`, `core/domain/usecases/`, `core/data/`, `core/presentation/` — usar caminhos do `plan.md` e nomes Gradle (`:entity`, `:usecases`, …).
- **Projeto único (outros):** `src/`, `tests/` na raiz
- **Web:** `backend/src/`, `frontend/src/`
- Ajustar conforme a estrutura definida em `plan.md`

**Tarefas de teste:** Incluir quando a spec ou a constitution exigirem evidência automatizada para domínio ou casos de uso (ver `~/.cursor/rules/test-patterns.mdc`).

<!--
  ============================================================================
  IMPORTANTE: As tarefas abaixo são EXEMPLOS ilustrativos.

  O comando /speckit.tasks DEVE substituí-las por tarefas reais com base em:
  - Histórias em spec.md (prioridades P1, P2…)
  - Requisitos em plan.md
  - Entidades em data-model.md
  - Contratos em contracts/

  NÃO manter estes exemplos no tasks.md final gerado.
  ============================================================================
-->

## Fase 1: Configuração (infraestrutura partilhada)

**Objetivo:** Inicialização e estrutura básica

- [ ] T001 Criar estrutura de pastas conforme o plano de implementação
- [ ] T002 Inicializar dependências do projeto [linguagem/framework]
- [ ] T003 [P] Configurar lint e formatação

---

## Fase 2: Fundações (pré-requisitos bloqueantes)

**Objetivo:** Infraestrutura central que DEVE estar pronta antes de qualquer história

**⚠️ CRÍTICO:** Nenhuma história de utilizador começa até esta fase estar completa

Exemplos (ajustar ao projeto):

- [ ] T004 Esquema de base de dados e migrações
- [ ] T005 [P] Autenticação/autorização
- [ ] T006 [P] Roteamento de API e middleware
- [ ] T007 Modelos base dos quais todas as histórias dependem
- [ ] T008 Tratamento de erros e logging
- [ ] T009 Configuração de ambiente

**Checkpoint:** Fundações prontas — pode iniciar histórias em paralelo

---

## Fase 3: História de utilizador 1 — [Título] (Prioridade: P1) 🎯 MVP

**Objetivo:** [O que esta história entrega]

**Teste independente:** [Como verificar isoladamente]

### Testes da US1 (opcional — só se testes pedidos) ⚠️

> **NOTA:** Escrever estes testes primeiro; devem FALHAR antes da implementação

- [ ] T010 [P] [US1] Teste de contrato para [endpoint] em …
- [ ] T011 [P] [US1] Teste de integração para [jornada] em …

### Implementação da US1

- [ ] T012 [P] [US1] Criar modelo [Entidade1] em …
- [ ] T013 [US1] Implementar [serviço] em … (depende de T012)
- [ ] T014 [US1] Implementar [feature/tela] em …
- [ ] T015 [US1] Validação e tratamento de erros
- [ ] T016 [US1] Logging das operações desta história

**Checkpoint:** A US1 deve estar funcional e testável de forma independente

---

## Fase 4: História de utilizador 2 — [Título] (Prioridade: P2)

**Objetivo:** [O que esta história entrega]

**Teste independente:** [Como verificar isoladamente]

### Testes da US2 (opcional)

- [ ] T018 [P] [US2] …
- [ ] T019 [P] [US2] …

### Implementação da US2

- [ ] T020 [P] [US2] …
- [ ] T021 [US2] …

**Checkpoint:** US1 e US2 funcionam de forma independente

---

## Fase 5: História de utilizador 3 — [Título] (Prioridade: P3)

**Objetivo:** [O que esta história entrega]

**Teste independente:** [Como verificar isoladamente]

### Testes da US3 (opcional)

- [ ] T024 [P] [US3] …

### Implementação da US3

- [ ] T026 [P] [US3] …

**Checkpoint:** Todas as histórias desejadas estão funcionais de forma independente

---

[Adicionar mais fases de histórias conforme o padrão acima]

---

## Fase N: Polimento e preocupações transversais

**Objetivo:** Melhorias que afetam várias histórias

- [ ] TXXX [P] Atualização de documentação em `docs/`
- [ ] TXXX Limpeza e refatoração
- [ ] TXXX Otimização de desempenho
- [ ] TXXX [P] Testes unitários adicionais (se pedidos)
- [ ] TXXX Endurecimento de segurança
- [ ] TXXX Validar `quickstart.md`

---

## Dependências e ordem de execução

### Dependências entre fases

- **Fase 1:** Sem dependências — pode começar já
- **Fase 2:** Depende da Fase 1 — **bloqueia** todas as histórias
- **Fases de histórias (3+):** Dependem da conclusão da Fase 2; podem seguir em paralelo (equipa) ou em ordem de prioridade (P1 → P2 → P3)
- **Fase final (polimento):** Depende das histórias desejadas estarem completas

### Dependências entre histórias

- **US1 (P1):** Após Fase 2 — sem dependência de outras histórias
- **US2 (P2):** Após Fase 2 — pode integrar com US1, mas deve ser testável sozinha
- **US3 (P3):** Após Fase 2 — idem

### Dentro de cada história

- Testes (se houver) escritos e a falhar antes da implementação
- Modelos antes de serviços; serviços antes de endpoints/UI exposta
- História concluída antes de avançar para a prioridade seguinte

### Oportunidades de paralelização

- Tarefas [P] da mesma fase sem dependência entre si
- Histórias diferentes por membros diferentes da equipa após as fundações

---

## Notas

- [P] = ficheiros diferentes, sem conflito de dependência
- Rótulo [História] liga a tarefa à US para rastreio
- Cada história deve ser completável e testável de forma independente
- Verificar testes a falhar antes de implementar (quando testes forem obrigatórios)
- Commits após cada tarefa ou grupo lógico
