package com.abc.us.accounting.supports.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class RedisValueOps(private val redisTemplate: RedisTemplate<String, String>) {
    private var ops: ValueOperations<String, String>? = null

    init {

        this.ops = redisTemplate.opsForValue()
    }

    fun get(k: String?): String? {
        return k?.let { ops!!.get(it) }
    }

    fun set(k: String?, v: String?) {
        ops!!.set(k!!, v!!)
    }

    fun setKeyWithExpiration(k: String?, value: String?, timeout: Long, unit: TimeUnit?) {
        ops!!.set(k!!, value!!, timeout, unit!!)
    }

    fun set(k: String?, v: String?, timeout: Duration?) {
        ops!!.set(k!!, v!!, timeout!!)
    }
}