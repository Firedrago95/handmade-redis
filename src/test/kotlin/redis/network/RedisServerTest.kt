package redis.network

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Assertions.*
import java.net.Socket
import kotlin.concurrent.thread

class RedisServerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            // 백그라운드 스레드에서 서버 자동 실행
            thread(isDaemon = true) {
                try {
                    val server = RedisServer()
                    server.start(6379)
                } catch (e: Exception) {
                    // 이미 포트가 사용 중이거나 에러가 발생한 경우 무시
                }
            }
            // 서버가 포트를 바인딩할 때까지 아주 잠시 대기
            Thread.sleep(100)
        }
    }

    @Test
    fun `서버는 6379 포트에서 클라이언트의 접속을 수락해야 한다`() {
        try {
            Socket("127.0.0.1", 6379).use { socket ->
                assertTrue(socket.isConnected, "소켓이 연결 상태여야 합니다")
            }
        } catch (e: Exception) {
            fail("6379 포트로의 연결이 실패했습니다. 서버가 먼저 실행되어 있는지 확인하세요. 에러: ${e.message}")
        }
    }

    @Test
    fun `RESP 규격 PING 명령어 수신 시 +PONG 을 응답해야 한다`() {
        try {
            Socket("127.0.0.1", 6379).use { socket ->
                socket.soTimeout = 2000
                val output = socket.getOutputStream()
                val input = socket.getInputStream()

                // RESP 규격: *1\r\n$4\r\nPING\r\n
                output.write("*1\r\n$4\r\nPING\r\n".toByteArray(Charsets.UTF_8))
                output.flush()

                val buffer = ByteArray(1024)
                val bytesRead = input.read(buffer)
                assertTrue(bytesRead > 0, "서버로부터 응답이 없습니다.")

                val response = String(buffer, 0, bytesRead, Charsets.UTF_8)
                assertEquals("+PONG\r\n", response, "서버의 응답이 +PONG 형식이 아닙니다.")
            }
        } catch (e: Exception) {
            fail("테스트 실패: ${e.message}")
        }
    }

    @Test
    fun `인자가 포함된 RESP PING 명령어 수신 시 해당 인자를 BulkString으로 응답해야 한다`() {
        try {
            Socket("127.0.0.1", 6379).use { socket ->
                socket.soTimeout = 2000
                val output = socket.getOutputStream()
                val input = socket.getInputStream()

                // RESP 규격: *2\r\n$4\r\nPING\r\n$5\r\nhello\r\n
                output.write("*2\r\n$4\r\nPING\r\n$5\r\nhello\r\n".toByteArray(Charsets.UTF_8))
                output.flush()

                val buffer = ByteArray(1024)
                val bytesRead = input.read(buffer)
                assertTrue(bytesRead > 0, "서버로부터 응답이 없습니다.")

                val response = String(buffer, 0, bytesRead, Charsets.UTF_8)
                assertEquals("$5\r\nhello\r\n", response, "서버의 응답이 $5\r\nhello\r\n 형식이 아닙니다.")
            }
        } catch (e: Exception) {
            fail("테스트 실패: ${e.message}")
        }
    }

    @Test
    fun `단일 클라이언트가 연결을 유지한 채 여러 번 명령을 보내도 모두 정상적으로 응답해야 한다`() {
        try {
            Socket("127.0.0.1", 6379).use { socket ->
                socket.soTimeout = 2000
                val output = socket.getOutputStream()
                val input = socket.getInputStream()
                val buffer = ByteArray(1024)

                // 1번째: PING
                output.write("*1\r\n$4\r\nPING\r\n".toByteArray(Charsets.UTF_8))
                output.flush()
                var bytesRead = input.read(buffer)
                assertTrue(bytesRead > 0, "1번째 응답이 없습니다.")
                assertEquals("+PONG\r\n", String(buffer, 0, bytesRead, Charsets.UTF_8))

                // 2번째: PING hello (연결 유지 상태)
                output.write("*2\r\n$4\r\nPING\r\n$5\r\nhello\r\n".toByteArray(Charsets.UTF_8))
                output.flush()
                bytesRead = input.read(buffer)
                assertTrue(bytesRead > 0, "2번째 응답이 없습니다.")
                assertEquals("$5\r\nhello\r\n", String(buffer, 0, bytesRead, Charsets.UTF_8))

                // 3번째: PING world
                output.write("*2\r\n$4\r\nPING\r\n$5\r\nworld\r\n".toByteArray(Charsets.UTF_8))
                output.flush()
                bytesRead = input.read(buffer)
                assertTrue(bytesRead > 0, "3번째 응답이 없습니다.")
                assertEquals("$5\r\nworld\r\n", String(buffer, 0, bytesRead, Charsets.UTF_8))
            }
        } catch (e: Exception) {
            fail("테스트 실패 (동일 연결 다중 명령): ${e.message}")
        }
    }

    @Test
    fun `서버는 여러 클라이언트가 동시에 접속해도 각각 독립적으로 명령을 처리할 수 있어야 한다`() {
        try {
            val client1 = Socket("127.0.0.1", 6379)
            val client2 = Socket("127.0.0.1", 6379)

            client1.use { c1 ->
                client2.use { c2 ->
                    c1.soTimeout = 2000
                    c2.soTimeout = 2000

                    val output1 = c1.getOutputStream()
                    val input1 = c1.getInputStream()

                    val output2 = c2.getOutputStream()
                    val input2 = c2.getInputStream()

                    // 클라이언트 1: PING
                    output1.write("*1\r\n$4\r\nPING\r\n".toByteArray(Charsets.UTF_8))
                    output1.flush()

                    // 클라이언트 2: PING hi
                    output2.write("*2\r\n$4\r\nPING\r\n$2\r\nhi\r\n".toByteArray(Charsets.UTF_8))
                    output2.flush()

                    val buffer1 = ByteArray(1024)
                    val bytesRead1 = input1.read(buffer1)
                    assertTrue(bytesRead1 > 0, "클라이언트 1이 응답을 받지 못했습니다.")
                    assertEquals("+PONG\r\n", String(buffer1, 0, bytesRead1, Charsets.UTF_8))

                    val buffer2 = ByteArray(1024)
                    val bytesRead2 = input2.read(buffer2)
                    assertTrue(bytesRead2 > 0, "클라이언트 2가 응답을 받지 못했습니다.")
                    assertEquals("$2\r\nhi\r\n", String(buffer2, 0, bytesRead2, Charsets.UTF_8))
                }
            }
        } catch (e: Exception) {
            fail("테스트 실패 (다중 클라이언트 동시 접속): ${e.message}")
        }
    }
}
