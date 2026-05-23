# Research: Importação de Dados da B3

**Feature**: `003-b3-import` | **Phase**: 0 — Outline & Research | **Date**: 2026-05-23

---

## 1. Biblioteca para Leitura de XLSX no KMP

**Decision**: FileMapper-KMP `io.github.mamon-aburawi:filemapper-kmp:1.0.0`

**Rationale**:
- Biblioteca KMP nativa que mapeia sheets XLSX diretamente para data classes Kotlin usando anotações `@Serializable` (kotlinx.serialization) e `@ColumnName("Header")`.
- Suporte completo a Android, Desktop (JVM), iOS e Web (Wasm/JS) — declarada em `commonMain`, eliminando a necessidade de implementações por plataforma para o parser XLSX.
- Mapeamento automático colunas → propriedades facilita evoluções futuras da feature (ex.: persistência tipada, validação semântica, exportação) sem refatorar entidades.
- Publicada no Maven Central sob `io.github.mamon-aburawi`; versão `1.0.0` lançada em 08/04/2026.
- Inclui `FileMapperPicker` para seleção de arquivo multiplataforma (Desktop, Android, iOS, Web) — elimina completamente a necessidade de `JFileChooser` ou qualquer código de plataforma específico.
- API `fileMapper.importData<T>(bytes, ignoreColumns, onSuccess, onFailed)` com callbacks e tratamento de erro via `FileMapperException`.

**Alternatives considered**:
- Apache POI `poi-ooxml` 5.5.1: padrão de mercado para JVM, mas restrito a `jvmMain` — impede reutilização em Android/iOS e exige manipulação manual de linhas/células sem mapeamento automático para data classes; descartado em favor do FileMapper-KMP.
- `xlsx4j` (parte do docx4j): mais pesado, licença LGPL; descartado por overhead desnecessário.
- Leitura manual de ZIP + XML: produz código frágil e difícil de manter; descartado.
- `fastexcel`: leve mas sem mapeamento para data classes e sem suporte KMP nativo; descartado.

---

## 2. Seletor de Arquivo Nativo no Desktop (JVM)

**Decision**: `FileMapperPicker.pickFile(type = FileType.XLSX)` — picker nativo integrado do FileMapper-KMP

**Rationale**:
- FileMapper-KMP inclui `FileMapperPicker` como parte da biblioteca, com suporte nativo em Android, Desktop (JVM), iOS e Web (Wasm/JS).
- API `suspend fun pickFile(type): PlatformFile?` — suspende a coroutine enquanto o diálogo está aberto; retorna `null` se o usuário cancelar.
- Elimina a dependência de `javax.swing.JFileChooser` e a necessidade de gerenciar a EDT (Event Dispatch Thread) manualmente.
- Para Compose Desktop, a integração alternativa `rememberFileMapper<T>` encapsula picking + parsing em um único controller reativo — útil para fases futuras com UI mais rica.
- A implementação do port `B3ImportPortImpl` (que encapsula picker + parsing + log) fica inteiramente em `commonMain`, sem `expect`/`actual` e sem código de plataforma.

**Alternatives considered**:
- `javax.swing.JFileChooser`: disponível sem dependência adicional, mas requer gerenciamento manual da EDT (`EventQueue.invokeAndWait`), restrito a JVM, e não reutilizável em Android/iOS; descartado em favor do picker nativo do FileMapper-KMP.
- `java.awt.FileDialog`: filtragem menos confiável em macOS; API mais baixo nível; descartado.

---

## 3. Dispatcher para Processamento do XLSX

**Decision**: `Dispatchers.Default` injetado via Koin no `UseCaseModule`

**Rationale**:
- O `UseCaseModule` já expõe um `@Single CoroutineDispatcher = Dispatchers.Default`.
- `AppUseCase` executa `execute()` dentro de `withContext(context)`, portanto a troca de dispatcher é automática ao injetar `Dispatchers.Default`.
- `Dispatchers.Default` é adequado para trabalho CPU-bound (parsing de XLSX) e não bloqueia a thread principal da UI.
- `FileMapperPicker.pickFile()` é uma função `suspend` que lida internamente com o contexto de plataforma correto; não há EDT blocking a gerenciar — a coroutine simplesmente suspende até o usuário confirmar ou cancelar o diálogo.

**Alternatives considered**:
- `Dispatchers.IO`: adequado para I/O de disco puro, mas `Dispatchers.Default` é preferível para parsing CPU-intensivo.
- `viewModelScope` + `Dispatchers.Main`: incorreto — processamento pesado nunca na thread principal.

---

## 4. Estratégia de Timeout

**Decision**: `withTimeout(30_000L)` dentro de `ImportB3FileUseCase.execute()`

**Rationale**:
- `withTimeout` lança `TimeoutCancellationException` automaticamente após 30 s, que é capturado pelo `runCatching` em `AppUseCase.result()` e retornado como `Result.failure(...)`.
- O ViewModel observa o `Result` e atualiza o estado para exibir a mensagem de erro e remover o spinner.
- Simples e alinhado ao padrão `AppUseCase` sem introduzir nova infraestrutura.

