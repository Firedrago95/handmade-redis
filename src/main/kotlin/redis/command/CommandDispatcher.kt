package redis.command

import redis.protocol.RespValue

class CommandDispatcher {

    fun dispatch(request: RespValue.Array) : RespValue {
        if (request.elements.isNullOrEmpty()) {
            return RespValue.Error("ERR 잘못된 request 명령어입니다.")
        }

        val elements = request.elements
        val firstElement = elements[0] as? RespValue.BulkString
            ?: return RespValue.Error("ERR 잘못된 request 명령어입니다.")
        val content = firstElement.content?.toString(Charsets.UTF_8)?.uppercase()
        val args = elements.drop(1)

        return when (content) {
            "PING" -> handlePing(args)
            else -> handleInvalidCommand()
        }
    }

    private fun handlePing(args: List<RespValue>): RespValue {
        if (args.isEmpty()) {
            return RespValue.SimpleString("PONG")
        }
        val firstArg = args[0] as RespValue.BulkString
        return RespValue.BulkString(firstArg.content)
    }

    private fun handleInvalidCommand(): RespValue {
        return RespValue.Error("ERR 잘못된 명령어 입니다.")
    }
}
