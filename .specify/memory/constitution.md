<!--
  Sync Impact Report
  ==================
  Versão: 1.1.1 → 1.2.0
  Tipo de bump: MINOR (novo princípio IX — validação sem build automático)

  Alterações:
    - Novo princípio IX: Validação sem Build Automático
    - Princípio V: esclarecido que escrever testes ≠ executar Gradle
    - Fluxo de Desenvolvimento: alinhado ao princípio IX

  Princípios modificados:
    - V. Testes Obrigatórios em Use Cases (clarificação execução Gradle)
    - IX. (NOVO) Validação sem Build Automático

  Templates verificados:
    - plan-template.md        ✅ compatível (Constitution Check genérico)
    - spec-template.md        ✅ compatível
    - tasks-template.md       ✅ atualizado (nota sobre build sob pedido)
    - AGENTS.md               ✅ atualizado

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

### V. Testes Obrigatórios em Use Cases

Alterações em `:domain:usecases` DEVEM incluir ou atualizar testes unitários.

- Padrão de nome: `GIVEN_WHEN_THEN` (em inglês).
- MockK para colaboradores externos.
- Dados de teste no próprio método; evitar factories centralizadas.
- **Escrever** testes é obrigatório; **executar** `./gradlew :domain:usecases:jvmTest` só quando o utilizador ou a tarefa pedirem validação (princípio IX).

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

## Restrições Técnicas

- **Injeção de dependências**: Koin — módulos Koin por camada.
- **Features UI**: DEVEM expor `*Contract.kt` com `*Screen(modifier: Modifier)` e ser registradas em `:apps:umbrellaApp`.
- **Previews Compose**: no mesmo arquivo do composable, com visibilidade `private`.
- **ViewModels**: estado via `StateFlow` com backing field explícito (`field = MutableStateFlow`).

## Fluxo de Desenvolvimento

1. Criar ou alterar código conforme specs e convenções do projeto.
2. Alterações em `:domain:usecases` → incluir ou atualizar testes unitários (princípio V).
3. **Não** executar build Gradle para validar funcionamento (princípio IX).
4. Compilação e testes (`compileKotlinJvm`, `jvmTest`, `assemble`, etc.) apenas quando pedido explicitamente, em CI/revisão, ou quando a tarefa exigir artefacto de build.
5. Módulo novo → perguntar o nome ao usuário antes de gerar arquivos, caso não informado.
6. Manter coerência entre código, documentação e regras de IA no mesmo PR.

## Governança

Esta constituição é a referência máxima do projeto. Em conflito com outros documentos, prevalece este.

- **Emendas**: DEVEM ser documentadas com justificativa, versionadas semanticamente e propagadas para templates e regras dependentes.
- **Conformidade**: toda revisão de código DEVE verificar aderência aos princípios acima.
- **Complexidade**: qualquer desvio DEVE ser justificado e registrado.
- **Guia operacional**: `AGENTS.md` e regras `.mdc` complementam esta constituição.

**Version**: 1.2.0 | **Ratified**: 2026-05-17 | **Last Amended**: 2026-05-29
