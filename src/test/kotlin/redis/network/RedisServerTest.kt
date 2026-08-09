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
                    server.start()
                } catch (e: Exception) {
                    // 이미 포트가 사용 중이거나 에러가 발생한 경우 무시 (다른 테스트에서 띄웠을 수 있음)
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
}
