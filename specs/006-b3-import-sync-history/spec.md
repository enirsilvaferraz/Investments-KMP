# Feature Specification: Sincronização de Histórico via Importação B3

**Feature Branch**: `006-b3-import-sync-history`

**Created**: 2026-05-27

**Status**: Draft

**Input**: User description: "Aprimorar a importação dos dados via xlsx. O DataSource de importação passa a retornar objetos de domínio B3Record (identificador + valor). Cada subtipo de posição B3 implementa dois métodos: identificador e valor final. O UseCase busca o histórico atual, relaciona os ativos do histórico com os da importação pelo identificador, substitui o valor atual pelo valor da B3 e salva. Emite log por ativo: atualizado, ignorado ou não registrado."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Importar arquivo XLSX e atualizar valores do histórico automaticamente (Priority: P1)

O usuário seleciona um arquivo XLSX exportado da B3. O sistema processa o arquivo, identifica cada posição de ativo (ações, ETFs, Tesouro Direto e renda fixa com identificador cadastrado), localiza os respectivos ativos no histórico do período atual e substitui o valor registrado pelo valor atualizado vindo da B3. Ao final, o histórico reflete os saldos reais reportados pela bolsa.

**Why this priority**: Essa é a entrega central da feature; sem ela, nenhuma outra parte tem valor.

**Independent Test**: Importar um arquivo com pelo menos um ativo variável (ação/ETF) e um de Tesouro Direto; verificar que os valores do histórico foram atualizados para os valores presentes no arquivo. Confirmar que os dados são persistidos após a importação.

**Acceptance Scenarios**:

1. **Given** existe um histórico do período atual com um ativo de renda variável cujo ticker coincide com o identificador presente na importação, **When** o usuário importa o arquivo XLSX, **Then** o valor do ativo no histórico é substituído pelo valor retornado pela B3 e persistido no banco.
2. **Given** existe um ativo de Tesouro Direto no histórico cujo identificador B3 coincide com o identificador retornado na importação, **When** o usuário importa o arquivo, **Then** o valor do ativo é atualizado e persistido.
3. **Given** existe um ativo de renda fixa com identificador B3 cadastrado cujo valor coincide com o identificador retornado na importação, **When** o usuário importa o arquivo, **Then** o valor do ativo é atualizado e persistido.
4. **Given** um arquivo XLSX com múltiplos tipos de ativo (ações, ETFs, FIIs, Tesouro Direto, renda fixa com identificador), **When** o usuário importa o arquivo, **Then** todos os ativos com correspondência no banco têm seus valores atualizados; os sem correspondência geram log de "não registrado" sem causar erro.

---

### User Story 2 - Acompanhar o progresso da importação por meio de log em tempo real (Priority: P2)

À medida que o sistema processa cada posição importada e cada ativo do histórico, exibe mensagens de log indicando o resultado individual de cada item. As categorias são:

- **atualizado**: valor do histórico substituído com sucesso pelo valor da B3.
- **não registrado**: posição da importação com identificador não encontrado no banco de dados do usuário.
- **identificador inexistente**: ativo do histórico possui identificador B3 cadastrado (ticker ou `b3Identifier`), mas esse identificador **não foi encontrado** no arquivo importado.
- **ignorado**: ativo do histórico **não possui** identificador B3 aplicável — `InvestmentFundAsset` (fundos multimercado, ações, previdência) e `FixedIncomeAsset` sem `b3Identifier` cadastrado.

**Why this priority**: Permite ao usuário entender quais ativos foram sincronizados e quais requerem atenção — seja por cadastro pendente, identificador ausente ou divergência com o que a B3 reportou.

**Independent Test**: Importar um arquivo com (a) um ativo presente no banco com identificador coincidente, (b) um ativo na B3 não cadastrado no banco, (c) um ativo no banco com identificador B3 que não aparece na importação e (d) um fundo multimercado no histórico. Verificar que quatro mensagens de log distintas são emitidas, uma de cada categoria.

