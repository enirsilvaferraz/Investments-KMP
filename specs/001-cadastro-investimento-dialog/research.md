# Pesquisa e decisões: Diálogo de cadastro de investimento

**Feature**: `001-cadastro-investimento-dialog`  
**Data**: 2026-04-09  
**Relacionado**: [spec.md](./spec.md), [plan.md](./plan.md)

## Decisões

### 1. Arquitetura de UI: MVI em camadas (referência Pokedex)

**Decisão:** Adotar **Model–View–Intent (MVI)** no módulo `:features:asset-management`, com ficheiros separados espelhando a referência do projeto Pokedex:

| Papel                                                                                                   | Ficheiro alvo (`com.eferraz.asset_management`) |
|---------------------------------------------------------------------------------------------------------|------------------------------------------------|
| Entrada pública da feature + `koinViewModel`                                                            | `AssetManagementContract.kt`                   |
| `StateFlow` + `Intent` + `dispatch`                                                                     | `AssetManagementViewModel.kt`                  |
| Árvore de composição e `when (UiState)`                                                                 | `AssetManagementScreen.kt`                     |
| Modelos imutáveis `@Immutable` para UI + mapeamento domínio → UI + validação de **formato/presentação** | `AssetManagementFormUi.kt`                     |
| Composables “puros” (campos, secções, diálogo)                                                          | `AssetManagementFormView.kt`                   |

**Racional:** Separa estado/intenção (ViewModel) de renderização (Screen/View) e de modelos de apresentação (FormUi), facilitando testes e pré-visualizações. O padrão já existe em Pokedex (`PokedexScreen` / `PokemonItemListUi` / `PokemonItemListView`).

**Alternativas consideradas:** MVVM só com estado solto (menos explícito para eventos); um único ficheiro gigante (pior para `explicitApi` e revisão).

---

### 2. Ponto de entrada: contrato

**Decisão:** Manter **`AssetManagementContract.kt`** no pacote raiz com **`public fun AssetManagementScreen(modifier: Modifier, …)`** (e parâmetros de navegação/dismiss, conforme `composeApp`), aplicando `modifier` no content root, conforme constitution (princípio II) e skill `kmp-module-feature-ui`.

**Racional:** Outros módulos (`:features:composeApp`) importam apenas o contrato; o restante fica `internal`.

---

### 3. Validação em duas camadas

**Decisão:**

- **UI (`AssetManagementFormUi` / ViewModel):** validação de **formato**, campos obrigatórios visíveis na categoria corrente, mensagens de erro por campo; estado “sujo” vs estado inicial (**RF-013**, **RF-014**); desactivar “Salvar” quando catálogo de emissores vazio (**edge case** da spec).
- **Domínio (`usecases`):** novo caso de uso de **upsert** com invariantes de negócio (datas futuras, valores positivos, emissor **obrigatório** resolvido por ID, **sem** criar emissor — **RF-012**), reutilizando regras alinhadas a `SaveAssetUseCase` onde fizer sentido.

**Racional:** A spec permite mensagens na UI; a constitution (princípio V) exige testes em `usecases` para regras de negócio repetíveis.

**Alternativa recusada:** Confiar só na UI — não gera evidência em `jvmTest` para invariantes críticas.

---

### 4. Novo caso de uso: upsert com emissor por ID

**Decisão:** Introduzir **`UpsertInvestmentAssetUseCase`** (nome exato na implementação) que:

- Recebe dados de formulário alinhados à categoria + **`issuerId: Long`**.
- Resolve **`Issuer`** via `IssuerRepository.getById(id)` (**novo** método na interface e na implementação em `:data:repositories` / `IssuerDataSource`).
- **Não** utiliza `GetOrCreateIssuerUseCase` (evita criação implícita de emissor, em conflito com **RF-012**).
- Persiste com `AssetRepository.upsert` e replica a lógica de `AssetHolding` / metas / corretora apenas se o fluxo do diálogo a exigir (na primeira versão, o plano assume **paridade** com `SaveAssetUseCase` para campos opcionais de corretora/meta **se** continuarem no modelo de formulário; caso contrário, documentar omissão explícita).

**Racional:** `SaveAssetUseCase` actual usa nome + `GetOrCreateIssuerUseCase`, o que viola o requisito de “só catálogo” para este diálogo.

**Alternativa:** Alterar `SaveAssetUseCase` com *flag* — aumenta complexidade e risco de regressão em fluxos existentes.

---

### 5. Listagem de emissores

**Decisão:** Utilizar **`GetIssuersUseCase`** já existente (`cruds`) para carregar o catálogo no `ViewModel` (por exemplo no `Intent` de inicialização ou `init`).

**Racional:** Já existe, está ligado a `IssuerRepository.getAll()`.

---

### 6. Alinhamento a `DOMAIN.md` e entidades

**Decisão:** Mapear campos do formulário para `FixedIncomeAsset`, `VariableIncomeAsset`, `InvestmentFundAsset` e `InvestmentCategory` conforme `core/domain/entity/docs/DOMAIN.md`. Onde o formulário da spec (`RF-006`) exige **nome** e **ticker** distintos para renda variável, estender o modelo de formulário / use case (hoje `SaveAssetUseCase` usa o ticker como `name` em alguns caminhos) para preencher **ambos** os campos da entidade.

**Racional:** Evitar divergência entre UI e modelo de domínio.

---

## Itens resolvidos (antes “NEEDS CLARIFICATION”)

| Tópico            | Resolução                                                                        |
|-------------------|----------------------------------------------------------------------------------|
| Stack UI          | Compose Multiplatform + ViewModel + Koin (já no módulo)                          |
| Persistência      | Via `AssetRepository` / `IssuerRepository` existentes + extensão `getById`       |
| Testes `usecases` | `jvmTest` para o novo caso de uso + eventual extração de validadores partilhados |

---

## Referências internas

- `SaveAssetUseCase` — validação e upsert actuais (referência; não é aderente a **RF-012** para o diálogo).
- `GetIssuersUseCase` — lista de emissores.
- `IssuerDao.getById` — já existe na base; falta expor na cadeia repositório.
