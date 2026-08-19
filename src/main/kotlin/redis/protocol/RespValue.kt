package redis.protocol

sealed interface RespValue {
    data class SimpleString(val content: String) : RespValue
    data class BulkString(val content: ByteArray?) : RespValue {
        override fun equals(other: Any?): Boolean =
            this === other || (other is BulkString && content.contentEquals(other.content))

        override fun hashCode(): Int = content?.contentHashCode() ?: 0
    }
    data class Array(val elements: List<RespValue>?) : RespValue
    data class Integer(val number: Long) : RespValue
    data class Error(val message: String) : RespValue
}