**Acceptance Scenarios**:

1. **Given** um ativo da importação tem correspondência no histórico pelo identificador, **When** o valor é substituído, **Then** o sistema emite log de **"ativo atualizado"** com o identificador do ativo.
2. **Given** um ativo da importação tem identificador preenchido mas não existe correspondente no histórico do período, **When** o sistema processa esse ativo, **Then** emite log de **"ativo não registrado"** indicando o identificador que não foi encontrado no banco.
3. **Given** um ativo do histórico possui identificador B3 cadastrado mas esse identificador não consta em nenhuma posição do arquivo importado, **When** a importação é concluída, **Then** o sistema emite log de **"identificador inexistente"** indicando o identificador que estava no banco mas não foi encontrado na importação.
4. **Given** o histórico contém um `InvestmentFundAsset` ou uma `FixedIncomeAsset` sem `b3Identifier` cadastrado, **When** a importação é concluída, **Then** o sistema emite log de **"ativo ignorado"** para cada um desses ativos, informando que não possuem identificador B3.

---

### User Story 3 - Regras de correspondência por tipo de ativo (Priority: P1)

O sistema aplica regras de correspondência por tipo de ativo ao correlacionar posições importadas da B3 com os ativos do histórico. É importante distinguir dois conceitos de "fundo": a aba "Fundo de Investimento" do extrato B3 representa **FIIs** (fundos imobiliários), que no sistema são cadastrados como **renda variável** com ticker. Os **fundos do sistema** (multimercado, ações, previdência — `InvestmentFundAsset`) não aparecem no extrato B3 e não participam desta sincronização.

- **Renda variável** (ações, ETFs e FIIs da B3): chave de correspondência é o **ticker** do ativo no banco versus o identificador retornado pela posição B3.
- **Tesouro Direto e renda fixa com identificador B3 cadastrado**: chave de correspondência é o **identificador B3** do cadastro do ativo versus o identificador retornado pela posição B3.
- **Renda fixa sem identificador B3 cadastrado**: sem correspondência — gera log de "não registrado" se a posição vier na importação, ou silenciosamente ignorada no passe do histórico.

**Why this priority**: Sem regras corretas de matching, ativos errados podem ter seus valores substituídos, comprometendo a integridade do histórico.

**Independent Test**: Importar arquivo com uma ação (ticker "PETR4"), um FII (ticker "HGLG11") e uma renda fixa com identificador "RF-XYZ". Verificar que cada ativo no banco é atualizado apenas quando o campo de correspondência correto coincide.

**Acceptance Scenarios**:

1. **Given** o banco contém um ativo de renda variável com ticker "PETR4" e a importação contém uma posição com identificador "PETR4", **When** o sistema processa a importação, **Then** o ativo de renda variável "PETR4" é atualizado e nenhum outro ativo é alterado por esse registro.
2. **Given** o banco contém um FII com ticker "HGLG11" (cadastrado como renda variável) e a importação contém uma posição da aba "Fundo de Investimento" com ticker "HGLG11", **When** o sistema processa a importação, **Then** o ativo é atualizado corretamente.
3. **Given** o banco contém uma renda fixa com identificador B3 "ABC-001" e a importação contém uma posição com identificador "ABC-001", **When** o sistema processa a importação, **Then** apenas aquele ativo de renda fixa é atualizado.
4. **Given** a importação contém uma posição com identificador que não corresponde a nenhum ativo do banco, **When** o sistema processa essa posição, **Then** nenhum ativo é atualizado e é emitido log de "não registrado".

---

### Edge Cases

