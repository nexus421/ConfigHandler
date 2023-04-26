
data class HeaderDetail(val key: String, val values: List<String>)

sealed interface Parts
data class Single(val headerDetail: HeaderDetail): Parts
data class Multiple(val headerDetails: MutableList<HeaderDetail> = mutableListOf()): Parts

data class ParseResult(val parsedLines: List<HeaderDetail>, val ignoredLines: List<String> = emptyList())