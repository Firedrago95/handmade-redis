package redis.protocol

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RespParserTest {

    private fun createParser(input: String): RespParser {
        return RespParser(input.byteInputStream(Charsets.UTF_8))
    }

    // --- SimpleString 테스트 ---

    @Test
    fun `+OK CRLF 입력 시 SimpleString(OK)로 파싱되어야 한다`() {
        val input = "+OK\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.SimpleString::class.java, parsed)
        assertEquals("OK", (parsed as RespValue.SimpleString).content)
    }

    @Test
    fun `+PONG CRLF 입력 시 SimpleString(PONG)으로 파싱되어야 한다`() {
        val input = "+PONG\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.SimpleString::class.java, parsed)
        assertEquals("PONG", (parsed as RespValue.SimpleString).content)
    }

    @Test
    fun `+ CRLF 입력 시 빈 문자열을 담은 SimpleString으로 파싱되어야 한다`() {
        val input = "+\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.SimpleString::class.java, parsed)
        assertEquals("", (parsed as RespValue.SimpleString).content)
    }

    // --- BulkString 테스트 ---

    @Test
    fun `$3 CRLF foo CRLF 입력 시 BulkString(foo)로 파싱되어야 한다`() {
        val input = "\$3\r\nfoo\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.BulkString::class.java, parsed)
        assertEquals("foo", (parsed as RespValue.BulkString).content)
    }

    @Test
    fun `$0 CRLF CRLF 입력 시 빈 문자열 BulkString으로 파싱되어야 한다`() {
        val input = "\$0\r\n\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.BulkString::class.java, parsed)
        assertEquals("", (parsed as RespValue.BulkString).content)
    }

    @Test
    fun `$-1 CRLF 입력 시 Null을 담은 BulkString으로 파싱되어야 한다`() {
        val input = "\$-1\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.BulkString::class.java, parsed)
        assertNull((parsed as RespValue.BulkString).content)
    }

    @Test
    fun `$-2 이하의 음수 길이가 입력된 경우 예외가 발생해야 한다`() {
        val input = "\$-2\r\n"
        val parser = createParser(input)
        assertThrows<IllegalArgumentException> {
            parser.parse()
        }
    }

    @Test
    fun `UTF-8 한글 3바이트 payload가 올바르게 파싱되어야 한다`() {
        val input = "\$3\r\n한\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.BulkString::class.java, parsed)
        assertEquals("한", (parsed as RespValue.BulkString).content)
    }

    @Test
    fun `BulkString 파싱 시 지정된 바이트 길이보다 데이터가 부족한 경우 예외가 발생해야 한다`() {
        val input = "\$3\r\nfo"
        val parser = createParser(input)
        assertThrows<IllegalArgumentException> {
            parser.parse()
        }
    }

    @Test
    fun `BulkString 종료 개행이 CRLF가 아닐 경우 예외가 발생해야 한다`() {
        val input = "\$3\r\nfooXX"
        val parser = createParser(input)
        assertThrows<IllegalArgumentException> {
            parser.parse()
        }
    }

    // --- Array 테스트 ---

    @Test
    fun `동일한 타입의 요소가 담긴 Array가 올바르게 파싱되어야 한다`() {
        val input = "*2\r\n\$3\r\nfoo\r\n\$3\r\nbar\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Array::class.java, parsed)
        val array = parsed as RespValue.Array
        assertNotNull(array.elements)
        assertEquals(2, array.elements!!.size)
        assertEquals(RespValue.BulkString("foo"), array.elements[0])
        assertEquals(RespValue.BulkString("bar"), array.elements[1])
    }

    @Test
    fun `서로 다른 타입의 요소가 담긴 Array가 올바르게 파싱되어야 한다`() {
        val input = "*2\r\n+OK\r\n\$3\r\nfoo\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Array::class.java, parsed)
        val array = parsed as RespValue.Array
        assertNotNull(array.elements)
        assertEquals(2, array.elements!!.size)
        assertEquals(RespValue.SimpleString("OK"), array.elements[0])
        assertEquals(RespValue.BulkString("foo"), array.elements[1])
    }

    @Test
    fun `*0 CRLF 입력 시 빈 리스트를 담은 Array로 파싱되어야 한다`() {
        val input = "*0\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Array::class.java, parsed)
        val array = parsed as RespValue.Array
        assertNotNull(array.elements)
        assertTrue(array.elements!!.isEmpty())
    }

    @Test
    fun `*-1 CRLF 입력 시 Null을 담은 Array로 파싱되어야 한다`() {
        val input = "*-1\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Array::class.java, parsed)
        val array = parsed as RespValue.Array
        assertNull(array.elements)
    }

    @Test
    fun `중첩된 Array 형태도 올바르게 재귀적으로 파싱되어야 한다`() {
        val input = "*1\r\n*1\r\n+OK\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Array::class.java, parsed)
        val outerArray = parsed as RespValue.Array
        assertNotNull(outerArray.elements)
        assertEquals(1, outerArray.elements!!.size)

        val innerArray = outerArray.elements[0] as RespValue.Array
        assertNotNull(innerArray.elements)
        assertEquals(RespValue.SimpleString("OK"), innerArray.elements!![0])
    }

    @Test
    fun `*-2 이하의 음수 개수가 입력된 경우 예외가 발생해야 한다`() {
        val input = "*-2\r\n"
        val parser = createParser(input)
        assertThrows<IllegalArgumentException> {
            parser.parse()
        }
    }

    @Test
    fun `지원하지 않는 식별자가 입력된 경우 예외가 발생해야 한다`() {
        val input = "?INVALID\r\n"
        val parser = createParser(input)
        assertThrows<IllegalArgumentException> {
            parser.parse()
        }
    }

    @Test
    fun `Integer prefix 1000 CRLF 입력 시 Integer(1000)로 파싱되어야 한다`() {
        val input = ":1000\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Integer::class.java, parsed)
        assertEquals(1000L, (parsed as RespValue.Integer).number)
    }

    @Test
    fun `Integer prefix -50 CRLF 입력 시 Integer(-50)로 파싱되어야 한다`() {
        val input = ":-50\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Integer::class.java, parsed)
        assertEquals(-50L, (parsed as RespValue.Integer).number)
    }

    @Test
    fun `Integer prefix 0 CRLF 입력 시 Integer(0)으로 파싱되어야 한다`() {
        val input = ":0\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Integer::class.java, parsed)
        assertEquals(0L, (parsed as RespValue.Integer).number)
    }

    @Test
    fun `Integer prefix에 정수가 아닌 abc 값이 입력되면 예외가 발생해야 한다`() {
        val input = ":abc\r\n"
        val parser = createParser(input)
        assertThrows<IllegalArgumentException> {
            parser.parse()
        }
    }

    @Test
    fun `Error prefix ERR unknown command CRLF 입력 시 Error(ERR unknown command)로 파싱되어야 한다`() {
        val input = "-ERR unknown command\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Error::class.java, parsed)
        assertEquals("ERR unknown command", (parsed as RespValue.Error).message)
    }

    @Test
    fun `Error prefix 빈 에러 메시지 CRLF 입력 시 Error()로 파싱되어야 한다`() {
        val input = "-\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Error::class.java, parsed)
        assertEquals("", (parsed as RespValue.Error).message)
    }

    @Test
    fun `Array 요소로 Integer와 Error가 포함된 경우 올바르게 파싱되어야 한다`() {
        val input = "*2\r\n:100\r\n-ERR test\r\n"
        val parser = createParser(input)
        val parsed = parser.parse()

        assertInstanceOf(RespValue.Array::class.java, parsed)
        val array = parsed as RespValue.Array
        assertNotNull(array.elements)
        assertEquals(2, array.elements!!.size)
        assertEquals(RespValue.Integer(100L), array.elements[0])
        assertEquals(RespValue.Error("ERR test"), array.elements[1])
    }

    @Test
    fun `CRLF 개행 시 carriage return 뒤에 n이 아닌 문자가 오면 예외가 발생해야 한다`() {
        val input = "+OK\rX"
        val parser = createParser(input)
        assertThrows<IllegalArgumentException> {
            parser.parse()
        }
    }
}



