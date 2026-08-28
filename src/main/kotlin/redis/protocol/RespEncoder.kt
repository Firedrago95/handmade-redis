package redis.protocol

import java.io.ByteArrayOutputStream

class RespEncoder {

    fun encode(input: RespValue): ByteArray {
        val out = ByteArrayOutputStream()
        encodeToStream(input, out)
        return out.toByteArray()
    }

    private fun encodeToStream(input: RespValue, out: ByteArrayOutputStream) {
        when (input) {
            is RespValue.SimpleString -> {
                out.write("+${input.content}\r\n".toByteArray(Charsets.UTF_8))
            }
            is RespValue.BulkString -> {
                if (input.content == null) {
                    out.write("$-1\r\n".toByteArray(Charsets.UTF_8))
                } else {
                    out.write("\$${input.content.size}\r\n".toByteArray(Charsets.UTF_8))
                    out.write(input.content)
                    out.write("\r\n".toByteArray(Charsets.UTF_8))
                }
            }
            is RespValue.Error -> {
                out.write("-${input.message}\r\n".toByteArray(Charsets.UTF_8))
            }
            is RespValue.Integer -> {
                out.write(":${input.number}\r\n".toByteArray(Charsets.UTF_8))
            }
            is RespValue.Array -> {
                if (input.elements == null) {
                    out.write("*-1\r\n".toByteArray(Charsets.UTF_8))
                } else {
                    out.write("*${input.elements.size}\r\n".toByteArray(Charsets.UTF_8))
                    for (element in input.elements) {
                        encodeToStream(element, out)
                    }
                }
            }
        }
    }
}
