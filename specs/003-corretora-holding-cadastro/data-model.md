# Modelo de dados e contratos de formulário: Corretora e posição

**Feature**: `003-corretora-holding-cadastro`  
**Data**: 2026-04-13  
**Relacionado**: [spec.md](./spec.md), [research.md](./research.md)

## 1. Entidades de domínio envolvidas

| Entidade | Papel nesta feature |
|----------|---------------------|
| `Brokerage` | Opções do dropdown; validação por `id` antes de persistir. |
| `Asset` | Continua a ser construído como hoje em `UpsertInvestmentAssetUseCase` (RF, RV, fundo). |
| `AssetHolding` | **Nova obrigação** no fluxo do diálogo: uma posição inicial por cada cadastro bem-sucedido, com `goal = null`, `asset` = ativo recém-gravado, `brokerage` = seleção, `owner` = `OwnerRepository.getFirst()`. |
| `Owner` | Titular da posição; única fonte `getFirst()` até existir escolha de titular na UI. |

Invariantes já documentadas em **`DOMAIN.md`**: `AssetHolding` liga *quem* / *onde* / *quê*; não armazena quantidade no modelo de holding (transacções / histórico noutras entidades).

## 2. Estado de formulário (UI)

### 2.1 Extensão de `AssetDraft` (`:features:asset-management`)

| Campo | Tipo | Inicial | Regra |
|-------|------|---------|--------|
| `brokerageId` | `Long?` | `null` | Obrigatório para **Salvar**; integra deteção de “formulário alterado” vs estado inicial. |

### 2.2 `withCategoryPreservingIssuerAndObs`

**Decisão:** Ao trocar só a categoria, **preservar** `brokerageId` tal como `issuerId` e `observations` (paridade com história P2 da spec).

## 3. Parâmetros do caso de uso `UpsertInvestmentAssetUseCase.Param`

Cada variante selada **inclui** `brokerageId: Long` (obrigatório no construtor; validação `> 0` e existência no repositório).

Campos existentes (`assetId`, `issuerId`, `observations`, …) **mantêm-se**.

## 4. Validação

| Camada | Regras novas |
|--------|----------------|
| UI | `brokerageId != null`; lista vazia → mensagem + bloqueio análogo a emissores; erros de campo com chave estável (ex. `brokerage`). |
| Domínio | `brokerageId` resolvível via `BrokerageRepository.getById`; `OwnerRepository.getFirst() != null`; regras actuais de ativo inalteradas por categoria. |

## 5. Persistência (transação)

Ordem dentro da transação Room:

1. `Asset` → obter `assetId` (insert/upsert conforme DAO actual).
2. `AssetHoldingEntity(id=0, assetId, ownerId, brokerageId, goalId=null)` → `upsert`.

Em qualquer falha antes do *commit*, nenhum dos dois efeitos deve ser visível fora da transação (**RF-007**).

## 6. Repositórios / portos a alterar (resumo)

| Porto / dados | Alteração |
|---------------|-----------|
| `BrokerageRepository` | `getById(Long): Brokerage?` |
| `BrokerageDataSource` | `getById(Long): Brokerage?` |
| Persistência atómica | Novo método ou *coordinator* transaccional (ver [research.md](./research.md)) consumido pelo caso de uso ou pela implementação do repositório. |

## 7. Koin / DI

- Registar implementação do *coordinator* (se for porto novo).
- `UpsertInvestmentAssetUseCase`: dependências adicionais (`OwnerRepository`, `BrokerageRepository` ampliado, porto transaccional ou equivalente).
