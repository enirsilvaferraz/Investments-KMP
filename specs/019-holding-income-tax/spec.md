# Feature Specification: Imposto de renda regressivo sobre rendimentos da posição

**Feature Branch**: `019-holding-income-tax`

**Created**: 2026-06-04

**Status**: Draft

**Input**: User description: "Vamos implementar o conceito de imposto de renda de um ativo. Baseie-se no padrão de objeto de regra de negócio de crescimento de posição (valor absoluto + percentual calculados a partir de entradas). As regras estão na tabela de IR regressivo (imposto sempre sobre rendimentos; tarifa diminui com o tempo investido). O novo objeto terá duas propriedades: percentual aplicado e valor do imposto. Recebe como parâmetro o lucro do ativo em reais, a data de compra (derivada das transações da posição) e a data atual."

## Clarifications

### Session 2026-06-04

- Q: O domínio deve arredondar `valor do imposto` a 2 casas em `calculate`, ou devolver `Double` bruto? → A: **Double bruto** no domínio; arredondamento/formato monetário fica na **UI ou camada de apresentação**.
- Q: Nome da propriedade da alíquota no código? → A: **`taxRate`** (inglês; substitui `appliedPercentage`), em percentual legível (22,5 = 22,5%).
- Q: Incluir `earliestPurchaseDate` em `List<AssetTransaction>`? → A: **Fora de escopo** — a data de compra é **sempre** parâmetro do chamador; derivação a partir de transações fica para feature/use case futuro.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Estimar IR sobre lucro realizado ou projetado (Priority: P1)

O investidor quer saber **quanto de imposto de renda incidiria** sobre o **lucro em reais** de uma posição, considerando há quanto tempo o capital está investido desde a **compra**. O sistema aplica a **tabela regressiva oficial** (22,5% → 20% → 17,5% → 15%) conforme o tempo entre a data de compra e a data de referência (hoje ou outra data escolhida para simulação).

**Why this priority**: Entrega o valor central — percentual da faixa e valor monetário do imposto — sem depender de ecrãs ou integrações externas.

**Independent Test**: Fornecer lucro positivo, data de compra e data atual em cada faixa da tabela; verificar percentual e valor do imposto esperados. Repetir com lucro zero ou negativo e validar imposto zero.

**Acceptance Scenarios**:

1. **Given** lucro de R$ 1.000,00, **181 dias** entre compra e data atual, **When** o cálculo é executado, **Then** a alíquota (`taxRate`) é **20%** e o valor do imposto é **R$ 200,00**.
2. **Given** lucro de R$ 500,00, **até 180 dias** inclusive (ex.: 90 ou 180 dias), **When** o cálculo é executado, **Then** a alíquota (`taxRate`) é **22,5%** e o valor do imposto é **R$ 112,50**.
3. **Given** lucro de R$ 800,00, **entre 361 e 720 dias** inclusive, **When** o cálculo é executado, **Then** a alíquota (`taxRate`) é **17,5%** e o valor do imposto é **R$ 140,00**.
4. **Given** lucro de R$ 2.000,00, **mais de 720 dias** (ex.: 721 dias), **When** o cálculo é executado, **Then** a alíquota (`taxRate`) é **15%** e o valor do imposto é **R$ 300,00**.
5. **Given** lucro **zero ou negativo**, **When** o cálculo é executado, **Then** a **alíquota** (`taxRate`) reflete a faixa temporal correspondente e o **valor do imposto é zero** (imposto incide sobre rendimentos, não sobre prejuízo).

---

### User Story 2 - Fronteiras exatas da tabela (Priority: P2)

O investidor e os testes de regressão precisam de comportamento **determinístico** nos limites de cada faixa (180, 360, 720 dias), evitando ambiguidade entre “até” e “de … a …” da tabela em anexo.

**Why this priority**: Erros de off-by-one em dias mudam a alíquota e o valor do imposto.

**Independent Test**: Calcular com exatamente 180, 181, 360, 361, 720 e 721 dias entre compra e data atual; comparar com a tabela.

**Acceptance Scenarios**:

1. **Given** **180 dias** entre compra e data atual, **When** o cálculo é executado, **Then** aplica-se a faixa **até 180 dias** (22,5%).
2. **Given** **181 dias**, **When** o cálculo é executado, **Then** aplica-se a faixa **181 a 360 dias** (20%).
3. **Given** **360 dias**, **When** o cálculo é executado, **Then** permanece na faixa **181 a 360 dias** (20%).
4. **Given** **361 dias**, **When** o cálculo é executado, **Then** aplica-se a faixa **361 a 720 dias** (17,5%).
5. **Given** **720 dias**, **When** o cálculo é executado, **Then** permanece na faixa **361 a 720 dias** (17,5%).
6. **Given** **721 dias**, **When** o cálculo é executado, **Then** aplica-se a faixa **acima de 720 dias** (15%).

---

### Edge Cases

