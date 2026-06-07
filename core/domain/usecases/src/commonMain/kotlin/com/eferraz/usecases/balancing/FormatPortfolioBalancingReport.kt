package com.eferraz.usecases.balancing

private const val COLUMN_SEPARATOR: String = " | "

public fun formatPortfolioBalancingReport(report: PortfolioBalancingReport): String =
    formatTreeReport(report)

internal fun formatTreeReport(report: PortfolioBalancingReport): String {
    val builder = StringBuilder()

    report.sections.forEachIndexed { index, section ->
        if (index > 0) {
            builder.appendLine()
        }
        builder.appendLine("=== ${section.nodeName} ===")
        builder.appendLine()

        val layout = ColumnLayout.from(section)
        builder.appendLine(layout.headerRow())
        builder.appendLine(layout.separatorRow())
        section.rows.forEach { row ->
            builder.appendLine(layout.formatRow(row))
        }
        builder.appendLine(layout.separatorRow())
        builder.appendLine(layout.formatRow(section.totalRow))
        val holdingsSection = formatHoldingsSection(section.rows.flatMap { it.holdings })
        if (holdingsSection.isNotEmpty()) {
            builder.appendLine()
            builder.append(holdingsSection)
        }
    }

    return buildString {
        appendLine()
        append(builder.toString().trimEnd())
        appendLine()
    }
}

private fun formatHoldingsSection(holdings: List<PortfolioBalancingHoldingLine>): String {
    if (holdings.isEmpty()) {
        return ""
    }

    val nameHeader = "Investimento"
    val valueHeader = "Valor"
    val nameWidth = maxOf(nameHeader.length, holdings.maxOf { it.displayName.length })
    val valueWidth = maxOf(valueHeader.length, holdings.maxOf { BalancingFormatters.formatMoney(it.value).length })

    return buildString {
        appendLine("Investimentos:")
        holdings.forEach { holding ->
            appendLine(
                "  " + listOf(
                    padRight(holding.displayName, nameWidth),
                    padLeft(BalancingFormatters.formatMoney(holding.value), valueWidth),
                ).joinToString("  "),
            )
        }
    }
}

private data class ColumnLayout(
    val nameWidth: Int,
    val actualWidth: Int,
    val actualWeightWidth: Int,
    val configuredWidth: Int,
    val idealWidth: Int,
    val deviationWidth: Int,
) {
    fun headerRow(): String = formatRow(
        name = "Nome",
        actual = "Valor actual",
        actualWeight = "Peso actual",
        configured = "Peso configurado",
        ideal = "Valor ideal",
        deviation = "Desvio",
    )

    fun separatorRow(): String = columnWidths().joinToString("-+-") { "-".repeat(it) }

    fun formatRow(line: PortfolioBalancingReportLine): String = formatRow(
        name = line.displayName,
        actual = BalancingFormatters.formatMoney(line.actualValue),
        actualWeight = line.actualWeightDisplay,
        configured = line.configuredWeightDisplay,
        ideal = BalancingFormatters.formatMoney(line.idealValue),
        deviation = BalancingFormatters.formatSignedMoney(line.deviation),
    )

    private fun columnWidths(): List<Int> = listOf(
        nameWidth,
        actualWidth,
        actualWeightWidth,
        configuredWidth,
        idealWidth,
        deviationWidth,
    )

    private fun formatRow(
        name: String,
        actual: String,
        actualWeight: String,
        configured: String,
        ideal: String,
        deviation: String,
    ): String = buildList {
        add(padRight(name, nameWidth))
        add(padLeft(actual, actualWidth))
        add(padLeft(actualWeight, actualWeightWidth))
        add(padLeft(configured, configuredWidth))
        add(padLeft(ideal, idealWidth))
        add(padLeft(deviation, deviationWidth))
    }.joinToString(COLUMN_SEPARATOR)

    companion object {
        fun from(section: PortfolioBalancingReportSection): ColumnLayout {
            val allRows = section.rows + section.totalRow
            val headers = listOf(
                "Nome",
                "Valor actual",
                "Peso actual",
                "Peso configurado",
                "Valor ideal",
                "Desvio",
            )
            return ColumnLayout(
                nameWidth = maxOf(headers[0].length, allRows.maxOf { it.displayName.length }),
                actualWidth = maxOf(
                    headers[1].length,
                    allRows.maxOf { BalancingFormatters.formatMoney(it.actualValue).length },
                ),
                actualWeightWidth = maxOf(
                    headers[2].length,
                    allRows.maxOf { it.actualWeightDisplay.length },
                ),
                configuredWidth = maxOf(
                    headers[3].length,
                    allRows.maxOf { it.configuredWeightDisplay.length },
                ),
                idealWidth = maxOf(
                    headers[4].length,
                    allRows.maxOf { BalancingFormatters.formatMoney(it.idealValue).length },
                ),
                deviationWidth = maxOf(
                    headers[5].length,
                    allRows.maxOf { BalancingFormatters.formatSignedMoney(it.deviation).length },
                ),
            )
        }
    }
}

private fun padRight(text: String, width: Int): String {
    if (text.length >= width) return text
    return text + " ".repeat(width - text.length)
}

private fun padLeft(text: String, width: Int): String {
    if (text.length >= width) return text
    return " ".repeat(width - text.length) + text
}
