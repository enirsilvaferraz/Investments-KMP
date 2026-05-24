package com.eferraz.filestore.b3

internal object B3WorkbookSheets {

    suspend fun extractKnownSheets(sheetsNames: List<String>, xlsxBytes: ByteArray): Map<String, ByteArray> {

        val unzipped = xlsxUnzip(xlsxBytes)
        if (unzipped[XlsxPaths.WORKBOOK] == null) return emptyMap()

        val pathIndex = XlsxSheetPathIndex.parse(unzipped) ?: return emptyMap()

        return buildMap {
            putAll(sheetsNames.mapNotNull { sheet ->
                pathIndex.toSheetBytes(unzipped, sheet)?.let { bytes -> sheet to bytes }
            })
        }
    }
}

/** Caminhos fixos dentro do ZIP de um `.xlsx`. */
private object XlsxPaths {
    const val WORKBOOK = "xl/workbook.xml"
    const val WORKBOOK_RELS = "xl/_rels/workbook.xml.rels"
    const val SINGLE_SHEET_WORKSHEET = "xl/worksheets/sheet1.xml"
}

/**
 * Lê `workbook.xml` + `workbook.xml.rels` e expõe **nome da aba → caminho da worksheet** no ZIP.
 */
private class XlsxSheetPathIndex private constructor(
    private val nameToWorksheetPath: Map<String, String>,
) {

    suspend fun toSheetBytes(
        unzipped: Map<String, ByteArray>,
        sheet: String,
    ): ByteArray? {
        return SingleSheetXlsx.pack(unzipped, nameToWorksheetPath[sheet] ?: return null)
    }

    companion object {

        fun parse(unzipped: Map<String, ByteArray>): XlsxSheetPathIndex? {

            val workbookXml = unzipped[XlsxPaths.WORKBOOK]?.decodeToString() ?: return null
            val relsXml = unzipped[XlsxPaths.WORKBOOK_RELS]?.decodeToString() ?: return null

            val relIdToPath = OoxmlSheetXml.relationshipTargets(relsXml)

            val nameToPath = OoxmlSheetXml.sheetNameToRelId(workbookXml).mapNotNull { (name, relId) ->
                val path = relIdToPath[relId] ?: return@mapNotNull null
                name to path
            }.toMap()

            return XlsxSheetPathIndex(nameToPath)
        }
    }
}

/** Parsing mínimo dos XML do OOXML (sem DOM). */
private object OoxmlSheetXml {

    private val sheetTag = Regex(
        """<(?:[a-zA-Z0-9]+:)?sheet\b[^>]*name="([^"]+)"[^>]*\br:id="([^"]+)"""",
    )
    private val relationshipTag = Regex(
        """<(?:[a-zA-Z0-9]+:)?Relationship\b[^>]*\bId="([^"]+)"[^>]*\bTarget="([^"]+)"""",
    )

    fun relationshipTargets(relsXml: String): Map<String, String> =
        relationshipTag.findAll(relsXml).associate { match ->
            val relId = match.groupValues[1]
            val target = match.groupValues[2]
            relId to normalizeWorksheetPath(target)
        }

    fun sheetNameToRelId(workbookXml: String): Sequence<Pair<String, String>> =
        sheetTag.findAll(workbookXml).map { match ->
            match.groupValues[1] to match.groupValues[2]
        }

    private fun normalizeWorksheetPath(target: String): String =
        when {
            target.startsWith("/") -> target.removePrefix("/")
            else -> "xl/$target"
        }
}

/** Reempacota uma worksheet do ZIP original num `.xlsx` de aba única para o FileMapper. */
private object SingleSheetXlsx {

    suspend fun pack(
        unzipped: Map<String, ByteArray>,
        worksheetPath: String,
    ): ByteArray {

        val worksheetXml = unzipped[worksheetPath]
            ?: error("Worksheet not found: $worksheetPath")

        val archive = unzipped.toMutableMap()
        archive[XlsxPaths.SINGLE_SHEET_WORKSHEET] = worksheetXml

        return xlsxZip(archive)
    }
}
