package com.eferraz.filestore

import java.io.File

internal actual fun writeCsv(content: String) {

    val downloadsDir = File("/storage/emulated/0/Download")
    if (!downloadsDir.exists()) downloadsDir.mkdirs()

    File(downloadsDir, RENDA_FIXA_CSV_FILE_NAME).writeText(content)
}
