# Cross-Artifact Checklist: Importação de Dados da B3

**Purpose**: Validar qualidade, completude e consistência dos requisitos entre `spec.md`, `plan.md`, `data-model.md`, `contracts/XlsxImportContract.md` e `tasks.md` antes e durante a implementação
**Created**: 2026-05-23
**Feature**: [spec.md](../spec.md)
**Scope**: Fase Desktop (MVP + US2/US3 conforme `tasks.md`)
**Audience**: Revisor (PR) — gate de alinhamento documental

---

## Consistência entre Artefatos

- [x] CHK001 - Os requisitos de feedback de erro na UI em `spec.md` (FR-014: exclusivamente console) estão alinhados com a User Story 2 e tarefas T017–T019 em `tasks.md` (Snackbar e `errorMessage`)? [Conflict, Spec §FR-014, Tasks §Phase 4] — **Resolvido 2026-05-23**: `tasks.md` regenerado; US2 = T017–T018 só no port (console); removidos Snackbar/`errorMessage`/DismissError.
- [x] CHK002 - O contrato de UI em `contracts/XlsxImportContract.md` (“futuro: snackbar”) foi atualizado para refletir a decisão vigente em `spec.md` ou em `tasks.md`, ou o conflito está documentado como mudança de escopo? [Consistency, Contract §Contrato da UI] — **Resolvido 2026-05-23**: contrato e `data-model.md` já console-only; `tasks.md` sem tarefas de Snackbar.
- [x] CHK003 - A premissa em `spec.md` de que Android/iOS podem exibir o botão como bypass está alinhada com `tasks.md` (“Android e iOS não recebem botão nesta fase”) e com `contracts/XlsxImportContract.md` (“nunca é chamado”)? [Conflict, Spec §Assumptions, Tasks §Notes] — **Resolvido 2026-05-23**: `tasks.md` Notes e T014 alinham bypass (botão opcional sem ação); port não invocado em mobile.
- [ ] CHK004 - O escopo “Desktop-only” está definido de forma idêntica em `spec.md`, `plan.md` e `tasks.md` quanto a quais camadas (UI, port, picker) executam comportamento real vs. stub? [Consistency, Spec §Assumptions, Plan §Technical Context]
- [ ] CHK005 - FR-015 (falha atómica sem saída parcial no console) está refletido no pseudocódigo de `contracts/XlsxImportContract.md` (parse sequencial por guia com `onFailed → throw`) e nas regras de `data-model.md`, sem ambiguidade sobre logs já emitidos antes da falha? [Consistency, Spec §FR-015, Contract §parseAndLog]
- [ ] CHK006 - SC-007 (falha atómica em 100% das execuções) possui critérios de aceitação correspondentes nas User Stories ou apenas nos Success Criteria? [Traceability, Spec §SC-007, Gap]
- [ ] CHK007 - FR-012 (guias desconhecidas ignoradas sem log) permanece consistente com o comportamento descrito em `data-model.md` e no contrato quando uma guia conhecida está ausente mas outras existem? [Consistency, Spec §FR-012, §FR-013]

## Completude dos Requisitos de Dados e Esquema

- [x] CHK008 - A lista de colunas obrigatórias por guia B3 está documentada como requisito em `spec.md`, ou apenas como detalhe de implementação em `data-model.md`? [Completeness, Gap, Spec §FR-015] — **Resolvido 2026-05-23**: FR-015 referencia `data-model.md` e `@ColumnName`.
- [ ] CHK009 - Os nomes exatos das cinco guias (`Acoes`, `ETF`, etc.) em `spec.md` correspondem, sem variação, aos `sheetName` em `contracts/XlsxImportContract.md` e aos títulos em `data-model.md`? [Consistency, Spec §FR-004]
- [x] CHK010 - Está especificado em requisitos (não só em design) o conjunto mínimo de colunas que disparam `MISSING_COLUMNS` / falha atómica para cada tipo de guia? [Completeness, Spec §FR-015, Data-model] — **Resolvido 2026-05-23**: via referência a `data-model.md` em FR-015; T010 explicita column set.
- [ ] CHK011 - Os requisitos definem o tratamento quando o cabeçalho da guia usa grafia alternativa (acentos, espaços extras) em relação aos `@ColumnName` documentados? [Coverage, Gap, Data-model]
- [ ] CHK012 - Está documentado se linhas com células parcialmente preenchidas contam como “dados” ou como linha em branco para FR-010? [Clarity, Spec §FR-010, Gap]
- [ ] CHK013 - As regras de filtragem (linha em branco, `Total`/`Subtotal`) em `data-model.md` estão rastreáveis a requisitos funcionais ou permanecem apenas como decisão de implementação? [Traceability, Data-model §Validações]

