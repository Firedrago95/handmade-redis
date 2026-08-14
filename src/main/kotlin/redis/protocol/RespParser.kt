package redis.protocol

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.IllegalArgumentException

class RespParser (private val inputStream: InputStream) {

    fun parse(): RespValue {
        val prefix = inputStream.read().toChar()

        return when (prefix) {
            '+' -> parseSimpleString()
            '$' -> parseBulkString()
            '*' -> readArray()
            else -> throw IllegalArgumentException("지원하지 않는 명령어 입니다. : $prefix")
        }
    }

    private fun parseSimpleString(): RespValue {
        val message = readLine()
        return RespValue.SimpleString(message)
    }

    private fun parseBulkString(): RespValue {
        val length = readLine().toInt()
        if (length == -1) return RespValue.BulkString(null)
        if (length < -1) throw IllegalArgumentException("BulkString은 -1 이외의 음수 길이를 가질 수 없습니다.")

        val readNBytes = inputStream.readNBytes(length)
        inputStream.readNBytes(2)

        return RespValue.BulkString(String(readNBytes))
    }

    private fun readArray(): RespValue {
        val count = readLine().toInt()
        if (count == -1) return RespValue.Array(null)
        if (count < -1) throw IllegalArgumentException("Array는 -1 이외의 음수 길이를 가질 수 없습니다.")

        val list = ArrayList<RespValue>()
        for (i in 0 until count) {
            list.add(parse())
        }
        return RespValue.Array(list)
    }

    private fun readLine() : String {
        val ba = ByteArrayOutputStream()
        var b: Int

        while (inputStream.read().also { b = it } != -1) {
            if (b == '\r'.code) {
                inputStream.read()
                break
            }
            ba.write(b)
        }

        return ba.toString(Charsets.UTF_8)
    }
}
