# Import Flow Checklist: Importação de Dados da B3

**Purpose**: Validar qualidade, clareza e cobertura dos requisitos de fluxo de importação XLSX (seleção → processamento → console) em `spec.md`, incluindo fases atómicas, canais de feedback e cenários de exceção
**Created**: 2026-05-23
**Feature**: [spec.md](../spec.md)
**Scope**: Requisitos funcionais e critérios de sucesso da fase Desktop (console-only)
**Audience**: Revisor (PR) — gate antes/durante implementação

**Note**: Gerado por `/speckit.checklist` sem argumentos; profundidade **standard**; foco em fluxo de importação e comunicação de falha/sucesso.

---

## Requirement Completeness

- [ ] CHK001 - O fluxo primário (botão → diálogo → leitura → console) está documentado com pré/pós-condições para cada etapa, ou apenas como cenários de aceitação narrativos? [Completeness, Spec §User Story 1]
- [ ] CHK002 - FR-011a (registo de timeout no UseCase) está rastreável a um critério de sucesso ou cenário de aceitação, ou permanece apenas como FR isolado? [Completeness, Spec §FR-011a, Gap]
- [ ] CHK003 - Os requisitos cobrem explicitamente quando o spinner deve aparecer em relação à abertura do diálogo (antes vs. após seleção de arquivo)? [Completeness, Gap, Spec §FR-009]
- [ ] CHK004 - Está documentado se `ImportB3File` em mobile (bypass) deve manter `isImporting` inalterado ou se o intent é ignorado antes de qualquer mutação de estado? [Completeness, Spec §Assumptions, Gap]

## Requirement Clarity

- [ ] CHK005 - A distinção entre Fase A (validação sem `println` de dados) e Fase B (log tabular) está definida nos requisitos da spec ou apenas em artefatos de design? [Clarity, Spec §FR-015, Gap]
- [ ] CHK006 - FR-010 (“apenas cabeçalho”) define critérios objetivos para distinguir guia vazia de guia com somente linha de total/subtotal do export B3? [Clarity, Spec §FR-010, Ambiguity]
- [ ] CHK007 - O termo “dados tabulares” em FR-006 especifica se linhas filtradas (em branco, Total/Subtotal) devem ou não aparecer no console? [Clarity, Spec §FR-006, Gap]
- [ ] CHK008 - FR-003 descreve de forma não ambígua o comportamento quando o SO aplica filtro visual mas ainda permite seleção de extensão incorreta via atalho ou renomeação? [Clarity, Spec §FR-003, §User Story 2]
- [ ] CHK009 - A mensagem exigida para rejeição de formato inválido (`INVALID_FORMAT` ou equivalente) está prescrita na spec ou deixada ao implementador? [Clarity, Spec §FR-003, §SC-003, Gap]

## Requirement Consistency

- [ ] CHK010 - FR-007 (cancelamento sem mudanças de estado) é consistente com FR-009 (spinner durante processamento) quanto ao momento em que `isImporting` ou equivalente pode tornar-se verdadeiro? [Consistency, Spec §FR-007, §FR-009]
- [ ] CHK011 - FR-012 (guias desconhecidas sem log) permanece consistente com FR-006 (log de cada guia conhecida) quando o arquivo contém mistura de guias conhecidas e desconhecidas? [Consistency, Spec §FR-012, §FR-006]
- [ ] CHK012 - FR-013 (sucesso silencioso sem guias B3) e o edge case `EMPTY_FILE` (workbook ilegível/0 bytes) usam terminologia que evita interpretar ambos como “importação vazia bem-sucedida”? [Consistency, Spec §FR-013, §Edge Cases]
- [ ] CHK013 - FR-014 e FR-016 juntos definem de forma coerente todos os estados terminais (sucesso com dados, sucesso sem dados, falha) sem exigir feedback na UI? [Consistency, Spec §FR-014, §FR-016]

## Acceptance Criteria Quality