**Alternatives considered**:
- `Job.cancel()` manual com timer: mais verboso e propenso a erros.
- `Flow.timeout()`: não aplicável — operação única, não um stream.

---

## 5. Estrutura Real do Arquivo B3 (arquivo inspecionado: `posicao-2026-05-23-12-53-16.xlsx`)

Arquivo XLSX exportado pela B3 (posição em carteira). Inspecionado em 2026-05-23.

### Guias presentes

| # | Nome da Guia            | Linhas de dados | Colunas |
|---|-------------------------|-----------------|---------|
| 1 | Acoes                   | 21              | 14      |
| 2 | ETF                     | 4               | 13      |
| 3 | Fundo de Investimento   | 28              | 14      |
| 4 | Renda Fixa              | 26              | 19      |
| 5 | Tesouro Direto          | 4               | 13      |

### Estrutura comum (guias Acoes, ETF, Fundo de Investimento)

| Coluna | Nome no XLSX                  | Tipo observado |
|--------|-------------------------------|----------------|
| 1      | Produto                       | String (com espaços à direita) |
| 2      | Instituição                   | String |
| 3      | Conta                         | String numérico |
| 4      | Código de Negociação          | String (ticker) |
| 5      | CNPJ da Empresa / CNPJ do Fundo | String |
| 6      | Código ISIN / Distribuição    | String |
| 7      | Tipo                          | String |
| 8      | Escriturador / Administrador  | String (Acoes/FII) — ausente em ETF |
| 9      | Quantidade                    | Numérico (inteiro) |
| 10     | Quantidade Disponível         | Numérico (inteiro) |
| 11     | Quantidade Indisponível       | "-" quando zero |
| 12     | Motivo                        | "-" quando vazio |
| 13     | Preço de Fechamento           | Numérico decimal |
| 14     | Valor Atualizado              | Numérico decimal |

> **Nota**: Última linha de cada guia contém a célula "Total" na coluna do valor e o somatório; as demais células dessa linha estão vazias. Essa linha é **descartada do parse** — o total exibido no log é recalculado pela implementação.

### Estrutura — Renda Fixa (19 colunas)

| Coluna | Nome no XLSX                    | Tipo observado |
|--------|---------------------------------|----------------|
| 1      | Produto                         | String |
| 2      | Instituição                     | String |
| 3      | Emissor                         | String |
| 4      | Código                          | String |
| 5      | Indexador                       | String (DI, IPCA, "-") |
| 6      | Tipo de regime                  | String (DEPOSITADO, REGISTRADO) |
| 7      | Data de Emissão                 | String (dd/MM/yyyy) |
| 8      | Vencimento                      | String (dd/MM/yyyy) |
| 9      | Quantidade                      | Numérico |
| 10     | Quantidade Disponível           | Numérico |
| 11     | Quantidade Indisponível         | "-" quando zero |
| 12     | Motivo                          | "-" quando vazio |
| 13     | Contraparte                     | "-" quando vazio |
| 14     | Preço Atualizado MTM            | "-" quando não aplicável |
| 15     | Valor Atualizado MTM            | "-" quando não aplicável |
| 16     | Preço Atualizado CURVA          | Numérico decimal |
| 17     | Valor Atualizado CURVA          | Numérico decimal |
| 18     | Preço Atualizado FECHAMENTO     | "-" quando não aplicável |
| 19     | Valor Atualizado FECHAMENTO     | "-" quando não aplicável |

> **Nota**: Linha de total aparece nas colunas 17 ("Total") e 19 ("Total") simultaneamente.

### Estrutura — Tesouro Direto (13 colunas)

| Coluna | Nome no XLSX            | Tipo observado |
|--------|-------------------------|----------------|
| 1      | Produto                 | String (ex.: "Tesouro IPCA+ 2029") |
| 2      | Instituição             | String |
| 3      | Código ISIN             | String |
| 4      | Indexador               | String (IPCA, SELIC) |
| 5      | Vencimento              | String (dd/MM/yyyy) |
| 6      | Quantidade              | Numérico decimal (frações de título) |
| 7      | Quantidade Disponível   | Numérico decimal |
| 8      | Quantidade Indisponível | Numérico inteiro |
| 9      | Motivo                  | "-" quando vazio |
| 10     | Valor Aplicado          | Numérico decimal |
| 11     | Valor bruto             | Numérico decimal |
| 12     | Valor líquido           | Numérico decimal |
| 13     | Valor Atualizado        | Numérico decimal |

### Padrões de limpeza identificados

