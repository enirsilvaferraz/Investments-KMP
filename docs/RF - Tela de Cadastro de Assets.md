# Requisitos Funcionais - Tela de Cadastro e Edição de Assets

## Índice

1. [Objetivo](#1-objetivo)
2. [Estrutura do Formulário](#2-estrutura-do-formulário)
3. [Campos do Formulário](#3-campos-do-formulário)
4. [Comportamento do Formulário](#4-comportamento-do-formulário)
5. [Casos de Uso](#5-casos-de-uso)

---

## 1. Objetivo

Esta tela tem como objetivo permitir ao usuário cadastrar novos ativos (`Asset`) no sistema e editar ativos já cadastrados. O formulário é dinâmico,
adaptando-se ao tipo de ativo selecionado e exibindo apenas os campos relevantes para cada categoria.

A tela deve suportar tanto o cadastro de novos ativos quanto a edição de ativos existentes através de um campo de seleção inicial.

---

## 2. Estrutura do Formulário

O formulário deve seguir uma estrutura hierárquica com um campo inicial de seleção:

### 2.1. Campo "Investimento" (Dropdown)

O primeiro campo do formulário é um dropdown menu que apresenta as seguintes opções:

| Item            | Descrição                                   | Comportamento                                                                                                                                                                                                                                                                                                                 |
|:----------------|:--------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Item 1**      | "Novo investimento"                         | Item padrão selecionado ao abrir a tela. Quando selecionado, exibe todos os campos do formulário para cadastro de um novo ativo.                                                                                                                                                                                              |
| **Items 2 a n** | Descrições dos investimentos já cadastrados | Cada item representa um ativo existente no sistema. A descrição exibida deve seguir o mesmo formato usado na tabela de consulta (ver seção 2.1 do documento "RF - Tela de Consulta de Ativos"). Quando selecionado, exibe todos os campos do formulário **preenchidos** com os dados do ativo selecionado, permitindo edição. |

**Regras de Exibição:**

- Quando o **Item 1** ("Novo investimento") estiver selecionado, os campos estarão vazios, prontos para cadastro
- Quando qualquer **Item 2+** (investimento existente) estiver selecionado, os campos estarão **preenchidos** com os dados do ativo selecionado

### 2.2. Campos Específicos por Tipo

Após a seleção de "Novo investimento" ou de um investimento existente, o formulário deve exibir:

1. **Campo de Seleção do Tipo de Ativo** (obrigatório)
    - Campo de seleção que determina qual categoria de ativo será cadastrado/editado
    - Opções: "Renda Fixa", "Renda Variável", "Fundos"
    - Este campo controla quais campos adicionais serão exibidos
    - Quando Item 1 ("Novo investimento") estiver selecionado, este campo estará sem seleção
    - Quando Item 2+ (investimento existente) estiver selecionado, este campo estará preenchido com o tipo do ativo (desabilitado)

2. **Campos Específicos por Tipo** (exibidos dinamicamente conforme a seleção do tipo de ativo)

3. **Campos Comuns** (sempre exibidos)
    - Emissor
    - Observações (opcional)

### 2.3. Campo "Emissor" (AutoComplete)

O campo "Emissor" utiliza o componente AutoComplete que permite:

- **Seleção de Emissor Existente**: O usuário pode selecionar um emissor da lista de sugestões que aparece ao digitar
- **Entrada Manual**: O usuário pode digitar manualmente o nome de um emissor, mesmo que não esteja na lista

**Comportamento:**

- O componente exibe sugestões baseadas nos emissores cadastrados conforme o usuário digita
- O componente fornece feedback visual em tempo real sobre o estado do valor digitado (existente ou não na lista)
- Ao salvar o ativo, se o nome do emissor não estiver na lista, o sistema cria automaticamente o novo emissor

Para detalhes técnicos completos sobre o funcionamento do componente AutoComplete, consulte o
documento [RN - Componente de AutoComplete](RN%20-%20Componente%20de%20AutoComplete.md).

---

## 3. Campos do Formulário

### 3.1. Renda Fixa (`FixedIncomeAsset`)

Quando o tipo selecionado for "Renda Fixa", o formulário deve exibir os seguintes campos:

| Campo                             | Tipo      | UI           | Obrigatório | Descrição                                                                   | Valores Possíveis                                                                                          |
|:----------------------------------|:----------|:-------------|:------------|:----------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------|
| **Tipo de Rendimento**            | Enum      | DropDown     | Sim         | Define como o rendimento é calculado                                        | `POST_FIXED` (Pós-fixado), `PRE_FIXED` (Pré-fixado), `INFLATION_LINKED` (IPCA)                             |
| **Subtipo**                       | Enum      | DropDown     | Sim         | O instrumento financeiro de renda fixa                                      | `CDB`, `LCI`, `LCA`, `CRA`, `CRI`, `DEBENTURE`                                                             |
| **Data de Vencimento**            | LocalDate | InputText    | Sim         | Data de vencimento do título                                                | Data futura válida                                                                                         |
| **Rentabilidade Contratada**      | Double    | InputText    | Sim         | Percentual de rentabilidade contratada                                      | Número decimal positivo (ex: 110.0, 12.5, 6.5)                                                             |
| **Rentabilidade Relativa ao CDI** | Double?   | InputText    | Não         | Rentabilidade relativa ao CDI (opcional, geralmente usado para pós-fixados) | Número decimal positivo ou vazio                                                                           |
| **Liquidez**                      | Enum      | DropDown     | Sim         | Regra de liquidez aplicável ao ativo                                        | `DAILY` (Diária), `AT_MATURITY` (No vencimento)                                                            |
| **Emissor**                       | Issuer    | AutoComplete | Sim         | Entidade que emitiu o ativo                                                 | Componente AutoComplete. Ver [RN - Componente de AutoComplete](RN%20-%20Componente%20de%20AutoComplete.md) |
| **Observações**                   | String?   | InputText    | Não         | Notas e observações adicionais sobre o ativo                                | Texto livre                                                                                                |

**Validações Específicas para Renda Fixa:**

- A data de vencimento deve ser uma data futura
- A rentabilidade contratada deve ser um número positivo
- Se o tipo de rendimento for `POST_FIXED`, sugerir preenchimento do campo "Rentabilidade Relativa ao CDI"

### 3.2. Renda Variável (`VariableIncomeAsset`)

Quando o tipo selecionado for "Renda Variável", o formulário deve exibir os seguintes campos:

| Campo           | Tipo    | UI           | Obrigatório | Descrição                                                      | Valores Possíveis                                                                                          |
|:----------------|:--------|:-------------|:------------|:---------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------|
| **Tipo**        | Enum    | DropDown     | Sim         | O tipo de ativo de renda variável                              | `NATIONAL_STOCK` (Ação), `INTERNATIONAL_STOCK` (Ações Internacionais), `REAL_ESTATE_FUND` (FII), `ETF`     |
| **Nome/Ticker** | String  | InputText    | Sim         | O nome ou código de negociação do ativo (ex: "PETR4", "B3SA3") | Texto alfanumérico (geralmente 4-6 caracteres)                                                             |
| **Emissor**     | Issuer  | AutoComplete | Sim         | Entidade que emitiu o ativo                                    | Componente AutoComplete. Ver [RN - Componente de AutoComplete](RN%20-%20Componente%20de%20AutoComplete.md) |
| **Observações** | String? | InputText    | Não         | Notas e observações adicionais sobre o ativo                   | Texto livre                                                                                                |

**Validações Específicas para Renda Variável:**

- O nome/ticker deve ser preenchido e não pode estar vazio
- A liquidez é fixa: sempre `D_PLUS_DAYS` com `liquidityDays = 2` (não exibido no formulário, aplicado automaticamente)

### 3.3. Fundos de Investimento (`InvestmentFundAsset`)

Quando o tipo selecionado for "Fundos", o formulário deve exibir os seguintes campos:

| Campo                  | Tipo       | UI           | Obrigatório | Descrição                                                                                 | Valores Possíveis                                                                                          |
|:-----------------------|:-----------|:-------------|:------------|:------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------|
| **Tipo de Fundo**      | Enum       | DropDown     | Sim         | A categoria do fundo de investimento                                                      | `PENSION` (Previdência), `STOCK_FUND` (Fundos de Ação), `MULTIMARKET_FUND` (Multimercado)                  |
| **Nome**               | String     | InputText    | Sim         | O nome do fundo de investimento                                                           | Texto livre (ex: "Verde AM", "XP Previdência")                                                             |
| **Liquidez**           | Enum       | DropDown     | Sim         | Regra de liquidez aplicável ao fundo                                                      | `D_PLUS_DAYS` (Dias após venda)                                                                            |
| **Dias para Resgate**  | Int        | InputText    | Sim         | Número de dias para o resgate ser efetivado (obrigatório quando liquidez é `D_PLUS_DAYS`) | Número inteiro positivo (ex: 2, 30, 60)                                                                    |
| **Data de Vencimento** | LocalDate? | InputText    | Não         | Data de vencimento do fundo (opcional, apenas para fundos com prazo definido)             | Data futura válida ou vazio                                                                                |
| **Emissor**            | Issuer     | AutoComplete | Sim         | Entidade que emitiu o fundo                                                               | Componente AutoComplete. Ver [RN - Componente de AutoComplete](RN%20-%20Componente%20de%20AutoComplete.md) |
| **Observações**        | String?    | InputText    | Não         | Notas e observações adicionais sobre o fundo                                              | Texto livre                                                                                                |

**Validações Específicas para Fundos:**

- O nome do fundo deve ser preenchido e não pode estar vazio
- Os dias para resgate devem ser um número inteiro positivo
- Se a data de vencimento for informada, deve ser uma data futura
- A liquidez para fundos é sempre `D_PLUS_DAYS` (campo pode ser fixo ou oculto)

---

## 4. Comportamento do Formulário

### 4.1. Exibição Dinâmica

#### 4.1.1. Estado Inicial

- Ao abrir a tela, o campo "Investimento" deve estar selecionado com o **Item 1** ("Novo investimento")
- Todos os campos do formulário devem estar **visíveis**
- O campo "Tipo de Ativo" deve estar sem seleção
- Os campos específicos por tipo devem estar **ocultos** até que um tipo seja selecionado
- Os campos comuns (Emissor e Observações) devem estar visíveis, mas vazios

#### 4.1.2. Seleção de "Novo investimento" (Item 1)

- Ao selecionar "Novo investimento" (ou quando já estiver selecionado por padrão), o sistema deve:
    1. Exibir o campo "Tipo de Ativo" (sem seleção prévia)
    2. Manter os campos específicos por tipo **ocultos** até que um tipo seja selecionado
    3. Exibir os campos comuns (Emissor e Observações) vazios, prontos para preenchimento

#### 4.1.3. Seleção de Investimento Existente (Items 2+)

- Ao selecionar um investimento existente, o sistema deve:
    1. Carregar todos os dados do ativo selecionado
    2. Exibir o campo "Tipo de Ativo" com o tipo do ativo já selecionado
    3. Exibir todos os campos específicos do tipo **preenchidos** com os dados do ativo
    4. Exibir os campos comuns (Emissor e Observações) preenchidos

#### 4.1.4. Alteração do Tipo de Ativo

- Ao alterar o tipo de ativo, o sistema deve:
    1. Ocultar os campos do tipo anterior
    2. Exibir os campos do novo tipo selecionado
    3. Limpar os valores dos campos do tipo anterior (exceto campos comuns)
    4. Manter os campos comuns (Emissor e Observações) visíveis

#### 4.1.5. Comportamento do Campo Emissor

- O campo "Emissor" utiliza o componente AutoComplete
- O componente permite digitação livre e exibe sugestões baseadas nos emissores cadastrados
- O componente fornece feedback visual em tempo real sobre o estado do valor digitado
- Ao salvar o ativo, se o nome do emissor não estiver na lista, o sistema cria automaticamente o novo emissor
- Quando o formulário está no estado inicial (Item 1 "Novo investimento" selecionado), o campo "Emissor" deve estar vazio

Para detalhes técnicos completos sobre o funcionamento do componente AutoComplete, consulte o
documento [RN - Componente de AutoComplete](RN%20-%20Componente%20de%20AutoComplete.md).

### 4.2. Validação

- Campos obrigatórios devem ser validados antes de permitir o salvamento
- Mensagens de erro devem ser exibidas abaixo de cada campo inválido
- O botão de salvar deve estar desabilitado enquanto:
    - Houver campos obrigatórios vazios
    - Houver campos com valores inválidos

### 4.3. Ações do Formulário

- **Botão "Salvar"**:
    - Se Item 1 ("Novo investimento") estiver selecionado: Valida e salva um novo ativo no banco de dados
    - Se Item 2+ (investimento existente) estiver selecionado: Valida e atualiza o ativo existente no banco de dados
    - Após salvar com sucesso, o formulário deve ser limpo e retornar ao estado inicial (Item 1 selecionado, campos vazios)
    - A lista de investimentos no dropdown deve ser atualizada (se um novo foi criado ou um existente foi editado)

- **Botão "Cancelar" ou "Limpar"**:
    - Limpa todos os campos
    - Retorna ao estado inicial (Item 1 selecionado, campos vazios, tipo de ativo sem seleção)

- **Botão "Excluir"** (apenas quando Item 2+ estiver selecionado):
    - Exibe diálogo de confirmação
    - Se confirmado, remove o ativo do banco de dados
    - Retorna ao estado inicial após exclusão (Item 1 selecionado, campos vazios)

---

## 5. Casos de Uso

### UC-01: Cadastrar Novo Ativo

**Ator**: Usuário do sistema

**Pré-condições**:

- Sistema inicializado
- Acesso ao repositório de ativos
- Acesso ao repositório de emissores (para seleção/criação de emissor)
- Usuário na Tela de Cadastro de Assets

**Fluxo Principal**:

1. Usuário acessa a Tela de Cadastro de Assets
2. Sistema exibe o formulário com o campo "Investimento" selecionado no Item 1 ("Novo investimento")
3. Sistema exibe o campo "Tipo de Ativo" e os campos comuns (Emissor e Observações) vazios
4. Os campos específicos por tipo estão ocultos até que um tipo seja selecionado
5. Usuário seleciona um tipo de ativo (ex: "Renda Fixa")
6. Sistema exibe os campos específicos para o tipo selecionado
7. Usuário preenche os campos obrigatórios do formulário
8. Usuário preenche os campos opcionais (se desejar)
9. Sistema valida os dados informados
10. Usuário clica no botão "Salvar"
11. Sistema persiste o novo ativo no banco de dados
12. Sistema atualiza a lista de investimentos no dropdown
13. Sistema limpa o formulário e retorna ao estado inicial (Item 1 selecionado, campos vazios)
14. Sistema exibe mensagem de sucesso

**Fluxos Alternativos**:

**FA-01.1: Cancelar Cadastro**

1. No passo 8 do fluxo principal, usuário clica em "Cancelar"
2. Sistema limpa todos os campos do formulário
3. Sistema retorna ao estado inicial (Item 1 selecionado, campos vazios, tipo de ativo sem seleção)

**FA-01.2: Validação de Campos Inválidos**

1. No passo 9 do fluxo principal, sistema identifica campos inválidos
2. Sistema exibe mensagens de erro abaixo dos campos inválidos
3. Sistema mantém o botão "Salvar" desabilitado
4. Usuário corrige os campos inválidos
5. Sistema revalida os campos
6. Retorna ao passo 10 do fluxo principal

**FA-01.3: Alterar Tipo de Ativo Durante o Preenchimento**

1. Durante o preenchimento do formulário (após passo 6), usuário altera a seleção do campo "Tipo de Ativo"
2. Sistema oculta os campos do tipo anterior
3. Sistema exibe os campos do novo tipo selecionado
4. Sistema limpa os valores dos campos do tipo anterior
5. Retorna ao passo 7 do fluxo principal

**FA-01.4: Criar Novo Emissor**

1. No passo 7 do fluxo principal, usuário não encontra o emissor desejado na lista de sugestões do campo "Emissor"
2. Usuário digita manualmente o nome do novo emissor no campo "Emissor"
3. Sistema verifica se o nome digitado existe na lista de emissores
4. Sistema exibe o campo em estado de **warning** (indicando que o emissor não existe)
5. Usuário continua preenchendo os demais campos do formulário
6. Ao salvar o ativo (passo 10 do fluxo principal), o sistema detecta que o emissor está em estado warning
7. Sistema cria automaticamente o novo emissor (ver [RN - Componente de AutoComplete](RN%20-%20Componente%20de%20AutoComplete.md) para detalhes)
8. Sistema atualiza a lista de sugestões de emissores
9. Sistema muda o campo "Emissor" para estado de **sucesso**
10. Retorna ao passo 8 do fluxo principal

**Pós-condições**:

- Novo ativo cadastrado no banco de dados
- Lista de investimentos no dropdown atualizada
- Formulário limpo e retornado ao estado inicial

---

### UC-02: Editar Ativo Existente

**Ator**: Usuário do sistema

**Pré-condições**:

- Sistema inicializado
- Acesso ao repositório de ativos
- Acesso ao repositório de emissores
- Existência de pelo menos um ativo cadastrado no sistema
- Usuário na Tela de Cadastro de Assets

**Fluxo Principal**:

1. Usuário acessa a Tela de Cadastro de Assets
2. Sistema exibe o formulário com o campo "Investimento" selecionado no Item 1 ("Novo investimento")
3. Sistema carrega a lista de investimentos existentes no dropdown (Items 2+)
4. Usuário seleciona um investimento existente no campo "Investimento" (Item 2+)
5. Sistema carrega os dados do ativo selecionado
6. Sistema exibe todos os campos do formulário preenchidos com os dados do ativo
7. Sistema exibe o campo "Tipo de Ativo" com o tipo do ativo já selecionado
8. Usuário modifica os campos desejados
9. Sistema valida os dados informados
10. Usuário clica no botão "Salvar"
11. Sistema atualiza o ativo no banco de dados
12. Sistema atualiza a descrição do investimento no dropdown (caso tenha mudado)
13. Sistema limpa o formulário e retorna ao estado inicial (Item 1 selecionado, campos vazios)
14. Sistema exibe mensagem de sucesso

**Fluxos Alternativos**:

**FA-02.1: Cancelar Edição**

1. No passo 8 do fluxo principal, usuário clica em "Cancelar"
2. Sistema descarta todas as alterações
3. Sistema retorna ao estado inicial (Item 1 selecionado, campos vazios, tipo de ativo sem seleção)

**FA-02.2: Validação de Campos Inválidos**

1. No passo 9 do fluxo principal, sistema identifica campos inválidos
2. Sistema exibe mensagens de erro abaixo dos campos inválidos
3. Sistema mantém o botão "Salvar" desabilitado
4. Usuário corrige os campos inválidos
5. Sistema revalida os campos
6. Retorna ao passo 10 do fluxo principal

**FA-02.3: Alterar Tipo de Ativo Durante Edição**

1. Durante a edição (após passo 7), usuário altera a seleção do campo "Tipo de Ativo"
2. Sistema exibe diálogo de confirmação informando que a alteração do tipo limpará os campos específicos
3. Se usuário confirmar:
    - Sistema oculta os campos do tipo anterior
    - Sistema exibe os campos do novo tipo selecionado
    - Sistema limpa os valores dos campos do tipo anterior
    - Retorna ao passo 8 do fluxo principal
4. Se usuário cancelar:
    - Sistema mantém o tipo original e os campos preenchidos
    - Retorna ao passo 8 do fluxo principal

**Pós-condições**:

- Ativo atualizado no banco de dados
- Lista de investimentos no dropdown atualizada (se a descrição mudou)
- Formulário limpo e retornado ao estado inicial

---

### UC-03: Excluir Ativo Existente

**Ator**: Usuário do sistema

**Pré-condições**:

- Sistema inicializado
- Acesso ao repositório de ativos
- Existência de pelo menos um ativo cadastrado no sistema
- Usuário na Tela de Cadastro de Assets
- Um investimento existente selecionado no campo "Investimento"

**Fluxo Principal**:

1. Usuário seleciona um investimento existente no campo "Investimento" (Item 2+)
2. Sistema carrega e exibe os dados do ativo no formulário
3. Usuário clica no botão "Excluir"
4. Sistema exibe diálogo de confirmação perguntando se deseja realmente excluir o ativo
5. Usuário confirma a exclusão
6. Sistema remove o ativo do banco de dados
7. Sistema remove o item do dropdown de investimentos
8. Sistema limpa o formulário e retorna ao estado inicial (Item 1 selecionado, campos vazios)
9. Sistema exibe mensagem de sucesso

**Fluxos Alternativos**:

**FA-03.1: Cancelar Exclusão**

1. No passo 5 do fluxo principal, usuário cancela a exclusão no diálogo de confirmação
2. Sistema fecha o diálogo
3. Sistema mantém o formulário com os dados do ativo
4. Retorna ao passo 3 do fluxo principal

**Pós-condições**:

- Ativo removido do banco de dados
- Item removido da lista de investimentos no dropdown
- Formulário limpo e retornado ao estado inicial

---


