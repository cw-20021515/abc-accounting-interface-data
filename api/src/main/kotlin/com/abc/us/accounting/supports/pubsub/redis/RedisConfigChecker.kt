package com.abc.us.accounting.supports.pubsub.redis

//@Component
//class RedisConfigChecker(
//    private val redisTemplate: RedisTemplate<String, String>
//) {
//    @EventListener(ApplicationReadyEvent::class)
//    fun checkAndSetKeyspaceNotifications() {
//        val connectionFactory = redisTemplate.connectionFactory
//
//        connectionFactory?.connection?.use { connection ->
//            // CONFIG GET 명령 실행 - 명령어와 인수를 ByteArray로 변환
//            val configValue = connection.execute(
//                "CONFIG",
//                "GET".toByteArray(),
//                "notify-keyspace-events".toByteArray()
//            ) as List<*>
//
//            val currentSetting = configValue.getOrNull(1)?.toString() ?: ""
//
//            println("Current notify-keyspace-events setting: $currentSetting")
//
//            // 만약 설정이 비어있다면, 필요한 옵션으로 설정
//            if (currentSetting.isEmpty()) {
//                connection.execute(
//                    "CONFIG",
//                    "SET".toByteArray(),
//                    "notify-keyspace-events".toByteArray(),
//                    "Ex".toByteArray()
//                )
//                println("Keyspace notifications 설정이 완료되었습니다.")
//            } else {
//                println("Keyspace notifications는 이미 활성화되어 있습니다.")
//            }
//        }
//    }
//}