## Clareza do Contrato de Porta e Saída no Console

- [ ] CHK014 - FR-006 (“identificando o nome da guia e seus dados tabulares”) está quantificado o suficiente em relação ao formato de log descrito no contrato (header | linhas | totais calculados)? [Clarity, Spec §FR-006, Contract §parseAndLog]
- [ ] CHK015 - Está definido nos requisitos o que constitui “totais calculados” (quais campos entram na soma por tipo de guia)? [Clarity, Gap, Contract §computeTotals]
- [ ] CHK016 - O termo “console da IDE” está alinhado entre `spec.md` e o contrato (`println`) quanto a canal, estrutura e ausência de dados sensíveis? [Clarity, Spec §FR-006, Assumption]
- [ ] CHK017 - O contrato distingue explicitamente `Result.success(Unit)` por cancelamento do picker vs. sucesso com zero guias B3 (FR-013), de forma verificável pelo chamador? [Clarity, Contract §Pós-condições, Spec §FR-013]
- [ ] CHK018 - Os códigos de erro do contrato (`EMPTY_FILE`, `INVALID_FORMAT`, `MISSING_COLUMNS`) estão mapeados a mensagens ou semântica exigida em `spec.md` (console) vs. `tasks.md` (UI)? [Consistency, Contract §Contrato de erro, Spec §FR-014]

## Qualidade dos Critérios de Aceitação e Mensurabilidade

- [x] CHK019 - SC-001 (“menos de 30 segundos”) e FR-011 (timeout 30 s) definem de forma não ambígua o comportamento no limite exato de 30,000 ms? [Clarity, Spec §SC-001, §FR-011] — **Resolvido 2026-05-23**: FR-011/SC-001 + clarificação `>= 30_000 ms` vs sucesso `< 30_000 ms`.
- [ ] CHK020 - SC-003 (“motivo registado no console”) pode ser verificado objetivamente sem especificar texto, código ou nível de log? [Measurability, Spec §SC-003]
- [ ] CHK021 - SC-008 (sem feedback de sucesso na UI) está alinhado com FR-016 e com o estado `HistoryState` mínimo em `contracts/XlsxImportContract.md`? [Consistency, Spec §SC-008, §FR-016]
- [ ] CHK022 - Os cenários de aceitação da User Story 1 descrevem verificação apenas por console, sem exigir comportamentos contraditórios com FR-014? [Consistency, Spec §User Story 1]

## Cobertura de Cenários e Fluxos de Exceção

