# Pesquisa — Cadastro e edição de transações no diálogo de ativo

## Decisão 1: Estratégia de staging em memória durante a edição

- **Decision**: Manter um estado de sessão em memória com transações criadas e transações editadas até o utilizador confirmar salvar o formulário principal.
- **Rationale**: Atende ao requisito de persistência em lote, evita gravações parciais e mantém o comportamento previsível do diálogo.
- **Alternatives considered**:
  - Persistir a cada edição: rejeitado por contrariar escopo da feature e aumentar risco de inconsistência parcial.
  - Manter somente diffs sem estado completo: rejeitado por elevar complexidade de reconciliação na UI.

## Decisão 2: Momento de confirmação da edição inline

- **Decision**: Confirmar edição no evento de finalização da célula; valor inválido é rejeitado, valor anterior é preservado e feedback de erro é exibido.
- **Rationale**: Reduz chance de estado inválido em memória e facilita validação de aceitação e regressão.
- **Alternatives considered**:
  - Aceitar valor inválido temporariamente: rejeitado por ambiguidade no salvamento final.
  - Auto-corrigir valor inválido: rejeitado por risco de alterar intenção do utilizador sem transparência.

## Decisão 3: Escopo funcional de operações na tabela

- **Decision**: Incluir apenas criação e edição de transações; exclusão fica fora de escopo desta versão.
- **Rationale**: Mantém foco no objetivo principal da entrega e reduz risco de expansão de regras de negócio.
- **Alternatives considered**:
  - Permitir exclusão total: rejeitado por ampliar regras de confirmação e auditoria.
  - Permitir exclusão apenas de itens novos: rejeitado por benefício limitado nesta iteração.

## Decisão 4: Regra de cancelamento/fechamento sem salvar

- **Decision**: Descartar imediatamente todo rascunho em memória ao cancelar/fechar sem salvar.
- **Rationale**: Garante semântica clara de confirmação explícita e evita persistência acidental de estado transitório.
- **Alternatives considered**:
  - Restaurar rascunho na mesma sessão: rejeitado por conflitar com decisão de descarte explícito.
  - Perguntar em modal de confirmação: rejeitado por adicionar fricção não solicitada.

## Decisão 5: Ordenação de transações na tabela

- **Decision**: Ordenar por data da transação, exibindo primeiro as mais recentes.
- **Rationale**: Facilita inspeção das últimas alterações e padroniza leitura independentemente da origem do item (carregado ou criado/alterado).
- **Alternatives considered**:
  - Manter ordem de carregamento: rejeitado por menor utilidade durante edição ativa.
  - Priorizar itens alterados na sessão: rejeitado por poder mascarar ordem temporal real.

## Decisão 6: Estratégia de validação e evidência

- **Decision**: Validar feature por cenários funcionais da spec + compilação do módulo `:features:asset-management`.
- **Rationale**: Mudanças são majoritariamente de apresentação/estado local e os critérios de aceitação são diretamente observáveis no fluxo.
- **Alternatives considered**:
  - Apenas validação manual sem roteiro: rejeitado por baixa rastreabilidade.
  - Cobertura de testes de domínio: rejeitado nesta fase por ausência de mudança em `usecases`.
