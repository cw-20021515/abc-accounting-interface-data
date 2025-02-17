package com.abc.us.accounting.supports.utils

import mu.KotlinLogging


class TimeLogger {
    inline fun <T> measureAndLog(tag: String = "", block: () -> T): T {
        val startTime = System.nanoTime()
        return block().also {
            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1_000_000 // 나노초를 밀리초로 변환
            logger.info{"$tag completed in ${duration}ms"}
        }
    }

    companion object {
        final val logger = KotlinLogging.logger {}
    }
}