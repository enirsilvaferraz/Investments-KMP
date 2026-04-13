# Modelo de dados: Diálogo de cadastro de investimento

**Feature**: `001-cadastro-investimento-dialog`  
**Data**: 2026-04-09  
**Fonte de verdade do domínio**: `core/domain/entity/docs/DOMAIN.md`

## 1. Enumerações e tipos já existentes

| Conceito       | Tipo no código                                                                                            |
|----------------|-----------------------------------------------------------------------------------------------------------|
| Categoria      | `InvestmentCategory`: `FIXED_INCOME`, `VARIABLE_INCOME`, `INVESTMENT_FUND`                                |
| Emissor        | `Issuer` (`id`, `name`, `isInLiquidation`)                                                                |
| Renda fixa     | `FixedIncomeAsset`, `FixedIncomeAssetType`, `FixedIncomeSubType`, `Liquidity`                             |
| Renda variável | `VariableIncomeAsset`, `VariableIncomeAssetType`, `CNPJ` opcional                                         |
| Fundo          | `InvestmentFundAsset`, `InvestmentFundAssetType`, `Liquidity`, `liquidityDays`, `expirationDate` opcional |

## 2. Estado de UI (MVI)

### 2.1 `UiState` (sugestão de forma)

- **Carregamento inicial** de emissores: `Loading` | `Ready` | `Error`.
- Em **`Ready`**:
  - Lista de emissores para o dropdown (`List<Issuer>` ou lista de pares `id` + rótulo).
  - **Categoria** seleccionada (default `FIXED_INCOME`).
  - **Campos** por categoria (ver secção 3).
  - **Erros** por chave de campo (`Map<String, String>` ou estrutura tipada).
  - **Flags:** `isSaving`, `saveError` (mensagem para falha de sistema — **RF-015**), `showDiscardConfirmation`.
  - **Snapshot inicial** para comparar “alterado” (**RF-013**/**RF-014**): igual ao estado após reset (categoria renda fixa + campos vazios + sem emissor).

### 2.2 `Intent` (exemplos)

- `InitialLoad`, `RetryLoadIssuers`
- `CategoryChanged(InvestmentCategory)`
- `FieldChanged(fieldKey, value)` ou intents específicos por campo para reduzir erros
- `IssuerSelected(issuerId: Long?)`
- `Save`, `DismissRequest` (Cancelar ou X), `ConfirmDiscard`, `CancelDiscard`

## 3. Campos por categoria (mapeamento RF ↔ domínio)

### Renda fixa (`RF-005`)

| Campo UI                        | Domínio / notas                   |
|---------------------------------|-----------------------------------|
| Tipo de cálculo                 | `FixedIncomeAssetType`            |
| Subtipo                         | `FixedIncomeSubType`              |
| Data de vencimento              | `FixedIncomeAsset.expirationDate` |
| Rentabilidade contratada        | `contractedYield`                 |
| Rentabilidade vs CDI (opcional) | `cdiRelativeYield`                |
| Liquidez                        | `Liquidity`                       |
| Emissor                         | `issuerId` → resolve `Issuer`     |
| Observações                     | `observations`                    |

### Renda variável (`RF-006`)

| Campo UI        | Domínio / notas                                             |
|-----------------|-------------------------------------------------------------|
| Nome do ativo   | `VariableIncomeAsset.name`                                  |
| Tipo            | `VariableIncomeAssetType`                                   |
| Ticker          | `ticker`                                                    |
| CNPJ (opcional) | `CNPJ` — normalizar máscara na validação de UI              |
| Emissor         | `issuerId`                                                  |
| Observações     | `observations`                                              |
| Liquidez        | **Não** editável; fixo no domínio (`D_PLUS_DAYS`, dias `2`) |

### Fundo (`RF-007`)

| Campo UI                                | Domínio / notas                                                                            |
|-----------------------------------------|--------------------------------------------------------------------------------------------|
| Nome do fundo                           | `InvestmentFundAsset.name`                                                                 |
| Categoria do fundo                      | `InvestmentFundAssetType`                                                                  |
| Regra de liquidez                       | `Liquidity` (hoje `SaveAssetUseCase` usa valor fixo; alinhar entidade + formulário à spec) |
| Dias para resgate                       | `liquidityDays`                                                                            |
| Data encerramento/vencimento (opcional) | `expirationDate`                                                                           |
| Emissor                                 | `issuerId`                                                                                 |
| Observações                             | `observations`                                                                             |

## 4. Entrada do caso de uso `UpsertInvestmentAssetUseCase`

- **`issuerId: Long`** (obrigatório para sucesso; se inválido → erro de validação).
- **Dados específicos** por categoria (sealed ou `AssetFormData` estendido com `issuerId` em vez de `issuerName` só para este fluxo — decisão técnica na implementação; ver [plan.md](./plan.md)).

Regras de negócio (datas futuras, valores positivos, emissor existente) devem estar no **use case** e ser cobertas por testes em **`:domain:usecases`**.

## 5. Transições relevantes

- Mudança de **categoria**: descartar campos específicos da categoria anterior do **payload** de gravação; **manter** emissor seleccionado e observações (**história P3**).
- **Fechar** com alterações: pedir confirmação; **sem** alterações: fechar sem diálogo extra.

## 6. Alterações em repositórios (domínio/dados)

- `IssuerRepository` + `IssuerDataSource`: expor **`getById(id: Long): Issuer?`** (DAO já tem `getById`).
