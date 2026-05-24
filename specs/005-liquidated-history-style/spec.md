# Feature Specification: Destaque visual de investimentos liquidados no Histórico

**Feature Branch**: `005-liquidated-history-style`

**Created**: 2026-05-24

**Status**: Draft

**Input**: User description: "Vamos identificar um registro de historico de investimento que já foi liquidado. Um investimento é declarado liquidado se o valor atual for igual a 0. Se um investimento estiver liquidado devemos colorir os textos (de todas as colunas) exibidos na tabela de historico da cor cinza (mesma cor da coluna de valorização quando está zerada), exceto icones."

## Clarifications

### Session 2026-05-24

- Q: Em linha liquidada, o texto dos tooltips dos ícones (B3, liquidez, etc.) deve usar cinza de liquidado ou estilo padrão? → A: Tooltips mantêm o estilo padrão atual (não aplicar cinza de liquidado).
- Q: O cinza de liquidado compete com cores por sinal (positivo/negativo) nas colunas? → A: (Revisão) As regras de cor das colunas **Valorização** e **Transações** **têm prioridade** sobre o cinza de liquidado; nas **demais** colunas de texto, o cinza de liquidado aplica-se quando valor atual = 0. Ícones e tooltips permanecem conforme já definido.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reconhecer posições liquidadas na tabela de Histórico (Priority: P1)

O usuário consulta a tela de **Histórico** (posicionamento no período selecionado) e precisa distinguir rapidamente quais linhas representam investimentos **já liquidados** — ou seja, posições cujo **valor atual** registrado para aquele período é **zero**. Linhas liquidadas exibem em **cinza** (mesmo tom visual já usado para **Valorização** quando o percentual é zero) os textos das colunas **exceto** onde já existem regras de cor por valor: nas colunas **Valorização** e **Transações**, as cores atuais por sinal (positivo, negativo, zero, alerta) **mantêm prioridade** sobre o cinza de liquidado. Nas demais colunas (corretora, nome, observação, valores monetários das outras colunas, valor atual, etc.), o cinza de liquidado aplica-se normalmente. **Ícones** (categoria, liquidez, identificador B3, ações com ícone, etc.) **mantêm** suas cores habituais.

**Why this priority**: Reduz erro de interpretação em revisões mensais — o usuário não trata posição zerada como posição ativa a atualizar.

**Independent Test**: No histórico, comparar linha com valor atual 0 (liquidada) e linha ativa; confirmar cinza nas colunas gerais da liquidada, cores semânticas preservadas em Valorização/Transações quando aplicável, e ícones inalterados.

**Acceptance Scenarios**:

1. **Given** uma linha na tabela de histórico com **valor atual igual a 0**, **When** o usuário visualiza a tabela, **Then** os textos das colunas **Corretora**, **nome**, **Observação**, **Valor Anterior**, **Valor Atual** e demais colunas **fora** de Valorização e Transações aparecem em **cinza** no mesmo padrão visual da valorização zerada.
2. **Given** uma linha na tabela de histórico com **valor atual igual a 0**, **When** o usuário visualiza a coluna de **ícones** (categoria, liquidez, identificador B3, ícones de ação), **Then** as cores desses **ícones** permanecem as mesmas de uma linha **não** liquidada (não são forçadas ao cinza dos textos).
3. **Given** uma linha liquidada com ícone que possui tooltip (ex.: identificador B3), **When** o usuário exibe o tooltip, **Then** o **texto do tooltip** mantém o **estilo padrão** de tooltip (sem cinza de liquidado).
4. **Given** uma linha na tabela de histórico com **valor atual maior que 0**, **When** o usuário visualiza a tabela, **Then** os textos dessa linha seguem o esquema de cores **atual** (sem aplicar o cinza de liquidado), inclusive regras já existentes para valorização positiva/negativa e saldo de transações.
5. **Given** uma linha liquidada (valor atual 0), **When** o usuário altera o valor atual para um valor **diferente de zero** e a alteração é persistida naquele período, **Then** a linha deixa de ser tratada como liquidada e os textos voltam ao esquema de cores padrão da linha ativa.
6. **Given** uma linha com valor atual positivo, **When** o usuário define o valor atual como **0** e salva, **Then** a linha passa a ser exibida como liquidada (cinza nas colunas gerais, cores semânticas em Valorização/Transações conforme regra, ícones inalterados).
7. **Given** uma linha liquidada com **valorização positiva, negativa ou zero** e/ou **saldo de transações** positivo, negativo ou zero, **When** o usuário visualiza as colunas **Valorização** e **Transações**, **Then** os textos seguem as **mesmas cores** que uma linha ativa (verde, vermelho, cinza de zero, alerta, etc.) — as regras dessas colunas **não** são substituídas pelo cinza de liquidado.
8. **Given** uma linha liquidada com valorização **zero** na coluna Valorização, **When** o usuário visualiza essa célula, **Then** o percentual permanece no **cinza de valorização zerada** (regra da coluna), não por substituição genérica de outras colunas.

