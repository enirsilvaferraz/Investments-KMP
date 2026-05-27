# Contract: B3ImportDataSource

**Módulo**: `:core:domain:usecases`
**Pacote**: `com.eferraz.usecases.repositories`
**Arquivo**: `B3ImportDataSource.kt`
**Tipo de alteração**: Substituição de método existente

## Interface Atualizada

```kotlin
package com.eferraz.usecases.repositories

import com.eferraz.usecases.entities.B3Record

public interface B3ImportDataSource {

    /**
     * Lê o arquivo XLSX exportado da B3 e retorna a lista de registros de domínio
     * prontos para correlação com o histórico.
     *
     * Linhas em branco são descartadas silenciosamente.
     * Posições com valor inválido emitem `println` de aviso e são descartadas.
     *
     * @return [Result.success] com a lista de [B3Record] (pode ser vazia);
     *         [Result.failure] apenas em caso de erro de acesso ao arquivo.
     */
    public suspend fun import(): Result<List<B3Record>>
}
```

## Impacto de Quebra

O método `importAndLog(): Result<Unit>` é **removido**. Todos os consumidores devem ser atualizados:

| Consumidor | Ação necessária |
|------------|----------------|
| `B3ImportDataSourceImpl` | Substituir implementação de `importAndLog()` por `import()` |
| `ImportB3FileUseCase` | Substituir chamada `port.importAndLog()` por `port.import()` |
