# Domain Review Checklist: Rateio de Taxas de Nota de Corretagem SINACOR

**Purpose**: Gate formal de revisão por par (PR review) — valida qualidade dos requisitos E cobertura dos cenários de domínio financeiro
**Created**: 2026-06-10
**Feature**: [spec.md](../spec.md)
**Depth**: Standard — Formal PR Gate
**Focus**: Qualidade de requisitos + cobertura de domínio (pipeline 3 etapas)
**Riscos obrigatórios**: Precisão aritmética · Hierarquia de exceções · Identidade de NoteAsset

---

## Completude dos Requisitos

- [ ] CHK001 - Cada uma das três etapas do pipeline (Etapa 1, Etapa 2, Etapa 3) é coberta por pelo menos um FR com critério de aceitação testável? [Completeness, Spec §FR-006–FR-020]
- [ ] CHK002 - Existe um requisito que descreve o comportamento quando `Soma_Taxas = 0` (todas as taxas rateáveis são zero)? [Completeness, Spec §Edge Cases]
- [ ] CHK003 - A exclusão de `impostos_retidos` do rateio está declarada como requisito explícito (FR-011) e não apenas como suposição? [Completeness, Spec §FR-011, Assumptions]
- [ ] CHK004 - Os campos de metadados (`numero_nota`, `data_pregao`, `data_liquidacao`, `corretora`, `cnpj_corretora`) têm algum requisito de validação, ou sua ausência de validação está explicitamente justificada? [Completeness, Gap, Spec §FR-002]
- [ ] CHK005 - Existe um requisito para o caso em que dois `NoteAsset` são estruturalmente idênticos em todos os campos (potencial colisão de chave no mapa de saída)? [Completeness, Gap, Spec §FR-017]

---

## Clareza dos Requisitos

- [ ] CHK006 - FR-006 (regra 1.1, volume total) usa a mesma abordagem de comparação em centavos inteiros definida em FR-007 e FR-016, ou utiliza "exatamente igual" sem especificar o método de comparação? [Clarity, Spec §FR-006, FR-007]
- [ ] CHK007 - FR-012 especifica sem ambiguidade que ROUND_HALF_UP é aplicado ao resultado **em centavos** (Long), e não a um intermediário Double? [Clarity, Spec §FR-012]
- [ ] CHK008 - O termo `valor_liquido_nota` é utilizado com a mesma convenção de sinal em todos os pontos da spec (FR-019, Clarifications, Assumptions)? [Clarity, Spec §FR-019, Clarifications]
- [ ] CHK009 - "Exceção de negócio descritiva" (FR-010) e "exceção de erro de cálculo" (FR-020) estão diferenciadas com especificidade suficiente para que o chamador as distinga programaticamente? [Clarity, Spec §FR-010, FR-020]
- [ ] CHK010 - `Soma_Taxas` tem uma fórmula canônica única definida uma vez (FR-011) e referenciada de forma consistente em FR-012, FR-013 e FR-018, sem redefinições locais? [Clarity, Spec §FR-011]
- [ ] CHK011 - FR-013 especifica que o resíduo é calculado em centavos inteiros (garantindo que `Σ taxa_proporcional == Soma_Taxas` passe em FR-018 sem drift de arredondamento)? [Clarity, Spec §FR-013]

---

## Consistência dos Requisitos

