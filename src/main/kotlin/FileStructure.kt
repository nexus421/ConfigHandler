
data class HeaderDetail(val header: String, val fields: Map<String, List<String>>)

/**
 * @param keyValueSplitter character which shows when the key ends and the value starts. Default ist "=". Example: ABC=123,456,789 results in "ABC" and "123,456,789"
 * @param valueSplitter character which shows where the values have to be split. Default ist ",". Example: 123,456,789 results in "123", "456", "789"
 * @param ignoreValueSplitterIfBetween if you want, that the values should not be separated in specific situations, you cant specify a regex for that. Default: (?=(?:[^"]*"[^"]*")*[^"]*$) -> ignoring the [valueSplitter] inside quotation marks Example: abc,"cd,efg",hij,klm results now in "abc", ""cd,efg"", "hij", "klm" instead of "abc", ""cd", "efg"", "hij", "klm"
 */
data class FieldSplitter(val keyValueSplitter: Char = '=', private val valueSplitter: Char? = ',', private val ignoreValueSplitterIfBetween: Regex? = "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex()) {
    val valueSplitterWithIgnorance = (valueSplitter.toString() + ignoreValueSplitterIfBetween.toString()).toRegex()
}

sealed class Parts<T>
data class Single<T>(val value: T): Parts<T>()
data class Multiple<T>(val groupedRegex: Regex, val values: List<T>): Parts<T>()

data class ParseResult(val parsedLines: List<HeaderDetail>, val ignoredLines: List<String> = emptyList())