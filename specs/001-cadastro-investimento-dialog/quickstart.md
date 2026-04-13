# Quickstart: Diálogo de cadastro de investimento

**Feature**: `001-cadastro-investimento-dialog`  
**Data**: 2026-04-09

## Pré-requisitos

- JDK e Gradle conforme o `build-logic` do repositório.
- Branch: `001-cadastro-investimento-dialog` (ou trabalhar a partir de `main` com a spec sincronizada).

## Onde está o código

| Camada       | Caminho                                                                                  |
|--------------|------------------------------------------------------------------------------------------|
| Feature UI   | `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/` |
| Casos de uso | `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/`                       |
| Repositórios | `core/data/repositories/`, `core/domain/usecases/.../repositories/`                      |
| Entidades    | `core/domain/entity/`                                                                    |

**Pré-visualizações (`@Preview`):** no mesmo ficheiro que o composable ou formulário (ex.: previews do formulário em `AssetManagementFormView.kt`), conforme `.specify/memory/constitution.md` (princípio VI) e `.cursorrules`.

## Módulos Gradle relevantes

- `:features:asset-management` — UI do diálogo.
- `:domain:usecases` — `UpsertInvestmentAssetUseCase`, `GetIssuersUseCase`.
- `:domain:entity` — tipos de ativo e `Issuer`.
- `:features:composeApp` — shell, rotas; já referencia `AssetManagementScreen` em `App.kt`.

## Comandos de verificação

```bash
cd /Users/enirferraz/AndroidStudioProjects/Investments-KMP

./gradlew :features:asset-management:compileKotlinJvm
./gradlew :domain:usecases:compileKotlinJvm
./gradlew :domain:usecases:jvmTest
```

Após alterar `IssuerRepository` / dados:

```bash
./gradlew :data:repositories:compileKotlinJvm
```

## Integração Koin

1. Registar **`AssetManagementViewModel`** com `@KoinViewModel` (ou módulo equivalente ao restante do repo).
2. Registar **`UpsertInvestmentAssetUseCase`** com `@Factory` (ou padrão do projecto).
3. Garantir que **`GetIssuersUseCase`** já está no módulo de DI (existente).

## Abrir o diálogo na app

- A rota `AssetManagementRouting` em `App.kt` já usa `DialogSceneStrategy.dialog()` e `AssetManagementScreen()`.
- Ao implementar, passar `onDismiss`/`Back` para cumprir **Cancelar**/**X** e fluxo de confirmação.

## Ordem sugerida de implementação

1. `IssuerRepository.getById` + implementações.
2. `UpsertInvestmentAssetUseCase` + testes `jvmTest`.
3. `AssetManagementViewModel` + `GetIssuersUseCase` + novo use case.
4. `AssetManagementFormUi` / `FormView` / `Screen` / `Contract`.
5. Ajustes de `DOMAIN.md` se o modelo de formulário alterar invariantes documentadas.