---

### User Story 2 - Consistência entre tipos de investimento no Histórico (Priority: P2)

A regra de liquidação (valor atual = 0) e o estilo cinza em textos aplicam-se a **todas** as linhas da tabela de histórico, independentemente de serem renda fixa, renda variável ou fundo — desde que o critério de valor atual zero seja atendido para aquele registro no período.

**Why this priority**: Evita que o usuário interprete apenas parte da carteira como liquidada.

**Independent Test**: Comparar no mesmo período uma linha de renda fixa e uma de renda variável, ambas com valor atual 0; ambas devem exibir cinza nas colunas gerais, regras de cor em Valorização/Transações preservadas, e ícones com cores normais.

**Acceptance Scenarios**:

1. **Given** linhas de histórico de **renda fixa**, **renda variável** e **fundo** com valor atual 0 no período, **When** o usuário visualiza a tabela, **Then** em **todas** essas linhas os textos das colunas gerais aparecem em cinza de liquidado, Valorização/Transações mantêm cores por regra própria, e os ícones mantêm cores habituais.
2. **Given** linha de renda fixa com identificador B3 **não informado** (ícone de aviso amarelo) e valor atual 0, **When** o usuário visualiza a linha, **Then** o ícone de aviso permanece **amarelo**, textos das colunas gerais ficam cinza, e Valorização/Transações seguem suas cores semânticas.

---

### Edge Cases

- **Valor atual exatamente zero**: Somente `valor atual == 0` define liquidação; valores muito pequenos mas não zero **não** são liquidados.
- **Valor anterior diferente de zero com valor atual zero**: Linha é liquidada; coluna Valor Anterior em cinza de liquidado; Valorização e Transações seguem regras próprias.
- **Precedência de cores**: Em linha liquidada, **Valorização** e **Transações** mantêm as regras de cor por sinal/valor **existentes** (prioridade sobre cinza de liquidado); nas **outras** colunas de texto, aplica-se o cinza de liquidado. Ícones e tooltips seguem exceções já definidas.
- **Valorização positiva ou negativa com valor atual zero**: Na coluna Valorização, exibe-se **verde** ou **vermelho** conforme a regra atual; o cinza de liquidado **não** substitui essas cores.
- **Valorização zero com valor atual zero**: Na coluna Valorização, exibe-se o **cinza de percentual zero** (regra da coluna), coerente com o tom de referência do liquidado.
- **Saldo de transações positivo, negativo ou zero com valor atual zero**: Na coluna Transações, o texto ("Adicionar", saldo formatado, etc.) segue **verde**, **vermelho**, **alerta** ou **cinza de saldo zero** conforme a regra atual — o cinza de liquidado **não** substitui essas cores.
- **Campo editável de valor atual**: Quando o valor atual é 0, o conteúdo exibido nessa coluna segue o cinza de liquidado; se o usuário editar para valor não zero, a linha deixa de ser liquidada após salvar.
- **Botões com texto na coluna Transações**: Cores do texto obedecem à **regra de Transações** (ex.: saldo positivo em verde), não ao cinza genérico de liquidado de outras colunas.
- **Tooltips de ícones**: Em linha liquidada, o **texto do tooltip** (ao passar o cursor ou equivalente) **não** usa cinza de liquidado — mantém o estilo padrão de tooltip do produto.
- **Valor atual negativo**: Não é liquidado (`valor atual != 0`); a linha mantém o esquema de cores atual das colunas.
- **Resumo / painéis fora da tabela**: Fora do escopo desta feature — apenas as **linhas da tabela de histórico** recebem o estilo liquidado.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE considerar um registro de histórico de investimento **liquidado** quando o **valor atual** desse registro no período exibido for **igual a zero**.
- **FR-002**: Para cada linha **liquidada**, o sistema DEVE exibir em **cinza** (mesmo padrão visual já usado para texto de **Valorização** quando o percentual é zero) os textos das colunas da tabela **exceto** as colunas **Valorização** e **Transações**.
- **FR-002a**: Nas colunas **Valorização** e **Transações** de linha liquidada, o sistema DEVE aplicar as **regras de cor por sinal/valor já existentes** (positivo, negativo, zero, alerta), que **têm prioridade** sobre o cinza de liquidado — sem forçar cinza de liquidado quando a regra da coluna define outra cor.
- **FR-003**: Para linhas liquidadas, o sistema **NÃO DEVE** alterar as cores dos **ícones** (incluindo ícones de categoria, liquidez, status de identificador B3 e ícones de ação que não são texto).
- **FR-003a**: Para linhas liquidadas, o **texto exibido em tooltips** associados a ícones da linha DEVE manter o **estilo padrão** de tooltip (sem aplicar o cinza de liquidado usado nas células da tabela).
- **FR-004**: Para linhas **não** liquidadas (valor atual diferente de zero), o sistema DEVE manter o comportamento visual atual de cores por coluna, sem aplicar o cinza de liquidado.
- **FR-005**: Quando o valor atual de uma linha deixa de ser zero (após edição e persistência no período), o sistema DEVE remover imediatamente o estilo de linha liquidada dessa linha na exibição do histórico.
- **FR-006**: A regra de liquidação e estilo cinza DEVE aplicar-se a linhas de **todos** os tipos de investimento presentes na tabela de histórico, sem exceção por categoria.
- **FR-007**: O estilo de linha liquidada DEVE aplicar-se **somente** à tabela de histórico de posicionamento; painéis de resumo, filtros e outras áreas da tela permanecem inalterados.

