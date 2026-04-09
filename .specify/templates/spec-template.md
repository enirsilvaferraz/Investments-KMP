# Especificação de feature: [NOME DA FEATURE]

**Branch da feature**: `[###-feature-name]`  
**Criada em**: [DATE]  
**Estado**: Rascunho  
**Entrada do utilizador**: "$ARGUMENTS"

**Constitution (Investments-KMP):** Incluir, quando aplicável, critérios de **qualidade**, **API/visibilidade** (**`~/.cursor/rules/explicit-api.mdc`**, explicitApi, superfície mínima entre módulos — princípio IV), **testabilidade** (incl. **`~/.cursor/rules/test-patterns.mdc`**, **inglês**, **GIVEN / WHEN / THEN** nos nomes de testes em `*Test.kt`, **KDoc** no método, comentários **`// GIVEN` / `// WHEN` / `// THEN`** no corpo quando aplicável (**linha em branco** antes de cada marcador), **MockK** para dependências externas quando aplicável, **criação de objetos no próprio teste** — **evitar** *factories* de teste (ex. `TestDataFactory`) para código novo, **testes obrigatórios** para mudanças em `core/domain/usecases/` — princípio V), **consistência de UX** (formatação, erros), **desempenho** (latência, volumes) e **coerência** código ↔ documentação ↔ `.specify` ↔ `.cursor` (princípios IV–IX); ver `.specify/memory/constitution.md`.

**Idioma:** Redigir este documento em **português do Brasil (pt-BR)** (princípio VIII).

## Cenários de utilizador e testes *(obrigatório)*

<!--
  Histórias de utilizador devem ser PRIORIZADAS como jornadas ordenadas por importância.
  Cada história deve ser TESTÁVEL DE FORMA INDEPENDENTE — implementar só uma ainda entrega valor.
  Prioridades: P1, P2, P3… (P1 = mais crítica).
-->

### História de utilizador 1 — [Título breve] (Prioridade: P1)

[Descrever a jornada em linguagem simples]

**Por que esta prioridade**: [valor e razão da prioridade]

**Teste independente**: [Como validar esta história isoladamente — ação e valor entregue]

**Cenários de aceitação**:

1. **Dado** [estado inicial], **quando** [ação], **então** [resultado esperado]
2. **Dado** [estado inicial], **quando** [ação], **então** [resultado esperado]

---

### História de utilizador 2 — [Título breve] (Prioridade: P2)

[Descrever a jornada em linguagem simples]

**Por que esta prioridade**: [valor e razão da prioridade]

**Teste independente**: [Como validar esta história isoladamente]

**Cenários de aceitação**:

1. **Dado** [estado inicial], **quando** [ação], **então** [resultado esperado]

---

### História de utilizador 3 — [Título breve] (Prioridade: P3)

[Descrever a jornada em linguagem simples]

**Por que esta prioridade**: [valor e razão da prioridade]

**Teste independente**: [Como validar esta história isoladamente]

**Cenários de aceitação**:

1. **Dado** [estado inicial], **quando** [ação], **então** [resultado esperado]

---

[Adicionar mais histórias conforme necessário, cada uma com prioridade]

### Casos extremos (edge cases)

<!--
  AÇÃO: Preencher casos de limites e erros.
-->

- O que acontece quando [condição de limite]?
- Como o sistema trata [cenário de erro]?

## Requisitos *(obrigatório)*

<!--
  AÇÃO: Preencher requisitos funcionais.
-->

### Requisitos funcionais

- **RF-001**: O sistema DEVE [capacidade específica]
- **RF-002**: O sistema DEVE [capacidade específica]
- **RF-003**: O utilizador DEVE poder [interação]
- **RF-004**: O sistema DEVE [requisito de dados]
- **RF-005**: O sistema DEVE [comportamento]

*Exemplo de requisito a esclarecer:*

- **RF-006**: O sistema DEVE autenticar utilizadores via [PRECISA ESCLARECER: método de auth]
- **RF-007**: O sistema DEVE reter dados do utilizador por [PRECISA ESCLARECER: período]

### Entidades principais *(incluir se a feature envolver dados)*

- **[Entidade 1]**: [O que representa, atributos sem detalhe de implementação]
- **[Entidade 2]**: [Relações com outras entidades]

## Critérios de sucesso *(obrigatório)*

<!--
  AÇÃO: Critérios mensuráveis e agnósticos de tecnologia.
-->

### Resultados mensuráveis

- **CS-001**: [Métrica, ex.: "O utilizador conclui o fluxo em menos de 2 minutos"]
- **CS-002**: [Métrica técnica ou de carga, se aplicável]
- **CS-003**: [Satisfação ou taxa de conclusão na primeira tentativa]
- **CS-004**: [Métrica de negócio, se aplicável]

## Premissas

<!--
  AÇÃO: Premissas razoáveis quando a descrição inicial não detalhou tudo.
-->

- [Premissa sobre utilizadores ou ambiente]
- [Premissa sobre âmbito, ex.: "Fora de âmbito na v1: …"]
- [Dependência de sistema ou serviço existente]
