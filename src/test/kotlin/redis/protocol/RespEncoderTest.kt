package redis.protocol

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class RespEncoderTest {

    private val encoder = RespEncoder()

    @Test
    fun `SimpleString을 RESP 형식으로 인코딩해야 한다`() {
        val input = RespValue.SimpleString("PONG")
        val encoded = encoder.encode(input)
        assertArrayEquals("+PONG\r\n".toByteArray(Charsets.UTF_8), encoded)
    }

    @Test
    fun `BulkString을 RESP 형식으로 인코딩해야 한다`() {
        val input = RespValue.BulkString("hello".toByteArray(Charsets.UTF_8))
        val encoded = encoder.encode(input)
        assertArrayEquals("$5\r\nhello\r\n".toByteArray(Charsets.UTF_8), encoded)
    }

    @Test
    fun `빈 바이트 배열 BulkString을 RESP 형식으로 인코딩해야 한다`() {
        val input = RespValue.BulkString(byteArrayOf())
        val encoded = encoder.encode(input)
        assertArrayEquals("$0\r\n\r\n".toByteArray(Charsets.UTF_8), encoded)
    }

    @Test
    fun `Null BulkString을 RESP 형식으로 인코딩해야 한다`() {
        val input = RespValue.BulkString(null)
        val encoded = encoder.encode(input)
        assertArrayEquals("$-1\r\n".toByteArray(Charsets.UTF_8), encoded)
    }

    @Test
    fun `임의의 8-bit 바이너리 데이터(0x00, 0xFF 등)를 가진 BulkString이 손실 없이 인코딩되어야 한다`() {
        val binaryData = byteArrayOf(0x00, 0xFF.toByte(), 0x1B, 0x7F)
        val input = RespValue.BulkString(binaryData)
        val encoded = encoder.encode(input)

        val expected = "\$4\r\n".toByteArray(Charsets.UTF_8) + binaryData + "\r\n".toByteArray(Charsets.UTF_8)
        assertArrayEquals(expected, encoded)
    }

    @Test
    fun `Error를 RESP 형식으로 인코딩해야 한다`() {
        val input = RespValue.Error("ERR 잘못된 명령어 입니다.")
        val encoded = encoder.encode(input)
        assertArrayEquals("-ERR 잘못된 명령어 입니다.\r\n".toByteArray(Charsets.UTF_8), encoded)
    }

    @Test
    fun `Integer를 RESP 형식으로 인코딩해야 한다`() {
        val input = RespValue.Integer(100L)
        val encoded = encoder.encode(input)
        assertArrayEquals(":100\r\n".toByteArray(Charsets.UTF_8), encoded)
    }

    @Test
    fun `Null Array를 RESP 형식으로 인코딩해야 한다`() {
        val input = RespValue.Array(null)
        val encoded = encoder.encode(input)
        assertArrayEquals("*-1\r\n".toByteArray(Charsets.UTF_8), encoded)
    }

    @Test
    fun `요소가 포함된 Array를 RESP 형식으로 인코딩해야 한다`() {
        val input = RespValue.Array(
            listOf(
                RespValue.BulkString("PING".toByteArray(Charsets.UTF_8)),
                RespValue.BulkString("hello".toByteArray(Charsets.UTF_8))
            )
        )
        val encoded = encoder.encode(input)
        assertArrayEquals("*2\r\n$4\r\nPING\r\n$5\r\nhello\r\n".toByteArray(Charsets.UTF_8), encoded)
    }
}
