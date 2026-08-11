package redis

import redis.network.RedisServer

fun main() {
    val server = RedisServer()

    server.start(6379)
}
