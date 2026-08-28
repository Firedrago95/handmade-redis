package redis.network

import io.github.oshai.kotlinlogging.KotlinLogging
import redis.command.CommandDispatcher
import redis.protocol.RespEncoder
import redis.protocol.RespParser
import redis.protocol.RespValue
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class RedisServer(
    private val dispatcher: CommandDispatcher = CommandDispatcher(),
    private val encoder: RespEncoder = RespEncoder()
) {

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

            try {
                processCommands(clientSocket.inputStream, clientSocket.outputStream)
            } catch (e: Exception) {
                log.debug { "연결 예외 발생: ${e.message}" }
            } finally {
                log.info { "클라이언트 연결 종료됨: [$clientIp:$clientPort]" }
            }
        }
    }

    private fun processCommands(inputStream: InputStream, outputStream: OutputStream) {
        val parser = RespParser(inputStream)

        while (true) {
            val request = parser.parse() as? RespValue.Array ?: break
            val response = dispatcher.dispatch(request)
            val encodedBytes = encoder.encode(response)

            outputStream.write(encodedBytes)
            outputStream.flush()
        }
    }
}

