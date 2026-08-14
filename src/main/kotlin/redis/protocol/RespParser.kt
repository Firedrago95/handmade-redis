package redis.protocol

import java.io.InputStream
import java.lang.IllegalArgumentException

class RespParser (inputStream: InputStream) {

    private val reader = inputStream.bufferedReader(Charsets.UTF_8)

    fun parse(): RespValue {
        val prefix = reader.read().toChar()

        return when (prefix) {
            '+' -> parseSimpleString()
            '$' -> parseBulkString()
            '*' -> readArray()
            else -> throw IllegalArgumentException("지원하지 않는 명령어 입니다. : $prefix")
        }
    }

    private fun parseSimpleString(): RespValue {
        val message = reader.readLine()
        return RespValue.SimpleString(message)
    }

    private fun parseBulkString(): RespValue {
        val length = reader.readLine().toInt()
        if (length == -1) return RespValue.BulkString(null)
        if (length < -1) throw IllegalArgumentException("BulkString은 -1 이외의 음수 길이를 가질 수 없습니다.")

        val buffer = CharArray(length)
        reader.read(buffer, 0, length)
        reader.readLine()

        return RespValue.BulkString(String(buffer))
    }

    private fun readArray(): RespValue {
        val count = reader.readLine().toInt()
        if (count == -1) return RespValue.Array(null)
        if (count < -1) throw IllegalArgumentException("Array는 -1 이외의 음수 길이를 가질 수 없습니다.")

        val list = ArrayList<RespValue>()
        for (i in 0 until count) {
            list.add(parse())
        }
        return RespValue.Array(list)
    }
}
