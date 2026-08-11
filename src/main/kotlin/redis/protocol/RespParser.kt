package redis.protocol

class RespParser {

    fun parse(input: String): RespValue {
        if (input.length < 3) {
            throw IllegalArgumentException("입력 문자열은 반드시 3자 이상이 되어야 합니다.")
        }

        if (!input.startsWith("+")) {
            throw IllegalArgumentException("입력 문자열은 반드시 +로 시작해야 합니다.")
        }

        if (!input.endsWith("\r\n")){
            throw IllegalArgumentException("입력 문자열은 반드시 개행문자로 끝나야 합니다.")
        }

        val substring = input.substring(1, input.length - 2)
        return RespValue.SimpleString(substring)
    }
}