- [ ] CHK012 - FR-007 (comparação em centavos, regra 1.2) está alinhado com FR-016 (toda aritmética monetária em centavos) sem contradição? [Consistency, Spec §FR-007, FR-016]
- [ ] CHK013 - FR-013 ("último ativo") é consistente com US4 que também descreve "29º ativo recebe o resíduo"? [Consistency, Spec §FR-013, US4]
- [ ] CHK014 - A convenção de sinal em FR-019 (`Σ compras − Σ vendas = valor_liquido_nota`) é consistente com a definição de sinal nas Clarifications ("negativo = crédito ao cliente")? [Consistency, Spec §FR-019, Clarifications]
- [ ] CHK015 - FR-014 e FR-015 (valor líquido de COMPRA e VENDA) são consistentes com os cenários 2 e 3 de US2? [Consistency, Spec §FR-014, FR-015, US2]
- [ ] CHK016 - FR-021 (lista de ativos vazia) e FR-010 (falha na Etapa 1) cobrem o mesmo caso? Se sim, a redundância é intencional ou um conflito latente? [Consistency, Spec §FR-021, FR-010]

---

## Cobertura de Cenários

- [ ] CHK017 - Existe um cenário (User Story ou Acceptance Scenario) para nota com exatamente **um único ativo** (absorve 100% via caminho residual)? [Coverage, Spec §Edge Cases]
- [ ] CHK018 - Existe um cenário para nota composta **somente por ativos de VENDA** (sem nenhum BUY), validando a equação de fechamento com `Σ compras = 0`? [Coverage, Spec §Edge Cases]
- [ ] CHK019 - Existe um cenário para dois `NoteAsset` com o **mesmo ticker, mesma quantidade e mesmo preço, mas `especificacao` diferente** (ex.: "BRB111F UNT N2" vs. "BRB111 UNT N2"), validando que não colidem no mapa? [Coverage, Spec §Key Entities]
- [ ] CHK020 - Está especificado o comportamento quando `total_compras_vista = 0` e `total_vendas_vista = volume_total_operado` (nota 100% VENDA)? [Coverage, Edge Case, Spec §FR-008]
- [ ] CHK021 - A nota canônica de `docs/nota.json` (SC-004) está formalmente referenciada como cenário de teste de integração para validar os três passos do pipeline? [Coverage, Spec §SC-004]

---

## 🔴 Risco Obrigatório — Precisão Aritmética [GATING]

*Itens marcados [Gating] bloqueiam aprovação do PR se não resolvidos.*

- [ ] CHK022 - FR-007 descreve com precisão a função de arredondamento aplicada em **ambos os lados** da comparação (`round(qty × price × 100).toLong()` e `round(grossValueTotal × 100).toLong()`)? [Clarity, Spec §FR-007, Gating]
- [ ] CHK023 - FR-012 exclui a possibilidade de acumulação em Double ao longo do loop de 29 ativos antes de converter para centavos inteiros? [Clarity, Spec §FR-012, FR-016, Gating]
- [ ] CHK024 - FR-013 garante que o resíduo é calculado como `Soma_Taxas_centavos − Σ fee_centavos[0..N-2]` (inteiros), nunca como diferença em Double? [Completeness, Spec §FR-013, Gating]
- [ ] CHK025 - FR-018 (regra 3.1, batimento de taxas) compara em centavos inteiros e não em Double, evitando drift de segunda rodada de arredondamento? [Completeness, Spec §FR-018, Gating]
- [ ] CHK026 - FR-019 (regra 3.2, fechamento contábil) especifica que a comparação com `valor_liquido_nota` é feita em centavos inteiros, e não via subtração de Doubles? [Completeness, Spec §FR-019, Gating]

---

## 🔴 Risco Obrigatório — Hierarquia de Exceções [GATING]

- [ ] CHK027 - FR-010 e FR-020 especificam **tipos distintos** de exceção (não apenas mensagens diferentes na mesma classe), permitindo ao chamador capturar seletivamente? [Completeness, Spec §FR-010, FR-020, Gating]
- [ ] CHK028 - O conteúdo mínimo obrigatório da exceção de FR-010 está definido (ex.: qual regra falhou, qual campo, qual ativo)? [Clarity, Spec §FR-010, Gating]
- [ ] CHK029 - O conteúdo mínimo obrigatório da exceção de FR-020 está definido (ex.: valor esperado vs. obtido em centavos, discrepância calculada)? [Clarity, Spec §FR-020, Gating]
- [ ] CHK030 - Todos os gatilhos de FR-010 (FR-006, FR-007, FR-008, FR-009, FR-021, FR-022) produzem o **mesmo tipo** de exceção de negócio, ou o spec distingue subtipos? [Completeness, Spec §FR-010, Gap, Gating]

