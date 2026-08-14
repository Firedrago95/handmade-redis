package redis.protocol

interface RespValueParser {
    fun parse(input: String): ParseResult
}
