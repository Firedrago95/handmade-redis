package redis.protocol

class SimpleStringParser : RespValueParser {
    override fun parse(input: String): ParseResult {
        if (input.length < 3) {
            throw IllegalArgumentException("SimpleString은 반드시 3자 이상이 되어야 합니다.")
        }

        if (!input.startsWith("+")) {
            throw IllegalArgumentException("SimpleString은 반드시 +로 시작해야 합니다.")
        }

        val firstCrlf = input.indexOf("\r\n")
        if (firstCrlf == -1) throw IllegalArgumentException("SimpleString은 반드시 개행문자를 포함해야 합니다.")

        val substring = input.substring(1, firstCrlf)
        return ParseResult(RespValue.SimpleString(substring), substring.length + 3)
    }
}