- [ ] CHK014 - SC-002 (“100% das guias B3 conhecidas lidas”) define como verificar omissão parcial quando FR-015 exige falha atómica sem saída? [Measurability, Spec §SC-002, §FR-015]
- [ ] CHK015 - SC-006 (arquivo sem guias B3 conhecidas) pode ser verificado sem ambiguidade em relação a SC-005 (corrompido/ilegível)? [Measurability, Spec §SC-006, §SC-005]
- [ ] CHK016 - Os cenários Given/When/Then da User Story 1 referenciam exclusivamente console e spinner, sem contradizer SC-008 ou FR-016? [Measurability, Spec §User Story 1, §SC-008]

## Scenario Coverage

- [ ] CHK017 - Existem requisitos para o fluxo alternativo de reimportação imediata (segundo arquivo antes do primeiro terminar)? [Coverage, Gap]
- [ ] CHK018 - O fluxo de exceção por `MISSING_COLUMNS` em uma guia presente está especificado quanto a guias conhecidas ausentes no mesmo arquivo (validar só presentes vs. exigir todas)? [Coverage, Spec §FR-015, Ambiguity]
- [ ] CHK019 - Os requisitos de recuperação após timeout (FR-011) definem se o usuário pode reiniciar importação sem reiniciar o app? [Coverage, Recovery, Spec §FR-011, Gap]
- [ ] CHK020 - O fluxo de cancelamento (User Story 3) cobre cancelamento durante processamento ativo ou apenas cancelamento do picker? [Coverage, Spec §User Story 3, Gap]

## Edge Case Coverage

- [ ] CHK021 - Edge cases resolvidos na spec (corrompido, timeout, permissão, colunas ausentes) possuem mapeamento 1:1 para FR/SC ou alguns permanecem só na secção Edge Cases? [Traceability, Spec §Edge Cases]
- [ ] CHK022 - Está definido o comportamento quando uma guia B3 conhecida existe mas contém apenas linhas filtradas (todas em branco ou Total), em relação a FR-010? [Edge Case, Spec §FR-010, Gap]
- [ ] CHK023 - Arquivo XLSX com macro, senha ou proteção está explicitamente fora de escopo nos requisitos? [Edge Case, Gap, Spec §Assumptions]

## Non-Functional Requirements

- [ ] CHK024 - SC-001 e FR-011 definem de forma mensurável o relógio de referência (toque no botão vs. fim da Fase B vs. início do parse)? [Clarity, Spec §SC-001, §FR-011]
- [ ] CHK025 - Existe requisito de privacidade ou sensibilidade para dados financeiros impressos no console nesta fase? [Non-Functional, Gap]
- [ ] CHK026 - Requisitos de acessibilidade para o botão de importação e o spinner estão documentados ou explicitamente excluídos? [Non-Functional, Gap]

## Dependencies & Assumptions

- [ ] CHK027 - A premissa “sem validação semântica dos dados” está alinhada com FR-015 (validação estrutural de colunas) sem conflito de escopo? [Assumption, Spec §Assumptions, §FR-015]
- [ ] CHK028 - A dependência de nomes exatos de guias B3 está documentada como risco se o export da B3 alterar títulos de aba? [Assumption, Spec §FR-004, Gap]
- [ ] CHK029 - A referência cruzada de FR-015 a `data-model.md` estabelece que alterações de colunas obrigatórias exigem atualização da spec ou é delegação implícita ao design? [Dependency, Spec §FR-015]

## Ambiguities & Conflicts

- [ ] CHK030 - Permanece ambiguidade entre “corrompido” (FR-008) e “ilegível” (`EMPTY_FILE` no edge case) nos critérios de mensagem de console? [Ambiguity, Spec §FR-008, §Edge Cases]
- [ ] CHK031 - User Story 2 ainda sugere “impedir preventivamente” 100% dos formatos inválidos em SOs sem filtro nativo, de forma compatível com SC-003? [Conflict, Spec §User Story 2, §SC-003]
- [ ] CHK032 - Existe conflito entre “apresentar no console da IDE” (requisito de produto) e exclusão de feedback na UI para cenários onde o investidor não executa via IDE? [Ambiguity, Spec §FR-006, §Assumptions]

## Notes

- Check items off as completed: `[x]`
- Itens obsoletos após clarificações devem ser anotados inline (ver também `cross-artifact.md` CHK035 sobre `ux-functional.md`)
- Este checklist complementa `cross-artifact.md` (alinhamento multi-documento) e `ux-functional.md` (detalhe de UI)
