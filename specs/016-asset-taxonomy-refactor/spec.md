# Feature Specification: Reestruturação da taxonomia de ativos

**Feature Branch**: `016-asset-taxonomy-refactor`

**Created**: 2026-06-02

**Status**: Draft

**Input**: User description: "Vamos fazer mudanças estruturais nos assets. InvestmentCategory vai se chamar AssetClass. FixedIncomeAssetType sera um indexador (alterar tbm no banco de dados). Vamos criar um AssetType que sera uma interface em comum de Asset. FixedIncomeSubType, VariableIncomeAssetType e InvestmentFundAssetType devem herdar dessa interface. (Alterar no banco de dados tbm). Mapear alteração no core/domain/entity/docs/DOMAIN.md"

## Clarifications

### Session 2026-06-02

- Q: Renomear colunas no SQLite na migração ou manter nomes legados com mapeamento Kotlin? → A: Renomear colunas SQL (`fixed_income_assets.type` → `indexer`, `subType` → `type`; `assets.category` → `asset_class`; valores enum inalterados).
- Q: Nome do enum Kotlin do indexador de renda fixa? → A: `YieldIndexer` (substitui `FixedIncomeAssetType`; membros POST_FIXED, PRE_FIXED, INFLATION_LINKED inalterados).
- Q: Nome do enum de tipo de produto em renda fixa? → A: Renomear `FixedIncomeSubType` para **`FixedIncomeAssetType`** (CDB, LCI, …); propriedade `type: FixedIncomeAssetType` em `FixedIncomeAsset`.
- Q: Renomear `category` em `asset_transactions` na mesma migração? → A: Sim — `asset_transactions.category` → `asset_class` na migração 6→7, em conjunto com `assets`.
- Q: Forma do contrato `AssetType`? → A: Interface **marcadora** sem membros; rótulos de UI permanecem em `:presentation:naming`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Vocabulário de domínio alinhado à prática de investimentos (Priority: P1)

O investidor e as telas do aplicativo passam a distinguir três conceitos de forma consistente em toda a carteira:

1. **Classe de ativo** — renda fixa, renda variável ou fundo de investimento (antes chamada internamente de “categoria de investimento”).
2. **Tipo de produto** — o instrumento concreto dentro da classe (ex.: CDB, ação nacional, fundo multimercado), modelado por um contrato comum aplicável às três classes.
3. **Indexador** (somente renda fixa) — como a rentabilidade é atrelada (pré-fixado, pós-fixado, inflação), separado do tipo de produto (ex.: CDB).

Cadastros, listagens, filtros e histórico continuam permitindo as mesmas operações de hoje, mas rótulos e agrupamentos refletem essa separação sem ambiguidade entre “tipo de renda fixa” (indexador) e “subtipo do título” (produto).

**Why this priority**: Corrige confusão semântica na base do produto; todas as demais camadas dependem dessa distinção.

**Independent Test**: Cadastrar um CDB pós-fixado e um ETF; verificar que a classe, o tipo de produto e o indexador (apenas no RF) aparecem corretamente no formulário e persistem após reabrir o cadastro.

**Acceptance Scenarios**:

1. **Given** o usuário cadastra um ativo de **renda fixa**, **When** escolhe indexador (ex.: pós-fixado) e tipo de produto (ex.: CDB) e salva, **Then** ao reabrir o cadastro os dois atributos permanecem distintos e corretos.
2. **Given** o usuário cadastra **renda variável** ou **fundo**, **When** escolhe o tipo de produto (ex.: ação nacional, fundo de previdência), **Then** não há campo de indexador e o tipo de produto é o discriminador principal do instrumento.
3. **Given** o usuário consulta carteira ou histórico filtrado por classe de ativo, **When** aplica filtro de renda fixa, **Then** apenas ativos da classe renda fixa aparecem, com o mesmo comportamento funcional de antes da mudança de nomenclatura.

---

### User Story 2 - Dados existentes preservados após atualização (Priority: P1)

Usuários com carteira já populada atualizam o aplicativo. Todos os ativos, posições, transações e histórico mensal permanecem acessíveis; valores de classe, indexador (antigo “tipo” de RF) e tipo de produto (antigo subtipo/tipo por classe) são **mapeados** para o novo modelo sem perda nem duplicação de registros.

**Why this priority**: Refatoração estrutural só é aceitável se a migração for transparente para quem já investe no app.

**Independent Test**: Base de demonstração com ativos RF (pré/pós/inflação + vários subtipos), RV e fundos antes da atualização; após migração, contagem e atributos de negócio equivalentes (emissor, rentabilidade, ticker, liquidez, etc.) intactos.

**Acceptance Scenarios**:

