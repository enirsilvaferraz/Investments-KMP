# Quickstart: Identificador B3 em Renda Fixa

**Feature**: `004-fixed-income-b3-id` | **Date**: 2026-05-24

---

## Pré-requisitos

- JDK 17+
- Base local com ativos de renda fixa (opcional, para testar migração v5→v6)
- `./gradlew` funcional na raiz do monorepo

---

## Verificação de build (após implementação)

```bash
./gradlew :domain:entity:compileKotlinJvm
# Revisar diff de core/domain/entity/docs/DOMAIN.md (b3Identifier em FixedIncomeAsset)
./gradlew :data:database:compileKotlinJvm
./gradlew :domain:usecases:compileKotlinJvm
./gradlew :domain:usecases:jvmTest
./gradlew :presentation:asset-management:compileKotlinJvm
./gradlew :features:composeApp:compileKotlinJvm
./gradlew :apps:desktopApp:run
```

> Confirmar que `core/data/database/schemas/com.eferraz.database.core.AppDatabase/6.json` foi gerado e commitado.

---

## Teste manual — migração

1. (Opcional) Instalar/usar build **anterior** com DB v5 e criar 1+ ativos de renda fixa.
2. Instalar build com esta feature.
3. Abrir app — **sem** crash de migração.
4. Abrir cadastro de RF legado → campo **Identificador B3** vazio.
5. Salvar sem preencher → continua vazio.

---

## Teste manual — cadastro

1. Desktop: `./gradlew :apps:desktopApp:run`
2. Cadastrar ou editar ativo **Renda fixa**.
3. Preencher **Identificador B3** com texto livre (ex.: `  CDB-2024/001  `).
4. Salvar e reabrir → valor sem espaços às bordas (`CDB-2024/001`).
5. Apagar campo, salvar → vazio ao reabrir.
6. Abrir cadastro de **Renda variável** → campo **não** aparece.

---

## Teste manual — histórico

1. Ir a **Posicionamento no Período**.
2. Período com linhas **mistas** (RF + RV + fundo, se existirem).

| Linha | Coluna mais à direita |
|-------|------------------------|
| Renda fixa com identificador | Ícone azul (info); tooltip com o valor |
| Renda fixa sem identificador | Ícone amarelo (warning); tooltip “Identificador B3 não informado.” |
| Renda variável | **Célula vazia** (sem ícone) |
| Fundo | **Célula vazia** (sem ícone) |

3. Editar RF no cadastro, adicionar identificador, salvar.
4. Voltar ao histórico → linha RF passa a ícone azul com valor atualizado.

---

## Testes automatizados

```bash
./gradlew :domain:usecases:jvmTest --tests '*UpsertAsset*'
```

Validar caso RF com `b3Identifier` persistido no mock do repositório.

---

## Rollback

Se migração falhar em desenvolvimento: apagar DB local da app Desktop (ficheiro SQLite do utilizador) ou reverter `version` apenas em ambiente de dev — **não** usar destructive migration em produção.
