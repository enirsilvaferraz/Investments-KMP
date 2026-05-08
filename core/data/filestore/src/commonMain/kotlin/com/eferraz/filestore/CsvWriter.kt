package com.eferraz.filestore

internal const val RENDA_FIXA_CSV_FILE_NAME: String = "renda_fixa.csv"

internal expect fun writeCsv(content: String)