- [ ] CHK023 - Existe requisito explícito para arquivo XLSX com senha ou proteção de leitura, ou a exclusão está documentada como fora de escopo? [Coverage, Gap, Spec §Edge Cases]
- [x] CHK024 - O comportamento quando `FileMapperPicker` retorna arquivo legível mas bytes vazios está coberto por FR-013, contrato `EMPTY_FILE`, ou ambos de forma consistente? [Coverage, Spec §FR-013, Contract §Contrato de erro] — **Resolvido 2026-05-23**: Edge case + FR-013 distinto de `EMPTY_FILE`; contrato e T010 alinhados.
- [ ] CHK025 - Estão definidos requisitos para falha de permissão (`AccessDeniedException`/`SecurityException`) com o mesmo canal de comunicação que FR-014 (console-only nesta fase)? [Completeness, Spec §Edge Cases, §FR-014]
- [x] CHK026 - O cenário de timeout durante parse de uma guia intermediária está especificado quanto a cancelamento de saída parcial no console? [Coverage, Exception Flow, Spec §FR-011, §FR-015] — **Resolvido 2026-05-23**: FR-011a — Fase B não executada após timeout; Fase A sem `println` de dados.
- [ ] CHK027 - Requisitos para reimportação consecutiva do mesmo arquivo (estado `isImporting`, idempotência do log) estão documentados ou explicitamente excluídos? [Coverage, Gap]

## Requisitos Não Funcionais e Arquitetura

- [ ] CHK028 - O requisito de performance (< 30 s, `Dispatchers.Default`) em `plan.md` está rastreável a SC-001/FR-011 na spec, ou permanece apenas como decisão técnica? [Traceability, Plan §Performance Goals, Spec §FR-011]
- [ ] CHK029 - A proibição de persistência nesta fase está repetida de forma consistente em `spec.md`, `plan.md`, `data-model.md` e contrato (sem vazamento de entidades de domínio)? [Consistency, Spec §Assumptions, Plan §Storage]
- [ ] CHK030 - Os princípios da constituição referenciados em `plan.md` (testes em UseCases, API explícita) possuem requisitos correspondentes em `spec.md` ou apenas em `tasks.md`? [Traceability, Plan §Constitution Check, Gap]

## Dependências, Premissas e Riscos Documentais

- [ ] CHK031 - A dependência FileMapper-KMP 1.0.0 em `plan.md` está documentada como premissa de formato B3 ou como risco se o export mudar colunas? [Assumption, Plan §Primary Dependencies, Spec §Assumptions]
- [ ] CHK032 - `quickstart.md` (quando usado para validação manual) define critérios de pass/fail alinhados aos SC/FR, sem introduzir requisitos novos não presentes na spec? [Consistency, Gap]
- [x] CHK033 - A nota em `tasks.md` sobre “FR-011 parcialmente coberto no MVP” está reconciliada com FR-014 (sem UI) e com SC-001, ou marca dívida técnica explícita? [Ambiguity, Tasks §Phase 3 checkpoint] — **Resolvido 2026-05-23**: nota obsoleta removida; T004/T016 cobrem FR-011a.

## Ambiguidades e Conflitos a Resolver

- [x] CHK034 - A User Story 2 em `spec.md` (rejeição registada no console, sem UI) conflita com o objetivo da Phase 4 em `tasks.md` (mensagem ao usuário via Snackbar) — qual documento prevalece para esta entrega? [Conflict, Spec §User Story 2, Tasks §Phase 4] — **Resolvido 2026-05-23**: `spec.md` prevalece; Phase 4 US2 realinhada (T017–T018, console-only).
- [ ] CHK035 - Itens em `checklists/ux-functional.md` que referem UI de erro (CHK002, CHK017) foram marcados como obsoletos ou atualizados após clarificações de 2026-05-23 em `spec.md`? [Consistency, Spec §Clarifications]
- [x] CHK036 - O diagrama em `data-model.md` que menciona “futuro: snackbar de erro” está sincronizado com a decisão atual de FR-014/FR-016? [Conflict, Data-model §Diagrama de Fluxo] — **Resolvido 2026-05-23**: diagrama em `data-model.md` sem snackbar; `tasks.md` alinhado.

## Notes

- Itens `[Conflict]` indicam divergência entre artefatos que deve ser resolvida antes de fechar US2 ou o MVP.
- Itens `[Gap]` indicam requisitos ausentes na `spec.md` que hoje vivem só em `data-model.md`, contrato ou `tasks.md`.
- Marcar `[x]` quando o requisito estiver claro e consistente em todos os artefatos relevantes; anotar decisão inline se um artefato for a fonte da verdade.
