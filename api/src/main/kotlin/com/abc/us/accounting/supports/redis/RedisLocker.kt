package com.abc.us.accounting.supports.redis

import kotlinx.coroutines.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisLocker(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * 다량의 키에 대해 병렬로 락을 시도하고, 성공한 키들을 반환
     */
    suspend fun tryLock(keys: List<String>, expireTime: Long): List<String> = coroutineScope {
        val lockValue = Thread.currentThread().id.toString()
        val successKeys = mutableListOf<String>()

        val jobs = keys.map { key ->
            async {
                val isLocked = redisTemplate.opsForValue().setIfAbsent(key, lockValue, expireTime, TimeUnit.SECONDS)
                if (isLocked == true) {
                    synchronized(successKeys) { successKeys.add(key) }  // 락 성공한 키 추가
                }
            }
        }
        jobs.awaitAll()  // 모든 병렬 실행 완료 대기

        successKeys
    }

    /**
     * 저장된 락 키들을 action 에 전달 후 실행하고, 자동 unlock
     */
    suspend fun <T> executeLockedActions(keys: List<String>, expireTime: Long, action: (List<String>) -> T): T? {
        val lockedKeys = tryLock(keys, expireTime)

        return if (lockedKeys.isNotEmpty()) {
            try {
                action(lockedKeys)  // 🔥 락이 성공한 키들을 action 에 전달
            } finally {
                unlock(lockedKeys)
            }
        } else {
            null
        }
    }

    /**
     * 다량의 키 락 해제 (Lua 스크립트 사용)
     */
    private fun unlock(keys: List<String>) {
        val script = """
            for i, key in ipairs(KEYS) do
                redis.call("DEL", key)
            end
        """.trimIndent()

        redisTemplate.execute(
            org.springframework.data.redis.core.script.DefaultRedisScript(script, Void::class.java),
            keys
        )
    }
}