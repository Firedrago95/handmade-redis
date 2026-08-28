package redis.network

import io.github.oshai.kotlinlogging.KotlinLogging
import redis.command.CommandDispatcher
import redis.protocol.RespEncoder
import redis.protocol.RespParser
import redis.protocol.RespValue
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class RedisServer (
    private val dispatcher : CommandDispatcher = CommandDispatcher(),
    private val encoder : RespEncoder = RespEncoder()
){

    private val log = KotlinLogging.logger {}
    private val executor = Executors.newVirtualThreadPerTaskExecutor()

    fun start(port: Int = 6379) {
        val serverSocket = ServerSocket(port)
        log.info { "서버가 $port 포트에서 시작되었습니다. 연결을 대기합니다..." }

        while (true) {
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
            val parser = RespParser(inputStream)

            // 1-3. 하나의 연결에서 다중 입력 처리
            try {
                while (true) {
                    val request = parser.parse() as? RespValue.Array ?: return
                    val response = dispatcher.dispatch(request)
                    val encodedBytes = encoder.encode(response)

                    // 1-2. 응답하기
                    outputStream.write(encodedBytes)
                    outputStream.flush()
                }
            } catch (e: Exception) {
                log.info("클라이언트 연결 종료:[${clientSocket.inetAddress.hostAddress}:${clientSocket.port}]")
            }
        }
    }
}
