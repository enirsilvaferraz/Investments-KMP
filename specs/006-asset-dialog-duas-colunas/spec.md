# Especificação de feature: Dialog de asset em duas colunas

**Branch da feature**: `[não definido nesta execução]`  
**Criada em**: 2026-04-25  
**Estado**: Rascunho  
**Entrada do utilizador**: "/speckit.specify Vamos fazer uma atualização na tela de cadastro e edição de assets. @core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/view/AssetManagementFormView.kt Vamos ter duas colunas nesse dialog. A primeira coluna será a tela atual com todos os campos existentes. A segunda coluna sera uma tabela que conterá todos as transaçãoes daquela asset/holding. Vamos mover o campo corretora da coluna 1 para o topo da coluna 2. Entre as colunas deve haver um separador suave. A coluna 1 deve se parecer com o conteudo existente hoje no dialog, sem a coluna corretora."

**Constitution (Investments-KMP):** Incluir, quando aplicável, critérios de **qualidade**, **API/visibilidade** (**`~/.cursor/rules/explicit-api.mdc`**, explicitApi, superfície mínima entre módulos — princípio IV), **testabilidade** (incl. **`~/.cursor/rules/test-patterns.mdc`**, **inglês**, **GIVEN / WHEN / THEN** nos nomes de testes em `*Test.kt`, **KDoc** no método, comentários **`// GIVEN` / `// WHEN` / `// THEN`** no corpo quando aplicável (**linha em branco** antes de cada marcador), **MockK** para dependências externas quando aplicável, **criação de objetos no próprio teste** — **evitar** *factories* de teste (ex. `TestDataFactory`) para código novo, **testes obrigatórios** para mudanças em `core/domain/usecases/` (módulo Gradle **`:domain:usecases`**) — princípio V), **consistência de UX** (formatação, erros; **pré-visualizações `@Preview` no mesmo ficheiro que o composable** — princípio VI), **desempenho** (latência, volumes) e **coerência** código ↔ documentação ↔ `.specify` ↔ `.cursor` (princípios IV–IX); ver `.specify/memory/constitution.md`.

**Idioma:** Redigir este documento em **português do Brasil (pt-BR)** (princípio VIII).

## Clarificações

### Sessão 2026-04-25

- Q: Como a coluna de histórico deve se comportar quando ainda não existir holding salva? → A: Exibir estado vazio com mensagem "Histórico disponível após salvar a holding".
- Q: Como tratar lista longa de transações na coluna 2 sem degradar o dialog? → A: Tabela com rolagem interna na coluna 2, mantendo altura do dialog estável.

## Cenários de utilizador e testes *(obrigatório)*

### História de utilizador 1 — Visualizar formulário e histórico lado a lado (Prioridade: P1)

Como utilizador que cadastra ou edita um asset, quero ver os campos do formulário e a tabela de transações da mesma holding no mesmo dialog para tomar decisões de edição com contexto completo.

**Por que esta prioridade**: Esta mudança é o núcleo da entrega e altera a experiência principal do fluxo de cadastro/edição.

**Teste independente**: Abrir o dialog de cadastro/edição e validar que há duas colunas com separação visual suave, contendo formulário na esquerda e tabela de transações na direita.

**Cenários de aceitação**:

1. **Dado** que o utilizador abre o dialog de asset, **quando** o conteúdo é exibido, **então** ele vê duas colunas distintas no corpo do dialog.
2. **Dado** que há transações para a holding, **quando** o dialog é carregado, **então** a segunda coluna exibe uma tabela com todas as transações daquela asset/holding.
3. **Dado** que não há transações para a holding, **quando** o dialog é exibido, **então** a segunda coluna apresenta estado vazio claro com a mensagem "Histórico disponível após salvar a holding", sem quebrar o layout em duas colunas.

---

### História de utilizador 2 — Manter a experiência atual do formulário (Prioridade: P2)

Como utilizador frequente do formulário atual, quero que a coluna de formulário continue semelhante ao dialog existente para não reaprender o fluxo.

**Por que esta prioridade**: Preserva familiaridade e reduz risco de regressão de usabilidade.

