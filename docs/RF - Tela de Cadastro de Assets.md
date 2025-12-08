# Requisitos Funcionais - Tela de Cadastro de Assets

## Índice

1. [Objetivo](#1-objetivo)
2. [Estrutura do Formulário](#2-estrutura-do-formulário)
3. [Campos do Formulário](#3-campos-do-formulário)
4. [Comportamento do Formulário](#4-comportamento-do-formulário)
5. [Casos de Uso](#5-casos-de-uso)

---

## 1. Objetivo

Esta tela tem como objetivo permitir ao usuário cadastrar novos ativos (`Asset`) no sistema e editar ativos existentes. O formulário é dinâmico,
adaptando-se ao tipo de ativo selecionado e exibindo apenas os campos relevantes para cada categoria.

O formulário pode operar em dois modos:
- **Modo de Cadastro**: Para cadastrar novos ativos (estado inicial)
- **Modo de Edição**: Para editar ativos existentes (quando um ativo é selecionado da lista)

---

## 2. Estrutura do Formulário

O formulário exibe os seguintes campos:

### 2.1. Campos Específicos por Tipo

O formulário deve exibir:

1. **Campo de Seleção do Tipo de Ativo** (obrigatório)
    - Campo de seleção que determina qual categoria de ativo será cadastrado
    - Opções: "Renda Fixa", "Renda Variável", "Fundos"
    - Este campo controla quais campos adicionais serão exibidos
    - Atualmente apenas "Renda Fixa" está implementada, mas o campo está habilitado para permitir a seleção quando os demais tipos forem implementados
    - Ao abrir a tela, este campo estará sem seleção

2. **Campos Específicos por Tipo** (exibidos dinamicamente conforme a seleção do tipo de ativo)

3. **Campos Comuns** (sempre exibidos)
    - Emissor
    - Observações (opcional)
    - Corretora (opcional)

### 2.2. Campo "Emissor" (Dropdown)

O campo "Emissor" utiliza um dropdown simples que permite:

- **Seleção de Emissor Existente**: O usuário pode selecionar um emissor da lista de emissores cadastrados

**Comportamento:**

- O componente exibe uma lista de emissores cadastrados no sistema
- O usuário seleciona o emissor desejado da lista

### 2.3. Campo "Corretora" (Dropdown)

O campo "Corretora" é um dropdown opcional que permite:

- **Seleção de Corretora Existente**: O usuário pode selecionar uma corretora da lista de corretoras cadastradas
- **Campo Opcional**: O campo não é obrigatório e pode ficar sem seleção

**Comportamento:**

- O componente exibe uma lista de corretoras cadastradas no sistema
- O usuário pode selecionar uma corretora ou deixar o campo vazio
- Se uma corretora for selecionada, ao salvar o ativo, será criado automaticamente um `AssetHolding` associando o ativo à corretora selecionada e ao proprietário padrão do sistema
- Se o campo estiver vazio, apenas o `Asset` será salvo, sem criar um `AssetHolding`

**Lógica de Salvamento:**

- **Corretora não preenchida**: Apenas o `Asset` é salvo no banco de dados
- **Corretora preenchida**: Além do `Asset`, é criado um `AssetHolding` com:
  - O ativo recém-cadastrado
  - A corretora selecionada
  - O proprietário padrão do sistema (primeiro proprietário cadastrado)
  - Valores iniciais zerados para quantidade, custo médio, valor investido e valor atual

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
| **Emissor**                       | Issuer    | DropDown     | Sim         | Entidade que emitiu o ativo                                                 | Lista de emissores cadastrados no sistema                                                                    |
| **Observações**                   | String?   | InputText    | Não         | Notas e observações adicionais sobre o ativo                                | Texto livre                                                                                                |
| **Corretora**                     | Brokerage | DropDown     | Não         | Instituição financeira onde o ativo está custodiado                        | Lista de corretoras cadastradas no sistema. Se preenchido, cria automaticamente um AssetHolding                |

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
| **Emissor**     | Issuer  | DropDown     | Sim         | Entidade que emitiu o ativo                                    | Lista de emissores cadastrados no sistema                                                                    |
| **Observações** | String? | InputText    | Não         | Notas e observações adicionais sobre o ativo                   | Texto livre                                                                                                |
| **Corretora**   | Brokerage | DropDown   | Não         | Instituição financeira onde o ativo está custodiado            | Lista de corretoras cadastradas no sistema. Se preenchido, cria automaticamente um AssetHolding                |

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
| **Emissor**            | Issuer     | DropDown     | Sim         | Entidade que emitiu o fundo                                                               | Lista de emissores cadastrados no sistema                                                                    |
| **Observações**        | String?    | InputText    | Não         | Notas e observações adicionais sobre o fundo                                              | Texto livre                                                                                                |
| **Corretora**          | Brokerage  | DropDown     | Não         | Instituição financeira onde o fundo está custodiado                                       | Lista de corretoras cadastradas no sistema. Se preenchido, cria automaticamente um AssetHolding                |

**Validações Específicas para Fundos:**

- O nome do fundo deve ser preenchido e não pode estar vazio
- Os dias para resgate devem ser um número inteiro positivo
- Se a data de vencimento for informada, deve ser uma data futura
- A liquidez para fundos é sempre `D_PLUS_DAYS` (campo pode ser fixo ou oculto)

---

## 4. Comportamento do Formulário

### 4.1. Exibição Dinâmica

#### 4.1.1. Estado Inicial

- Ao abrir a tela, o formulário inicia vazio, pronto para cadastro de novo ativo
- O campo "Tipo de Ativo" (Categoria) está sem seleção
- Os campos específicos por tipo estão **ocultos** até que um tipo seja selecionado
- Os campos comuns (Emissor, Observações e Corretora) estão visíveis, mas vazios

#### 4.1.2. Seleção do Tipo de Ativo

- Ao selecionar um tipo de ativo (ex: "Renda Fixa"), o sistema deve:
    1. Exibir os campos específicos para o tipo selecionado
    2. Manter os campos comuns (Emissor, Observações e Corretora) visíveis

#### 4.1.3. Alteração do Tipo de Ativo

- Ao alterar o tipo de ativo, o sistema deve:
    1. Ocultar os campos do tipo anterior
    2. Exibir os campos do novo tipo selecionado
    3. Limpar os valores dos campos do tipo anterior (exceto campos comuns)
    4. Manter os campos comuns (Emissor, Observações e Corretora) visíveis

### 4.2. Validação

- Campos obrigatórios devem ser validados antes de permitir o salvamento
- Mensagens de erro devem ser exibidas abaixo de cada campo inválido
- O botão de salvar deve estar desabilitado enquanto:
    - Houver campos obrigatórios vazios
    - Houver campos com valores inválidos

### 4.3. Ações do Formulário

- **Botão "Salvar"**:
    - Valida e salva um novo ativo ou atualiza um ativo existente no banco de dados
    - Se o campo "Corretora" estiver preenchido, além de salvar o `Asset`, cria automaticamente um `AssetHolding` associando o ativo à corretora selecionada e ao proprietário padrão do sistema
    - Se o campo "Corretora" estiver vazio, apenas o `Asset` é salvo
    - Após salvar com sucesso, o formulário deve ser limpo e retornar ao estado inicial (campos vazios, tipo de ativo sem seleção)
    - Sistema exibe mensagem de sucesso

### 4.4. Modo de Edição

Quando o formulário é aberto para editar um ativo existente (selecionado da lista de consulta), o comportamento é o seguinte:

#### 4.4.1. Carregamento dos Dados

- O formulário é preenchido automaticamente com todos os dados do ativo selecionado
- Todos os campos são populados com os valores atuais do ativo
- O campo "Categoria" (Tipo de Ativo) é **bloqueado para edição** e exibido visualmente como desabilitado
- O título do cabeçalho muda de "Novo Ativo" para "Editar Ativo"

#### 4.4.2. Restrições no Modo de Edição

- **Categoria bloqueada**: O campo "Categoria" não pode ser alterado durante a edição
- **Demais campos editáveis**: Todos os outros campos podem ser modificados normalmente
- **Validação**: As mesmas regras de validação se aplicam ao modo de edição

#### 4.4.3. Salvamento no Modo de Edição

- Ao salvar, o sistema atualiza o ativo existente no banco de dados (mantendo o mesmo ID)
- Após salvar com sucesso, o formulário retorna ao modo de cadastro (estado inicial)
- A lista de ativos é atualizada automaticamente para refletir as alterações

---

## 5. Casos de Uso

### UC-01: Cadastrar Novo Ativo

**Ator**: Usuário do sistema

**Pré-condições**:

- Sistema inicializado
- Acesso ao repositório de ativos
- Acesso ao repositório de emissores (para seleção de emissor)
- Usuário na Tela de Cadastro de Assets

**Fluxo Principal**:

1. Usuário acessa a Tela de Cadastro de Assets
2. Sistema exibe o formulário vazio, pronto para cadastro de novo ativo
3. Sistema exibe o campo "Tipo de Ativo" (Categoria) e os campos comuns (Emissor, Observações e Corretora) vazios
4. Os campos específicos por tipo estão ocultos até que um tipo seja selecionado
5. Usuário seleciona um tipo de ativo (ex: "Renda Fixa")
6. Sistema exibe os campos específicos para o tipo selecionado
7. Usuário preenche os campos obrigatórios do formulário
8. Usuário preenche os campos opcionais (se desejar)
9. Sistema valida os dados informados
10. Usuário clica no botão "Salvar"
11. Sistema persiste o novo ativo no banco de dados
12. Se o campo "Corretora" estiver preenchido, sistema cria automaticamente um `AssetHolding` associando o ativo à corretora e ao proprietário padrão
13. Sistema limpa o formulário e retorna ao estado inicial (campos vazios, tipo de ativo sem seleção)
14. Sistema exibe mensagem de sucesso

**Fluxos Alternativos**:

**FA-01.1: Validação de Campos Inválidos**

1. No passo 9 do fluxo principal, sistema identifica campos inválidos
2. Sistema exibe mensagens de erro abaixo dos campos inválidos
3. Sistema mantém o botão "Salvar" desabilitado
4. Usuário corrige os campos inválidos
5. Sistema revalida os campos
6. Retorna ao passo 10 do fluxo principal

**FA-01.2: Alterar Tipo de Ativo Durante o Preenchimento**

1. Durante o preenchimento do formulário (após passo 6), usuário altera a seleção do campo "Tipo de Ativo"
2. Sistema oculta os campos do tipo anterior
3. Sistema exibe os campos do novo tipo selecionado
4. Sistema limpa os valores dos campos do tipo anterior
5. Retorna ao passo 7 do fluxo principal

**Pós-condições**:

- Novo ativo cadastrado no banco de dados
- Formulário limpo e retornado ao estado inicial

### UC-02: Editar Ativo Existente

**Ator**: Usuário do sistema

**Pré-condições**:

- Sistema inicializado
- Acesso ao repositório de ativos
- Acesso ao repositório de emissores (para seleção de emissor)
- Usuário na Tela de Consulta de Ativos
- Pelo menos um ativo cadastrado no sistema

**Fluxo Principal**:

1. Usuário visualiza a lista de ativos na Tela de Consulta de Ativos
2. Usuário clica em um item da tabela (linha de um ativo)
3. Sistema abre o painel de formulário de cadastro/edição
4. Sistema carrega os dados do ativo selecionado
5. Sistema preenche todos os campos do formulário com os dados do ativo
6. Sistema bloqueia o campo "Categoria" para edição
7. Sistema exibe o título "Editar Ativo" no cabeçalho do formulário
8. Usuário visualiza os dados do ativo preenchidos
9. Usuário modifica os campos desejados (exceto a categoria)
10. Sistema valida os dados informados
11. Usuário clica no botão "Salvar"
12. Sistema atualiza o ativo no banco de dados
13. Sistema limpa o formulário e retorna ao estado inicial (modo de cadastro)
14. Sistema exibe mensagem de sucesso
15. Sistema atualiza a lista de ativos na tela de consulta

**Fluxos Alternativos**:

**FA-02.1: Validação de Campos Inválidos**

1. No passo 10 do fluxo principal, sistema identifica campos inválidos
2. Sistema exibe mensagens de erro abaixo dos campos inválidos
3. Sistema mantém o botão "Salvar" desabilitado
4. Usuário corrige os campos inválidos
5. Sistema revalida os campos
6. Retorna ao passo 11 do fluxo principal

**Pós-condições**:

- Ativo atualizado no banco de dados
- Formulário limpo e retornado ao estado inicial (modo de cadastro)
- Lista de ativos atualizada na tela de consulta

---


