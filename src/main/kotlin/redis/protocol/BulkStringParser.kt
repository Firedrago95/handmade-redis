package redis.protocol

class BulkStringParser : RespValueParser {
    override fun parse(input: String): RespValue {
        if (input.length < 4) {
            throw IllegalArgumentException("BulkString은 반드시 4자 이상이 되어야 합니다.")
        }

        if (!input.startsWith("$")) {
            throw IllegalArgumentException("BulkString은 반드시 \$로 시작해야 합니다.")
        }

        if (!input.endsWith("\r\n")){
            throw IllegalArgumentException("BulkString은 반드시 개행문자로 끝나야 합니다.")
        }

        val sb = StringBuilder()
        val firstCrlf = input.indexOf("\r\n")
        val length = Integer.parseInt(input.substring(1, firstCrlf))
        if (length == -1) return RespValue.BulkString(null)

        val message = input.substring(firstCrlf + 2, input.length - 2)
        if (message.length != length) throw IllegalArgumentException("BulkString의 입력 문자열 갯수와 실제 메시지 갯수가 다릅니다.")

        sb.append(message)

        return RespValue.BulkString(sb.toString())
    }
}
