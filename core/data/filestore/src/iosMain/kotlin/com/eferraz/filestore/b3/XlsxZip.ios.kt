package com.eferraz.filestore.b3

/**
 * B3 import runs only on Desktop; iOS never invokes [B3ImportDataSourceImpl].
 */
internal actual suspend fun xlsxUnzip(bytes: ByteArray): Map<String, ByteArray> = emptyMap()

internal actual suspend fun xlsxZip(files: Map<String, ByteArray>): ByteArray = byteArrayOf()
