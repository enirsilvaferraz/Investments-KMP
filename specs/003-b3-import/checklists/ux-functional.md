# UX + Funcional Checklist: Importação de Dados da B3

**Purpose**: Validar qualidade, completude e clareza dos requisitos de UX e funcionais antes de avançar para o plano de implementação
**Created**: 2026-05-23
**Feature**: [spec.md](../spec.md)
**Scope**: Desktop (plataforma única para esta iteração)
**Audience**: Autor — gate pré-`/speckit-plan`

---

## Completude dos Requisitos de UX

- [ ] CHK001 - A aparência visual do botão de importação está especificada (ícone, rótulo, tamanho, estilo)? [Completeness, Gap]
- [ ] CHK002 - O local de exibição da mensagem de erro ao usuário está especificado (toast, diálogo modal, mensagem inline)? [Completeness, Gap, Spec §FR-008]
- [ ] CHK003 - O estado do botão de importação enquanto a caixa de diálogo de seleção está aberta (mas antes de o arquivo ser escolhido) está definido? [Completeness, Gap]
- [ ] CHK004 - O comportamento do botão de importação após sucesso é diferenciado do comportamento após erro, ou ambos são idênticos? [Clarity, Consistency, Spec §FR-009]

## Clareza dos Requisitos de Interação

- [ ] CHK005 - "Mesmo tamanho do botão" para o spinner é definido de forma objetivamente verificável, ou depende de interpretação do implementador? [Clarity, Spec §FR-009]
- [ ] CHK006 - O termo "console da IDE" está definido com especificidade suficiente (ex.: qual canal de saída, tag de log, ou nível de log esperado)? [Clarity, Ambiguity, Spec §FR-006]
- [ ] CHK007 - A distinção entre "arquivo corrompido" e "arquivo malformado/ilegível" está clarificada, ou ambos são tratados como equivalentes em FR-008? [Clarity, Ambiguity, Spec §FR-008]
- [ ] CHK008 - A sequência de transição de estado (botão → spinner → botão) está descrita como uma máquina de estados completa e sem ambiguidade? [Clarity, Consistency, Spec §FR-009, §FR-011]

## Completude dos Requisitos Funcionais

- [ ] CHK009 - Existe um FR cobrindo o cenário em que a permissão de acesso ao sistema de arquivos é negada pelo sistema operacional? [Completeness, Gap, Edge Case]
- [ ] CHK010 - O formato da saída no console está especificado (texto plano, estruturado com rótulos, tabular)? [Completeness, Gap, Spec §FR-006]
- [ ] CHK011 - Existe um FR cobrindo um arquivo XLSX que não contém nenhuma guia (zero abas)? [Coverage, Gap]
- [ ] CHK012 - Existe um FR cobrindo um arquivo XLSX com 0 bytes ou completamente vazio? [Coverage, Gap]
- [ ] CHK013 - O que acontece com o processamento em andamento se o usuário navegar para fora da tela de histórico de ativos está definido? [Completeness, Gap]

## Consistência dos Requisitos

- [ ] CHK014 - SC-001 ("em menos de 30 segundos") e FR-011 (timeout de 30 segundos) estão alinhados, ou criam uma fronteira ambígua (o sucesso é possível em exatamente 30s)? [Consistency, Spec §SC-001, §FR-011]
- [ ] CHK015 - FR-003 (filtragem preventiva na caixa de diálogo) e SC-003 ("100% das tentativas rejeitadas") são consistentes considerando o cenário de fallback em que o SO não suporta filtragem? [Consistency, Spec §FR-003, §SC-003]
- [ ] CHK016 - O comportamento descrito em FR-009 (spinner substitui botão) é consistente com o descrito em FR-011 (remover spinner e restaurar botão) e com o cenário 3 da User Story 1? [Consistency, Spec §FR-009, §FR-011]

## Qualidade dos Critérios de Aceitação

- [ ] CHK017 - SC-003 ("mensagem clara ao usuário") pode ser verificado objetivamente sem uma definição do que constitui uma mensagem "clara"? [Measurability, Spec §SC-003]
- [ ] CHK018 - SC-005 ("sem travar o aplicativo") está quantificado com um critério mensurável (ex.: tempo máximo de resposta após o erro)? [Measurability, Clarity, Spec §SC-005]
- [ ] CHK019 - Os critérios de sucesso cobrem o estado visual do spinner (transição botão→spinner→botão) em termos verificáveis? [Measurability, Gap]

## Cobertura de Cenários e Casos de Borda

- [ ] CHK020 - Existe requisito para o cenário em que o usuário seleciona o mesmo arquivo duas vezes consecutivas? [Coverage, Gap]
- [ ] CHK021 - O comportamento do botão de importação quando outra caixa de diálogo do sistema já está aberta está definido? [Coverage, Gap]
- [ ] CHK022 - O edge case de arquivo XLSX protegido por senha está endereçado nos requisitos? [Coverage, Gap]

## Dependências e Premissas

- [ ] CHK023 - A premissa de que Android e iOS terão apenas um bypass (stub sem comportamento) está documentada de forma clara o suficiente para guiar a implementação KMP (ex.: `expect/actual` vazio vs. exceção não tratada)? [Assumption, Clarity, Spec §Assumptions]
- [ ] CHK024 - A premissa "formato XLSX padrão da B3" está documentada com especificidade suficiente para guiar decisões sobre arquivos atípicos (ex.: versões antigas do Excel, macros embutidas)? [Assumption, Clarity, Spec §Assumptions]

## Notes

- Itens marcados como `[Gap]` indicam requisitos ausentes que podem precisar ser adicionados à spec antes do `/speckit-plan`.
- Itens `[Ambiguity]` indicam terminologia vaga que pode gerar interpretações divergentes na implementação.
- Itens `[Consistency]` indicam potenciais conflitos entre seções da spec que devem ser reconciliados.
- Escopo restrito a Desktop nesta iteração; cobertura Android/iOS deve ser adicionada em checklist futuro antes da implementação multi-plataforma.
