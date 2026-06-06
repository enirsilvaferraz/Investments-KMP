package com.eferraz.usecases.balancing

private const val COLUMN_SEPARATOR: String = " | "

public fun formatPortfolioBalancingReport(report: PortfolioBalancingReport): String {

    val componentRows = report.lines.map { line ->
        FormattedRow(
            groupId = line.groupId,
            groupName = line.groupName,
            name = line.componentName,
            actual = BalancingFormatters.formatMoney(line.actualValue),
            actualWeight = line.actualWeightDisplay,
            configuredWeight = line.configuredWeightDisplay,
            normalizedWeight = line.normalizedWeightDisplay,
            ideal = BalancingFormatters.formatMoney(line.idealValue),
            deviation = BalancingFormatters.formatMoney(line.deviation),
            actualValue = line.actualValue,
            idealValue = line.idealValue,
            deviationValue = line.deviation,
            actualWeightPercent = line.actualWeightPercent,
            normalizedWeightPercent = line.normalizedWeightPercent,
        )
    }

    val rowsWithTotals = componentRows.appendGroupTotalRows()
    val layout = ColumnLayout.from(rowsWithTotals, showNormalizedWeight = report.hasDynamicWeight)
    val holdingsByGroup = report.groupHoldings.associateBy { it.groupId }

    val builder = StringBuilder()

    for (groupId in report.orderedGroupIds) {

        val groupRows = rowsWithTotals.filter { it.groupId == groupId }
        if (groupRows.isEmpty()) continue

        if (builder.isNotEmpty()) {
            builder.appendLine()
        }

        builder.appendLine("=== ${groupRows.first().groupName} ===")
        builder.appendLine(layout.headerRow())
        builder.appendLine(layout.separatorRow())
        val dataRows = groupRows.filter { it.name != "Total" }
        val totalRow = groupRows.firstOrNull { it.name == "Total" }
        dataRows.forEach { builder.appendLine(layout.formatRow(it)) }
        if (totalRow != null) {
            builder.appendLine(layout.separatorRow())
            builder.appendLine(layout.formatRow(totalRow))
        }
        builder.appendLine()
        builder.appendLine(formatHoldingsSection(holdingsByGroup[groupId]?.holdings.orEmpty()))
    }

    return buildString {
        appendLine()
        append(builder.toString().trimEnd())
        appendLine()
    }
}

