# Plano de implementação: [FEATURE]

**Branch**: `[###-feature-name]` | **Data**: [DATE] | **Spec**: [link]
**Entrada**: Especificação da feature em `/specs/[###-feature-name]/spec.md`

**Nota:** Preenchido pelo comando `/speckit.plan`. Fluxo de execução: ver `.specify/templates/plan-template.md`.

**Idioma:** Redigir este documento em **português do Brasil (pt-BR)**, conforme `.specify/memory/constitution.md` princípio VIII.

## Resumo

[Extrair da spec: requisito principal + abordagem técnica da pesquisa]

## Contexto técnico

<!--
  AÇÃO: Substituir pelo detalhe técnico do projeto. A estrutura abaixo é orientadora.
-->

**Linguagem / versão**: [ex.: Kotlin 2.x, ou PRECISA ESCLARECER]  
**Dependências principais**: [ex.: Compose Multiplatform, Ktor, Room — ou PRECISA ESCLARECER]  
**Armazenamento**: [se aplicável, ex.: Room/SQLite, ou N/A]  
**Testes**: [ex.: jvmTest, commonTest — ou PRECISA ESCLARECER]  
**Plataformas-alvo**: [ex.: Android, iOS, JVM desktop — ou PRECISA ESCLARECER]  
**Tipo de projeto**: [ex.: app KMP multiplataforma — ou PRECISA ESCLARECER]  
**Metas de desempenho**: [específicas do domínio, ex.: lista fluida em ecrã X — ou PRECISA ESCLARECER]  
**Restrições**: [ex.: offline, tamanho de APK — ou PRECISA ESCLARECER]  
**Escala / âmbito**: [ex.: número de ecrãs, utilizadores — ou PRECISA ESCLARECER]

## Verificação da constitution (Constitution Check)

*GATE: Deve passar antes da fase 0 de pesquisa. Rever após o desenho da fase 1.*

**Investments-KMP** (`.specify/memory/constitution.md`):

- [ ] **Domínio:** Impacto em `DOMAIN.md` identificado se entidades ou invariantes mudarem.
- [ ] **Qualidade de código:** Camadas respeitadas; **explicitApi** e visibilidade mínima (`private`/`internal` por defeito, `public` só para API entre módulos); complexidade justificada se necessário.
- [ ] **Testes:** Estratégia de evidência (automatizada ou verificação explícita) alinhada a `.cursor/rules/test-patterns.mdc` para lógica relevante.
- [ ] **UX:** Reutilização de componentes/formatos; estados de erro e vazio considerados.
- [ ] **Desempenho:** Objetivos ou riscos (listas, rede, agregações) registados quando a spec exigir.
- [ ] **Build:** Módulos Gradle e `compileKotlinJvm` coerentes com `.cursorrules`.
- [ ] **Idioma:** Documentos da feature em **pt-BR** (princípio VIII).
- [ ] **Coerência:** Alterações de código com impacto em domínio, convenções ou regras de IA **refletidas** em `*.md` aplicáveis, em **`.specify/`** e em **`.cursor*`** quando aplicável (princípio IX).

## Estrutura do projeto

### Documentação (esta feature)

```text
specs/[###-feature]/
├── plan.md              # Este ficheiro (saída do /speckit.plan)
├── research.md          # Fase 0 (/speckit.plan)
├── data-model.md        # Fase 1 (/speckit.plan)
├── quickstart.md        # Fase 1 (/speckit.plan)
├── contracts/           # Fase 1 (/speckit.plan)
└── tasks.md             # Fase 2 (/speckit.tasks — NÃO criado pelo /speckit.plan)
```

### Código-fonte (raiz do repositório)

<!--
  AÇÃO: Substituir a árvore placeholder pela estrutura real desta feature.
  Remover opções não usadas. O plano entregue não deve incluir rótulos "Opção".
-->

```text
# [REMOVER SE NÃO USADO] Opção 1: projeto único (genérico)
src/
├── models/
├── services/
├── cli/
└── lib/

tests/
├── contract/
├── integration/
└── unit/

# [REMOVER SE NÃO USADO] Opção 2: web (frontend + backend)
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/

# [REMOVER SE NÃO USADO] Opção 3: mobile + API
api/
└── [igual ao backend acima]

ios/ ou android/
└── [estrutura específica da plataforma]
```

**Decisão de estrutura**: [Descrever a opção escolhida e os caminhos reais, p.ex. `core/domain/...` no KMP]

## Registo de complexidade (Complexity Tracking)

> **Preencher SOMENTE** se a verificação da constitution tiver violações que precisem justificação

| Violação | Por que é necessária | Alternativa mais simples recusada porque |
|----------|----------------------|------------------------------------------|
| [ex.: 4.º projeto] | [necessidade atual] | [por que 3 projetos não bastam] |
| [ex.: padrão Repositório] | [problema específico] | [por que acesso direto ao BD não serve] |
