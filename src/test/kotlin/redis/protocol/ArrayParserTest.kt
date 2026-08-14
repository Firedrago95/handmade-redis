package redis.protocol

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ArrayParserTest {

    private val mainParser = RespParser()
    private val parser = ArrayParser(mainParser)

    @Test
    fun `동일한 타입의 요소가 담긴 Array가 올바르게 파싱되어야 한다`() {
        val input = "*2\r\n\$3\r\nfoo\r\n\$3\r\nbar\r\n"
        val result = parser.parse(input)

        assertInstanceOf(RespValue.Array::class.java, result.value)
        val array = result.value as RespValue.Array
        assertNotNull(array.elements)
        assertEquals(2, array.elements!!.size)
        assertEquals(RespValue.BulkString("foo"), array.elements[0])
        assertEquals(RespValue.BulkString("bar"), array.elements[1])
        assertEquals(input.length, result.consumedBytes)
    }

    @Test
    fun `서로 다른 타입의 요소가 담긴 Array가 올바르게 파싱되어야 한다`() {
        val input = "*2\r\n+OK\r\n\$3\r\nfoo\r\n"
        val result = parser.parse(input)

        assertInstanceOf(RespValue.Array::class.java, result.value)
        val array = result.value as RespValue.Array
        assertNotNull(array.elements)
        assertEquals(2, array.elements!!.size)
        assertEquals(RespValue.SimpleString("OK"), array.elements[0])
        assertEquals(RespValue.BulkString("foo"), array.elements[1])
        assertEquals(input.length, result.consumedBytes)
    }

    @Test
    fun `*0 CRLF 입력 시 빈 리스트를 담은 Array로 파싱되어야 한다`() {
        val input = "*0\r\n"
        val result = parser.parse(input)

        assertInstanceOf(RespValue.Array::class.java, result.value)
        val array = result.value as RespValue.Array
        assertNotNull(array.elements)
        assertTrue(array.elements!!.isEmpty())
        assertEquals(4, result.consumedBytes)
    }

    @Test
    fun `*-1 CRLF 입력 시 Null을 담은 Array로 파싱되어야 한다`() {
        val input = "*-1\r\n"
        val result = parser.parse(input)

        assertInstanceOf(RespValue.Array::class.java, result.value)
        val array = result.value as RespValue.Array
        assertNull(array.elements)
        assertEquals(5, result.consumedBytes)
    }

    @Test
    fun `중첩된 Array 형태도 올바르게 재귀적으로 파싱되어야 한다`() {
        val input = "*1\r\n*1\r\n+OK\r\n"
        val result = parser.parse(input)

        assertInstanceOf(RespValue.Array::class.java, result.value)
        val outerArray = result.value as RespValue.Array
        assertNotNull(outerArray.elements)
        assertEquals(1, outerArray.elements!!.size)

        val innerArray = outerArray.elements[0] as RespValue.Array
        assertNotNull(innerArray.elements)
        assertEquals(RespValue.SimpleString("OK"), innerArray.elements!![0])
        assertEquals(input.length, result.consumedBytes)
    }

    @Test
    fun `* 로 시작하지 않는 경우 예외가 발생해야 한다`() {
        val input = "2\r\n\$3\r\nfoo\r\n"
        assertThrows<IllegalArgumentException> {
            parser.parse(input)
        }
    }

    @Test
    fun `선언된 개수보다 실제 요소 개수가 부족한 경우 예외가 발생해야 한다`() {
        val input = "*3\r\n\$3\r\nfoo\r\n"
        assertThrows<IllegalArgumentException> {
            parser.parse(input)
        }
    }

    @Test
    fun `개수 헤더가 숫자가 아닌 경우 예외가 발생해야 한다`() {
        val input = "*abc\r\n"
        assertThrows<IllegalArgumentException> {
            parser.parse(input)
        }
    }
}
