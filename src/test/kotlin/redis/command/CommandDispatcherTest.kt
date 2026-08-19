package redis.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import redis.protocol.RespValue

class CommandDispatcherTest {

    private val dispatcher = CommandDispatcher()

    @Test
    fun `PING 입력 시 SimpleString(PONG) 응답을 반환해야 한다`() {
        val request = RespValue.Array(
            listOf(RespValue.BulkString("PING".toByteArray()))
        )
        val response = dispatcher.dispatch(request)

        assertInstanceOf(RespValue.SimpleString::class.java, response)
        assertEquals("PONG", (response as RespValue.SimpleString).content)
    }

    @Test
    fun `PING과 인자가 입력 시 해당 인자를 BulkString으로 반환해야 한다`() {
        val request = RespValue.Array(
            listOf(RespValue.BulkString("PING".toByteArray()), RespValue.BulkString("hello".toByteArray()))
        )
        val response = dispatcher.dispatch(request)

        assertInstanceOf(RespValue.BulkString::class.java, response)
        org.junit.jupiter.api.Assertions.assertArrayEquals("hello".toByteArray(), (response as RespValue.BulkString).content)
    }

    @Test
    fun `소문자 ping 입력 시에도 대소문자 구분 없이 PONG 응답을 반환해야 한다`() {
        val request = RespValue.Array(
            listOf(RespValue.BulkString("ping".toByteArray()))
        )
        val response = dispatcher.dispatch(request)

        assertInstanceOf(RespValue.SimpleString::class.java, response)
        assertEquals("PONG", (response as RespValue.SimpleString).content)
    }

    @Test
    fun `지원하지 않는 명령어 입력 시 Error 응답을 반환해야 한다`() {
        val request = RespValue.Array(
            listOf(RespValue.BulkString("UNKNOWN".toByteArray()))
        )
        val response = dispatcher.dispatch(request)

        assertInstanceOf(RespValue.Error::class.java, response)
        assertEquals("ERR 잘못된 명령어 입니다.", (response as RespValue.Error).message)
    }

    @Test
    fun `명령어 위치에 BulkString이 아닌 Integer 타입이 들어오면 Error 응답을 반환해야 한다`() {
        val request = RespValue.Array(
            listOf(RespValue.Integer(100L))
        )
        val response = dispatcher.dispatch(request)

        assertInstanceOf(RespValue.Error::class.java, response)
        assertEquals("ERR 잘못된 request 명령어입니다.", (response as RespValue.Error).message)
    }
}

