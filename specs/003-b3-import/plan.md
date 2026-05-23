# Implementation Plan: Importação de Dados da B3

**Branch**: `003-b3-import` | **Date**: 2026-05-23 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/003-b3-import/spec.md`

## Summary

Implementar importação de arquivos XLSX exportados pela B3 para a tela `AssetHistoryScreen` (Desktop-only). O fluxo: botão de upload → `B3ImportPort.importAndLog()` (em `:data:filestore`) que usa **FileMapper-KMP** para abrir o seletor nativo, parsear as 5 guias em DTOs tipados (`B3StockPosition`, `B3EtfPosition`, etc.) e logar no console. O UseCase apenas gerencia timeout e estado — nenhum detalhe de XLSX ou DTO vaza para o domínio. Android e iOS recebem stubs sem comportamento.

## Technical Context

**Language/Version**: Kotlin 2.3.21 (KMP) — alvo JVM para Desktop

**Primary Dependencies**:
- FileMapper-KMP `io.github.mamon-aburawi:filemapper-kmp:1.0.0` (`commonMain` de `:data:filestore` — suporte nativo a Android, Desktop, iOS, Web; inclui parser XLSX **e** file picker multiplataforma via `FileMapperPicker`)
- `kotlinx-serialization-core` (requerido pelo FileMapper-KMP para anotações `@Serializable` nos DTOs internos de `:data:filestore`)
- `kotlinx-coroutines-core` (já presente)
- MockK + kotlinx-coroutines-test (testes de UseCases)

> `javax.swing.JFileChooser` **removido** — substituído por `FileMapperPicker.pickFile()` nativo do FileMapper-KMP, que funciona em todas as plataformas KMP sem dependência extra.

**Storage**: N/A — nenhum dado é persistido nesta fase; apenas log no console

**Testing**: `./gradlew :domain:usecases:jvmTest` (padrão do projeto)

**Target Platform**: Desktop (JVM) — implementação completa. Android e iOS: stubs sem comportamento.

**Project Type**: Kotlin Compose Multiplatform — feature de ecrã existente

**Performance Goals**: Processamento completo em < 30 s; `Dispatchers.Default` para parsing CPU-bound

**Constraints**:
- Timeout de 30 segundos com cancelamento automático (FR-011)
- Apenas arquivos `.xlsx` aceitos (FR-003)
- Nenhuma persistência ou validação semântica nesta fase

**Scale/Scope**: Um arquivo por vez; até ~100 linhas por guia (arquivo real B3 analisado)

## Constitution Check

| Princípio | Status | Observação |
|---|---|---|
| I. SOLID / DRY | ✅ | Cada UseCase tem responsabilidade única; ports evitam duplicação |
| II. Clean Architecture | ✅ | Ports em `:domain:usecases`; impls em `:data:filestore`; UI mínima |
| III. KMP First | ✅ | `commonMain` para domínio, FileMapper-KMP (parser + picker nativos multiplataforma) e ambas as implementações de port — zero código específico de plataforma; stubs permanecem apenas no nível de UI/ViewModel |
| IV. Foundation Plugins | ✅ | Módulos existentes; nenhum plugin novo criado |
| V. Testes em UseCases | ✅ | `ImportB3FileUseCase` terá testes JVM com mock de `B3ImportPort` |
| VI. API Explícita | ✅ | Visibilidade `public`/`internal` explícita em todo símbolo novo |
| VII. Docs Sincronizados | ✅ | Nenhuma entidade de domínio nova nesta fase; `DOMAIN.md` sem alterações |
| VIII. Idioma | ✅ | Código em inglês; documentação em pt-BR |

**Resultado: APROVADO — sem violações.**

## Project Structure

### Documentation (this feature)

```text
specs/003-b3-import/
├── plan.md              # Este ficheiro
├── research.md          # Fase 0 — decisões técnicas
├── data-model.md        # Fase 1 — entidades e mapeamento do XLSX
├── quickstart.md        # Fase 1 — guia de verificação
├── contracts/
│   └── XlsxImportContract.md   # Contratos de porta (port interfaces)
└── tasks.md             # Fase 2 — gerado por /speckit-tasks
```

### Source Code (repository root)

```text
build-logic/gradle/libs.versions.toml          # + filemapper-kmp = "1.0.0" + lib declaration

core/domain/entity/src/commonMain/
└── (sem arquivos novos nesta fase — domínio não expõe entidades B3 enquanto objetivo é só log)

core/domain/usecases/src/commonMain/
└── com/eferraz/usecases/
    ├── repositories/
    │   └── B3ImportPort.kt                    # NOVO — único port: suspend importAndLog(): Result<Unit>
    └── services/
        ├── ImportB3FileUseCase.kt              # NOVO — withTimeout(30s) + delega ao B3ImportPort
        └── (testes em jvmTest/ — mock de B3ImportPort)

core/data/filestore/src/commonMain/com/eferraz/filestore/
├── b3/
│   ├── dto/
│   │   ├── B3StockPosition.kt                 # NOVO — DTO interno (@Serializable, guia Acoes)
│   │   ├── B3EtfPosition.kt                   # NOVO — DTO interno (@Serializable, guia ETF)
│   │   ├── B3FundPosition.kt                  # NOVO — DTO interno (@Serializable, guia Fundo)
│   │   ├── B3FixedIncomePosition.kt           # NOVO — DTO interno (@Serializable, guia Renda Fixa)
│   │   └── B3TreasuryPosition.kt              # NOVO — DTO interno (@Serializable, guia Tesouro)
│   └── B3ImportPortImpl.kt                    # NOVO — FileMapperPicker + importData<T> + println
└── FileStoreModule.kt                         # ALTERAR — registrar B3ImportPortImpl.bind<B3ImportPort>()
# ⚠️ Nenhum jvmMain / androidMain / iosMain: FileMapper-KMP é commonMain-only

core/presentation/composeApp/src/commonMain/
└── com/eferraz/presentation/features/history/
    ├── HistoryState.kt                         # ALTERAR — adicionar isImporting: Boolean
    ├── HistoryViewModel.kt                     # ALTERAR — injetar ImportB3FileUseCase, novo intent
    └── AssetHistoryScreen.kt                  # ALTERAR — botão/spinner em Actions
```

**Structure Decision**: Módulos existentes reaproveitados. Nenhum subprojeto Gradle novo. O domínio fica limpo — nenhuma entidade B3 nova nesta fase. Todo o conhecimento de XLSX (DTOs, FileMapper-KMP, file picker, log) fica encapsulado em `:data:filestore`; o domínio vê apenas `B3ImportPort` com assinatura `suspend importAndLog(): Result<Unit>`. FileMapper-KMP é `commonMain`, eliminando completamente `expect`/`actual` e código específico de plataforma.

## Complexity Tracking

> Sem violações — seção não aplicável.
