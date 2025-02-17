package com.abc.us.accounting.configs

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.util.concurrent.ThreadPoolExecutor

@EnableAsync
@Configuration
class AsyncThreadPoolConfig () {
    private var corePoolSize = 0
    private var maxPoolSize = 0
    private var queueCapacity = 0
    private var awaitTerminationSeconds = 0
    private var keepAliveSeconds = 0

    init {

        val osBean = ManagementFactory.getPlatformMXBean(
            OperatingSystemMXBean::class.java
        )

        this.corePoolSize = calcCorePoolSize(osBean)
        this.maxPoolSize = calcCorePoolSize(osBean) * 2
        this.queueCapacity = 1000
        this.awaitTerminationSeconds = 10
        this.keepAliveSeconds = 60
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun calcCorePoolSize(osBean: OperatingSystemMXBean): Int {
        return osBean.availableProcessors * 4
    }

    private fun createExecutor(prefix: String): ThreadPoolTaskExecutor {
        val taskExecutor = ThreadPoolTaskExecutor()
        // 동시에 실행시킬 스레드 개수를 의미
        // 보통 CPU * 2 의 공식으로 스레드 갯수를 정함
        taskExecutor.corePoolSize = corePoolSize

        // 스레드 풀의 최대 사이즈
        // CPU * 2 의 값으로 셋팅
        taskExecutor.maxPoolSize = maxPoolSize

        // 스레드 풀 큐의 사이즈
        // corePoolSize 개수를 넘어서는 task 인입 시 큐에 task가 쌓임
        // maxPoolSize 개수 만큼 큐에 적재 가능
        // 해당 부분은 테스트를 진행 해보면서 설정해 줘야 할 것임
        taskExecutor.queueCapacity = queueCapacity
        taskExecutor.setThreadNamePrefix(prefix)
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true)

        //스레드가 setAwaitTerminationSeconds 까지 끝나지 않으면 강제로 종료한다.
        taskExecutor.setAwaitTerminationSeconds(awaitTerminationSeconds)


        // maxPoolSize 까지 생성 후 queueCapacity 까지 채우진 상태라면 RejectedExecutionException 예외 발생됨
        taskExecutor.setRejectedExecutionHandler { r: Runnable?, e: ThreadPoolExecutor ->
            // Runnable r, ThreadPoolExecutor executor
            logger.info("{}", e)
            // DiscardOldestPolicy
            if (!e.isShutdown) {
                e.queue.poll()
                e.execute(r)
            }
        }
        taskExecutor.setRejectedExecutionHandler(ThreadPoolExecutor.AbortPolicy())
        taskExecutor.keepAliveSeconds = keepAliveSeconds //디폴트가 60임
        taskExecutor.initialize()

        logger.info(
            "Config ThreadPool : corePoolSize={} maxPoolSize={} queueCapacity={} awaitTerminationSeconds={} keepAliveSeconds={}",
            corePoolSize, maxPoolSize, queueCapacity, awaitTerminationSeconds, keepAliveSeconds
        )
        return taskExecutor
        //return CleanableAsyncTaskExecutor(taskExecutor)
    }

    @Bean(name = ["eventMessageExecutor"])
    fun getAsyncCollectExecutor(): ThreadPoolTaskExecutor {
        return createExecutor("eventMessageExecutor-")
    }
}
//
//    class CleanableAsyncTaskExecutor internal constructor(private val executor: AsyncTaskExecutor) :
//        AsyncTaskExecutor {
//        override fun execute(task: Runnable) {
//            executor.execute(createWrappedRunnable(task))
//        }
//
//        override fun execute(task: Runnable, startTimeout: Long) {
//            executor.execute(createWrappedRunnable(task), startTimeout)
//        }
//
//        override fun submit(task: Runnable): Future<*> {
//            return executor.submit(createWrappedRunnable(task))
//        }
//
//        override fun <T> submit(task: Callable<T>): Future<T> {
//            return executor.submit(createCallable(task))
//        }
//
//        private fun <T> createCallable(task: Callable<T>): Callable<T> {
//            return Callable {
//                try {
//                    val callResult = task.call()
//                    return@Callable callResult
//                } catch (ex: Exception) {
//                    handle(ex)
//                    throw ex
//                }
//            }
//        }
//
//        private fun createWrappedRunnable(task: Runnable): Runnable {
//            return Runnable {
//                try {
//                    task.run()
//                } catch (ex: Exception) {
//                    handle(ex)
//                }
//            }
//        }
//
//        private fun handle(ex: Exception) {
//            logger.info("Failed to execute task. : {}", ex.message)
//            logger.error("Failed to execute task. ", ex)
//        }
//
//        fun destroy() {
//            if (executor is ThreadPoolTaskExecutor) {
//                executor.shutdown()
//            }
//        }
//    }
//}