- **Arquivo vazio ou sem abas reconhecidas**: A importação conclui sem erro, sem atualizar nenhum ativo, e o log indica que nenhuma posição foi processada.
- **Valor numérico inválido ou ausente no arquivo**: A posição com valor inválido é ignorada com log de erro descritivo; o restante do arquivo continua sendo processado.
- **Dois ativos no banco com o mesmo ticker ou identificador B3**: A correspondência atualiza todos os registros coincidentes (comportamento consistente com o existente para holdings duplicadas).
- **Histórico do período atual ausente**: Se não existir entrada de histórico para o período corrente para um ativo identificado, o sistema emite log de "não registrado" ao invés de criar novo registro.
- **Ativo do histórico com identificador não presente na importação**: O ativo não é alterado e o sistema emite log de "identificador inexistente". Isso pode ocorrer quando o usuário importa um extrato parcial que não cobre todos os ativos cadastrados.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE alterar a interface `B3ImportDataSource` para que retorne uma lista de `B3Record` (objeto de domínio com identificador e valor) ao invés de registrar linhas diretamente.
- **FR-002**: Cada subtipo de posição B3 DEVE implementar dois métodos herdados de `B3Position`: um que retorna o **identificador** da posição e outro que retorna o **valor final** — sem adicionar novas propriedades de dados. Mapeamento: ações e ETFs usam `ticker` e `updatedValue`; FIIs (aba "Fundo de Investimento") usam `ticker` e `updatedValue`; renda fixa usa `code` e `curveValue`; Tesouro Direto usa `isinCode` e `updatedValue`.
- **FR-003**: O `B3ImportDataSourceImpl` DEVE usar os dois métodos de `B3Position` para construir a lista de `B3Record`, descartando linhas em branco (conforme `isBlankRow()` existente) e linhas com valor inválido (com log de aviso).
- **FR-004**: O sistema DEVE disponibilizar um UseCase que, após a importação, busque o histórico do período atual e correlacione as posições importadas com os ativos do histórico segundo as regras de matching.
- **FR-005**: Para **renda variável** (ações, ETFs e FIIs), a chave de correspondência DEVE ser o **ticker** do ativo cadastrado no banco comparado ao identificador do `B3Record`, com comparação **exata (case-sensitive)**.
- **FR-006**: Para **Tesouro Direto** e **renda fixa com identificador B3 cadastrado**, a chave de correspondência DEVE ser o **identificador B3** do ativo no banco comparado ao identificador do `B3Record`, com comparação **exata (case-sensitive)**.
- **FR-007**: `InvestmentFundAsset` (fundos multimercado, ações, previdência) e `FixedIncomeAsset` sem `b3Identifier` cadastrado não participam do matching e DEVEM gerar log de **ignorado** no passe do histórico.
- **FR-008**: Quando houver correspondência, o sistema DEVE substituir o `endOfMonthValue` do registro de histórico pelo valor do `B3Record` e **persistir** a alteração.
- **FR-009**: O UseCase DEVE emitir mensagens de log progressivas com quatro categorias: **atualizado**, **não registrado**, **identificador inexistente** e **ignorado**.
- **FR-010**: As mensagens de log DEVEM ser emitidas progressivamente à medida que cada posição é processada, não apenas ao final da importação.
- **FR-011**: O processamento DEVE continuar para as demais posições mesmo que uma posição individual falhe (isolamento de erro por posição).

### Key Entities

- **B3Record**: Objeto de domínio imutável com dois campos — **identificador** (texto que identifica o ativo na B3, podendo ser ausente para tipos sem identificador) e **valor** (valor financeiro atualizado reportado pela B3).
- **Posição B3** (`B3Position`): Representação de uma linha do arquivo XLSX exportado da B3; cada subtipo define como extrai seu identificador e valor final sem adicionar novas propriedades de dados.
- **Registro de histórico** (`HoldingHistoryEntry`): Entrada mensal do histórico de posicionamento de um ativo; tem seu valor substituído quando correspondência com `B3Record` é encontrada.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos ativos de renda variável e Tesouro Direto presentes no arquivo XLSX e com correspondência no banco têm seus valores de histórico atualizados após a importação, verificado em teste de aceitação com arquivo real.
- **SC-002**: 0 atualizações indevidas ocorrem para renda fixa sem identificador B3 cadastrado e para fundos multimercado/previdência, confirmado por inspeção do banco antes e depois da importação.
- **SC-003**: Para cada item processado (posição B3 ou ativo do histórico), exatamente uma mensagem de log da categoria correta (atualizado / não registrado / identificador inexistente / ignorado) é emitida.
- **SC-004**: Nenhum dado de histórico pré-existente é perdido ou corrompido após a importação, mesmo que o arquivo contenha posições sem correspondência ou com valores inválidos.

