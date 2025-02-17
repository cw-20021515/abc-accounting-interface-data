package com.abc.us.billing.inventory.cache.service

import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component

/*
 * Field와 value로 구성
 * Hashes는 key 하나에 여러개의 field와 value로 구성됨
 * key 하나에 field와 value 쌍을 40억개(4,294,967,295)까지 저장 가능합
 */
@Component
class RedisHashOps(private val redisTemplate: RedisTemplate<String, String>) {

    private var ops: HashOperations<String, String, String>? = null

    init {

        this.ops = redisTemplate.opsForHash()
    }

    fun set(k: String?, hK: String?, v: String?) {
        ops!!.put(k!!, hK!!, v!!)
    }

    fun get(k: String?, hk: String?): String? {
        return ops!![k!!, hk!!]
    }

    fun keys(k: String?): Set<String> {
        return ops!!.keys(k!!)
    }

    fun values(k: String?): List<String> {
        return ops!!.values(k!!)
    }

    fun entries(k: String?): Map<String, String> {
        return ops!!.entries(k!!)
    }

    fun scan(k: String?, options: ScanOptions?): Cursor<Map.Entry<String, String>> {
        return ops!!.scan(k!!, options!!)
    }

    fun size(k: String?): Long {
        return ops!!.size(k!!)
    }

    fun remove(k: String?, vararg hkeys: String?): Long {
        return ops!!.delete(k!!, *hkeys)
    }
}