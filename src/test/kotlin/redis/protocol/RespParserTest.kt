package redis.protocol

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RespParserTest {

    private val parser = RespParser()

    @Test
    fun `+OK CRLF 입력 시 SimpleString(OK)로 파싱되어야 한다`() {
        val input = "+OK\r\n"
        val parsed = parser.parse(input)

        assertInstanceOf(RespValue.SimpleString::class.java, parsed)
        val simpleString = parsed as RespValue.SimpleString
        assertEquals("OK", simpleString.content)
    }

    @Test
    fun `+PONG CRLF 입력 시 SimpleString(PONG)으로 파싱되어야 한다`() {
        val input = "+PONG\r\n"
        val parsed = parser.parse(input)

        assertInstanceOf(RespValue.SimpleString::class.java, parsed)
        val simpleString = parsed as RespValue.SimpleString
        assertEquals("PONG", simpleString.content)
    }

    @Test
    fun `+ CRLF 입력 시 빈 문자열을 담은 SimpleString으로 파싱되어야 한다`() {
        val input = "+\r\n"
        val parsed = parser.parse(input)

        assertInstanceOf(RespValue.SimpleString::class.java, parsed)
        val simpleString = parsed as RespValue.SimpleString
        assertEquals("", simpleString.content)
    }

    @Test
    fun `CRLF 개행 문자로 끝나지 않는 경우 예외가 발생해야 한다`() {
        val input = "+OK"
        assertThrows<IllegalArgumentException> {
            parser.parse(input)
        }
    }

    @Test
    fun `+ 로 시작하지 않는 경우 예외가 발생해야 한다`() {
        val input = "OK\r\n"
        assertThrows<IllegalArgumentException> {
            parser.parse(input)
        }
    }

    @Test
    fun `$3 CRLF foo CRLF 입력 시 BulkString(foo)로 파싱되어야 한다`() {
        val input = "\$3\r\nfoo\r\n"
        val parsed = parser.parse(input)

        assertInstanceOf(RespValue.BulkString::class.java, parsed)
        val bulkString = parsed as RespValue.BulkString
        assertEquals("foo", bulkString.content)
    }

    @Test
    fun `$0 CRLF CRLF 입력 시 빈 문자열 BulkString으로 파싱되어야 한다`() {
        val input = "\$0\r\n\r\n"
        val parsed = parser.parse(input)

        assertInstanceOf(RespValue.BulkString::class.java, parsed)
        val bulkString = parsed as RespValue.BulkString
        assertEquals("", bulkString.content)
    }

    @Test
    fun `$-1 CRLF 입력 시 Null을 담은 BulkString으로 파싱되어야 한다`() {
        val input = "\$-1\r\n"
        val parsed = parser.parse(input)

        assertInstanceOf(RespValue.BulkString::class.java, parsed)
        val bulkString = parsed as RespValue.BulkString
        assertNull(bulkString.content)
    }

    @Test
    fun `BulkString 지정된 길이와 실제 데이터 길이가 다른 경우 예외가 발생해야 한다`() {
        val input = "\$5\r\nfoo\r\n"
        assertThrows<IllegalArgumentException> {
            parser.parse(input)
        }
    }
}
