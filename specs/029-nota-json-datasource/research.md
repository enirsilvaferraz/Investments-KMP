# Research: Importação de Nota de Corretagem via JSON

**Feature**: `029-nota-json-datasource` | **Phase**: 0 — Outline & Research | **Date**: 2026-06-11

---

## 1. Desserialização JSON (kotlinx.serialization)

**Decision**: `kotlinx.serialization` com `Json { ignoreUnknownKeys = true }` e DTOs `@Serializable` em `core/data/filestore/.../brokeragenote/dto/`.

**Rationale**:

- Já presente em `:data:filestore` (`build.gradle.kts` declara `libs.kotlinx.serialization`).
- Padrão idêntico aos DTOs B3 (`B3StockPosition`, etc.) no mesmo módulo.
- `ignoreUnknownKeys = true` atende FR-007 / clarificação: propriedades extras são ignoradas silenciosamente.
- Campos obrigatórios ausentes ou tipos incompatíveis propagam `SerializationException` → encapsuladas em `Result.failure` pelo data source.

**Alternatives considered**:

- Reutilizar entidades de `:domain:entity` com `@Serializable`: **rejeitado** — viola FR-002 e acopla nomes de domínio ao esquema JSON em português.
- `kotlinx.serialization` com `@JsonNames` nos tipos de domínio: mesmo problema de acoplamento.
- Parser manual (`JSONObject` / regex): frágil, mais código, sem ganho de valor.

---

## 2. Nomes de campos JSON em português

**Decision**: DTOs intermediários com propriedades Kotlin em **inglês** (convenção do projeto) e `@SerialName("snake_case_pt")` para cada campo JSON.

**Rationale**:

- Alinha com B3 DTOs (`@ExcelColumn` mapeia header PT → propriedade EN).
- Mantém mapeador explícito DTO → domínio legível e testável.
- Exemplo: `@SerialName("numero_nota") val noteNumber: String`.

**Alternatives considered**:

- Propriedades DTO em português (`numeroNota`): inconsistente com restante do módulo `filestore`.
- Copiar literais JSON como nomes de propriedade Kotlin: viola princípio VIII (código em inglês).

---

## 3. Parsing de datas `dd/MM/yyyy`

**Decision**: Desserializar datas como `String` nos DTOs; converter para `kotlinx.datetime.LocalDate` no mapper via função interna `parseBrazilianDate(String): LocalDate`.

**Rationale**:

- `kotlinx.datetime` já é dependência transitiva via `:domain:entity`.
- Formato fixo e documentado na spec (`10/06/2026`); sem timezone.
- Falha de parse (string malformada) → exceção descritiva no mapper, capturada pelo data source como `Result.failure`.
- Evita `KSerializer` customizado global — escopo mínimo (princípio X).

**Alternatives considered**:

- `KSerializer<LocalDate>` com formato BR: reutilizável, mas over-engineering para dois campos.
- `Instant` / epoch: incompatível com o esquema de entrada.

---

## 4. Fonte do JSON de referência (`nota2.json`)

**Decision** *(revisto 2026-06-11 — clarificação spec)*: Conteúdo de `docs/nota2.json` materializado como constante Kotlin `internal val raw: String` em `Nota2JsonFixture.kt` (`commonMain`); **sem** `commonMain/resources/`, **sem** `expect/actual`, **sem** file picker.

**Rationale**:

- Clarificação da spec: API pública do data source parseia apenas a constante fixa; sem parâmetro de conteúdo.
- 100% `commonMain` alinha com plano (princípio III — evita `expect/actual` desnecessário).
- Fixture de referência estável para data source e testes (`Nota2JsonFixture.raw`).
- Fonte canónica permanece `docs/nota2.json`; a constante é gerada/copiada a partir desse ficheiro.

**Alternatives considered** *(rejeitadas na clarificação)*:

- `expect/actual` + `commonMain/resources/nota2.json`: exigiria I/O por plataforma; superseded pela constante.
- Compose Multiplatform Resources: escopo extra, viola princípio X.
- File picker (padrão B3): fora de escopo explícito da spec.


---

## 5. Contrato e localização da implementação

**Decision** *(revisto 2026-06-11 — limite de escopo)*: Interface pública `BrokerageNoteJsonDataSource` e implementação `BrokerageNoteJsonDataSourceImpl` **ambas** em `:data:filestore` (`brokeragenote/`); registo Koin via `@Factory(binds = [...])`. **Sem** port em `:domain:usecases` nesta feature.

**Rationale**:

- Clarificação da spec: alterações **somente** em `:data:filestore` (princípio X).
- Interface `public` + impl `internal` atende princípio VI (`explicitApi`).
- Integração futura com features pode extrair port para `:domain:usecases` em feature separada (ver `plan.md` — Complexity Tracking).
- Padrão B3 (`B3ImportDataSource` em usecases) **não** replicado aqui por restrição explícita de escopo.

**Alternatives considered**:

- Port em `:domain:usecases` + impl em filestore (padrão B3): **adiado** — violaria limite de módulos alterados.
- UseCase `LoadBrokerageNoteFromJsonUseCase`: YAGNI — spec limita-se ao data source.

---

## 6. Mapeamento `movimentacao` → `TradeType`

**Decision**: Função interna `parseTradeType(raw: String): TradeType` com `when` exaustivo para `"COMPRA"` → `BUY`, `"VENDA"` → `SELL`; qualquer outro literal → `IllegalArgumentException`.

**Rationale**:

- Atende FR-006 e User Story 3 cenário 3.
- Enum de domínio `TradeType` permanece inalterado (fora de escopo alterar entity).

---

## 7. Mapeamento `valor_liquido_nota` → `netValue`

**Decision**: Pass-through numérico sem inversão de sinal.

**Rationale**:

- Clarificação 2026-06-11 e alinhamento com feature 026: JSON de referência (`nota2.json`) já expressa débito líquido como positivo (`12294.92`).
- Diferente de `docs/nota.json` (nota anterior com sinal invertido) — **fora do escopo** desta feature.

---

## 8. Estratégia de testes

**Decision**: Testes unitários em `:data:filestore` (`jvmTest`), pacote `com.eferraz.filestore.brokeragenote`, padrão `GIVEN_WHEN_THEN`.

**Rationale**:

- Mapeamento e parse são responsabilidade da camada de dados; não há UseCase novo (princípio V aplica-se a `:domain:usecases` — sem alteração de regras de negócio).
- Fixture via `Nota2JsonFixture.raw` (constante Kotlin) nos testes JVM; falhas via JSON inline em `BrokerageNoteJsonMapper.parse(String)` (FR-010).
- SC-004: asserções campo-a-campo contra valores literais do JSON; **sem** invocar `BrokerageNoteValidator`.
- Testes de JSON inválido/incompleto cobrem User Story 3 (SC-003).

**Alternatives considered**:

- Testes em `:domain:entity`: domínio não conhece JSON.
- Testes em `:domain:usecases`: sem UseCase, MockK do port seria circular.

---

## 9. Performance (SC-002)

**Decision**: Parse síncrono em memória; operação `suspend` na interface `BrokerageNoteJsonDataSource` (consistência com `B3ImportDataSource`) executada em `Dispatchers.Default`.

**Rationale**:

- JSON de referência (~47 ativos, ~15 KB) completa em sub-milissegundo em JVM.
- SC-002 (< 1 s) é facilmente atingível sem otimização prematura.