private fun formatHoldingsSection(holdings: List<PortfolioBalancingHoldingLine>): String {

    if (holdings.isEmpty()) {
        return "Investimentos: (nenhum)"
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

private fun List<FormattedRow>.appendGroupTotalRows(): List<FormattedRow> {

    if (isEmpty()) return this

    val result = mutableListOf<FormattedRow>()
    var currentGroupId: BalancingGroupId? = null
    var groupRows = mutableListOf<FormattedRow>()

    fun flushGroup() {
        if (groupRows.isEmpty()) return
        result += groupRows
        result += groupRows.buildTotalRow()
        groupRows = mutableListOf()
    }

    for (row in this) {
        if (row.groupId != currentGroupId) {
            flushGroup()
            currentGroupId = row.groupId
        }
        groupRows += row
    }
    flushGroup()
    return result
}

private fun List<FormattedRow>.buildTotalRow(): FormattedRow = FormattedRow(
    groupId = this.first().groupId,
    groupName = this.first().groupName,
    name = "Total",
    actual = BalancingFormatters.formatMoney(sumOf { it.actualValue }),
    actualWeight = BalancingFormatters.formatPercent(sumOf { it.actualWeightPercent }),
    configuredWeight = "100,00%",
    normalizedWeight = BalancingFormatters.formatPercent(sumOf { it.normalizedWeightPercent }),
    ideal = BalancingFormatters.formatMoney(sumOf { it.idealValue }),
    deviation = BalancingFormatters.formatMoney(sumOf { it.deviationValue }),
    actualValue = sumOf { it.actualValue },
    idealValue = sumOf { it.idealValue },
    deviationValue = sumOf { it.deviationValue },
    actualWeightPercent = sumOf { it.actualWeightPercent },
    normalizedWeightPercent = sumOf { it.normalizedWeightPercent },
)

private data class FormattedRow(
    val groupId: BalancingGroupId,
    val groupName: String,
    val name: String,
    val actual: String,
    val actualWeight: String,
    val configuredWeight: String,
    val normalizedWeight: String,
    val ideal: String,
    val deviation: String,
    val actualValue: Double,
    val idealValue: Double,
    val deviationValue: Double,
    val actualWeightPercent: Double,
    val normalizedWeightPercent: Double,
)

private data class ColumnLayout(
    val nameWidth: Int,
    val actualWidth: Int,
    val actualWeightWidth: Int,
    val configuredWidth: Int,
    val normalizedWidth: Int,
    val idealWidth: Int,
    val deviationWidth: Int,
    val showNormalizedWeight: Boolean,
) {
    fun headerRow(): String = formatRow(
        name = "Nome",
        actual = "Valor actual",
        actualWeight = "Percentual actual",
        configuredWeight = "Peso configurado",
        normalizedWeight = "Peso normalizado",
        ideal = "Valor ideal",
        deviation = "Desvio",
    )

    fun separatorRow(): String = columnWidths().joinToString("-+-") { "-".repeat(it) }

    fun formatRow(row: FormattedRow): String = formatRow(
        name = row.name,
        actual = row.actual,
        actualWeight = row.actualWeight,
        configuredWeight = row.configuredWeight,
        normalizedWeight = row.normalizedWeight,
        ideal = row.ideal,
        deviation = row.deviation,
    )

    private fun columnWidths(): List<Int> = buildList {
        add(nameWidth)
        add(actualWidth)
        add(actualWeightWidth)
        add(configuredWidth)
        if (showNormalizedWeight) add(normalizedWidth)
        add(idealWidth)
        add(deviationWidth)
    }

    private fun formatRow(
        name: String,
        actual: String,
        actualWeight: String,
        configuredWeight: String,
        normalizedWeight: String,
        ideal: String,
        deviation: String,
    ): String = buildList {
        add(padRight(name, nameWidth))
        add(padLeft(actual, actualWidth))
        add(padLeft(actualWeight, actualWeightWidth))
        add(padLeft(configuredWeight, configuredWidth))
        if (showNormalizedWeight) add(padLeft(normalizedWeight, normalizedWidth))
        add(padLeft(ideal, idealWidth))
        add(padLeft(deviation, deviationWidth))
    }.joinToString(COLUMN_SEPARATOR)

    companion object {
        fun from(rows: List<FormattedRow>, showNormalizedWeight: Boolean): ColumnLayout {
            val headers = HeaderLabels(
                name = "Nome",
                actual = "Valor actual",
                actualWeight = "Percentual actual",
                configuredWeight = "Peso configurado",
                normalizedWeight = "Peso normalizado",
                ideal = "Valor ideal",
                deviation = "Desvio",
            )
            return ColumnLayout(
                nameWidth = maxOf(headers.name.length, rows.maxOfOrNull { it.name.length } ?: 0),
                actualWidth = maxOf(headers.actual.length, rows.maxOfOrNull { it.actual.length } ?: 0),
                actualWeightWidth = maxOf(
                    headers.actualWeight.length,
                    rows.maxOfOrNull { it.actualWeight.length } ?: 0,
                ),
                configuredWidth = maxOf(
                    headers.configuredWeight.length,
                    rows.maxOfOrNull { it.configuredWeight.length } ?: 0,
                ),
                normalizedWidth = if (showNormalizedWeight) {
                    maxOf(
                        headers.normalizedWeight.length,
                        rows.maxOfOrNull { it.normalizedWeight.length } ?: 0,
                    )
                } else {
                    0
                },
                idealWidth = maxOf(headers.ideal.length, rows.maxOfOrNull { it.ideal.length } ?: 0),
                deviationWidth = maxOf(headers.deviation.length, rows.maxOfOrNull { it.deviation.length } ?: 0),
                showNormalizedWeight = showNormalizedWeight,
            )
        }
    }
}

private data class HeaderLabels(
    val name: String,
    val actual: String,
    val actualWeight: String,
    val configuredWeight: String,
    val normalizedWeight: String,
    val ideal: String,
    val deviation: String,
)

private fun padRight(text: String, width: Int): String {
    if (text.length >= width) return text
    return text + " ".repeat(width - text.length)
}

private fun padLeft(text: String, width: Int): String {
    if (text.length >= width) return text
    return " ".repeat(width - text.length) + text
}
