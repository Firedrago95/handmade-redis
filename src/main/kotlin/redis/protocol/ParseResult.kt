package redis.protocol

data class ParseResult(
    val value: RespValue,
    val consumedBytes: Int
)