1. **Given** existem ativos cadastrados **antes** da atualização em cada classe, **When** o usuário abre a nova versão, **Then** a quantidade de ativos e holdings é a mesma e cada ativo exibe classe, indexador (RF) e tipo de produto coerentes com os valores anteriores.
2. **Given** um ativo de renda fixa com indexador pós-fixado e subtipo CDB legado (`FixedIncomeSubType`), **When** a migração é aplicada, **Then** o indexador continua pós-fixado (`YieldIndexer`) e o tipo de produto continua CDB (`FixedIncomeAssetType`).
3. **Given** transações e snapshots de histórico vinculados a holdings, **When** a migração conclui, **Then** consultas de histórico e saldo por período retornam os mesmos totais e quantidades que antes da atualização.

---

### User Story 3 - Documentação canônica do domínio atualizada (Priority: P2)

Mantenedores e revisores de código consultam o documento canônico do módulo de entidades (`DOMAIN.md`) e encontram a nova taxonomia: **AssetClass**, contrato **AssetType**, **`YieldIndexer`** e **`FixedIncomeAssetType`** (produto), diagramas ER e tabelas de pacotes sem referência obsoeta a `InvestmentCategory`, `FixedIncomeSubType` nem uso de indexador como “tipo do título”.

**Why this priority**: Garante que decisões futuras (filtros, import B3, relatórios) usem o mesmo vocabulário que o código e o banco.

**Independent Test**: Revisar `DOMAIN.md` após a entrega; todos os enums e relacionamentos da §6.5 e §9.1 refletem AssetClass, indexador RF e AssetType.

**Acceptance Scenarios**:

1. **Given** um desenvolvedor abre `DOMAIN.md`, **When** lê o pacote `assets` e o diagrama ER, **Then** vê **AssetClass** no lugar de categoria de investimento e indexador separado do tipo de produto em `FixedIncomeAsset`.
2. **Given** a lista de enums na §6.5, **When** comparada ao código-fonte, **Then** inclui o contrato **AssetType** e os três enums de produto que o implementam, sem listar o antigo nome como tipo de RF.

---

### Edge Cases

- **Valores de enum inalterados**: POST_FIXED, PRE_FIXED, INFLATION_LINKED e demais constantes de produto mantêm os mesmos identificadores persistidos; apenas papéis e nomes de conceito mudam (assumido até decisão contrária no plano).
- **Consultas por classe**: Filtros e repositórios que hoje usam “categoria” passam a usar “classe de ativo” com o mesmo conjunto de três valores (renda fixa, renda variável, fundo).
- **Importações e integrações externas** (ex.: B3): Mapeamentos que liam o antigo campo “tipo” de RF como indexador devem ser atualizados; dados já importados migram com a base local.
- **Ativos órfãos ou inconsistência pré-migração**: Migração falha de forma controlada com mensagem diagnóstica; não grava estado parcial que impeça rollback (detalhe de implementação no plano).
- **UI e relatórios legados**: Textos visíveis ao usuário devem preferir “Indexador” / “Tipo” em RF e “Tipo” em RV/fundo, evitando rótulo “Tipo” para o indexador em renda fixa.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE modelar a discriminação de alto nível entre renda fixa, renda variável e fundo de investimento como **classe de ativo** (`AssetClass`), substituindo o conceito anterior de categoria de investimento em entidades, persistência e APIs internas de domínio.
- **FR-002**: O sistema DEVE representar em ativos de **renda fixa** o **indexador** de rentabilidade via enum **`YieldIndexer`** (pré-fixado, pós-fixado, atrelado à inflação), persistido na coluna `indexer`, distinto do **tipo de produto** (ex.: CDB, LCI) na coluna `type`, sem reutilizar o mesmo campo ou enum para ambos.
- **FR-003**: O sistema DEVE introduzir contrato **`AssetType` marcadora** (sem propriedades), implementada pelos enums de produto (`FixedIncomeAssetType`, `VariableIncomeAssetType`, `InvestmentFundAssetType`), para tratamento polimórfico onde a classe exige “tipo de produto” genérico. Rótulos visíveis ao usuário NÃO fazem parte de `AssetType` (permanecem em naming/UI). O enum legado `FixedIncomeSubType` DEVE ser eliminado após renomeação/migração de referências.
- **FR-004**: O sistema DEVE persistir a nova taxonomia no armazenamento local com **renomeação física de colunas** na migração 6→7: `assets.category` → `asset_class`; `asset_transactions.category` → `asset_class`; em `fixed_income_assets`, `type` → `indexer` e `subType` → `type` (tipo de produto). Valores de discriminador em ativos e transações DEVEM usar o mesmo vocabulário (`FIXED_INCOME`, `VARIABLE_INCOME`, `INVESTMENT_FUND`); registos legados com `FUNDS` em transações DEVEM ser migrados para `INVESTMENT_FUND` na mesma migração 6→7. A migração DEVE preservar todos os registros existentes.
- **FR-005**: O sistema DEVE atualizar todas as camadas consumidoras do modelo de ativos (cadastro, repositórios, casos de uso, histórico, filtros de carteira, testes de domínio) para compilar e comportar-se de forma equivalente à versão anterior quanto a regras de negócio já estabelecidas.
- **FR-006**: O documento **`DOMAIN.md`** DEVE ser a fonte canônica atualizada: mapa de pacotes, invariantes, §6.5 (enums), diagrama ER §9.1 e notas de manutenção refletindo AssetClass, AssetType, indexador e remoção de vocabulário obsoleto.
- **FR-007**: Cadastros e edição de ativos DEVE exibir indexador apenas para renda fixa e tipo de produto conforme a classe selecionada, sem regressão de validações já existentes (emissor, vencimento, liquidez, identificador B3 opcional em RF, etc.).

