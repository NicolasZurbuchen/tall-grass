package io.nicolaszurbuchen.tallgrass.datagen

/**
 * A row of an upstream CSV, addressed by column name because upstream gains columns between releases
 * and an index-based reader would silently read the wrong field.
 */
class CsvRow(
    private val header: Map<String, Int>,
    private val cells: List<String>,
) {
    operator fun get(column: String): String {
        val index = header[column] ?: error("No column '$column'. Columns: ${header.keys.joinToString()}")
        return cells.getOrElse(index) { "" }
    }

    // Trimmed, because upstream does not always write a clean number: five rows of
    // `encounter_methods.csv` store their order as ' 45' rather than '45'. A leading space is never
    // meaningful in a numeric column, so this reads them rather than making every call site guess
    // which columns upstream has been careless with.
    fun int(column: String): Int = this[column].trim().toIntOrNull() ?: error("Column '$column' is not an integer: '${this[column]}'")

    fun intOrNull(column: String): Int? = this[column].trim().takeIf { it.isNotEmpty() }?.toIntOrNull()

    fun bool(column: String): Boolean = this[column] == "1"
}

/**
 * Parses an upstream CSV.
 *
 * **A quoted field may contain newlines**, so this scans the whole text rather than splitting into
 * lines first: `growth_rates.csv` stores multi-line LaTeX, and a line-at-a-time reader tears one row
 * into six whose first column is a fragment of an equation.
 */
fun parseCsv(text: String): List<CsvRow> {
    val rows = splitCsvRows(text)
    if (rows.isEmpty()) return emptyList()

    val header = rows.first().withIndex().associate { (index, name) -> name to index }
    return rows.drop(1).map { CsvRow(header, it) }
}

private fun splitCsvRows(text: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    var row = mutableListOf<String>()
    val cell = StringBuilder()
    var inQuotes = false
    var index = 0

    fun endCell() {
        row.add(cell.toString())
        cell.clear()
    }

    fun endRow() {
        endCell()
        // A trailing newline would otherwise produce a final row of one empty cell.
        if (row.size > 1 || row.first().isNotEmpty()) rows.add(row)
        row = mutableListOf()
    }

    while (index < text.length) {
        val char = text[index]
        when {
            inQuotes && char == '"' && text.getOrNull(index + 1) == '"' -> {
                cell.append('"')
                index++
            }
            char == '"' -> inQuotes = !inQuotes
            char == ',' && !inQuotes -> endCell()
            char == '\r' && !inQuotes -> Unit
            char == '\n' && !inQuotes -> endRow()
            else -> cell.append(char)
        }
        index++
    }
    if (cell.isNotEmpty() || row.isNotEmpty()) endRow()

    return rows
}
