import java.io.File



fun main() {
    val filesRegex = "<+~+[(].*[)]~+>+".toRegex()
    val headerRegex = "\\[.*]".toRegex()
    //readAndParseFile(headerRegex = filesRegex, groupBy = listOf("\\[#_\\d+]".toRegex()))

    val headerLevel1 = Splitter.findHeaders(readFileToString().trim().lines(), filesRegex, removeEmptyLines = true)
    val headerLevel2 = Splitter.findHeaders(headerLevel1.first().lines, headerRegex)
    val grouped = Splitter.groupHeaders(headerLevel2, listOf("\\[#_\\d+]".toRegex()))
    val groupedAndSplitted = grouped.splitValuesEF()

    val short = headerLevel1.first().findHeaders(headerRegex).groupHeadersEF(listOf("\\[#_\\d+]".toRegex())).splitValuesEF()


    println()
}

fun readFileToString() = File("test2.txt").readText()


inline fun Boolean.ifTrue(doThis: () -> Unit) {
    if (this) doThis()
}