### Key Entities

- **AssetClass**: Classe de ativo (renda fixa, renda variável, fundo de investimento); substitui **InvestmentCategory** no papel de discriminador de `Asset`.
- **AssetType** (contrato marcadora): Tipo de produto dentro de uma classe; implementada pelos três enums de produto; sem membros na interface.
- **YieldIndexer**: Indexador de rentabilidade em renda fixa (POST_FIXED, PRE_FIXED, INFLATION_LINKED); substitui o enum e ficheiro `FixedIncomeAssetType`.
- **FixedIncomeAssetType** (produto RF): Instrumentos CDB, LCI, etc. (valores do antigo `FixedIncomeSubType`); implementa **AssetType**. Não confundir com o antigo `FixedIncomeAssetType` indexador — esse papel é **`YieldIndexer`**.
- **VariableIncomeAssetType**, **InvestmentFundAssetType**: Tipos de produto nas outras classes; implementam **AssetType**.
- **Asset** (sealed): Mantém emissor e observações; passa a expor **classe de ativo** (`AssetClass`) em vez de categoria; `FixedIncomeAsset` expõe `indexer: YieldIndexer` e `type: FixedIncomeAssetType`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Após migração em base com pelo menos um ativo por classe, **100%** dos ativos legados permanecem listáveis com classe, indexador (RF) e tipo de produto semanticamente equivalentes aos valores pré-atualização (verificável por amostragem ou suite de regressão de domínio).
- **SC-002**: Em teste de fumaça de cadastro, um usuário consegue criar e reabrir um ativo de cada classe em **menos de 5 minutos** sem erro de validação atribuível à mudança de taxonomia.
- **SC-003**: Consultas de histórico e totais de posição para um período fixo antes e depois da migração produzem **zero divergência** em valor investido e quantidade para a mesma carteira de teste.
- **SC-004**: Revisão de `DOMAIN.md` não encontra referência ativa a `InvestmentCategory` nem `FixedIncomeSubType`; indexador RF = **`YieldIndexer`**, produto RF = **`FixedIncomeAssetType`**; diagrama ER e §6.5 alinhados ao código entregue.

## Assumptions

- Os **valores** dos enums (constantes) permanecem os mesmos na migração; mudam nomes de tipos, papéis e colunas persistidas, não o significado de negócio de cada constante.
- **AssetClass** conserva os três valores atuais (FIXED_INCOME, VARIABLE_INCOME, INVESTMENT_FUND), apenas renomeando o tipo.
- Migração Room **6 → 7** com `AutoMigrationSpec` manual se necessário para `RENAME COLUMN` em `assets`, `asset_transactions` e `fixed_income_assets` na mesma versão de schema.
- O enum **`YieldIndexer`** absorve os membros de indexador do antigo `FixedIncomeAssetType` (POST_FIXED, PRE_FIXED, INFLATION_LINKED). O enum **`FixedIncomeAssetType`** passa a designar o **produto** RF (membros do antigo `FixedIncomeSubType`); ficheiro `FixedIncomeSubType.kt` removido após migração de referências.
- Escopo inclui **módulo entity**, **persistência (Room/migrações)**, **repositórios**, **casos de uso**, **UI de gestão de ativos** e **testes** que referenciam os tipos antigos; fora de escopo: novos tipos de produto ou indexadores não solicitados.
- Não há alteração de regras de cálculo de posição, metas ou import B3 além do necessário para compilar e mapear os novos nomes de campos.
- **`AssetType`** não centraliza strings de exibição; evita duplicar `FieldLabels` / recursos de naming.

## Dependencies

- Modelo atual documentado em `core/domain/entity/docs/DOMAIN.md` e implementado em `core/domain/entity`.
- Esquema Room em `core/data/database` com tabelas `assets`, `fixed_income_assets`, `variable_income_assets`, `investment_fund_assets`, `asset_transactions`; após migração, discriminador `asset_class` em `assets` e `asset_transactions` (substitui `category`) e colunas `indexer` / `type` em `fixed_income_assets`.
- Features que consomem `InvestmentCategory` ou `FixedIncomeAssetType` (histórico, filtros de carteira 014/015, gestão de ativos, import B3) devem migrar para `AssetClass` e `YieldIndexer` na implementação.
