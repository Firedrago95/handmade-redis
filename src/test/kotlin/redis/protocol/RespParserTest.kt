package redis.protocol

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RespParserTest {

    private val parser = RespParser()

    @Test
    fun `+ 식별자 입력 시 SimpleStringParser로 라우팅되어 결과를 반환해야 한다`() {
        val input = "+OK\r\n"
        val result = parser.parse(input)

        assertInstanceOf(RespValue.SimpleString::class.java, result.value)
        assertEquals("OK", (result.value as RespValue.SimpleString).content)
        assertEquals(input.length, result.consumedBytes)
    }

    @Test
    fun `$ 식별자 입력 시 BulkStringParser로 라우팅되어 결과를 반환해야 한다`() {
        val input = "\$3\r\nfoo\r\n"
        val result = parser.parse(input)

        assertInstanceOf(RespValue.BulkString::class.java, result.value)
        assertEquals("foo", (result.value as RespValue.BulkString).content)
        assertEquals(input.length, result.consumedBytes)
    }

    @Test
    fun `지원하지 않는 식별자가 입력된 경우 IllegalArgumentException이 발생해야 한다`() {
        val input = "?INVALID\r\n"
        assertThrows<IllegalArgumentException> {
            parser.parse(input)
        }
    }
}
