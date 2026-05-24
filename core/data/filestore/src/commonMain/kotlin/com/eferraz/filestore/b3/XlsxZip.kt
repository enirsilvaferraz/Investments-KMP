package com.eferraz.filestore.b3

internal expect suspend fun xlsxUnzip(bytes: ByteArray): Map<String, ByteArray>

internal expect suspend fun xlsxZip(files: Map<String, ByteArray>): ByteArray