### Key Entities

- **Registro de histórico de investimento (linha)**: Representa a posição de um ativo em uma corretora para um período; inclui **valor atual**, valores anteriores, saldos de transação, percentual de valorização e metadados de exibição (nomes, observações, status de identificador).
- **Estado liquidado (derivado)**: Condição calculada a partir do valor atual = 0; controla o tema visual da linha na tabela, sem exigir campo persistido adicional.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em teste com pelo menos 10 linhas mistas (liquidadas e ativas), **100%** das linhas com valor atual 0 exibem cinza de liquidado nas colunas gerais (fora Valorização/Transações), e **0%** das linhas com valor atual > 0 exibem esse cinza nas colunas gerais.
- **SC-001a**: Em teste com pelo menos 3 linhas liquidadas com valorização ≠ 0 e/ou saldo de transações ≠ 0, **100%** exibem verde/vermelho/alerta conforme regra em Valorização e Transações — **0%** forçam cinza de liquidado nessas células quando a regra da coluna define outra cor.
- **SC-002**: Em teste com linhas liquidadas que possuem ícones de aviso/informação, **100%** dos ícones mantêm a cor semântica original (ex.: aviso amarelo, informação azul) conforme regras já existentes.
- **SC-003**: Usuários conseguem, em revisão de histórico, identificar posições liquidadas **sem ler valores numéricos** — apenas pelo contraste visual da linha — em **menos de 5 segundos** por linha em cenário de teste moderado (até 20 linhas visíveis).
- **SC-004**: Após alterar valor atual de 0 para valor positivo e salvar, a linha deixa de aparecer liquidada na **mesma sessão** de uso, sem necessidade de reiniciar o aplicativo.

## Assumptions

- **Critério numérico**: Comparação de liquidação usa valor atual **exatamente** zero (0), alinhado à regra de negócio informada pelo usuário.
- **Referência visual**: "Mesma cor cinza da valorização zerada" significa reutilizar o **padrão visual existente** no produto para valorização em zero, garantindo consistência perceptiva sem definir códigos de cor na especificação.
- **Escopo de tela**: Limitado à **tabela de histórico** na tela de Histórico; listas de transações detalhadas ou outras telas não são alteradas nesta feature.
- **Ícones vs. texto**: Elementos puramente gráficos (ícones) estão fora do cinza de liquidado; controles que misturam ícone e texto aplicam cinza **apenas** na parte textual quando aplicável.
- **Tooltips**: Conteúdo de tooltip em hover/foco não é “texto de coluna” da tabela; permanece com estilo padrão mesmo em linha liquidada.
- **Persistência**: O valor atual já é persistido pelo fluxo existente; esta feature não altera regras de cálculo de valorização ou de saldo, apenas a **apresentação** condicionada ao valor atual.
- **Precedência visual**: Regras de cor de **Valorização** e **Transações** > cinza de liquidado nessas colunas; cinza de liquidado > cor padrão de texto nas **demais** colunas quando valor atual = 0; ícones e tooltips fora do cinza de liquidado.
