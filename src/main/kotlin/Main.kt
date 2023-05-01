import java.io.File



fun main() {
    val filesRegex = "<+~+[(].*[)]~+>+".toRegex()
    val headerRegex = "\\[.*]".toRegex()
    //readAndParseFile(headerRegex = filesRegex, groupBy = listOf("\\[#_\\d+]".toRegex()))

    val headerLevel1 = Splitter.findHeaders(readFileToString().trim().lines(), filesRegex, removeEmptyLines = true)
    val headerLevel2 = Splitter.findHeaders(headerLevel1.first().lines, headerRegex)
    val grouped = Splitter.groupHeaders(headerLevel2, listOf("\\[#_\\d+]".toRegex()))


    println()
}

fun readFileToString() = File("test2.txt").readText()

//fun readAndParseFile(
//    headerRegex: Regex = "\\[.*]".toRegex(),
//    endType: EndType = EndType.EmptyOrHeader,
//    groupBy: List<Regex>? = null,
//    returnIgnoredLinesToo: Boolean = true,
//    splitter: FieldSplitter = FieldSplitter()
//) {
//    val input = readFileToString()
//
//    val splitResult = findHeader(input.lines(), headerRegex, endType, returnIgnoredLinesToo, splitter)
//    val groupedResult = groupHeaders(splitResult.parsedLines, groupBy)
//
//
//
//    println(splitResult)
//    println(groupedResult)
//
//}

//fun findHeader(lines: List<String>, headerRegex: Regex, endType: EndType, returnIgnoredLinesToo: Boolean, splitter: FieldSplitter): ParseResult {
//    var start = -1
//    var end = lines.size
//    val ignoredLines = mutableListOf<String>()
//
//    run breaking@{
//        lines.forEachIndexed { index, line ->
//            if (start == -1 && headerRegex.matches(line)) start = index
//            else if (start != -1) {
//                when (endType) {
//                    EndType.EmptyLine -> line.isEmpty()
//                    EndType.Header -> headerRegex.matches(line)
//                    EndType.EmptyOrHeader -> line.isEmpty() || headerRegex.matches(line)
//                }.ifTrue {
//                    end = index
//                    return@breaking
//                }
//            }
//
//            if (returnIgnoredLinesToo && start == -1) {
//                ignoredLines.add(line)
//            }
//        }
//    }
//
//    //Header Vor-Parsen
//    val headerDetail = lines.subList(start, end).let { line ->
//        HeaderDetail(line.first(), prepareFields(line.subList(1, line.size), splitter))
//    }
//
//    //Leere Zeilen aus ignorierten Zeilen entfernen
//    ignoredLines.removeAll { it.isEmpty() }
//
//    //Sind noch Zeilen übrig, die man prüfen könnte?
//    if (end < lines.size) {
//        val remainingInput = lines.subList(end, lines.size)
//        //Gibt es noch einen Header, den man parsen kann?
//        if (remainingInput.find { headerRegex.matches(it) } != null) {
//            val findNext = findHeader(remainingInput, headerRegex, endType, returnIgnoredLinesToo, splitter)
//            return ParseResult(mutableListOf(headerDetail).apply { addAll(findNext.parsedLines) }, ignoredLines.apply { addAll(findNext.ignoredLines) })
//        }
//        //Es gibt keine weiteren Header mehr. Wenn es noch übrige Zeilen gibt, werden diese bei den ignorierten hinzugefügt
//        else if (returnIgnoredLinesToo) ignoredLines.addAll(remainingInput)
//    }
//    return ParseResult(listOf(headerDetail), ignoredLines)
//}
//
///**
// * Separates the key from the values und separates the values by the given splitter.
// *
// * @param fields wich should be splitted
// * @param splitter settings to split the fields
// *
// * @return splitted fields
// */
//fun prepareFields(fields: List<String>, splitter: FieldSplitter): Map<String, List<String>> {
//    return fields.associate { line ->
//        val keyValue = line.split(splitter.keyValueSplitter)
//        if (keyValue.size != 2) {
//            //Kein Key/Value gefunden. Fehler!
//            Pair(keyValue.first(), "")
//        }
//
//        val values = splitter.valueSplitterWithIgnorance?.let {
//            keyValue[1].split(it)
//        } ?: listOf(keyValue[1])
//
//        Pair(keyValue.first(), values)
//    }
//}
//
//fun groupHeaders(headerDetails: List<HeaderDetail>, groupBy: List<Regex>?): List<Parts> {
//    if (groupBy.isNullOrEmpty()) return headerDetails.map { Single(it) }
//
//    val result = hashMapOf<String, MutableList<HeaderDetail>>()
//
//    headerDetails.groupBy { headerDetail ->
//        val foundPattern = groupBy.find { regex -> regex.matches(headerDetail.header) }?.pattern
//
//        if (foundPattern == null) result.put(headerDetail.header, mutableListOf(headerDetail))
//        else {
//            result.getOrPut(foundPattern) {
//                mutableListOf()
//            }.add(headerDetail)
//        }
//    }
//
//    return result.map {
//        if(it.value.size > 1) Multiple(it.value, it.key.toRegex())
//        else Single(it.value.first())
//    }
//
//}

fun <T> List<T>.splitFilter() {

}

enum class EndType {
    EmptyLine,
    Header,
    EmptyOrHeader,
}

inline fun Boolean.ifTrue(doThis: () -> Unit) {
    if (this) doThis()
}