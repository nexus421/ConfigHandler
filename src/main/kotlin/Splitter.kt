object Splitter {

    var errorLogger = { log: String -> System.err.println(log) }

    /**
     * Sucht und unterteilt eine Liste [lines] von Strings anhand des [headerRegex]. Alle Zeilen zwischen den Headern werden inkl. des zugehörigen Headers in [HeaderWithLines] gesammelt.
     *
     * @param lines Prüft alle Zeilen anhand des [headerRegex] und gruppiert diese entsprechend
     * @param headerRegex wird verwendet, um die Header in [lines] zu identifizieren
     * @param endType wird verwendet um das Ende eines Blocks zu definieren. Default [EndType.Header]
     * @param removeEmptyLines wenn true, werden alle Zeilen entfernt, die nach [String.isBlank] true sind.
     *
     * @return Sammelt alle Header und die zugehörigen Zeilen in einer Liste aus [HeaderWithLines]
     */
    fun findHeaders(lines: List<String>, headerRegex: Regex, endType: EndType = EndType.Header, removeEmptyLines: Boolean = false): List<HeaderWithLines> {
        val result = mutableListOf<HeaderWithLines>()

        var start = 0
        var end = 0

        while (true) {
            var foundHeader = false
            run breaking@{
                lines.forEachIndexed { index, line ->
                    if (index < end) return@forEachIndexed //Wir sind noch nicht bei neuen Zeilen angekommen.
                    if (foundHeader.not() && headerRegex.matches(line)) {
                        start = index
                        foundHeader = true
                    } else if (foundHeader) {
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
            val headerWithLines = HeaderWithLines(lines[start], lines.subList(start + 1, end).let { subList -> if (removeEmptyLines) subList.filter { it.isNotBlank() } else subList })
            result.add(headerWithLines)

            if (end == lines.size) break
        }

        return result
    }

    /**
     * Gruppiert eine vorbereitete Liste von [HeaderWithLines] anhand der übergebenen Regexes [groupBy].
     * Es gibt eine extension function für diese Methode -> [List.groupByHeaders]
     *
     * @param headerDetails Liste mit bereits gesuchten Headern, die weiter gruppiert werden sollen
     * @param groupBy liste von Regexes, die gruppiert werden soll
     *
     * @return Liste mit gruppierten [Parts]. Wenn es eine Gruppierung gab, wird [Multiple] verwendet, ansonsten [Single].
     */
    fun groupHeaders(headerDetails: List<HeaderWithLines>, groupBy: List<Regex> = listOf("\\[.*]".toRegex())): List<Parts<HeaderWithLines>> {

        val result = mutableListOf<Parts<HeaderWithLines>>()
        headerDetails.forEach { headerDetail ->
            val matchedRegex = groupBy.find { regex -> regex.matches(headerDetail.header) }

            if (matchedRegex == null) result.add(Single(headerDetail))
            else {
                result.mapNotNull { if (it is Multiple && it.groupedRegex == matchedRegex) it else null }.find { it.groupedRegex == matchedRegex }?.let {
                    (it.values as MutableList).add(headerDetail)
                } ?: result.add(Multiple(matchedRegex, mutableListOf(headerDetail)))
            }
        }
        return result
    }

    /**
     * Siehe [splitValuesSimple]
     */
    fun splitValues(headerDetails: List<HeaderWithLines>, splitBy: FieldSplitter = FieldSplitter()): List<HeaderWithKeyValue<Line.KeyValues>> {
        return splitValues(headerDetails.map { Single(it) }, splitBy).mapNotNull { if(it is Single) it.value else null }
    }

    /**
     * Einzelne Zeilen der [HeaderWithLines.lines] in deren key/values aufsplitten.
     * Values werden, entsprechend des [FieldSplitter] weiter aufgeteilt, falls möglich.
     *
     * @param headerDetails Header mit zugehörigen Zeilen. ggf. vorgruppiert.
     * @param splitBy key/value werden anhand des [FieldSplitter] geteilt und verarbeitet
     *
     * @return Gibt die gleiche Liste wieder zurück, tauscht [HeaderWithLines] allerdings gegen [HeaderWithKeyValue] aus.
     */
    @JvmName("splitGroupedValues")
    fun splitValues(headerDetails: List<Parts<HeaderWithLines>>, splitBy: FieldSplitter = FieldSplitter()): List<Parts<HeaderWithKeyValue<Line.KeyValues>>> {
        return headerDetails.map { parts ->
            when (parts) {
                is Multiple -> Multiple(parts.groupedRegex, parts.values.map { splitHeaderWithLines(it, splitBy) })
                is Single -> Single(splitHeaderWithLines(parts.value, splitBy))
            }
        }
    }

    /**
     * Wandelt jede Zeile (außer dem Header natürlich) mittels [splitBy] in [HeaderWithKeyValue] um.
     * Dabei wird jede Zeile in Key und Value aufgespalten und der Value zusätzlich nochmal weiter gesplitted.
     */
    private fun splitHeaderWithLines(headerWithLines: HeaderWithLines, splitBy: FieldSplitter): HeaderWithKeyValue<Line.KeyValues> {
        val keyValues = headerWithLines.lines.mapNotNull { line ->
            val keyValue = line.split(splitBy.keyValueSplitter)
            when (keyValue.size) {
                1 -> Line.KeyValues(keyValue.first(), emptyList())
                2 -> Line.KeyValues(keyValue.first(), keyValue[1].split(splitBy.valueSplitter))
                else -> {
                    errorLogger("illegal KeyValue line! -> $line")
                    null
                }
            }
        }
        return HeaderWithKeyValue(headerWithLines.header, keyValues)
    }

    /**
    *  Einzelne Zeilen der [HeaderWithLines.lines] in deren key/values aufsplitten.
    * Values werden nicht weiter verarbeitet.
    *
    * @param headerDetails Header mit zugehörigen Zeilen. ggf. vorgruppiert.
    * @param splitBy key/value werden anhand [splitBy] geteilt. Value wird nicht weiter verarbeitet.
    *
    * @return Gibt die gleiche Liste wieder zurück, tauscht [HeaderWithLines] allerdings gegen [HeaderWithKeyValue] aus.
    */
    fun splitGroupedValuesSimple(headerDetails: List<Parts<HeaderWithLines>>, splitBy: String = "="): List<Parts<HeaderWithKeyValue<Line.KeyValue>>> {
        return headerDetails.map { parts ->
            when (parts) {
                is Multiple -> Multiple(parts.groupedRegex, parts.values.map { splitHeaderWithLines(it, splitBy) })
                is Single -> Single(splitHeaderWithLines(parts.value, splitBy))
            }
        }
    }

    /**
     * Siehe [splitValuesSimple]
     */
    fun splitValuesSimple(headerDetails: List<HeaderWithLines>, splitBy: String = ""): List<HeaderWithKeyValue<Line.KeyValue>> {
        return splitGroupedValuesSimple(headerDetails.map { Single(it) }, splitBy).mapNotNull { if(it is Single) it.value else null }
    }

    /**
     * Wandelt jede Zeile (außer dem Header natürlich) mittels [splitBy] in [HeaderWithKeyValue] um.
     * Dabei wird jede Zeile in Key und Value aufgespalten.
     *
     */
    private fun splitHeaderWithLines(headerWithLines: HeaderWithLines, splitBy: String): HeaderWithKeyValue<Line.KeyValue> {
        val keyValues = headerWithLines.lines.mapNotNull { line ->
            val keyValue = line.split(splitBy)
            when (keyValue.size) {
                1 -> Line.KeyValue(keyValue.first(), "")
                2 -> Line.KeyValue(keyValue.first(), keyValue[1])
                else -> {
                    errorLogger("illegal KeyValue line! -> $line")
                    null
                }
            }
        }
        return HeaderWithKeyValue(headerWithLines.header, keyValues)
    }
}

fun List<HeaderWithLines>.groupHeadersEF(groupBy: List<Regex> = listOf("\\[.*]".toRegex())) = Splitter.groupHeaders(this, groupBy)
@JvmName("splitGroupedValuesEF")
fun List<Parts<HeaderWithLines>>.splitValuesEF(splitBy: FieldSplitter = FieldSplitter()) = Splitter.splitValues(this, splitBy)
fun List<HeaderWithLines>.splitValuesEF(splitBy: FieldSplitter = FieldSplitter()) = Splitter.splitValues(this, splitBy)
fun List<HeaderWithLines>.splitValuesSimpleEF(splitBy: String = "=") = Splitter.splitValuesSimple(this, splitBy)
@JvmName("splitGroupedValuesSimpleEF")
fun List<Parts<HeaderWithLines>>.splitValuesSimpleEF(splitBy: String = "=") = Splitter.splitGroupedValuesSimple(this, splitBy)

/**
 * Sucht in einer spezifischen [HeaderWithLines] nach weiteren Headern.
 * Rest siehe [Splitter.findHeaders]
 */
fun HeaderWithLines.findHeaders(headerRegex: Regex, endType: EndType = EndType.Header, removeEmptyLines: Boolean = false): List<HeaderWithLines> {
    return  Splitter.findHeaders(this.lines, headerRegex, endType, removeEmptyLines)
}

