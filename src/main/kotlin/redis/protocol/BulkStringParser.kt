package redis.protocol

class BulkStringParser : RespValueParser {
    override fun parse(input: String): ParseResult {
        if (input.length < 4) {
            throw IllegalArgumentException("BulkString은 반드시 4자 이상이 되어야 합니다.")
        }

        if (!input.startsWith("$")) {
            throw IllegalArgumentException("BulkString은 반드시 \$로 시작해야 합니다.")
        }

        val sb = StringBuilder()
        val firstCrlf = input.indexOf("\r\n")
        val length = Integer.parseInt(input.substring(1, firstCrlf))
        if (length == -1) return ParseResult(RespValue.BulkString(null), 5)

        val requiredLength = firstCrlf + 2 + length + 2
        if (input.length < requiredLength) throw IllegalArgumentException("BulkString 파싱을 위한 데이터가 부족합니다.")

        val message = input.substring(firstCrlf + 2, (firstCrlf + 2) + length)
        if (!input.substring((firstCrlf + 2) + length).startsWith("\r\n")) throw IllegalArgumentException("BulkString의 입력 문자열 갯수와 실제 메시지 갯수가 다릅니다.")

        sb.append(message)

        return ParseResult(RespValue.BulkString(sb.toString()), requiredLength)
    }
}
