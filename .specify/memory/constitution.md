<!--
  Sync Impact Report
  ==================
  Versão: 1.3.0 → 1.4.0
  Tipo de bump: MINOR (princípio V expandido — testes obrigatórios para migrações Room)

  Alterações:
    - Princípio V: adicionada regra de teste unitário para toda migração Room
    - Fluxo de Desenvolvimento: passo 3 atualizado para mencionar migrações Room
    - AGENTS.md: referência ao princípio V atualizada

  Princípios modificados:
    - V. Testes Obrigatórios em Use Cases → Testes Obrigatórios em Use Cases e Migrações Room

  Templates verificados:
    - plan-template.md        ✅ compatível
    - spec-template.md        ✅ compatível
    - tasks-template.md       ✅ compatível
    - AGENTS.md               ✅ atualizado (referência ao princípio V)

  TODOs pendentes: nenhum
-->

# Investments-KMP — Constituição do Projeto

## Princípios Fundamentais

### I. SOLID, DRY e Boas Práticas

Todo código DEVE seguir estes fundamentos de engenharia:

- **S — Responsabilidade Única**: cada classe/módulo tem um único motivo para mudar.
- **O — Aberto/Fechado**: extensível por adição, não por modificação de código existente.
- **L — Substituição de Liskov**: subtipos DEVEM ser intercambiáveis com seus tipos base sem quebrar comportamento.
- **I — Segregação de Interfaces**: interfaces coesas e específicas; evitar interfaces "gordas".
- **D — Inversão de Dependência**: módulos de alto nível dependem de abstrações, nunca de implementações concretas.
- **DRY**: eliminar duplicação de lógica. Extrair para função, classe ou módulo compartilhado.
- **KISS**: preferir a solução mais simples que resolva o problema.
- **YAGNI**: não implementar funcionalidade especulativa.

Este princípio é prioritário: em caso de dúvida sobre design, recorrer a SOLID primeiro.

### II. Clean Architecture

Monorepo em quatro camadas com **regra de dependência de fora para dentro**:

- **`:apps`** → `:features` → `:domain` → (nenhuma dependência interna)
- **`:data`** → `:domain:entity` (nunca `:features`, nunca `:domain:usecases`)
- `:features` NUNCA depende de `:data`; acessa dados via abstrações em `:domain`.

O domínio é isolado:
- `:domain:entity` define tipos e estruturas, sem lógica de negócio nem framework.
- `:domain:usecases` contém regras de negócio e depende apenas de `:domain:entity`. Colaboradores externos chegam via interfaces (ports) — aplicação direta do princípio D (inversão de dependência).

Cada módulo tem responsabilidade única (princípio S). Criar módulo novo DEVE respeitar este grafo.

### III. Kotlin Multiplatform First

Código compartilhado reside em `commonMain`. Alvos: Android, iOS e Desktop (JVM).

- Compose Multiplatform para UI.
- `kotlinx.datetime` para datas; `kotlinx.serialization` para serialização.
- Código de plataforma (`androidMain`, `iosMain`, `jvmMain`) somente quando inevitável, com `expect`/`actual`.

### IV. Plugins Foundation

Módulos DEVEM usar plugins `foundation.*` de `build-logic/` — nunca aplicar diretamente plugins externos de Compose, Room ou Ktor no `build.gradle.kts` do subprojeto.

Plugins disponíveis: `foundation.project`, `foundation.library.comp`, `koin`, `room`, `ktor`.

### V. Testes Obrigatórios em Use Cases e Migrações Room

Alterações em `:domain:usecases` DEVEM incluir ou atualizar testes unitários.

- Padrão de nome: `GIVEN_WHEN_THEN` (em inglês).
- MockK para colaboradores externos.
- Dados de teste no próprio método; evitar factories centralizadas.
- **Escrever** testes é obrigatório; **executar** `./gradlew :domain:usecases:jvmTest` só quando o utilizador ou a tarefa pedirem validação (princípio IX).

Toda migração de banco de dados Room (incremento de `version` em `@Database`) DEVE ter um teste unitário de migração correspondente.

- Usar `MigrationTestHelper` (AndroidX Room Testing) para validar a migração da versão N-1 para N.
- O teste DEVE verificar que o schema resultante é válido e que dados pré-existentes sobrevivem corretamente à migração.
- Padrão de nome: `migration_N_to_M_migratesCorrectly` (em inglês).
- Os testes de migração residem no módulo `:data` que declara o `@Database`, em `androidTest/` ou no target de testes instrumentados equivalente.
- **Escrever** o teste é obrigatório junto à implementação da migração; **executar** só quando pedido (princípio IX).

### VI. API Explícita

Em módulos com `explicitApi()`, toda declaração de nível de arquivo DEVE ter visibilidade explícita.

