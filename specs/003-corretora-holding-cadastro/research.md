# Pesquisa e decisões: Corretora obrigatória e posição no cadastro

**Feature**: `003-corretora-holding-cadastro`  
**Data**: 2026-04-13  
**Relacionado**: [spec.md](./spec.md), [plan.md](./plan.md), [spec 001](../001-cadastro-investimento-dialog/spec.md)

## Decisões

### 1. Persistência atómica (investimento + `AssetHolding`)

**Decisão:** Garantir **RF-007** / **CS-005** com uma **única transação Room** que executa, em sequência dentro de `withTransaction` no `AppDatabase`: (1) persistência do `Asset` (mesma lógica que `AssetDao.save` / mapeamento já usado por `AssetDataSourceImpl`); (2) `upsert` de `AssetHoldingEntity` com `assetId` gerado, `brokerageId` escolhido, `ownerId` do titular por omissão, `goalId = null`.

**Racional:** Duas chamadas independentes (`AssetRepository.upsert` + `AssetHoldingRepository.upsert`) sem transação violam o “tudo ou nada” se a segunda falhar. Rollback manual por `delete(assetId)` é mais frágil e duplica regras.

**Alternativas consideradas:** Apenas estender `UpsertInvestmentAssetUseCase` com segunda chamada ao repositório — **recusada** pelo risco de estado parcial; compensação por apagar o ativo — **recusada** (dupla escrita e falhas em cascata).

**Local sugerido no código:** Novo componente no módulo **`:data:database`** (ex. *coordinator* ou extensão de `AssetDataSource`) com acesso a `AppDatabase` + DAOs necessários, exposto ao domínio via interface em **`:domain:usecases`** (ex. método único no porto de persistência ou novo porto estreito `RegisterInvestmentAssetGateway`) implementado em **`:data:repositories`**.

---

### 2. Corretora por **id** (catálogo), alinhado ao emissor

**Decisão:** O formulário guarda **`brokerageId: Long?`** (obrigatório para **Salvar**); o domínio valida `brokerageId > 0` e resolve `Brokerage` via **`BrokerageRepository.getById`** — método **novo** na interface `BrokerageRepository`, com implementação em `BrokerageRepositoryImpl` delegando em **`BrokerageDataSource.getById`** (também novo), usando **`BrokerageDao.getById`** já existente.

**Racional:** Lista do dropdown deve refletir o catálogo; seleção por id evita ambiguidade de homónimos e coincide com o padrão `issuerId` do mesmo diálogo.

**Alternativa recusada:** Só `getByName` — já existe no porto, mas é frágil para UI por id e menos eficiente.

---

### 3. Titular (`Owner`) da posição

**Decisão:** Reutilizar **`OwnerRepository.getFirst()`**, igual ao fluxo de `SaveAssetUseCase` ao criar `AssetHolding`. Se `getFirst()` for `null`, o caso de uso **DEVE** falhar com **`ValidateException`** (mapa de erros, chave estável, ex. `owner` / `brokerage`) e mensagem compreensível — **sem** persistir ativo nem posição (a transação nem arranca ou falha antes do commit).

**Racional:** `AssetHolding` exige `ownerId` na BD; a spec não introduz escolha de titular no diálogo; comportamento alinhado ao legado, mas **explícito** para não “silenciar” a criação da posição.

---

### 4. Âmbito do `UpsertInvestmentAssetUseCase`

**Decisão:** **Estender** `UpsertInvestmentAssetUseCase` (e `Param` selado) com **`brokerageId: Long`** em todas as variantes (`FixedIncomeRegistration`, `VariableIncomeRegistration`, `InvestmentFundRegistration`). Após validações actuais do ativo + nova validação da corretora e do owner, delegar na operação **transaccional** ativo+holding em vez de só `assetRepository.upsert`.

**Racional:** Mantém uma única entrada de negócio para o diálogo; concentra invariantes e testes `jvmTest` no mesmo caso de uso (princípio V). O KDoc actual que diz que **não** cria `AssetHolding` **deve** ser actualizado.

**Alternativa recusada:** Novo caso de uso paralelo só para holding — duplicaria validação e parâmetros.

---

### 5. UI (`:features:asset-management`)

**Decisão:** Carregar corretoras com **`GetBrokeragesUseCase`** (já existente) no `AssetManagementViewModel` (paralelo a `GetIssuersUseCase`). Acrescentar ao **`AssetDraft`** o campo `brokerageId: Long?`; dropdown no mesmo padrão visual dos restantes (grelha / `baseForm`); validação de UI: corretora obrigatória; estado inicial **sem** selecção (**RF-003**). Catálogo vazio: mensagem análoga à de emissores vazios, sem **Salvar** concluído.

**Racional:** Reutiliza stack MVI e padrões do módulo (`001`).

---

### 6. Documentação de domínio

**Decisão:** Após implementação, rever **`core/domain/entity/docs/DOMAIN.md`** — se ainda não estiver explícito que o **cadastro via diálogo** cria **`AssetHolding`** inicial (só ligação ativo–titular–corretora, meta opcional nula), acrescentar uma linha na secção de fluxos / invariantes (princípio IX).

---

## Itens verificados no código existente

- `UpsertInvestmentAssetUseCase` — KDoc afirma explicitamente que **não** cria `AssetHolding` (há que inverter após a feature).
- `SaveAssetUseCase` — já cria/atualiza `AssetHolding` com `brokerage` + `ownerRepository.getFirst()` (referência de comportamento, não de chamada directa pelo novo diálogo).
- Entidades Room: `AssetHoldingEntity` com FKs `assetId`, `ownerId`, `brokerageId`.
- `GetBrokeragesUseCase` + `BrokerageRepository.getAll()` disponíveis para a lista.
