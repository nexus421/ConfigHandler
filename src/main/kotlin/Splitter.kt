object Splitter {

    fun findHeaders(lines: List<String>, headerRegex: Regex, endType: EndType = EndType.Header, removeEmptyLines: Boolean = false): List<HeaderWithLines> {
        val result = mutableListOf<HeaderWithLines>()

        var start = 0
        var end = 0

        while (true) {
            var foundHeader = false
            run breaking@{
                lines.forEachIndexed { index, line ->
                    if(index < end) return@forEachIndexed //Wir sind noch nicht bei neuen Zeilen angekommen.
                    if (foundHeader.not() && headerRegex.matches(line)) {
                        start = index
                        foundHeader = true
                    }
                    else if (foundHeader) {
                        when (endType) {
                            EndType.EmptyLine -> line.isEmpty()
                            EndType.Header -> headerRegex.matches(line)
                            EndType.EmptyOrHeader -> line.isEmpty() || headerRegex.matches(line)
                        }.ifTrue {
                            end = index
                            return@breaking
                        }
                    }
                }
                end = lines.size //Die Schleife ist durchgelaufen und hat kein Ende mehr gefunden. Wir sind also fertig und können den letzten Header hinzufügen.
            }
            val headerWithLines = HeaderWithLines(lines[start], lines.subList(start + 1, end).let { subList -> if(removeEmptyLines) subList.filter { it.isNotBlank() } else subList })
            result.add(headerWithLines)

            if(end == lines.size) break
        }

        return result
    }

    fun groupHeaders(headerDetails: List<HeaderWithLines>, groupBy: List<Regex>): List<Parts<HeaderWithLines>> {

        val result = mutableListOf<Parts<HeaderWithLines>>()
        headerDetails.forEach { headerDetail ->
            val matchedRegex = groupBy.find { regex -> regex.matches(headerDetail.header) }

            if (matchedRegex == null) result.add(Single(headerDetail))
            else {
                result.mapNotNull { if(it is Multiple && it.groupedRegex == matchedRegex) it else null }.find { it.groupedRegex == matchedRegex }?.let {
                    (it.values as MutableList).add(headerDetail)
                } ?:result.add(Multiple(matchedRegex, mutableListOf(headerDetail)))
            }
        }

        return result
    }


}

fun List<HeaderWithLines>.groupByHeaders(regexes: Regex) {

}