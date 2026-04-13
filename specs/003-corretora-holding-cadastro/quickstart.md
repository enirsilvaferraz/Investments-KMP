# Quickstart de implementação: Corretora e posição no cadastro

**Feature**: `003-corretora-holding-cadastro`  
**Data**: 2026-04-13

## Ordem sugerida

1. **Dados** — `BrokerageDataSource.getById` + `BrokerageRepository.getById` + `BrokerageRepositoryImpl`.
2. **Transação** — implementar persistência atómica ativo + `AssetHolding` no módulo `:data:database` (e *wiring* Koin em `:data:repositories` ou módulo que já publica os *datasources*).
3. **Domínio** — estender `UpsertInvestmentAssetUseCase.Param` com `brokerageId`; validar corretora e owner; substituir `assetRepository.upsert` isolado pela operação transaccional; actualizar KDoc.
4. **Testes** — `:domain:usecases:jvmTest` para novos cenários (sucesso, corretora inexistente, owner null, falha simulada na segunda escrita se testável com *fake*).
5. **UI** — `AssetDraft`, `AssetManagementViewModel`, `baseForm` / `AssetManagementFormView`, `AssetManagementUpsertParam.kt`, mensagens e estado vazio.
6. **Documentação** — `DOMAIN.md` se o fluxo do diálogo não estiver descrito; `compileKotlinJvm` nos módulos tocados.

## Comandos Gradle úteis

```bash
./gradlew :domain:usecases:jvmTest
./gradlew :features:asset-management:compileKotlinJvm
./gradlew :data:database:compileKotlinJvm
./gradlew :data:repositories:compileKotlinJvm
```

## Pós-condição

- Dropdown de corretora obrigatório com dados reais do catálogo.
- **Salvar** cria ativo **e** `AssetHolding` de forma atómica (**RF-007**).
- Nenhum estado final “só ativo” ou “só posição” após falha percebida (**CS-005**).
