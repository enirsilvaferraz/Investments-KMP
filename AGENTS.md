# AGENTS.md — Investments-KMP

Instruções para agentes de código neste repositório. Complementa o [README.md](README.md) com convenções, build e testes.

## Visão geral do projeto

- **Stack:** Kotlin Multiplatform (KMP) com Compose Multiplatform — alvos Android, iOS e Desktop (JVM).
- **Organização:** monorepo em camadas (`:apps`, `:features`, `:domain`, `:data`) com plugins `foundation.*` em `build-logic/`.
- **Pacote base:** `com.eferraz.<nome-do-módulo>`.
- **Idioma da documentação:** português do Brasil (pt-BR) em specs, planos e docs de projeto. Exceções: identificadores de código, APIs externas e termos técnicos estáveis em inglês.

### Mapa de módulos (Gradle)

Listar projetos: `./gradlew projects`. Usar **sempre** o identificador Gradle (não o caminho no disco).

| Gradle                                                                     | Caminho no repositório               |
|----------------------------------------------------------------------------|--------------------------------------|
| `:domain:entity`                                                           | `core/domain/entity`                 |
| `:domain:usecases`                                                         | `core/domain/usecases`               |
| `:features:composeApp`                                                     | `core/presentation/composeApp`       |
| `:features:design-system`                                                  | `core/presentation/design-system`    |
| `:features:asset-management`                                               | `core/presentation/asset-management` |
| `:data:database`, `:data:network`, `:data:repositories`, `:data:filestore` | `core/data/…`                        |
| `:apps:umbrellaApp`, `:apps:androidApp`, `:apps:desktopApp`                | `core/apps/…`                        |

## Comandos de build

Após alterar código Kotlin (`.kt`, `.kts`) ou Gradle, **executar** compilação JVM do módulo tocado e **informar** se o build passou.

```bash
./gradlew :<módulo>:compileKotlinJvm
```

- Compilar **apenas** JVM — não usar `:module:assemble` nem `:module:jvmJar` para verificação rápida.
- **Não** rodar build automático ao editar só documentação (`.md`, etc.).
- Exemplos: `./gradlew :domain:entity:compileKotlinJvm`, `./gradlew :features:asset-management:compileKotlinJvm`.

## Instruções de teste

- Testes unitários em `*Test.kt` seguem `~/.cursor/rules/test-patterns.mdc` (constituição do projeto, princípio V).
- **Texto dos testes em inglês:** nomes `GIVEN_WHEN_THEN`, KDoc `/** … */`, comentários `// GIVEN`, `// WHEN`, `// THEN` (linha em branco antes de cada um quando aplicável).
- **MockK** para colaboradores externos (ports, repositórios, clients).
- Preferir dados de teste **no próprio método**; evitar factories centralizadas (`TestDataFactory`) em código novo.
- Alterações executáveis em `core/domain/usecases/` **devem** incluir ou atualizar testes em `:domain:usecases` e validar com:

```bash
./gradlew :domain:usecases:jvmTest
```

## Estilo de código Kotlin

### `explicitApi`

Em módulos com `explicitApi()`, seguir `~/.cursor/rules/explicit-api.mdc`:

- Modificador de visibilidade explícito em declarações ao nível do ficheiro.
- Priorizar a visibilidade mais restrita: `private` → `internal` → `public` só quando outro módulo importar.

### ViewModels

Estado com `StateFlow` e backing field explícito (`field = MutableStateFlow`); ver skill/padrão `kmp-viewmodel-stateflow-pattern` quando aplicável.

### Compose `@Preview`

- Previews **no mesmo ficheiro** do composable — não criar `*Previews.kt` dedicados.
- Visibilidade `internal` nas funções de preview quando a análise estática do projeto exigir superfície pública mínima.

## Módulos Gradle KMP (novos ou alterados)

Ao criar ou alterar `build.gradle.kts` de subprojeto ou entradas em `settings.gradle.kts`:

1. Seguir `~/.cursor/rules/kmp-module-patterns.mdc` (princípios II e IX da constituição).
2. Usar plugins `foundation.*` de `build-logic`; respeitar grafo de dependências entre camadas.
3. **Nome do subprojeto:** se o utilizador não indicou o nome, **perguntar** antes de gerar ficheiros.
4. **Features UI Compose** (exceto `design-system`): criar `*Contract.kt` em `commonMain` com `*Screen(modifier: Modifier)` e, no mesmo ciclo, `implementation(projects.features.<accessorCamelCase>)` em `core/apps/umbrellaApp/build.gradle.kts`.
5. Skills globais `kmp-module-*` (`kmp-modules-index` em `~/.cursor/skills/`) são guia operacional — em conflito, prevalece o `.mdc`.

Mudanças permanentes de padrão **devem** atualizar o `.mdc` correspondente e a constituição em `.specify/` quando aplicável.

## Documentação de domínio

Sempre que alterar ficheiros em `core/domain/` (`entity`, `usecases`):

1. Verificar se é preciso atualizar `core/domain/entity/docs/DOMAIN.md`, `docs/rules/`, docs de use cases ou outros em `docs/`.
2. Avaliar: novas entidades, propriedades, relacionamentos, casos de uso ou regras de negócio.
3. Atualizar documentação se necessário.
4. Informar o utilizador se houve ou não atualização.

Refactors sem mudança de comportamento **não** exigem atualização de docs, salvo corrigir informação objetivamente falsa.

## Coerência código, documentos e regras de IA

Quando uma alteração mudar domínio, invariantes, convenções de build/testes ou regras deste ficheiro:

- Atualizar no **mesmo PR** (ou PR imediato de seguimento) Markdown aplicável (`DOMAIN.md`, `docs/`, `specs/`), `.specify/` e regras de IA (`.cursorrules`, `.mdc`, comandos personalizados).

## Referências rápidas

| Tópico                    | Onde                                      |
|---------------------------|-------------------------------------------|
| Regras do projeto (fonte) | [`.cursorrules`](.cursorrules)            |
| Padrão AGENTS.md          | [agents.md](https://agents.md/)           |
| Módulos KMP               | `~/.cursor/rules/kmp-module-patterns.mdc` |
| Testes                    | `~/.cursor/rules/test-patterns.mdc`       |
| API explícita Kotlin      | `~/.cursor/rules/explicit-api.mdc`        |
| Modelo de domínio         | `core/domain/entity/docs/DOMAIN.md`       |
