package redis.protocol

import java.lang.IllegalArgumentException

class RespParser {

    companion object {
        val parsers: Map<String, RespValueParser> = mapOf(
            "+" to SimpleStringParser(),
            "$" to BulkStringParser()
        )
    }

    fun parse(input: String): RespValue {
        val firstCommand = input.substring(0, 1)

        val parser = parsers[firstCommand]
            ?: throw IllegalArgumentException("지원하지 않는 RESP 타입입니다: $firstCommand")

        return parser.parse(input)
    }
}
