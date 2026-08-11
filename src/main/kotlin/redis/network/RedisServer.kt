package redis.network

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class RedisServer {

    private val log = KotlinLogging.logger {}
    private val executor = Executors.newVirtualThreadPerTaskExecutor()

    fun start(port: Int) {
        val serverSocket = ServerSocket(port)
        log.info { "서버가 $port 포트에서 시작되었습니다. 연결을 대기합니다..." }

        // 다중 클라이언트 연결을 위해 반복문을 통해 소켓 연결확인 및 작업 할당
        while (true) {
            // 소켓연결이 될때까지 스레드 블로킹, 연결시 소켓 객체 생성
            // 가상스레드를 통해 동기식 코드 흐름 유지하면서도, 효율적인 연결 가능
            val socket = serverSocket.accept()
            executor.submit { handleClient(socket) }
        }
    }

    private fun handleClient(socket: Socket) {
        socket.use { clientSocket ->
            val clientIp = clientSocket.inetAddress.hostAddress
            val clientPort = clientSocket.port
            log.info { "새로운 클라이언트 연결 수락됨: [$clientIp:$clientPort]" }

            // 연결한번당 Stream은 한번만 호출
            val inputStream = clientSocket.inputStream
            val outputStream = clientSocket.outputStream

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
        }
    }
}
