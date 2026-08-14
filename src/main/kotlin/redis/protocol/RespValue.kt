package redis.protocol

sealed interface RespValue {
    data class SimpleString(val content: String) : RespValue
    data class BulkString(val content: String?) : RespValue
    data class Array(val elements: List<RespValue>?) : RespValue
}
