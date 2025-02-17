package com.abc.us.accounting.supports.redis

import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.SetOperations
import org.springframework.stereotype.Component

@Component
class RedisSetOps(private val redisTemplate: RedisTemplate<String, String>) {
    private var ops: SetOperations<String, String>? = null

    init {

        this.ops = redisTemplate.opsForSet()
    }

    fun add(key: String?, vararg values: String?): Long? {
        return ops!!.add(key!!, *values)
    }


    fun members(key: String?): Set<String>? {
        return ops!!.members(key!!)
    }

    fun size(key: String?): Long? {
        return ops!!.size(key!!)
    }

    fun remove(key: String?, vararg values: String?): Long? {
        return ops!!.remove(key!!, *values)
    }


    fun ismember(key: String?, o: String?): Boolean? {
        return ops!!.isMember(key!!, o!!)
    }

    fun ismember(key: String?, vararg objects: String?): MutableMap<Any, Boolean>? {
        return ops!!.isMember(key!!, *objects)
    }


    fun scan(key: String?, options: ScanOptions?): Cursor<String> {
        return ops!!.scan(key!!, options!!)
    }
}