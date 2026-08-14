package redis.protocol

import java.io.InputStream

interface RespValueParser {
    fun parse(inputStream: InputStream): RespValue
}
