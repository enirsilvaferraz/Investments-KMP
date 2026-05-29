<!--
  Sync Impact Report
  ==================
  Versão: 1.1.0 → 1.1.1
  Tipo de bump: PATCH (remoção de obrigatoriedade de compilação automática pelo agente)

  Alterações:
    - Removida exigência de `./gradlew :<módulo>:compileKotlinJvm` após cada alteração de código
    - Fluxo de desenvolvimento: build/testes Gradle apenas quando pedido ou exigido pela tarefa

  Versão anterior (1.0.0 → 1.1.0):
  Tipo de bump: MINOR (princípio adicionado, reorganização sem remoção)

  Princípios modificados:
    I.   (NOVO) SOLID, DRY e Boas Práticas
    II.  Arquitetura em Camadas → Clean Architecture (absorveu antigo IV "Domínio Isolado")
    III. Kotlin Multiplatform First (sem alteração)
    IV.  Plugins Foundation (sem alteração)
    V.   Testes Obrigatórios em Use Cases (sem alteração)
    VI.  API Explícita (sem alteração)
    VII. Documentação Sincronizada (sem alteração)
    VIII.Idioma e Convenções de Nomes (sem alteração)

  Princípios removidos:
    - IV antigo ("Domínio Isolado") absorvido pelo novo II

  Templates verificados:
    - plan-template.md        ✅ compatível
    - spec-template.md        ✅ compatível
    - tasks-template.md       ✅ compatível

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
- Validação: `./gradlew :domain:usecases:jvmTest`.

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

## Restrições Técnicas

- **Injeção de dependências**: Koin — módulos Koin por camada.
- **Features UI**: DEVEM expor `*Contract.kt` com `*Screen(modifier: Modifier)` e ser registradas em `:apps:umbrellaApp`.
- **Previews Compose**: no mesmo arquivo do composable, com visibilidade `private`.
- **ViewModels**: estado via `StateFlow` com backing field explícito (`field = MutableStateFlow`).

## Fluxo de Desenvolvimento

1. Criar ou alterar código conforme specs e convenções do projeto.
2. Alterações em `:domain:usecases` → incluir ou atualizar testes unitários (princípio V); executar `./gradlew :domain:usecases:jvmTest` apenas quando o utilizador ou a tarefa pedirem validação.
3. Compilação Gradle (`compileKotlinJvm`, `assemble`, etc.) **não** é obrigatória após cada implementação pelo agente — só quando pedido explicitamente, em CI/revisão, ou quando a tarefa exigir artefacto de build (ex.: export de schema Room).
4. Módulo novo → perguntar o nome ao usuário antes de gerar arquivos, caso não informado.
5. Manter coerência entre código, documentação e regras de IA no mesmo PR.

## Governança

Esta constituição é a referência máxima do projeto. Em conflito com outros documentos, prevalece este.

- **Emendas**: DEVEM ser documentadas com justificativa, versionadas semanticamente e propagadas para templates e regras dependentes.
- **Conformidade**: toda revisão de código DEVE verificar aderência aos princípios acima.
- **Complexidade**: qualquer desvio DEVE ser justificado e registrado.
- **Guia operacional**: `AGENTS.md` e regras `.mdc` complementam esta constituição.

**Version**: 1.1.1 | **Ratified**: 2026-05-17 | **Last Amended**: 2026-05-29