- **Data de compra posterior à data atual**: o cálculo **não** deve produzir resultado válido; a integração deve tratar como entrada inválida.
- **Mesmo dia de compra e referência** (0 dias decorridos): enquadra-se em **até 180 dias** (22,5%).
- **Lucro positivo com valor muito pequeno** (ex.: R$ 0,01): imposto proporcional ao lucro em `Double` bruto no domínio; formatação a duas casas decimais só na apresentação.
- **Múltiplas compras / derivação da data**: fora do escopo — o chamador passa **uma** `purchaseDate` já resolvida; lotes e `earliestPurchaseDate` ficam para evolução futura.
- **Ativos isentos ou com regra fiscal diferente** (ex.: alguns títulos ou ações): o objeto aplica **sempre** a tabela regressiva fornecida; exclusões ficam a cargo de quem chama o cálculo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE expor um resultado de imposto de renda com exatamente duas informações de saída: **alíquota** (`taxRate`, percentual legível da faixa, ex.: 22,5 para 22,5%) e **valor do imposto** (`taxValue`, em reais).
- **FR-002**: O cálculo DEVE receber três entradas: **lucro em reais** (rendimento sobre o qual incide o imposto), **data de compra** e **data atual** (ou data de referência equivalente).
- **FR-003**: O sistema DEVE determinar o **tempo investido** como o número de **dias corridos** entre a data de compra e a data atual, com convenção de fronteira definida em FR-004.
- **FR-004**: O sistema DEVE aplicar a tabela regressiva de IR conforme anexo:
  - **Até 180 dias** inclusive → alíquota **22,5%**
  - **De 181 a 360 dias** inclusive → alíquota **20%**
  - **De 361 a 720 dias** inclusive → alíquota **17,5%**
  - **Acima de 720 dias** → alíquota **15%**
- **FR-005**: O **valor do imposto** (`taxValue`) DEVE ser `lucro × (taxRate / 100)` quando o lucro for **estritamente positivo**; caso contrário, `taxValue` DEVE ser **zero**. O domínio **NÃO** arredonda monetariamente o resultado — devolve precisão `Double` integral; arredondamento a duas casas decimais é responsabilidade da UI/apresentação.
- **FR-006**: O imposto DEVE incidir **somente sobre rendimentos** (lucro informado); não há imposto sobre prejuízo ou lucro nulo/negativo.
- **FR-007**: A cobrança DEVE ser **regressiva**: quanto maior o tempo investido (mais dias), menor ou igual a `taxRate`, nunca invertendo a ordem das faixas.
- **FR-008**: Se a data de compra for **posterior** à data de referência, o cálculo DEVE falhar de forma explícita (sem valores fictícios de imposto).
- **FR-009**: O objeto de regra de negócio DEVE seguir o mesmo **papel** que o de crescimento de posição: valor imutável produzido por operação de cálculo a partir de entradas, sem estado global, adequado a testes unitários com entradas e saídas fixas.

### Key Entities

- **Resultado de imposto de renda (IR da posição)**: encapsula **`taxRate`** (alíquota) e **`taxValue`** (valor do imposto) após aplicar a tabela sobre o lucro e o tempo investido.
- **Lucro em reais**: rendimento financeiro da posição no contexto do cálculo (pode vir de valorização, resgate parcial ou outro agregador já existente no produto).
- **Data de compra**: marco temporal de início do investimento para a faixa regressiva; **entrada obrigatória** fornecida pelo chamador (não calculada nesta feature).
- **Data de referência**: normalmente “hoje”, mas pode ser outra data para simulação ou fecho de período.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Para **100%** dos casos de teste documentados nas faixas (incluindo dias 180, 181, 360, 361, 720, 721), `taxRate` e `taxValue` coincidem com a tabela e a fórmula `lucro × (taxRate / 100)`.
- **SC-002**: Para lucro ≤ 0 em qualquer faixa temporal, o valor do imposto é **sempre zero** em testes automatizados.
- **SC-003**: Um investidor consegue, a partir de lucro e datas informadas, **prever o imposto** sem consultar tabela externa; na **exibição** (após arredondamento a duas casas na apresentação), a divergência face ao valor bruto do domínio é no máximo **R$ 0,01** em cenários de exemplo do produto. *(Fora de escopo v1 — validação na camada de apresentação; ver [tasks.md](./tasks.md) traceability.)*

## Assumptions

- A tabela da imagem em anexo refere-se ao **IR regressivo sobre rendimentos** típico de investimentos sujeitos a essa regra no Brasil; o núcleo desta feature é o **motor de cálculo**, não a decisão fiscal de quais classes de ativo entram em cada regime na UI.
- **Contagem de dias**: diferença em dias corridos entre data de compra e data atual (mesma convenção de calendário usada no restante do produto para `LocalDate`).
- **Derivação da data de compra** (`earliestPurchaseDate`, primeira compra a partir de `holding.transactions`, FIFO por lote): **fora de escopo** nesta feature.
- **`taxRate` na saída**: alíquota em **percentual legível** (22,5 para 22,5%, e não 0,225 em formato decimal); identificador em inglês no código.
- **Arredondamento monetário**: o motor de domínio **não** arredonda `valor do imposto`; a UI/apresentação formata em reais com duas casas decimais (arredondamento matemático padrão), salvo norma contábil futura.
- **Lucro** já vem calculado por outro componente (ex.: crescimento/valorização); este objeto **não** recalcula lucro a partir de preços ou transações.
- Exibição em ecrãs, retenção na fonte, DARF, isenções e **helpers de data de compra a partir de transações** **não** fazem parte desta feature — apenas `IncomeTax.calculate` com entradas explícitas.

## Viabilidade dos parâmetros pedidos

**Conclusão**: O cálculo é **viável** com os três parâmetros indicados (**lucro em reais**, **data de compra**, **data de referência**). O motor **não** lê transações nem posição — quem integra resolve a data de compra (ex.: use case futuro sobre `holding.transactions`) antes de chamar `calculate`.
