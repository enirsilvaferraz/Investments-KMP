# Tela de Acompanhamento de Metas

## Visão Geral
Esta tela destina-se ao acompanhamento detalhado da evolução das metas financeiras do usuário. Ela deve ser integrada ao Navigation Rail da aplicação, seguindo o padrão de navegação e layout das telas de "Assets" e "Histórico".

A principal forma de navegação entre as diferentes metas será através de um **Segmented Control** dinâmico.

## Estrutura da Tela

A implementação deve seguir a estrutura base do `HistoryScreen.kt`, utilizando o `AppScaffold` com painel principal e painel lateral (supporting pane).

### 1. Seletor de Período (Header de Ações)
-   **Componente:** `SegmentedControl` localizado na área de ações do `AppScaffold`.
-   **Opções:** "Mensal" e "Anual"
-   **Comportamento:** Permite alternar entre visualização mensal e anual dos dados.

### 2. Navegação de Metas (Segmented Control)
-   **Componente:** `SegmentedControl` localizado na parte inferior esquerda da área de conteúdo principal (alinhado ao bottomStart).
-   **Comportamento Dinâmico:** As opções deste controle serão as próprias metas cadastradas pelo usuário.
    -   *Exemplo:* [Aposentadoria] | [Apartamento] | [Automóvel]
-   **Visibilidade:** O controle só é exibido quando há metas cadastradas.
-   **Seleção:** A seleção de uma aba atualiza a Tabela Principal e o Painel Lateral para o contexto da meta selecionada.
-   **Restrições:** Não é necessário scroll horizontal (assume-se quantidade limitada de metas) e não é necessário tratar estado vazio (empty state).

### 3. Painel Lateral (Detalhes e Ativos)
O painel lateral (Supporting pane em layouts adaptativos) exibirá as informações detalhadas da meta selecionada, organizadas em seções:

#### A. Resumo da Meta
-   **Nome da Meta**
-   **Valor Alvo**
-   **Data de Início**

#### B. Previsão de Evolução
-   **Data Prevista**
-   **Aporte Mensal**
-   **Rentabilidade Anual**

#### C. Evolução Real
-   **Data Prevista**
-   **Aporte Mensal (Médio)**
-   **Rentabilidade Anual (Real)**

#### D. Ativos Vinculados
Logo abaixo das seções anteriores, haverá uma tabela contendo todos os ativos financeiros atrelados a esta meta.
-   **Campos:** Exibir apenas o **Nome** do ativo em uma tabela simples.

### 4. Tabela de Evolução (Painel Principal)
A visualização principal dos dados será através de uma tabela (`UiTable`), exibindo o histórico mês a mês ou ano a ano, dependendo da seleção do período.

#### Colunas Definidas

| Título               | Descrição                                                                       | Tipo de Dado | Alinhamento   | Footer (Rodapé)     |
|:---------------------|:--------------------------------------------------------------------------------|:-------------|:--------------|:--------------------|
| **Mês/Ano**          | Data de referência do registro. Ordenável por data.                             | `YearMonth`  | Centro        | -                   |
| **Meta**             | Projeção de quanto deve ter naquele mês/ano específico.                         | `Money`      | Início        | -                   |
| **Valor Total**      | Saldo acumulado do objetivo naquele mês.                                        | `Money`      | Início        | -                   |
| **Saldo**            | Diferença entre o valor total e a meta.                                         | `Money`      | Início        | -                   |
| **Aportes**          | Valor aportado especificamente naquele mês.                                     | `Money`      | Fim (Direita) | Soma dos Aportes    |
| **Retiradas**        | Saques realizados naquele mês (se houver).                                      | `Money`      | Fim (Direita) | Soma das Retiradas  |
| **Crescimento (R$)** | Rendimento absoluto do mês (Valor Final - Valor Inicial - Aportes + Retiradas). | `Money`      | Fim (Direita) | Soma do Crescimento |
| **Crescimento (%)**  | Rendimento percentual do mês.                                                   | `Percentage` | Fim (Direita) | -                   |
| **Lucro (R$)**       | Lucro absoluto acumulado do investimento até o momento.                         | `Money`      | Fim (Direita) | -                   |
| **Lucro (%)**        | Rentabilidade acumulada do investimento até o momento.                          | `Percentage` | Fim (Direita) | -                   |