Prioridade: `private` → `internal` → `public` (só quando outro módulo importar). Aplicação direta do princípio I (segregação de interfaces) no nível de módulo.

### VII. Documentação Sincronizada

Alterações em entidades, invariantes ou convenções DEVEM atualizar no mesmo PR:

- `core/domain/entity/docs/DOMAIN.md` (modelo de domínio)
- `AGENTS.md` e regras `.mdc` (convenções de agentes)
- `.specify/` (specs, planos, tasks)

Refactors sem mudança de comportamento NÃO exigem atualização.

### VIII. Idioma e Convenções de Nomes

- **Documentação**: português do Brasil (pt-BR).
- **Código**: identificadores, APIs e termos técnicos em inglês.
- **Testes**: nomes, KDoc e comentários de seção em inglês.
- **Pacote base**: `com.eferraz.<nome_do_modulo>` (underscores, sem hífens).

### IX. Validação sem Build Automático

Agentes e ferramentas de IA **NÃO DEVEM** executar Gradle (`compileKotlinJvm`, `assemble`, `jvmTest`, `run`, etc.) para validar funcionamento após implementação ou alteração de código.

- A validação de correção baseia-se em revisão de código, specs, contratos e análise estática — **não** em build local automático.
- Build e testes Gradle só quando: (a) o utilizador pedir explicitamente; (b) a tarefa ou spec exigir artefacto de build (ex.: schema Room exportado); (c) CI ou revisão de PR.
- Tarefas em `tasks.md` que mencionem `./gradlew` são **opcionais para o agente** salvo pedido explícito do utilizador ou flag obrigatória na tarefa.

**Rationale**: builds KMP são lentos e consomem recursos; o utilizador valida quando necessário. Escrever testes (princípio V) permanece obrigatório — executá-los não.

### X. Simplicidade, Legibilidade e Escopo Focado

Todo desenvolvimento DEVE priorizar **menos código e mais valor**:

- **Simplicidade**: preferir a solução mais direta que atenda à spec; evitar abstrações prematuras, over-engineering e diffs desnecessários.
- **Manutenabilidade**: código fácil de alterar — responsabilidades claras, dependências mínimas, convenções do projeto.
- **Legibilidade**: nomes expressivos, fluxo linear; comentários somente onde a intenção não é óbvia.
- **Escopo fiel à spec**: implementar APENAS o que a spec, plano ou `tasks.md` pedem; NÃO inventar funcionalidades, refactors paralelos ou "melhorias" fora do escopo.
- **Diff mínimo**: alterar o menor conjunto de ficheiros necessário; reutilizar código existente antes de criar novo.

Em dúvida entre duas abordagens válidas, escolher a que produz menos linhas e menos superfície de mudança, desde que não viole os princípios I–II.

**Rationale**: código extra aumenta custo de manutenção e risco de regressão; valor entregue mede-se pelo que a spec pede, não pelo volume de código escrito.

## Restrições Técnicas

- **Injeção de dependências**: Koin — módulos Koin por camada.
- **Features UI**: DEVEM expor `*Contract.kt` com `*Screen(modifier: Modifier)` e ser registradas em `:apps:umbrellaApp`.
- **Previews Compose**: no mesmo arquivo do composable, com visibilidade `private`.
- **ViewModels**: estado via `StateFlow` com backing field explícito (`field = MutableStateFlow`).

## Fluxo de Desenvolvimento

1. Ler spec/plano/tasks e delimitar escopo antes de codificar (princípio X).
2. Criar ou alterar código conforme specs e convenções do projeto — diff mínimo, sem funcionalidades fora do pedido.
3. Alterações em `:domain:usecases` → incluir ou atualizar testes unitários (princípio V); migrações Room → incluir teste de migração com `MigrationTestHelper` (princípio V).
4. **Não** executar build Gradle para validar funcionamento (princípio IX).
5. Compilação e testes (`compileKotlinJvm`, `jvmTest`, `assemble`, etc.) apenas quando pedido explicitamente, em CI/revisão, ou quando a tarefa exigir artefacto de build.
6. Módulo novo → perguntar o nome ao usuário antes de gerar arquivos, caso não informado.
7. Manter coerência entre código, documentação e regras de IA no mesmo PR.

## Governança

Esta constituição é a referência máxima do projeto. Em conflito com outros documentos, prevalece este.

- **Emendas**: DEVEM ser documentadas com justificativa, versionadas semanticamente e propagadas para templates e regras dependentes.
- **Conformidade**: toda revisão de código DEVE verificar aderência aos princípios acima, incluindo escopo e simplicidade (princípio X).
- **Complexidade**: qualquer desvio DEVE ser justificado e registrado.
- **Guia operacional**: `AGENTS.md` e regras `.mdc` complementam esta constituição.

**Version**: 1.4.0 | **Ratified**: 2026-05-17 | **Last Amended**: 2026-06-12
