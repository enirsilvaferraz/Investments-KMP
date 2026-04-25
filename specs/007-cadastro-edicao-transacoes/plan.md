# Plano de implementação: Cadastro e edição de transações no diálogo de ativo

**Branch**: `007-cadastro-edicao-transacao` | **Data**: 2026-04-25 | **Spec**: `specs/007-cadastro-edicao-transacoes/spec.md`  
**Entrada**: Especificação da feature em `specs/007-cadastro-edicao-transacoes/spec.md`

**Nota:** Preenchido pelo comando `/speckit.plan`. Fluxo de execução: ver `.specify/templates/plan-template.md`.

**Idioma:** Redigir este documento em **português do Brasil (pt-BR)**, conforme `.specify/memory/constitution.md` princípio VIII.

## Resumo

Adicionar criação de transações vinculadas ao holding e edição inline de todos os campos da tabela dentro do diálogo de gestão de ativo, mantendo alterações em memória durante a sessão e persistindo apenas no salvar final. O fluxo deve rejeitar edição inline inválida (com feedback), descartar rascunho ao cancelar/fechar e exibir lista ordenada por data da transação (mais recente primeiro).

## Contexto técnico

**Linguagem / versão**: Kotlin Multiplatform (Kotlin 2.x do projeto)  
**Dependências principais**: Compose Multiplatform, Material3, Koin, módulos `:domain:entity`, `:domain:usecases`, `:features:design-system`  
**Armazenamento**: persistência final no banco já existente no fluxo de salvar da holding, com staging em memória no diálogo  
**Testes**: validação funcional por cenários da spec + compilação de `:features:asset-management`  
**Plataformas-alvo**: Android e Desktop (apps KMP do projeto)  
**Tipo de projeto**: app KMP multiplataforma em camadas  
**Metas de desempenho**: criação/edição refletida na tabela em até 1 segundo e comportamento fluido durante edição inline  
**Restrições**: sem exclusão de transações nesta versão; sem persistência parcial antes do salvar final; cancelamento descarta rascunho  
**Escala / âmbito**: mudanças concentradas em `AssetManagementFormView`, `AssetManagementFormTransaction` e contexto de edição de transações

## Verificação da constitution (Constitution Check)

*GATE: Deve passar antes da fase 0 de pesquisa. Rever após o desenho da fase 1.*

**Investments-KMP** (`.specify/memory/constitution.md`):

- [x] **Domínio:** Sem mudança de invariantes de domínio formal; impacto principal na sessão de edição de transações da UI.
- [x] **Qualidade de código:** Escopo restrito à camada `presentation`, com visibilidade explícita mínima e sem expansão indevida de API pública.
- [x] **Testes:** Estratégia de evidência definida com critérios testáveis; sem alteração em `core/domain/usecases/` nesta feature.
- [x] **UX:** Fluxos de criação, edição inline, erro de validação e cancelamento definidos de forma objetiva.
- [x] **Desempenho:** Meta de atualização visual rápida e ordenação estável definida para lista de transações.
- [x] **Build:** Módulo alvo identificado para verificação: `:features:asset-management:compileKotlinJvm`.
- [x] **Idioma:** Artefatos da feature em pt-BR.
- [x] **Coerência:** Clarificações do spec refletidas no plano e artefatos de design.

## Estrutura do projeto

### Documentação (esta feature)

```text
specs/007-cadastro-edicao-transacoes/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ui-asset-transactions-inline-edit.md
└── tasks.md
```

### Código-fonte (raiz do repositório)

```text
core/presentation/asset-management/
└── src/commonMain/kotlin/com/eferraz/asset_management/
    ├── AssetManagementEditContext.kt
    └── view/
        ├── AssetManagementFormView.kt
        ├── AssetManagementFormTransaction.kt
        └── AssetManagementFormFields.kt
```

**Decisão de estrutura**: implementação concentrada no módulo `:features:asset-management`, reutilizando contratos já existentes do formulário de ativo e expandindo estado/eventos para ciclo de vida das transações em memória.

## Fase 0 — Pesquisa e decisões

Ver `research.md` para decisões de gestão de staging em memória, gatilho de commit de edição inline, política de ordenação e tratamento de falha de persistência no salvar final.

## Fase 1 — Desenho e contratos

Entregáveis gerados:
- `data-model.md` com entidades de apresentação, regras de validação e transições de estado.
- `contracts/ui-asset-transactions-inline-edit.md` com contrato funcional da UI de transações.
- `quickstart.md` com roteiro de validação funcional e build local.

## Reavaliação da constitution (pós-desenho)

- [x] Escopo permanece na camada de apresentação e mantém fronteiras arquiteturais.
- [x] Critérios de UX e comportamento agora são verificáveis por cenários explícitos.
- [x] Não há necessidade de exceções no registo de complexidade.

## Registo de complexidade (Complexity Tracking)

Sem violações que exijam exceção.
