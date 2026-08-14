package redis.protocol

class ArrayParser(mainParser: RespParser) : RespValueParser {

    private val mainParser = mainParser;

    override fun parse(input: String): ParseResult {
        if (input.length < 4) {
            throw IllegalArgumentException("Array는 반드시 4자 이상이 되어야 합니다.")
        }

        if (!input.startsWith("*")) {
            throw IllegalArgumentException("Array는 반드시 *로 시작해야 합니다.")
        }

        val firstCrlf = input.indexOf("\r\n")
        if (firstCrlf == -1) throw IllegalArgumentException("Array는 반드시 개행문자를 포함해야 합니다.")

        val length = try {
            input.substring(1, firstCrlf).toInt()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Array 요소 개수가 올바른 숫자가 아닙니다.")
        }

        if (length == -1) return ParseResult(RespValue.Array(null), firstCrlf + 2)

        var currentIndex = firstCrlf + 2
        val list = ArrayList<RespValue>()
        for (i in 0 until length) {
            if (currentIndex >= input.length) {
                throw IllegalArgumentException("Array 파싱을 위한 요소 데이터가 부족합니다.")
            }
            val substring = input.substring(currentIndex)
            val result = mainParser.parse(substring)
            list.add(result.value)
            currentIndex += result.consumedBytes
        }
        return ParseResult(RespValue.Array(list), currentIndex)
    }
}
