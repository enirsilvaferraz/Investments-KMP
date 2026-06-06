package com.eferraz.usecases.balancing

public fun formatPortfolioBalancingReport(report: PortfolioBalancingReport): String {
    val rows = report.lines.map { line ->
        FormattedRow(
            groupId = line.groupId,
            groupName = line.groupName,
            name = line.componentName,
            actual = formatMoney(line.actualValue),
            weight = line.targetWeightDisplay,
            ideal = formatMoney(line.idealValue),
            deviation = formatMoney(line.deviation),
        )
    }

    val layout = ColumnLayout(
        nameWidth = maxOf(24, "Nome".length, rows.maxOfOrNull { it.name.length } ?: 0),
        actualWidth = maxOf(14, "Valor actual".length, rows.maxOfOrNull { it.actual.length } ?: 0),
        weightWidth = maxOf(18, "Peso alvo".length, rows.maxOfOrNull { it.weight.length } ?: 0),
        idealWidth = maxOf(14, "Valor ideal".length, rows.maxOfOrNull { it.ideal.length } ?: 0),
        deviationWidth = maxOf(14, "Desvio".length, rows.maxOfOrNull { it.deviation.length } ?: 0),
    )

    val builder = StringBuilder()
    var currentGroupId: BalancingGroupId? = null

    for (row in rows) {
        if (row.groupId != currentGroupId) {
            if (currentGroupId != null) {
                builder.appendLine()
            }
            builder.appendLine("=== ${row.groupName} ===")
            builder.appendLine(layout.headerRow())
            builder.appendLine(layout.separatorRow())
            currentGroupId = row.groupId
        }
        builder.appendLine(layout.dataRow(row))
    }

    return builder.toString().trimEnd()
}

private data class FormattedRow(
    val groupId: BalancingGroupId,
    val groupName: String,
    val name: String,
    val actual: String,
    val weight: String,
    val ideal: String,
    val deviation: String,
)

private data class ColumnLayout(
    val nameWidth: Int,
    val actualWidth: Int,
    val weightWidth: Int,
    val idealWidth: Int,
    val deviationWidth: Int,
) {
    fun headerRow(): String = dataRow(
        name = "Nome",
        actual = "Valor actual",
        weight = "Peso alvo",
        ideal = "Valor ideal",
        deviation = "Desvio",
        nameAlignLeft = true,
    )

    fun separatorRow(): String {
        val segment = { width: Int -> "-".repeat(width) }
        return listOf(
            segment(nameWidth),
            segment(actualWidth),
            segment(weightWidth),
            segment(idealWidth),
            segment(deviationWidth),
        ).joinToString("-+-")
    }

    fun dataRow(row: FormattedRow): String = dataRow(
        name = row.name,
        actual = row.actual,
        weight = row.weight,
        ideal = row.ideal,
        deviation = row.deviation,
        nameAlignLeft = true,
    )

    private fun dataRow(
        name: String,
        actual: String,
        weight: String,
        ideal: String,
        deviation: String,
        nameAlignLeft: Boolean,
    ): String {
        val nameCell = if (nameAlignLeft) padRight(name, nameWidth) else padLeft(name, nameWidth)
        return listOf(
            nameCell,
            padLeft(actual, actualWidth),
            padLeft(weight, weightWidth),
            padLeft(ideal, idealWidth),
            padLeft(deviation, deviationWidth),
        ).joinToString(" | ")
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

private fun formatMoney(value: Double): String {
    val sign = if (value < 0) "-" else " "
    val absolute = kotlin.math.abs(value)
    val cents = kotlin.math.round(absolute * 100.0).toLong()
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    val wholeFormatted = whole.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
    return "$sign R$ $wholeFormatted,$fraction"
}
