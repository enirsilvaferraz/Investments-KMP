# Checklist de qualidade da especificação: Corretora obrigatória e posição no cadastro

**Objetivo**: Validar completude e qualidade da especificação antes de `/speckit.clarify` ou `/speckit.plan`  
**Criada em**: 2026-04-13  
**Feature**: [spec.md](../spec.md)

## Qualidade do conteúdo

- [x] Sem detalhes de implementação (linguagens, frameworks, APIs)
- [x] Focado em valor para o utilizador e necessidade de negócio
- [x] Redigido para partes interessadas não técnicas (com conceitos de domínio explícitos quando necessário)
- [x] Todas as secções obrigatórias preenchidas

## Completude dos requisitos

- [x] Não restam marcadores [NEEDS CLARIFICATION]
- [x] Requisitos testáveis e sem ambiguidade material
- [x] Critérios de sucesso mensuráveis
- [x] Critérios de sucesso agnósticos de tecnologia (sem referência a stack)
- [x] Cenários de aceitação definidos
- [x] Casos extremos identificados
- [x] Âmbito delimitado (extensão de `001`; cadastro novo no mínimo)
- [x] Dependências e premissas identificadas (catálogo de corretoras, consistência com `001`)

## Prontidão da feature

- [x] Requisitos funcionais com critérios de aceitação associados nas histórias
- [x] Cenários cobrem fluxo principal (lista, obrigatoriedade, gravação com posição)
- [x] Resultados mensuráveis alinhados aos critérios de sucesso
- [x] Especificação sem vazamento de detalhe de implementação

## Validação (revisão)

| Item             | Estado | Notas                                                                   |
| ---------------- | ------ | ----------------------------------------------------------------------- |
| Conteúdo para stakeholders | Passa | Domínio (corretora, posição) nomeado; sem stack                         |
| RF testáveis     | Passa  | RF-001 a RF-006 com comportamento verificável                           |
| CS mensuráveis   | Passa  | Percentagens e ausência de discrepância entre escolha e associação guardada |
| Edge cases       | Passa  | Catálogo vazio, falha gravar, lista longa, corretora removida           |
| Âmbito           | Passa  | Ligação explícita a `specs/001-cadastro-investimento-dialog`            |

## Notas

- Diretório canónico para o branch `003-corretora-holding-cadastro`: `specs/003-corretora-holding-cadastro`. Cópia anterior em `specs/002-…` pode ser arquivada para evitar duplicação.
- Após sessão de clarificação: rever itens "atomicidade" e falha parcial se atualizados no `spec.md`.