1. **Células com "-"**: representam ausência de valor — devem ser preservadas como string no log desta fase.
2. **Espaços à direita em "Produto"**: strings como `"AXIA6 - AXIA ENERGIA S.A.                                 "` — fazer `.trim()` antes de logar.
3. **Linha de total do arquivo**: última linha real de cada guia contém "Total" (e em Renda Fixa também nas colunas 17 e 19). **Descartada** em `parseAndLog`: linhas cuja primeira célula (campo `product`) começa com `"Total"` ou `"Subtotal"` **não são mapeadas para DTOs** e não chegam ao resultado final.
4. **Linhas completamente vazias**: não presentes no arquivo analisado, mas a lógica **filtra** linhas onde todos os campos são nulos, vazios ou `"-"` após trim — nenhuma linha em branco chega ao resultado final.
5. **Header das colunas**: o FileMapper-KMP usa o cabeçalho da sheet internamente para resolver `@ColumnName` → campo do DTO; o cabeçalho não é incluído em `importData<T>`. Para o log, `parseAndLog` **reconstrói o header** a partir das anotações `@ColumnName` do DTO e o imprime antes das linhas de dados.
6. **Total calculado no log**: ao final de cada sheet, `parseAndLog` imprime uma linha de totais calculados pela implementação — somando campos numéricos relevantes (ex.: quantidade, valor atualizado) das `dataRows` já parseadas. O valor **não é lido do arquivo**.

---

## 6. Padrão `expect`/`actual` vs Interface+Impl

**Decision**: Usar **interface + impl** (não `expect`/`actual`) para os ports

**Rationale**:
- `expect`/`actual` é adequado para tipos primitivos e funções de plataforma.
- Para ports (interfaces de domínio com implementações de plataforma), o padrão Koin com `interface` + `bind<Port>()` é mais flexível e alinhado ao princípio D da constituição (inversão de dependência).
- Com FileMapper-KMP em `commonMain`, a implementação do único port (`B3ImportPortImpl`) também é `commonMain` — sem nenhum código de plataforma necessário.
- Registra-se no `FileStoreModule.kt` via `singleOf(::B3ImportPortImpl).bind<B3ImportPort>()`.

**Alternatives considered**:
- `expect`/`actual`: válido mas mais rígido; desnecessário quando a lib subjacente já é KMP.

---

## 7. Localização dos DTOs B3 e da Lógica de Log

**Decision**: DTOs B3 e lógica de log ficam **inteiramente em `:data:filestore`** — nunca expostos ao domínio

**Rationale**:
- DTOs (`B3StockPosition`, `B3EtfPosition`, `B3FundPosition`, `B3FixedIncomePosition`, `B3TreasuryPosition`) dependem de `@ColumnName` (FileMapper-KMP) e `@Serializable` (kotlinx.serialization) — anotações de infraestrutura que não pertencem à camada de domínio.
- A lógica de log (`println` de posições por guia) é um detalhe de desenvolvimento desta fase, não lógica de negócio — pertence à implementação, não ao UseCase.
- O UseCase (`ImportB3FileUseCase`) enxerga apenas `suspend B3ImportPort.importAndLog(): Result<Unit>` — sem DTOs, sem guias, sem FileMapper.
- Essa separação viabiliza substituir o log por persistência ou exportação em fases futuras **sem tocar no UseCase**.

**Alternatives considered**:
- DTOs em `:domain:entity`: violaria Clean Architecture — o domínio dependeria de libs de infraestrutura (`@ColumnName`).
- Log no UseCase: acoplaria o UseCase a detalhes de formato de arquivo; tornaria o teste do UseCase dependente dos DTOs B3.

---

## 8. Nomes das Classes B3

**Decision**: Inglês — `B3StockPosition`, `B3EtfPosition`, `B3FundPosition`, `B3FixedIncomePosition`, `B3TreasuryPosition`

**Rationale**:
- Convenção do projeto: código em inglês (constituição, princípio VIII).
- DTOs internos ao módulo `filestore` — visibilidade `internal`; nunca aparecem em APIs públicas.

---

## Resoluções de NEEDS CLARIFICATION

| Item | Resolução |
|------|-----------|
| Biblioteca XLSX | FileMapper-KMP `io.github.mamon-aburawi:filemapper-kmp:1.0.0` (`commonMain`) |
| File picker | `FileMapperPicker.pickFile(FileType.XLSX)` — nativo KMP, incluído no FileMapper-KMP |
| Dispatcher | `Dispatchers.Default` (já injetado via `UseCaseModule`) |
| Timeout | `withTimeout(30_000L)` em `ImportB3FileUseCase` |
| Padrão de plataforma | Interface + Impl em `commonMain` (FileMapper-KMP elimina código por plataforma) |
| Linha de total no XLSX | **Descartada do parse** — não mapeada para DTO; o total exibido no log é **calculado** pela implementação sobre as `dataRows` |
| Células com "-" | Preservadas como string; `.trim()` tratado pelo FileMapper-KMP |
| Localização dos DTOs B3 | `:data:filestore` — internos, visibilidade `internal` |
| Lógica de log | `:data:filestore` (`B3ImportPortImpl`) — UseCase não conhece DTOs |
| Nomes das classes B3 | Inglês: `B3StockPosition`, `B3EtfPosition`, `B3FundPosition`, `B3FixedIncomePosition`, `B3TreasuryPosition` |
| Número de ports | Um único `B3ImportPort` — encapsula picker + parsing + log |
