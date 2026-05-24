package com.eferraz.filestore.b3

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual suspend fun xlsxUnzip(bytes: ByteArray): Map<String, ByteArray> = withContext(Dispatchers.IO) {
    val fileMap = mutableMapOf<String, ByteArray>()
    ZipInputStream(ByteArrayInputStream(bytes)).use { zipInput ->
        var entry = zipInput.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) {
                fileMap[entry.name] = zipInput.readBytes()
            }
            zipInput.closeEntry()
            entry = zipInput.nextEntry
        }
    }
    fileMap
}

internal actual suspend fun xlsxZip(files: Map<String, ByteArray>): ByteArray = withContext(Dispatchers.IO) {
    val output = ByteArrayOutputStream()
    ZipOutputStream(output).use { zipOutput ->
        files.forEach { (path, content) ->
            zipOutput.putNextEntry(ZipEntry(path))
            zipOutput.write(content)
            zipOutput.closeEntry()
        }
    }
    output.toByteArray()
}
