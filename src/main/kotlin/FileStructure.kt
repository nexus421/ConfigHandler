/**
 * @param keyValueSplitter character which shows when the key ends and the value starts. Default ist "=". Example: ABC=123,456,789 results in "ABC" and "123,456,789"
 * @param splitValueBy character which shows where the values have to be split. Default ist ",". Example: 123,456,789 results in "123", "456", "789"
 * @param ignoreValueSplitterIfBetween if you want, that the values should not be separated in specific situations, you cant specify a regex for that. Default: (?=(?:[^"]*"[^"]*")*[^"]*$) -> ignoring the [valueSplitter] inside quotation marks Example: abc,"cd,efg",hij,klm results now in "abc", ""cd,efg"", "hij", "klm" instead of "abc", ""cd", "efg"", "hij", "klm"
 */
data class FieldSplitter(val keyValueSplitter: Char = '=', private val splitValueBy: Char? = ',', private val ignoreValueSplitterIfBetween: Regex? = "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex()) {
    val valueSplitter = (splitValueBy.toString() + ignoreValueSplitterIfBetween.toString()).toRegex()
}

sealed class Parts<T>
data class Single<T>(val value: T): Parts<T>()
data class Multiple<T>(val groupedRegex: Regex, val values: List<T>): Parts<T>()

data class HeaderWithLines(val header: String, val lines: List<String>)

data class HeaderWithKeyValue<T: Line>(val header: String, val lines: List<T>)

sealed class Line(val key: String) {
    class KeyValue(key: String, val value: String) : Line(key) {
        override fun toString() = "$key -> $value"
    }
    class KeyValues(key: String, val values: List<String>): Line(key) {
        override fun toString() = "$key -> ${values.joinToString()}"
    }
}

enum class EndType {
    /**
     * Sobald eine leere Zeile gefunden wird, wird die Suche abgeschlossen.
     */
    EmptyLine,

    /**
     * Sobald der nächste Header gefunden wird, wird die Suche abgeschlossen.
     */
    Header,

    /**
     * Sobald eine leere Zeile oder der nächste Header gefunden wird, wird die Suche abgebrochen.
     */
    EmptyOrHeader,
}