---

## 🔴 Risco Obrigatório — Identidade de NoteAsset [GATING]

- [ ] CHK031 - A lista completa de campos que participam de `equals`/`hashCode` de `NoteAsset` está explicitamente enumerada na spec (ticker, especificacao, tradeType, quantity, unitPrice, grossValueTotal)? [Completeness, Spec §Key Entities, Gating]
- [ ] CHK032 - O campo `especificacao` está formalmente declarado tanto em FR-004 (modelo de entrada) quanto na entidade `AtivoNota` (Key Entities), sem discrepância de nomenclatura? [Consistency, Spec §FR-004, Key Entities, Gating]
- [ ] CHK033 - `valor_bruto_total` (informado pela fonte) é um campo de `NoteAsset` que participa da identidade estrutural E da validação FR-007 — esses dois papéis estão claramente distinguidos na spec? [Clarity, Spec §FR-004, FR-007, Gating]
- [ ] CHK034 - Está especificado o comportamento quando dois `NoteAsset` são estruturalmente idênticos em todos os campos (colisão de chave em `Map<NoteAsset, Double>`)? [Coverage, Spec §FR-017, Gap, Gating]

---

## Qualidade dos Critérios de Aceitação

- [ ] CHK035 - SC-004 ("nota canônica processada com sucesso") pode ser verificado objetivamente sem cálculo manual dos 29 ativos? Existe um resultado esperado documentado por ativo? [Measurability, Spec §SC-004]
- [ ] CHK036 - SC-001 ("diferença = R$ 0,00") é inequívoco quanto a se a comparação é em centavos inteiros ou em Double exibido ao chamador? [Clarity, Spec §SC-001]
- [ ] CHK037 - SC-003 define o que constitui "violação detectável" (limitando o escopo da garantia de 100%)? [Clarity, Spec §SC-003]
- [ ] CHK038 - Cada SC (SC-001 a SC-005) é rastreável a pelo menos um FR e uma User Story? [Traceability, Spec §Success Criteria]

---

## Edge Cases e Condições de Contorno

- [ ] CHK039 - O comportamento para um ativo com `valor_unitario` ausente (ex.: campo `unitario` com typo na fonte, como SBFG3 em `docs/nota.json`) está endereçado? Parsing está fora do escopo — mas o comportamento do validador com campo nulo/zero é definido pelo FR-009? [Coverage, Edge Case, Spec §FR-009, Assumptions]
- [ ] CHK040 - Estão especificados os requisitos para valores de entrada na fronteira exata de 0,5 centavo (caso de empate ROUND_HALF_UP na regra FR-012)? [Coverage, Edge Case, Spec §FR-012]

---

## Dependências e Premissas

- [ ] CHK041 - A premissa "parsing está fora do escopo" está formalmente reconciliada com FR-004 que diz "DEVE conter especificação" — quem garante que esse campo estará presente? [Assumption, Spec §FR-004, Assumptions]
- [ ] CHK042 - A premissa "campos numéricos já foram parseados" está vinculada a uma restrição formal no construtor da entidade (ex.: campos non-null), ou é apenas uma assunção implícita? [Assumption, Spec §Assumptions]

---

## Notas

- Itens **[Gating]** (CHK022–CHK034) bloqueiam aprovação do PR; devem ser resolvidos ou explicitamente aceitos com justificativa técnica documentada.
- Marcar como concluído: `[x]`
- Adicionar observações ou evidências inline após cada item
- Para itens **[Gap]**: criar um FR ou registrar como limitação conhecida na seção Assumptions
