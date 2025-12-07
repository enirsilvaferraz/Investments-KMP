# Regras de Negócio - Componente de AutoComplete

## Índice

1. [Objetivo](#1-objetivo)
2. [Funcionalidades Principais](#2-funcionalidades-principais)
3. [Estados Visuais](#3-estados-visuais)
4. [Regras de Funcionamento](#4-regras-de-funcionamento)
5. [Comportamento de Salvamento](#5-comportamento-de-salvamento)
6. [Especificações de UI/UX](#6-especificações-de-uiux)

---

## 1. Objetivo

O componente AutoComplete é um campo de entrada de texto com sugestões (suggestion box/autocomplete) que permite ao usuário selecionar um item existente de uma lista ou digitar manualmente um novo valor. O componente fornece feedback visual em tempo real sobre o estado do valor digitado (existente ou não na lista).

Este componente é utilizado em formulários onde é necessário permitir tanto a seleção de valores pré-cadastrados quanto a criação de novos valores durante o preenchimento do formulário.

---

## 2. Funcionalidades Principais

### 2.1. Digitação Livre

O componente permite que o usuário digite livremente qualquer texto no campo, sem restrições de seleção obrigatória da lista de sugestões.

**Comportamento:**
- O campo aceita qualquer entrada de texto do usuário
- Não há validação que impeça a digitação de valores não existentes na lista
- O usuário pode continuar digitando mesmo que o valor não corresponda a nenhuma sugestão

### 2.2. Exibição de Sugestões

O componente exibe uma lista de sugestões baseada nos itens cadastrados que correspondem ao texto digitado pelo usuário.

**Comportamento:**
- As sugestões são exibidas automaticamente conforme o usuário digita
- A lista de sugestões é filtrada em tempo real baseada no texto digitado
- A filtragem deve ser case-insensitive (não diferencia maiúsculas de minúsculas)
- A lista de sugestões deve aparecer abaixo do campo de entrada
- A lista deve ser scrollável caso haja muitas sugestões

**Critérios de Filtragem:**
- As sugestões devem corresponder parcialmente ao texto digitado
- A correspondência pode ser no início, meio ou fim do nome do item
- Exemplo: Se o usuário digita "Banco", devem aparecer sugestões como "Banco do Brasil", "Banco Inter", "Banco Master", etc.

### 2.3. Seleção de Sugestão

O usuário pode selecionar uma sugestão da lista para preencher o campo automaticamente.

**Comportamento:**
- Ao clicar em uma sugestão da lista, o campo é preenchido com o valor completo da sugestão
- Após a seleção, a lista de sugestões é fechada
- O campo muda para o estado de **Sucesso** (ver seção 3.2)
- O usuário pode continuar editando o valor após selecionar uma sugestão

---

## 3. Estados Visuais

O componente possui três estados visuais distintos que fornecem feedback imediato ao usuário sobre o status do valor digitado.

### 3.1. Estado Normal

**Condição:** Campo vazio ou em edição (sem correspondência verificada)

**Aparência Visual:**
- Campo exibido normalmente, sem indicação visual especial
- Borda padrão do campo de texto
- Sem ícones ou cores especiais

**Quando Ocorre:**
- Quando o campo está vazio
- Durante a digitação, antes da validação ser concluída
- Quando o usuário está editando o campo mas ainda não há correspondência verificada

### 3.2. Estado Sucesso

**Condição:** O nome digitado corresponde exatamente a um item existente na lista

**Aparência Visual:**
- Campo exibido com indicação visual de sucesso
- Borda verde
- Cor de fundo levemente esverdeada com baixa opacidade
- **Nota:** O feedback visual é fornecido apenas através de cores (bordas e fundo), sem ícones

**Quando Ocorre:**
- Quando o texto digitado corresponde exatamente (case-insensitive) a um item da lista de sugestões
- Após selecionar uma sugestão da lista
- Após salvar um novo item que estava em estado Warning (ver seção 5)

**Validação:**
- A correspondência deve ser exata (ignorando maiúsculas/minúsculas)
- Espaços em branco no início e fim devem ser ignorados na comparação
- Exemplo: "Banco do Brasil" corresponde a "banco do brasil" ou "BANCO DO BRASIL"

### 3.3. Estado Warning

**Condição:** O nome digitado não corresponde a nenhum item existente na lista

**Aparência Visual:**
- Campo exibido com indicação visual de aviso
- Borda amarela ou laranja
- Cor de fundo levemente amarelada/alaranjada com baixa opacidade
- **Nota:** O feedback visual é fornecido apenas através de cores (bordas e fundo), sem ícones

**Quando Ocorre:**
- Quando o texto digitado não corresponde a nenhum item da lista de sugestões
- Quando o usuário digita um valor completamente novo
- O estado deve ser mantido até que o valor seja salvo ou alterado para corresponder a um item existente

**Importante:**
- O estado Warning não impede o salvamento do formulário
- É apenas um indicador visual de que o valor não existe na lista
- O sistema deve criar o novo item automaticamente ao salvar (ver seção 5)

---

## 4. Regras de Funcionamento

### 4.1. Validação em Tempo Real

A validação visual (estados Sucesso/Warning) deve ocorrer em tempo real conforme o usuário digita.

**Comportamento:**
- A validação deve ser executada após um pequeno delay (debounce) para evitar validações excessivas
- O delay recomendado é de 300-500ms após o usuário parar de digitar
- A validação deve verificar se o texto digitado corresponde a algum item da lista
- O estado visual deve ser atualizado imediatamente após a validação

**Algoritmo de Validação:**
1. Usuário digita no campo
2. Aguarda o delay de debounce
3. Busca na lista de itens cadastrados por correspondência exata (case-insensitive)
4. Se encontrar correspondência: muda para estado **Sucesso**
5. Se não encontrar correspondência: muda para estado **Warning**
6. Se campo vazio: mantém estado **Normal**

### 4.2. Filtragem de Sugestões

A lista de sugestões deve ser filtrada dinamicamente conforme o usuário digita.

**Comportamento:**
- A filtragem deve ser case-insensitive
- Deve buscar correspondências parciais (não apenas no início)
- A lista deve ser atualizada em tempo real
- Se não houver sugestões correspondentes, a lista não deve ser exibida

**Critérios de Filtragem:**
- Busca por correspondência parcial no nome do item
- Exemplo: "Banco" deve retornar "Banco do Brasil", "Banco Inter", "Banco Master"
- Exemplo: "Brasil" deve retornar "Banco do Brasil"
- Exemplo: "Inter" deve retornar "Banco Inter"

### 4.3. Interação com a Lista de Sugestões

**Comportamento da Lista:**
- A lista deve aparecer quando há sugestões correspondentes ao texto digitado
- A lista deve desaparecer quando:
  - O usuário seleciona uma sugestão
  - O campo perde o foco (blur)
  - Não há mais sugestões correspondentes
  - O usuário pressiona ESC (opcional, mas recomendado)

**Navegação:**
- O usuário pode navegar pela lista usando as setas do teclado (opcional, mas recomendado)
- O usuário pode selecionar uma sugestão clicando com o mouse
- O usuário pode selecionar uma sugestão pressionando Enter quando uma sugestão está destacada

### 4.4. Estado Inicial

Quando o componente é exibido pela primeira vez ou quando o formulário é limpo:

**Comportamento:**
- O campo deve estar vazio
- O campo deve estar no estado **Normal**
- A lista de sugestões não deve ser exibida
- O campo está pronto para receber entrada do usuário

---

## 5. Comportamento de Salvamento

Quando o formulário que contém o componente AutoComplete é salvo, o sistema deve verificar se o valor digitado está em estado Warning (não existe na lista) e, se estiver, criar automaticamente o novo item.

### 5.1. Detecção de Novo Item

**Condição:** O valor do campo está em estado **Warning** (não corresponde a nenhum item existente)

**Validação Pré-Salvamento:**
- O sistema deve verificar se o nome não está vazio
- O sistema deve verificar se o nome não contém apenas espaços em branco
- Se o nome estiver vazio ou contiver apenas espaços, o sistema deve exibir uma mensagem de erro e impedir o salvamento

### 5.2. Criação Automática do Novo Item

**Processo de Salvamento:**

1. **Validação do Nome:**
   - Verificar se o nome não está vazio
   - Remover espaços em branco no início e fim do nome
   - Validar que o nome não contém apenas espaços

2. **Persistência no Banco de Dados:**
   - Criar o novo item no banco de dados
   - O item deve ser salvo com o nome exatamente como digitado pelo usuário (após remoção de espaços extras)
   - O item deve estar disponível imediatamente para futuras sugestões

3. **Atualização da Lista de Sugestões:**
   - A lista de sugestões deve ser atualizada para incluir o novo item
   - O novo item deve estar disponível para seleção em futuras utilizações do componente

4. **Atualização do Estado Visual:**
   - O campo deve mudar automaticamente do estado **Warning** para o estado **Sucesso**
   - A mudança de estado deve ocorrer imediatamente após o salvamento bem-sucedido

### 5.3. Salvamento de Item Existente

Quando o valor do campo está em estado **Sucesso** (corresponde a um item existente):

**Comportamento:**
- O sistema utiliza o item existente
- Não há necessidade de criar um novo item
- O campo permanece no estado **Sucesso**

### 5.4. Tratamento de Erros

**Cenários de Erro:**

1. **Nome Vazio:**
   - Se o campo estiver vazio e for obrigatório, exibir mensagem de erro
   - Impedir o salvamento do formulário
   - Manter o campo no estado **Normal** ou exibir estado de **Erro**

2. **Falha na Persistência:**
   - Se houver erro ao salvar o novo item no banco de dados, exibir mensagem de erro
   - O formulário não deve ser salvo
   - O campo deve permanecer no estado **Warning**
   - O usuário deve ser notificado sobre o erro

3. **Item Duplicado:**
   - Se durante o salvamento for detectado que um item com o mesmo nome já existe (caso de race condition), o sistema deve:
     - Utilizar o item existente
     - Atualizar o campo para estado **Sucesso**
     - Continuar com o salvamento do formulário normalmente

---

## 6. Especificações de UI/UX

### 6.1. Aparência Visual

**Campo de Entrada:**
- Deve seguir o design system do aplicativo
- Deve ter aparência consistente com outros campos de texto do formulário
- Deve ter label visível indicando o propósito do campo
- Deve ter placeholder opcional para orientar o usuário

**Lista de Sugestões:**
- Deve aparecer abaixo do campo de entrada
- Implementada usando `ExposedDropdownMenu` do Material3 para melhor integração e acessibilidade
- Deve ter estilo visual consistente com o design system
- Deve ter scroll quando houver muitas sugestões
- Deve destacar a sugestão sob o cursor do mouse ou teclado
- Deve ter altura máxima para não ocupar toda a tela

### 6.2. Feedback Visual por Estado

**Estado Normal:**
- Borda padrão (cor neutra)
- Sem ícones adicionais
- Fundo padrão

**Estado Sucesso:**
- Borda verde (ex: `#4CAF50` ou similar)
- Fundo levemente esverdeado com baixa opacidade
- **Nota:** O feedback visual é fornecido apenas através de cores (bordas e fundo), sem ícones

**Estado Warning:**
- Borda amarela/laranja (ex: `#FF9800` ou similar)
- Fundo levemente amarelado/alaranjado com baixa opacidade
- **Nota:** O feedback visual é fornecido apenas através de cores (bordas e fundo), sem ícones

### 6.3. Acessibilidade

**Recomendações:**
- O campo deve ser acessível via leitores de tela
- Os estados visuais devem ter equivalentes textuais para leitores de tela
- A lista de sugestões deve ser navegável via teclado
- Deve haver indicação clara de qual sugestão está destacada
- O componente deve seguir as diretrizes de acessibilidade da plataforma (Android/iOS)

### 6.4. Performance

**Otimizações Recomendadas:**
- Implementar debounce na validação para evitar validações excessivas
- Implementar cache da lista de sugestões quando apropriado
- Limitar o número de sugestões exibidas (ex: máximo de 10-15 itens)
- Implementar busca otimizada (ex: índice de busca, busca incremental)

### 6.5. Responsividade

**Comportamento em Diferentes Tamanhos de Tela:**
- Em telas pequenas, a lista de sugestões deve se adaptar ao espaço disponível
- A lista não deve sobrepor outros elementos importantes da interface
- Em dispositivos móveis, considerar exibir a lista em modal ou bottom sheet se necessário

---

## Referências

Este componente é utilizado no campo "Emissor" da Tela de Cadastro de Assets. Para mais informações sobre o uso específico neste contexto, consulte o documento [RF - Tela de Cadastro de Assets](RF%20-%20Tela%20de%20Cadastro%20de%20Assets.md).

