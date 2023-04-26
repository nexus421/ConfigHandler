import java.io.File

fun main() {
    readAndParseFile()
}

fun readFileToString() = File("test.txt").readText()

fun readAndParseFile(headerRegex: Regex = "\\[.*]".toRegex(), endType: EndType = EndType.EmptyLine, groupBy: Regex? = null, returnIgnoredLinesToo: Boolean = false) {
    val input = readFileToString()
    var fileDetails: Parts? = null

    val result = findHeader(input.lines(), headerRegex, endType, returnIgnoredLinesToo)


    println(result)


}

fun findHeader(lines: List<String>, headerRegex: Regex, endType: EndType, returnIgnoredLinesToo: Boolean): ParseResult {
    var start = -1
    var end = -1
    val ignoredLines = mutableListOf<String>()

    run breaking@ {
        lines.forEachIndexed { index, line ->
            if (headerRegex.matches(line)) start = index
            else if(start != -1) {
                when (endType) {
                    EndType.EmptyLine -> line.isEmpty()
                    EndType.NewHeader -> TODO()
                }.ifTrue {
                    end = index
                    return@breaking
                }
            }

            if(returnIgnoredLinesToo && start == -1) {
                ignoredLines.add(line)
            }
        }
    }

    //Header Vor-Parsen
    val headerDetail = lines.subList(start, end).let { line ->
        HeaderDetail(line.first(), line.subList(1, line.size))
    }

    //Leere Zeilen aus ignorierten Zeilen entfernen
    ignoredLines.removeAll { it.isEmpty() }

    //Sind noch Zeilen übrig, die man prüfen könnte?
    if(end < lines.size) {
        val remainingInput = lines.subList(end, lines.size)
        //Gibt es noch einen Header, den man parsen kann?
        if(remainingInput.find { headerRegex.matches(it) } != null) {
            val findNext = findHeader(remainingInput, headerRegex, endType, returnIgnoredLinesToo)
            return ParseResult(mutableListOf(headerDetail).apply { addAll(findNext.parsedLines) }, ignoredLines.apply { addAll(findNext.ignoredLines) })
        }
        //Es gibt keine weiteren Header mehr. Wenn es noch übrige Zeilen gibt, werden diese bei den ignorierten hinzugefügt
        else if(returnIgnoredLinesToo) ignoredLines.addAll(remainingInput)
    }
return ParseResult(listOf(headerDetail), ignoredLines)
}

fun groupHeaders(headerDetails: MutableList<HeaderDetail>, groupBy: Regex?): List<Parts> {

    if(groupBy == null) return headerDetails.map { Single(it) }

    TODO("Mehrere Group By angeben. damit man auch mehrere Header gruppieren kann?")

}

enum class EndType {
    EmptyLine,
    NewHeader,
}

inline fun Boolean.ifTrue(doThis: () -> Unit) {
    if (this) doThis()
}