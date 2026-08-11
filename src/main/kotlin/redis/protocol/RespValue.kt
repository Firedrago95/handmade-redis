package redis.protocol

sealed interface RespValue {
    data class SimpleString(val content: String) : RespValue
}
