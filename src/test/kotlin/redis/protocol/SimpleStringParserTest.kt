package redis.protocol

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SimpleStringParserTest {

    private val parser = SimpleStringParser()

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
}
