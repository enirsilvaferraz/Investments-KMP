# Plano de implementação: Dialog de asset em duas colunas

**Branch**: `006-asset-dialog-duas-colunas` | **Data**: 2026-04-25 | **Spec**: `specs/006-asset-dialog-duas-colunas/spec.md`  
**Entrada**: Especificação da feature em `specs/006-asset-dialog-duas-colunas/spec.md`

**Nota:** Preenchido pelo comando `/speckit.plan`. Fluxo de execução: ver `.specify/templates/plan-template.md`.

**Idioma:** Redigir este documento em **português do Brasil (pt-BR)**, conforme `.specify/memory/constitution.md` princípio VIII.

## Resumo

Reestruturar o dialog de cadastro/edição de asset para um layout de duas colunas: formulário atual na esquerda (sem o campo corretora) e, na direita, campo corretora no topo + tabela de transações da holding. A implementação deve preservar ações existentes (salvar/cancelar), incluir separador visual suave entre colunas, estado vazio com mensagem definida e rolagem interna na coluna de histórico para listas longas.

## Contexto técnico

**Linguagem / versão**: Kotlin Multiplatform (Kotlin 2.x do projeto)  
**Dependências principais**: Compose Multiplatform, Material3, Koin, módulos `:domain:entity`, `:domain:usecases`, `:features:design-system`  
**Armazenamento**: N/A (feature de apresentação; dados já existentes)  
**Testes**: validação principal por testes de UI/comportamento no módulo `:features:asset-management` (quando aplicável) + verificação manual guiada  
**Plataformas-alvo**: Android e Desktop (via módulos de app KMP)  
**Tipo de projeto**: app Kotlin Multiplatform em camadas  
**Metas de desempenho**: dialog permanece responsivo com histórico extenso via rolagem interna da tabela (sem crescimento do container principal)  
**Restrições**: não alterar regras de negócio; manter UX conhecida na coluna 1; evitar quebra de contrato de eventos da tela  
**Escala / âmbito**: 1 tela/composable principal (`AssetManagementFormView`) e componentes associados de formulário/histórico

## Verificação da constitution (Constitution Check)

*GATE: Deve passar antes da fase 0 de pesquisa. Rever após o desenho da fase 1.*

**Investments-KMP** (`.specify/memory/constitution.md`):

- [x] **Domínio:** Sem mudança de entidade/invariante; `DOMAIN.md` não requer atualização para esta feature de layout.
- [x] **Qualidade de código:** Mudanças restritas à camada `presentation`, mantendo dependências atuais e visibilidade explícita.
- [x] **Testes:** Estratégia de evidência definida (cenários de aceitação + validação de comportamento de UI); sem mudança em `core/domain/usecases/`.
- [x] **UX:** Estados vazio/rolagem e organização de campos definidos explicitamente.
- [x] **Desempenho:** Risco principal (lista longa no histórico) mitigado por rolagem interna e altura estável do dialog.
- [x] **Build:** Módulo alvo de compilação identificado: `:features:asset-management` com `compileKotlinJvm`.
- [x] **Idioma:** Artefatos de especificação e planejamento em pt-BR.
- [x] **Coerência:** Artefatos `specs/` atualizados conforme decisões de clarificação.

## Estrutura do projeto

### Documentação (esta feature)

```text
specs/006-asset-dialog-duas-colunas/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ui-asset-dialog-two-columns.md
└── tasks.md
```

### Código-fonte (raiz do repositório)

```text
core/presentation/asset-management/
├── build.gradle.kts
└── src/commonMain/kotlin/com/eferraz/asset_management/
    ├── view/
    │   ├── AssetManagementFormView.kt
    │   └── AssetManagementFormFields.kt
    └── vm/
        ├── UiState.kt
        └── VMEvents.kt
```

**Decisão de estrutura**: Implementação concentrada no módulo `:features:asset-management`, reutilizando contratos de estado/eventos já existentes no `vm` e componentes de UI já disponíveis.

## Fase 0 — Pesquisa e decisões

Ver `research.md` para decisões consolidadas de layout, separador visual, estado vazio e estratégia de rolagem interna.

## Fase 1 — Desenho e contratos

Entregáveis gerados:
- `data-model.md` com entidades de apresentação e regras de exibição.
- `contracts/ui-asset-dialog-two-columns.md` com contrato funcional de UI.
- `quickstart.md` com roteiro de validação local.

## Reavaliação da constitution (pós-desenho)

- [x] Escopo permanece em apresentação; sem violação de fronteiras de camada.
- [x] Requisitos de UX ficaram testáveis e sem ambiguidades remanescentes.
- [x] Não há necessidade de exceção no registo de complexidade.

## Registo de complexidade (Complexity Tracking)

Sem violações que exijam exceção.