**Teste independente**: Comparar a primeira coluna com o dialog atual e validar que os campos existentes permanecem, exceto o campo de corretora.

**Cenários de aceitação**:

1. **Dado** o novo layout em duas colunas, **quando** o utilizador interage com a primeira coluna, **então** encontra os mesmos campos atuais do formulário, mantendo ordem e comportamento, com exceção do campo corretora.
2. **Dado** o novo layout, **quando** o utilizador procura o campo corretora, **então** ele não está na primeira coluna.

---

### História de utilizador 3 — Encontrar corretora junto ao histórico (Prioridade: P3)

Como utilizador, quero ver o campo de corretora no topo da segunda coluna para relacionar a seleção da corretora com as transações exibidas.

**Por que esta prioridade**: Melhora organização semântica dos dados da holding no mesmo bloco visual.

**Teste independente**: Verificar que o campo corretora aparece antes da tabela de transações na coluna direita.

**Cenários de aceitação**:

1. **Dado** o dialog aberto, **quando** o conteúdo da segunda coluna é renderizado, **então** o campo corretora aparece no topo da coluna.
2. **Dado** o campo corretora no topo da segunda coluna, **quando** o utilizador altera seu valor, **então** a interação de preenchimento continua funcional.

---

### Casos extremos (edge cases)

- O que acontece quando a lista de transações é longa e excede a altura visível do dialog?
- Como o sistema trata holdings sem transações sem perder o alinhamento visual entre as colunas?
- O que acontece quando o nome/descrição de transações é muito extenso na tabela?
- Quando houver muitas transações, a coluna de histórico mantém rolagem interna, preservando altura estável do dialog e posição das ações principais.

## Requisitos *(obrigatório)*

### Requisitos funcionais

- **RF-001**: O sistema DEVE apresentar o conteúdo do dialog de cadastro/edição de asset em duas colunas no corpo principal.
- **RF-002**: O sistema DEVE manter na primeira coluna todos os campos existentes do formulário atual, removendo apenas o campo corretora.
- **RF-003**: O sistema DEVE posicionar o campo corretora no topo da segunda coluna.
- **RF-004**: O sistema DEVE exibir na segunda coluna uma tabela com todas as transações associadas à asset/holding em edição ou cadastro.
- **RF-005**: O sistema DEVE exibir um separador visual suave entre as duas colunas para indicar divisão de áreas.
- **RF-006**: O sistema DEVE preservar as ações de cancelar e salvar do dialog sem alterar sua disponibilidade por causa da nova distribuição em colunas.
- **RF-007**: O sistema DEVE apresentar estado vazio legível na tabela de transações quando não houver registros, exibindo a mensagem "Histórico disponível após salvar a holding" e mantendo a estrutura de duas colunas.
- **RF-008**: O sistema DEVE manter altura estável do dialog quando houver muitas transações, aplicando rolagem interna na tabela da segunda coluna.

### Entidades principais *(incluir se a feature envolver dados)*

- **Asset/Holding**: Registro principal em cadastro/edição, com dados do formulário e relacionamento com transações.
- **Transação de Asset**: Evento financeiro associado à holding, exibido na tabela da segunda coluna.
- **Corretora**: Informação de vínculo da holding, reposicionada para o topo da segunda coluna.

## Critérios de sucesso *(obrigatório)*

### Resultados mensuráveis

- **CS-001**: 100% das aberturas do dialog de cadastro/edição exibem duas colunas visíveis e separadas visualmente.
- **CS-002**: Em validação funcional, 100% dos campos do formulário antigo permanecem disponíveis na primeira coluna, exceto corretora.
- **CS-003**: Em validação funcional, 100% das telas exibem o campo corretora antes da tabela de transações na segunda coluna.
- **CS-004**: Em testes de usabilidade interna, pelo menos 90% dos utilizadores identificam corretamente em até 10 segundos onde editar formulário e onde consultar transações.

## Premissas

- O dialog atual já possui fonte de dados confiável para recuperar transações por asset/holding.
- O escopo desta feature é exclusivamente a organização visual e estrutural do dialog, sem mudança de regras de negócio.
- As ações de salvar e cancelar permanecem com o mesmo comportamento funcional já existente.
