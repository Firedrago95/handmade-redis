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

        // 소켓을 통해 입력 읽기
        val inputStream = socket.inputStream
        val outputStream = socket.outputStream

        val buffer = ByteArray(1024)
        val byteRead = inputStream.read(buffer)
        val response = String(buffer, 0, byteRead)

        // 응답하기
        outputStream.write("+PONG\r\n".toByteArray())
        outputStream.flush()
    }
}
