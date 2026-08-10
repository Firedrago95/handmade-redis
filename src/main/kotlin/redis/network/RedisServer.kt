package redis.network

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.ServerSocket

class RedisServer {

    private val log = KotlinLogging.logger {}

    fun start() {
        val port = 6379
        val serverSocket = ServerSocket(port)
        log.info { "서버가 $port 포트에서 시작되었습니다. 연결을 대기합니다..." }

        // 소켓연결이 될때까지 스레드 블로킹, 연결시 소켓 객체 생성
        val socket = serverSocket.accept()
        val clientIp = socket.inetAddress.hostAddress
        val clientPort = socket.port
        log.info { "새로운 클라이언트 연결 수락됨: [$clientIp:$clientPort]" }

        // 연결한번당 Stream은 한번만 호출
        val inputStream = socket.inputStream
        val outputStream = socket.outputStream

        // 1-3. 하나의 연결에서 다중 입력 처리
        while (true) {
            // 1-2. 소켓을 통해 입력 읽기
            val buffer = ByteArray(1024)
            val byteRead = inputStream.read(buffer)
            if (byteRead == -1) break

            val response = String(buffer, 0, byteRead)

            // 1-2. 응답하기
            outputStream.write("+PONG\r\n".toByteArray())
            outputStream.flush()
        }
        socket.close()
    }
}
