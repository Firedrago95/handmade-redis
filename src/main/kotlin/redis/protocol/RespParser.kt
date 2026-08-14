package redis.protocol

import java.lang.IllegalArgumentException

class RespParser {

    private val parsers: Map<String, RespValueParser> by lazy {
        mapOf(
            "+" to SimpleStringParser(),
            "$" to BulkStringParser(),
            "*" to ArrayParser(this)
        )
    }

    fun parse(input: String): ParseResult {
        val firstCommand = input.substring(0, 1)

        val parser = parsers[firstCommand]
            ?: throw IllegalArgumentException("지원하지 않는 RESP 타입입니다: $firstCommand")

        return parser.parse(input)
    }
}
