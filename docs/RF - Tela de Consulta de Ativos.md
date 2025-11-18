# Requisitos Funcionais - Tela de Consulta e Gestão de Ativos

## Índice

1. [Objetivo](#1-objetivo)
2. [Pré-requisitos](#2-pré-requisitos)
3. [Formatação de Dados](#3-formatação-de-dados)
4. [Dados Exibidos na Tabela de Ativos](#4-dados-exibidos-na-tabela-de-ativos)
5. [Comportamento da Tabela](#5-comportamento-da-tabela)
6. [Ações da Tela](#6-ações-da-tela)
7. [Tela de Cadastro e Edição de Ativo](#7-tela-de-cadastro-e-edição-de-ativo)
8. [Validações](#8-validações)
9. [Navegação](#9-navegação)
10. [Exemplos](#10-exemplos)
11. [Detalhes Técnicos](#11-detalhes-técnicos)
12. [Casos de Uso](#12-casos-de-uso)

---

## 1. Objetivo

Esta tela tem como objetivo principal fornecer ao usuário uma visão completa de todos os ativos (`Asset`) cadastrados no sistema, independentemente de haver uma posição (`AssetHolding`) associada a eles. A tela servirá como um catálogo central de ativos, permitindo ao usuário não apenas consultar os detalhes, mas também navegar para as funcionalidades de **cadastro** de um novo ativo e **edição** de um ativo existente.

A principal funcionalidade da tela será uma tabela que exibirá um resumo de cada ativo, com um botão de ação para "adicionar" um novo ativo.

### 1.1. Funcionalidades Principais

- **Listagem**: Exibir todos os ativos cadastrados em formato tabular
- **Visualização**: Permitir visualização rápida dos principais atributos de cada ativo
- **Navegação**: Facilitar acesso às telas de cadastro e edição
- **Ordenação**: Permitir ordenação dos dados por colunas específicas

---

## 2. Pré-requisitos

### 2.1. Dados Necessários

- Existência de pelo menos um `Issuer` cadastrado no sistema (para seleção no formulário)
- Acesso ao repositório de ativos (`AssetRepository`) para recuperação e persistência de dados

### 2.2. Componentes do Sistema

- Componente `DataTable` reutilizável para exibição tabular
- Sistema de navegação configurado para roteamento entre telas
- ViewModel com injeção de dependência (Koin)

---

## 3. Formatação de Dados

Esta seção detalha como os dados devem ser formatados para exibição na interface.

### 3.1. Formatação de Datas

- **Formato**: `YYYY-MM-DD` (exemplo: `2028-01-15`)
- **Campo Vencimento**: 
  - Quando presente: exibido no formato `YYYY-MM-DD`
  - Quando ausente (opcional ou não aplicável): exibido como `"-"`
- **Validação**: Datas devem ser válidas e no formato correto

### 3.2. Formatação de Valores Numéricos

- **Porcentagens**: Exibidas com o símbolo `%` após o valor (exemplo: `110%`, `12.5%`)
- **Rentabilidade Contratada**: 
  - Pós-fixado: `X% do CDI`
  - Pré-fixado: `X% a.a.`
  - IPCA: `X%` (adicionado ao índice)
- **Valores Decimais**: Permitir até 2 casas decimais quando necessário

### 3.3. Formatação de Texto

- **Campos Obrigatórios**: Sempre devem ter valor (não podem ser vazios)
- **Campos Opcionais**: Quando vazios, podem ser exibidos como string vazia ou `"-"` dependendo do contexto
- **Observações**: Campo de texto livre, exibido como está (sem formatação especial)

### 3.4. Formatação de Enums

- **Categorias**: 
  - `FixedIncomeAsset` → `"Renda Fixa"`
  - `VariableIncomeAsset` → `"Renda Variável"`
  - `InvestmentFundAsset` → `"Fundo de Investimento"`
- **Subcategorias**: 
  - `FixedIncomeAsset`: Usa `subType.name` diretamente (ex: `"CDB"`, `"LCI"`, `"LCA"`)
  - Outros tipos: Usa formatação do `type` (ex: `"Ações Nacionais"`, `"Multimercado"`)

---

## 4. Dados Exibidos na Tabela de Ativos

A tabela principal deverá exibir os seguintes dados para cada `Asset` cadastrado:

| Coluna             | Descrição                                                                                | Fonte                                                                                                                                                                                          | Formatação                                                                                                 |
|:-------------------|:-----------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------|
| **Categoria**      | O tipo principal do ativo.                                                               | Calculado em tempo de execução, baseado na classe do `Asset`: `FixedIncomeAsset` -> "Renda Fixa", `VariableIncomeAsset` -> "Renda Variável", `InvestmentFundAsset` -> "Fundo de Investimento". | Texto formatado conforme enum (ver seção 3.4)                                                              |
| **Subcategoria**   | O tipo específico do ativo.                                                              | `asset.subType.name` para `FixedIncomeAsset`, `asset.type.formated()` para os demais.                                                                                                         | Para Renda Fixa: nome do enum (CDB, LCI, LCA). Para outros: texto formatado (ex: "Ações Nacionais").      |
| **Nome/Descrição** | O nome principal do ativo, formatado de acordo com o tipo.                               | Calculado dinamicamente conforme o tipo de ativo (ver detalhes abaixo).                                                                                                                       | Formatação específica por tipo (ver seção 4.1)                                                             |
| **Vencimento**     | A data de vencimento do ativo.                                                           | `asset.expirationDate` para `FixedIncomeAsset` (obrigatório) e `InvestmentFundAsset` (opcional). Para `VariableIncomeAsset`, este campo não se aplica.                                      | Formato `YYYY-MM-DD` quando presente, `"-"` quando ausente ou não aplicável.                             |
| **Emissor**        | A entidade que emitiu o ativo.                                                           | `asset.issuer.name`                                                                                                                                                                            | Texto simples, nome do emissor.                                                                            |
| **Observações**    | Notas e observações adicionais sobre o ativo.                                            | `asset.observations` (opcional, `String?`).                                                                                                                                                   | Texto livre, exibido como está. Se vazio, exibido como string vazia.                                      |

### 4.1. Formatação do Campo Nome/Descrição por Tipo de Ativo

#### Renda Fixa (`FixedIncomeAsset`)

O nome é formatado dinamicamente com base no tipo de rendimento:

- **Pós-fixado** (`POST_FIXED`): 
  ```
  "{subType} de {contractedYield}% do CDI"
  ```
  Exemplo: `"CDB de 110% do CDI"`

- **Pré-fixado** (`PRE_FIXED`): 
  ```
  "{subType} de {contractedYield}% a.a."
  ```
  Exemplo: `"LCI de 12.5% a.a."`

- **IPCA** (`INFLATION_LINKED`): 
  ```
  "{subType} + {contractedYield}%"
  ```
  Exemplo: `"LCA + 6.5%"`

#### Renda Variável (`VariableIncomeAsset`)

- O nome é exibido diretamente do campo `asset.name`
- O campo `name` já deve incluir o ticker quando aplicável
- Exemplo: `"Magazine Luiza (MGLU3)"`

#### Fundo de Investimento (`InvestmentFundAsset`)

- O nome é exibido diretamente do campo `asset.name`
- Exemplo: `"Verde AM"`

---

## 5. Comportamento da Tela

### 5.1. Estados da Tela

A tela deve tratar os seguintes estados:

- **Carregando**: Exibir indicador de carregamento enquanto os dados são recuperados do repositório
- **Sucesso**: Exibir a tabela com os ativos cadastrados
- **Lista Vazia**: Exibir mensagem informativa quando não houver ativos cadastrados (ex: "Nenhum ativo cadastrado. Clique no botão + para adicionar um novo ativo.")
- **Erro**: Exibir mensagem de erro quando houver falha na recuperação dos dados

### 5.2. Comportamento da Tabela

#### 5.2.1. Ordenação

- A tabela deve suportar ordenação por colunas
- **Coluna Vencimento**: Deve ter ordenação habilitada, ordenando por data (mais antiga primeiro ou mais recente primeiro, conforme preferência do usuário)
- Outras colunas podem ter ordenação opcional

#### 5.2.2. Largura de Colunas

- **Descrição**: Peso relativo de `2.0` (coluna mais larga, para acomodar textos longos)
- **Observação**: Peso relativo de `2.0` (coluna mais larga, para acomodar textos longos)
- **Demais colunas**: Largura automática baseada no conteúdo

#### 5.2.3. Responsividade

- A tabela deve se adaptar ao tamanho da tela
- Em telas menores, considerar scroll horizontal se necessário
- Manter legibilidade em diferentes tamanhos de tela

### 5.3. Layout da Tela

- **TopAppBar**: Exibe o título "Ativos"
- **Conteúdo Principal**: Tabela de ativos
- **Padding**: Aplicar padding adequado para espaçamento visual (ex: 32.dp)

---

## 6. Ações da Tela

### 6.1. Adicionar Novo Ativo

- **Componente**: Um botão de Ação Flutuante (Floating Action Button - FAB) com um ícone de "+".
- **Posicionamento**: Canto inferior direito da tela (padrão Material Design)
- **Ação**: Ao ser clicado, o usuário será redirecionado para a **Tela de Cadastro de Ativo**.
- **Parâmetros**: Nenhum (modo de criação)

### 6.2. Editar Ativo Existente

- **Componente**: A própria linha da tabela (área clicável).
- **Ação**: Ao tocar (clicar) na linha, o usuário será redirecionado para a **Tela de Edição de Ativo**, com os campos pré-preenchidos com os dados do ativo selecionado.
- **Parâmetros**: O `id` do ativo será passado como parâmetro para a tela de edição.
- **Feedback Visual**: A linha pode ter efeito hover/press para indicar que é clicável

---

## 7. Tela de Cadastro e Edição de Ativo

Esta tela será um formulário único utilizado tanto para criar um novo ativo quanto para modificar um existente. A interface deve se adaptar dinamicamente com base no tipo de ativo selecionado.

### 7.1. Modo de Operação

- **Modo Cadastro**: Quando acessada através do botão "Adicionar", todos os campos começam vazios
- **Modo Edição**: Quando acessada através do clique em uma linha da tabela, todos os campos são pré-preenchidos com os dados do ativo selecionado

### 7.2. Seleção da Categoria do Ativo

- **Componente**: Um seletor (Dropdown ou Radio Group) no topo do formulário.
- **Opções**: 
  - "Renda Fixa"
  - "Renda Variável"
  - "Fundo de Investimento"
- **Comportamento**: A seleção de uma categoria exibirá dinamicamente os campos específicos para aquele tipo de ativo.
- **Validação**: Campo obrigatório (deve ser selecionado antes de salvar)

### 7.3. Campos do Formulário

#### 7.3.1. Campos Comuns (Visíveis para todas as categorias)

- **Nome/Descrição**: 
  - Tipo: `name` (Obrigatório, `String`)
  - Descrição: Nome principal do ativo
  - Regras: 
    - Para Renda Variável, este campo deve incluir o ticker entre parênteses (ex: "Magazine Luiza (MGLU3)")
    - Para Renda Fixa, este campo será gerado automaticamente com base nos outros campos (ver seção 4.1)
    - Para Fundo de Investimento, é o nome do fundo
- **Emissor**: 
  - Tipo: `issuer` (Obrigatório, seleção de um `Issuer` previamente cadastrado)
  - Descrição: Entidade que emitiu o ativo
  - Validação: Deve selecionar um emissor da lista de emissores cadastrados
- **Observações**: 
  - Tipo: `observations` (Opcional, `String`)
  - Descrição: Campo de texto livre para notas e observações adicionais sobre o ativo
  - Limite: Sem limite de caracteres (ou limite razoável como 1000 caracteres)

#### 7.3.2. Campos Específicos por Categoria

##### Renda Fixa (`FixedIncomeAsset`)

- **Tipo de Renda Fixa**: 
  - Tipo: `type` (Obrigatório, `Enum`: `POST_FIXED`, `PRE_FIXED`, `INFLATION_LINKED`)
  - Descrição: Define como a rentabilidade é calculada
  - Opções formatadas: "Pós Fixado", "Prefixado", "IPCA"
- **Subtipo de Renda Fixa**: 
  - Tipo: `subType` (Obrigatório, `Enum`: `CDB`, `LCI`, `LCA`)
  - Descrição: O instrumento financeiro específico
  - Opções: CDB, LCI, LCA
- **Data de Vencimento**: 
  - Tipo: `expirationDate` (Obrigatório, `LocalDate`)
  - Descrição: Data em que o título vence
  - Formato: `YYYY-MM-DD`
  - Validação: Deve ser uma data futura
- **Rentabilidade Contratada**: 
  - Tipo: `contractedYield` (Obrigatório, `Double`)
  - Descrição: Taxa de rentabilidade acordada
  - Validação: Deve ser um número positivo
  - Formato: Permite decimais (ex: 110.5, 12.75)
- **Rentabilidade Relativa ao CDI**: 
  - Tipo: `cdiRelativeYield` (Opcional, `Double`)
  - Descrição: Rentabilidade em relação ao CDI (apenas para títulos pós-fixados)
  - Validação: Se preenchido, deve ser um número positivo
- **Liquidez**: 
  - Tipo: `liquidity` (Obrigatório, `Enum`: `Daily`, `AtMaturity`)
  - Descrição: Regra de liquidez do título
  - Opções: "Diária", "No Vencimento"

##### Renda Variável (`VariableIncomeAsset`)

- **Tipo de Renda Variável**: 
  - Tipo: `type` (Obrigatório, `Enum`: `NATIONAL_STOCK`, `INTERNATIONAL_STOCK`, `REAL_ESTATE_FUND`, `ETF`)
  - Descrição: Categoria do ativo de renda variável
  - Opções formatadas: "Ações Nacionais", "Ações Internacionais", "Fundos de Imobiliários", "ETF"
- **Ticker**: 
  - Tipo: `ticker` (Obrigatório, `String`)
  - Descrição: Código de negociação único do ativo na bolsa de valores
  - Exemplos: "PETR4", "MGLU3", "HGLG11"
  - Validação: 
    - Campo obrigatório
    - Deve ser único no sistema (não pode haver dois ativos com o mesmo ticker)
    - Formato típico: 4-5 caracteres alfanuméricos
- **Liquidez**: 
  - Tipo: `liquidity` (Obrigatório, `OnDaysAfterSale`)
  - Descrição: Regra de liquidez baseada em dias após a venda
  - Campo numérico: Número de dias (ex: D+0, D+1, D+30)
  - Validação: Deve ser um número inteiro não negativo

##### Fundo de Investimento (`InvestmentFundAsset`)

- **Tipo de Fundo**: 
  - Tipo: `type` (Obrigatório, `Enum`: `PENSION`, `STOCK_FUND`, `MULTIMARKET_FUND`)
  - Descrição: Categoria do fundo de investimento
  - Opções formatadas: "Previdência", "Fundos de Ação", "Multimercado"
- **Liquidez**: 
  - Tipo: `liquidity` (Obrigatório, `OnDaysAfterSale`)
  - Descrição: Regra de liquidez baseada em dias após a solicitação de resgate
  - Campo numérico: Número de dias (ex: D+0, D+1, D+30)
  - Validação: Deve ser um número inteiro não negativo
- **Data de Vencimento**: 
  - Tipo: `expirationDate` (Opcional, `LocalDate`)
  - Descrição: Data de vencimento do fundo (se aplicável)
  - Formato: `YYYY-MM-DD`
  - Validação: Se preenchido, deve ser uma data futura

### 7.4. Ações do Formulário

- **Botão "Salvar"**:
    - **Validação**: Verifica se todos os campos obrigatórios estão preenchidos e se os valores são válidos (ver seção 8).
    - **Ação**:
        - Se for um **cadastro**, cria uma nova instância do `Asset` correspondente e a salva no repositório.
        - Se for uma **edição**, atualiza os dados do `Asset` existente no repositório.
    - **Feedback**: 
        - Exibe uma mensagem de sucesso ("Ativo salvo com sucesso") ou de erro (com detalhes do problema).
        - Em caso de erro de validação, destacar os campos com problemas.
    - **Redirecionamento**: Após salvar com sucesso, redireciona o usuário de volta para a **Tela de Consulta de Ativos**.

- **Botão "Cancelar"**:
    - **Ação**: Descarta todas as alterações e redireciona o usuário de volta para a **Tela de Consulta de Ativos**.
    - **Confirmação**: Se houver alterações não salvas, pode solicitar confirmação do usuário antes de descartar.

---

## 8. Validações

Esta seção detalha todas as validações que devem ser aplicadas aos campos do formulário.

### 8.1. Validações Gerais

- Todos os campos obrigatórios devem estar preenchidos antes de permitir o salvamento
- Mensagens de erro devem ser claras e específicas, indicando qual campo tem problema
- Validações devem ser executadas tanto no cliente quanto no servidor (quando aplicável)

### 8.2. Validações por Campo

#### 8.2.1. Campos Comuns

- **Nome/Descrição**:
  - Obrigatório
  - Não pode ser vazio ou apenas espaços em branco
  - Comprimento mínimo: 3 caracteres
  - Comprimento máximo: 200 caracteres (ou conforme necessidade)
  
- **Emissor**:
  - Obrigatório
  - Deve ser um `Issuer` válido e existente no sistema
  
- **Observações**:
  - Opcional
  - Se preenchido, comprimento máximo: 1000 caracteres

#### 8.2.2. Renda Fixa

- **Tipo de Renda Fixa**:
  - Obrigatório
  - Deve ser um dos valores válidos do enum
  
- **Subtipo de Renda Fixa**:
  - Obrigatório
  - Deve ser um dos valores válidos do enum
  
- **Data de Vencimento**:
  - Obrigatório
  - Deve ser uma data válida no formato `YYYY-MM-DD`
  - Deve ser uma data futura (não pode ser hoje ou passado)
  
- **Rentabilidade Contratada**:
  - Obrigatório
  - Deve ser um número válido (Double)
  - Deve ser maior que zero
  - Pode ter até 2 casas decimais
  
- **Rentabilidade Relativa ao CDI**:
  - Opcional
  - Se preenchido, deve ser um número válido (Double)
  - Deve ser maior que zero
  - Pode ter até 2 casas decimais
  
- **Liquidez**:
  - Obrigatório
  - Deve ser um dos valores válidos do enum

#### 8.2.3. Renda Variável

- **Tipo de Renda Variável**:
  - Obrigatório
  - Deve ser um dos valores válidos do enum
  
- **Ticker**:
  - Obrigatório
  - Não pode ser vazio
  - Deve ser único no sistema (não pode haver outro ativo com o mesmo ticker)
  - Formato: 4-5 caracteres alfanuméricos (pode ter variações)
  - Não pode conter espaços ou caracteres especiais (exceto se permitido pelo padrão de tickers)
  
- **Liquidez (Dias)**:
  - Obrigatório
  - Deve ser um número inteiro não negativo
  - Valor típico: 0 (D+0) ou maior

#### 8.2.4. Fundo de Investimento

- **Tipo de Fundo**:
  - Obrigatório
  - Deve ser um dos valores válidos do enum
  
- **Liquidez (Dias)**:
  - Obrigatório
  - Deve ser um número inteiro não negativo
  - Valor típico: 0 (D+0) ou maior
  
- **Data de Vencimento**:
  - Opcional
  - Se preenchido, deve ser uma data válida no formato `YYYY-MM-DD`
  - Se preenchido, deve ser uma data futura

### 8.3. Validações de Negócio

- **Unicidade de Ticker**: Para Renda Variável, o ticker deve ser único no sistema
- **Data Futura**: Datas de vencimento devem ser futuras (não podem ser hoje ou passado)
- **Valores Positivos**: Todos os valores numéricos (rentabilidade, dias) devem ser positivos ou zero (conforme o caso)

---

## 9. Navegação

### 9.1. Rotas

- **Tela de Consulta de Ativos**: Rota principal (`/assets` ou similar)
- **Tela de Cadastro de Ativo**: Rota para criação (`/assets/new` ou similar)
- **Tela de Edição de Ativo**: Rota para edição com parâmetro (`/assets/{id}/edit` ou similar)

### 9.2. Fluxos de Navegação

#### 9.2.1. Navegação para Cadastro

1. Usuário está na **Tela de Consulta de Ativos**
2. Usuário clica no botão FAB (+)
3. Sistema navega para **Tela de Cadastro de Ativo** (modo criação)
4. Após salvar ou cancelar, retorna para **Tela de Consulta de Ativos**

#### 9.2.2. Navegação para Edição

1. Usuário está na **Tela de Consulta de Ativos**
2. Usuário clica em uma linha da tabela
3. Sistema navega para **Tela de Edição de Ativo** (modo edição) com o `id` do ativo
4. Sistema carrega os dados do ativo e preenche o formulário
5. Após salvar ou cancelar, retorna para **Tela de Consulta de Ativos**

### 9.3. Comportamento de Voltar

- **Botão Voltar do Sistema**: Deve retornar para a **Tela de Consulta de Ativos**
- **Botão Cancelar**: Retorna para a **Tela de Consulta de Ativos** descartando alterações
- **Após Salvar**: Retorna automaticamente para a **Tela de Consulta de Ativos** após sucesso

### 9.4. Parâmetros de Rota

- **Cadastro**: Nenhum parâmetro (modo criação)
- **Edição**: Parâmetro `id` (Long) do ativo a ser editado

---

## 10. Exemplos

### 10.1. Exemplo de Tabela de Consulta

| Categoria              | Subcategoria      | Nome/Descrição                    | Vencimento | Emissor             | Observações                    |
|:-----------------------|:------------------|:----------------------------------|:-----------|:--------------------|:-------------------------------|
| Renda Fixa             | CDB               | CDB de 110% do CDI                | 2028-01-01 | Banco Master        | Aplicação inicial              |
| Renda Fixa             | LCI               | LCI de 12.5% a.a.                 | 2029-06-15 | Banco Inter         |                                |
| Renda Fixa             | LCA               | LCA + 6.5%                        | 2030-03-20 | Banco do Brasil     | Isento de IR                   |
| Renda Variável         | Ações Nacionais   | Magazine Luiza (MGLU3)            | -          | Magazine Luiza S.A. |                                |
| Renda Variável         | ETF               | iShares Ibovespa (BOVA11)         | -          | BlackRock           | ETF de índice                  |
| Renda Variável         | Fundos Imobiliários | HGLG11                          | -          | HGLG                | FII de galpões logísticos      |
| Fundo de Investimento  | Multimercado      | Verde AM                          | -          | Verde Asset         | Fundo multimercado             |
| Fundo de Investimento  | Previdência       | XP Previdência                    | 2045-12-31 | XP Investimentos    | Plano de previdência privada   |

### 10.2. Exemplos de Formatação de Nome

#### Renda Fixa - Pós-fixado
```
Entrada: subType = CDB, contractedYield = 110.0
Saída: "CDB de 110% do CDI"
```

#### Renda Fixa - Pré-fixado
```
Entrada: subType = LCI, contractedYield = 12.5
Saída: "LCI de 12.5% a.a."
```

#### Renda Fixa - IPCA
```
Entrada: subType = LCA, contractedYield = 6.5
Saída: "LCA + 6.5%"
```

#### Renda Variável
```
Entrada: name = "Magazine Luiza (MGLU3)"
Saída: "Magazine Luiza (MGLU3)"
```

#### Fundo de Investimento
```
Entrada: name = "Verde AM"
Saída: "Verde AM"
```

### 10.3. Exemplos de Estados da Tela

#### Estado: Carregando
```
[Indicador de carregamento]
Carregando ativos...
```

#### Estado: Lista Vazia
```
[Ícone ou ilustração]
Nenhum ativo cadastrado.
Clique no botão + para adicionar um novo ativo.
```

#### Estado: Erro
```
[Ícone de erro]
Erro ao carregar ativos.
Por favor, tente novamente.
[Botão: Tentar Novamente]
```

---

## 11. Detalhes Técnicos

### 11.1. Componentes Utilizados

- **DataTable**: Componente reutilizável para exibição de dados tabulares
  - Localização: `com.eferraz.pokedex.ui.components.DataTable`
  - Suporta ordenação, colunas com pesos, e formatação de dados
  
- **TableColumn**: Componente para definição de colunas da tabela
  - Propriedades: `title`, `extractValue`, `weight`, `sortComparator`

### 11.2. Estrutura de Dados

#### AssetView

Classe de apresentação que encapsula os dados formatados para exibição:

```kotlin
internal class AssetView(
    val category: String,        // Categoria formatada
    val subCategory: String,     // Subcategoria formatada
    val name: String,            // Nome/Descrição formatado
    val maturity: LocalDate?,    // Data de vencimento (pode ser null)
    val issuer: String,         // Nome do emissor
    val notes: String,           // Observações (pode ser vazio)
)
```

- **Método de Criação**: `AssetView.create(asset: Asset)` - Factory method que transforma uma entidade `Asset` em `AssetView`
- **Localização**: `com.eferraz.presentation.assets.AssetView`

#### Formatters

Object singleton com funções de extensão para formatação:

- `Asset.formated()`: Formata a categoria do ativo
- `FixedIncomeAssetType.formated()`: Formata o tipo de renda fixa
- `InvestmentFundAssetType.formated()`: Formata o tipo de fundo
- `VariableIncomeAssetType.formated()`: Formata o tipo de renda variável
- `LocalDate?.formated()`: Formata data no formato `YYYY-MM-DD` ou retorna `"-"` se null

- **Localização**: `com.eferraz.presentation.assets.Formatters`

### 11.3. ViewModel

- **Classe**: `AssetsViewModel`
- **Localização**: `com.eferraz.presentation.assets.AssetsViewModel`
- **Responsabilidades**:
  - Gerenciar estado da tela (`AssetsState`)
  - Carregar lista de ativos do repositório
  - Expor estado através de `StateFlow`

- **Estado**:
```kotlin
data class AssetsState(
    val list: List<Asset>
)
```

### 11.4. Repository

- **Interface**: `AssetRepository` (Domain Layer)
- **Implementação**: `AssetRepositoryImpl` (Data Layer)
- **Métodos utilizados**:
  - `getAll()`: Retorna lista de todos os ativos cadastrados

### 11.5. Injeção de Dependência

- **Framework**: Koin
- **ViewModel**: Anotado com `@KoinViewModel` para injeção automática
- **Repository**: Injetado via construtor do ViewModel

---

## 12. Casos de Uso

### 12.1. Casos de Uso Principais

#### CU-01: Consultar Lista de Ativos

**Ator**: Usuário do sistema

**Pré-condições**: 
- Sistema inicializado
- Acesso ao repositório de ativos

**Fluxo Principal**:
1. Usuário acessa a Tela de Consulta de Ativos
2. Sistema exibe indicador de carregamento
3. Sistema recupera lista de ativos do repositório
4. Sistema formata os dados de cada ativo
5. Sistema exibe tabela com todos os ativos cadastrados
6. Usuário visualiza os dados na tabela

**Fluxos Alternativos**:
- **3a. Lista vazia**: Sistema exibe mensagem informativa
- **3b. Erro na recuperação**: Sistema exibe mensagem de erro com opção de tentar novamente

**Pós-condições**: 
- Tela exibindo lista de ativos (ou estado apropriado)

#### CU-02: Adicionar Novo Ativo

**Ator**: Usuário do sistema

**Pré-condições**: 
- Usuário está na Tela de Consulta de Ativos
- Existe pelo menos um `Issuer` cadastrado

**Fluxo Principal**:
1. Usuário clica no botão FAB (+)
2. Sistema navega para Tela de Cadastro de Ativo
3. Usuário seleciona categoria do ativo
4. Sistema exibe campos específicos da categoria selecionada
5. Usuário preenche todos os campos obrigatórios
6. Usuário clica em "Salvar"
7. Sistema valida os dados
8. Sistema salva o novo ativo no repositório
9. Sistema exibe mensagem de sucesso
10. Sistema redireciona para Tela de Consulta de Ativos
11. Sistema atualiza a lista de ativos (novo ativo aparece na tabela)

**Fluxos Alternativos**:
- **7a. Validação falha**: Sistema exibe mensagens de erro e destaca campos com problemas
- **8a. Erro ao salvar**: Sistema exibe mensagem de erro
- **5a. Usuário clica em "Cancelar"**: Sistema descarta alterações e retorna para Tela de Consulta de Ativos

**Pós-condições**: 
- Novo ativo cadastrado no sistema (se salvou com sucesso)
- Tela de Consulta de Ativos exibida

#### CU-03: Editar Ativo Existente

**Ator**: Usuário do sistema

**Pré-condições**: 
- Usuário está na Tela de Consulta de Ativos
- Existe pelo menos um ativo cadastrado

**Fluxo Principal**:
1. Usuário clica em uma linha da tabela (ativo)
2. Sistema navega para Tela de Edição de Ativo com o `id` do ativo
3. Sistema carrega dados do ativo do repositório
4. Sistema preenche formulário com dados do ativo
5. Usuário modifica campos desejados
6. Usuário clica em "Salvar"
7. Sistema valida os dados
8. Sistema atualiza o ativo no repositório
9. Sistema exibe mensagem de sucesso
10. Sistema redireciona para Tela de Consulta de Ativos
11. Sistema atualiza a lista de ativos (alterações refletidas na tabela)

**Fluxos Alternativos**:
- **3a. Ativo não encontrado**: Sistema exibe mensagem de erro e retorna para Tela de Consulta
- **7a. Validação falha**: Sistema exibe mensagens de erro e destaca campos com problemas
- **8a. Erro ao atualizar**: Sistema exibe mensagem de erro
- **5a. Usuário clica em "Cancelar"**: Sistema descarta alterações e retorna para Tela de Consulta de Ativos

**Pós-condições**: 
- Ativo atualizado no sistema (se salvou com sucesso)
- Tela de Consulta de Ativos exibida

### 12.2. Cenários de Erro

#### Erro: Falha ao Carregar Ativos

- **Causa**: Problema de conexão com repositório, erro no banco de dados, etc.
- **Comportamento**: Exibir mensagem de erro com opção de tentar novamente
- **Recuperação**: Usuário pode tentar novamente ou voltar para tela anterior

#### Erro: Validação de Campos

- **Causa**: Campos obrigatórios vazios, valores inválidos, etc.
- **Comportamento**: Destacar campos com problemas e exibir mensagens de erro específicas
- **Recuperação**: Usuário corrige os campos e tenta salvar novamente

#### Erro: Ticker Duplicado

- **Causa**: Tentativa de cadastrar Renda Variável com ticker já existente
- **Comportamento**: Exibir mensagem de erro específica no campo Ticker
- **Recuperação**: Usuário altera o ticker para um valor único

#### Erro: Data de Vencimento Inválida

- **Causa**: Data no passado ou formato inválido
- **Comportamento**: Exibir mensagem de erro no campo Data de Vencimento
- **Recuperação**: Usuário seleciona uma data futura válida

---

## Anexos

### A. Glossário

- **Asset**: Ativo financeiro cadastrado no sistema
- **AssetHolding**: Posição de um ativo (quantidade, valor investido, etc.)
- **Issuer**: Emissor do ativo (banco, empresa, etc.)
- **Ticker**: Código de negociação único de um ativo na bolsa de valores
- **CDI**: Certificado de Depósito Interbancário
- **IPCA**: Índice Nacional de Preços ao Consumidor Amplo
- **FAB**: Floating Action Button (Botão de Ação Flutuante)

### B. Referências

- Documento Arquitetural - Tela de Listagem
- Modelagem de Domínio
- Modelagem do Banco de Dados
