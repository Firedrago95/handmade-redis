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
            ':' -> parseInteger()
            '-' -> parseError()
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

        val bytes = inputStream.readNBytes(length)
        if (bytes.size != length) {
            throw IllegalArgumentException("BulkString 파싱을 위한 데이터가 길이와 일치하지 않습니다.")
        }

        val crlf = inputStream.readNBytes(2)
        if (crlf.size < 2 || crlf[0] != '\r'.code.toByte() || crlf[1] != '\n'.code.toByte()) {
            throw IllegalArgumentException("BulkString 종료 개행이 올바르지 않습니다.")
        }

        return RespValue.BulkString(String(bytes, Charsets.UTF_8))
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

    private fun parseInteger(): RespValue {
        val num = readLine().toLongOrNull()
        if (num == null) throw IllegalArgumentException("Integer는 숫자만 입력가능합니다.")

        return RespValue.Integer(num)
    }

    private fun parseError(): RespValue {
        val message = readLine()
        return RespValue.Error(message)
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