## Assumptions

- O período de referência para busca e atualização do histórico é o **período atual** (mês corrente), conforme `GetCurrentYearMonthUseCase` já existente.
- O campo **identificador B3** de renda fixa é o introduzido pela feature `004-fixed-income-b3-id`; renda fixa sem esse campo cadastrado não participa da sincronização.
- O identificador de `B3TreasuryPosition` é o campo `isinCode` já presente; não é necessário alterar propriedades do modelo de dados da posição.
- **Identificador por subtipo**: `B3StockPosition` → `ticker`; `B3EtfPosition` → `ticker`; `B3FundPosition` → `ticker` (representa FIIs); `B3FixedIncomePosition` → `code`; `B3TreasuryPosition` → `isinCode`. Nenhum novo campo é adicionado aos DTOs.
- **Valor final por subtipo**: `B3StockPosition`, `B3EtfPosition`, `B3FundPosition`, `B3TreasuryPosition` → `updatedValue`; `B3FixedIncomePosition` → `curveValue` (Valor Atualizado CURVA).
- A aba "Fundo de Investimento" do extrato B3 contém FIIs, que no sistema são `VariableIncomeAsset` com tipo `REAL_ESTATE_FUND`. `InvestmentFundAsset` (fundos multimercado, ações, previdência) não aparece no extrato B3.
- `AssetHolding.asset` já carrega a categoria (`InvestmentCategory`) que permite ao UseCase selecionar a regra de matching correta.
- O log não é persistido no banco — destina-se à saída de console/UI durante a sessão de importação.
- Esta feature **não** altera a interface visual de importação; o usuário continua selecionando o arquivo XLSX pelo mesmo fluxo existente.

## Clarifications

### Session 2026-05-27

- Q: Qual campo de cada subtipo de posição B3 deve ser retornado como "valor final"? → A: `updatedValue` para ações, ETFs e FIIs; `curveValue` (Valor Atualizado CURVA) para renda fixa; `updatedValue` para Tesouro Direto.
- Q: Qual campo `B3FundPosition` usa como identificador? → A: `ticker` — a aba "Fundo de Investimento" da B3 representa FIIs, que no sistema são renda variável (`VariableIncomeAsset`); os fundos multimercado/previdência do sistema (`InvestmentFundAsset`) não aparecem no extrato B3.
- Q: Implementação deve ser simples (sem `B3SyncLogEvent`)? → A: Sim — log via `println` direto; sem sealed interface de eventos.
- Q: Quais ativos do histórico devem gerar log de "ignorado"? → A: `InvestmentFundAsset` (fundos multimercado, ações, previdência) e `FixedIncomeAsset` sem `b3Identifier` cadastrado — esses ativos não têm chave de correspondência com o extrato B3.
- Q: A comparação de identificadores no matching deve ser case-sensitive ou case-insensitive? → A: Case-sensitive — comparação exata de texto.
- Q: Comportamento no timeout — manter atualizações parciais ou reverter? → A: Timeout fora de escopo desta spec; sem controle de tempo limite.

## Out of Scope

- Alteração da tela de seleção de arquivo ou do fluxo de upload.
- Validação de formato do arquivo XLSX além do já existente.
- Persistência ou histórico das mensagens de log geradas durante a importação.
- Criação de novos registros de histórico para ativos presentes na B3 mas não cadastrados no banco (apenas log de "não registrado").
- Sincronização automática ou agendada sem ação explícita do usuário.
- Suporte a outros formatos de arquivo além de XLSX.
- Controle de timeout ou cancelamento da operação de importação.
