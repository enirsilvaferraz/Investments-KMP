# Quickstart: Dialog Unificado de Cadastro de Ativo + Holding

**Feature**: 001-asset-holding-dialog | **Date**: 2026-05-17

## Pré-requisitos

- JDK 21+ configurado
- Projeto Investments-KMP clonado e na branch `001-asset-holding-dialog`

## Verificação Rápida

```bash
# Compilar o módulo afetado (JVM)
./gradlew :features:asset-management:compileKotlinJvm

# Compilar o shell da app (verifica integração com App.kt)
./gradlew :apps:umbrellaApp:compileKotlinJvm

# Rodar testes de usecases (se alterados)
./gradlew :domain:usecases:jvmTest
```

## Arquivos Principais

### Novos

| Arquivo | Descrição |
|---------|-----------|
| `dialog/DialogViewModel.kt` | ViewModel de ciclo de vida do dialog |

### Modificados

| Arquivo | Alteração |
|---------|-----------|
| `assets/AssetManagementViewModel.kt` | Adicionar injeção de `GetBrokeragesUseCase`, `GetAssetHoldingUseCase`, `UpsertAssetHoldingUseCase`. Persistência sequencial asset → holding. |
| `assets/AssetManagementScreen.kt` | Refatorar em `AssetManagementScreen` (público, shell do dialog com Scaffold+TopAppBar) + `AssetFormView` (interno, formulário). Adicionar dropdown de corretora. |
| `assets/AssetManagementUiState.kt` | Adicionar campos `brokerage`, `brokerages`, `brokerageError`, `holdingId`, `owner`. |
| `assets/AssetManagementEvents.kt` | Adicionar `BrokerageChanged`. Alterar `ScreenEntered` para receber `holdingId`. |
| `App.kt` (composeApp) | Descomentar entry de `AssetManagementRouting` com `AssetManagementScreen`. |

### Removidos

| Arquivo | Motivo |
|---------|--------|
| `holdings/HoldingManagementView.kt` | Campo de corretora incorporado inline. |
| `holdings/HoldingManagementViewModel.kt` | Lógica absorvida pelo AssetManagementViewModel. |
| `holdings/HoldingManagementUiState.kt` | Estado consolidado. |
| `holdings/HoldingManagementEvents.kt` | Eventos consolidados. |

## Fluxo de Teste Manual

1. Executar o app (Android ou Desktop).
2. Clicar no FAB (+) na barra de navegação.
3. Verificar que o dialog em tela cheia abre com formulário de asset + dropdown de corretora.
4. Preencher todos os campos obrigatórios (incluindo corretora).
5. Clicar em "Salvar" → verificar que o dialog fecha após sucesso.
6. Verificar no banco (Room) que tanto o asset quanto o holding foram criados.
7. Ir ao Histórico → clicar para editar um holding → verificar que o dialog abre pré-populado.
8. Alterar a corretora → salvar → verificar atualização.
9. Testar fechar com botão X → verificar que dados são descartados.

## Decisões de Design

Consultar `research.md` para justificativas detalhadas de cada decisão:
- Comunicação via callback (`onComplete`/`onDismiss`) — padrão existente no projeto.
- `DialogViewModel` como coordenador de ciclo de vida.
- Persistência sequencial sem rollback.
- Remoção do subpacote `holdings/` após incorporação.
