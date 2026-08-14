package redis.protocol

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BulkStringParserTest {

    private val parser = BulkStringParser()

    @Test
    fun `$3 CRLF foo CRLF 입력 시 BulkString(foo)로 파싱되어야 한다`() {
        val input = "\$3\r\nfoo\r\n"
        val result = parser.parse(input)

        assertInstanceOf(RespValue.BulkString::class.java, result.value)
        val bulkString = result.value as RespValue.BulkString
        assertEquals("foo", bulkString.content)
        assertEquals(input.length, result.consumedBytes)
    }

    @Test
    fun `$0 CRLF CRLF 입력 시 빈 문자열 BulkString으로 파싱되어야 한다`() {
        val input = "\$0\r\n\r\n"
        val result = parser.parse(input)

        assertInstanceOf(RespValue.BulkString::class.java, result.value)
        val bulkString = result.value as RespValue.BulkString
        assertEquals("", bulkString.content)
        assertEquals(input.length, result.consumedBytes)
    }

    @Test
    fun `$-1 CRLF 입력 시 Null을 담은 BulkString으로 파싱되어야 한다`() {
        val input = "\$-1\r\n"
        val result = parser.parse(input)

        assertInstanceOf(RespValue.BulkString::class.java, result.value)
        val bulkString = result.value as RespValue.BulkString
        assertNull(bulkString.content)
        assertEquals(input.length, result.consumedBytes)
    }

    @Test
    fun `BulkString 지정된 길이와 실제 데이터 길이가 다른 경우 예외가 발생해야 한다`() {
        val input = "\$5\r\nfoo\r\n"
        assertThrows<IllegalArgumentException> {
            parser.parse(input)
        }
    }

    @Test
    fun `$ 로 시작하지 않는 경우 예외가 발생해야 한다`() {
        val input = "3\r\nfoo\r\n"
        assertThrows<IllegalArgumentException> {
            parser.parse(input)
        }
    }

    @Test
    fun `CRLF 개행 문자로 끝나지 않는 경우 예외가 발생해야 한다`() {
        val input = "\$3\r\nfoo"
        assertThrows<IllegalArgumentException> {
            parser.parse(input)
        }
    }
}
