package com.abc.us.accounting.supports.pubsub

import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import jakarta.persistence.Entity
import mu.KotlinLogging
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.lang.reflect.Method

@Component
@Configuration
class EventListenerContainer(private val context: ApplicationContext)
    : ApplicationListener<ContextRefreshedEvent> {

    val listeners :MutableMap<String,Pair<Method, Any> > = mutableMapOf()
    val entities :MutableMap<String,Any > = mutableMapOf()
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private fun collectAsyncEventListener() {
        // Spring Context에서 모든 @Service 빈을 가져옵니다.
        val serviceBeans = context.getBeansWithAnnotation(Service::class.java)

        // 각 빈에서 @MessageListener 어노테이션이 붙은 메서드를 찾아서 메시지 전달
        for (bean in serviceBeans.values) {
            // 프록시가 아닌 실제 클래스에서 메서드를 가져옴
            val targetClass = AopProxyUtils.ultimateTargetClass(bean)
            val methods: Array<Method> = targetClass.methods

            for (method in methods) {
                val annotation = method.getAnnotation(AsyncEventListener::class.java)
                annotation?.let {
                    listeners[annotation.listener] = method to bean
                    //logger.info { "Found AsyncEventListener in ${targetClass.simpleName}, method: ${method.name}, descriptor: ${annotation.listener}" }
                }
            }
        }
    }

    private fun collectEntities() {
        val entityBeans = context.getBeansWithAnnotation(Entity::class.java)
        entityBeans.forEach { name, bean -> entities[name] = bean }
    }

//    fun findEntityClasses(packageName: String): Set<Class<*>> {
//        // 패키지를 스캔합니다.
//        val reflections = Reflections(packageName)
//
//        // @Entity 어노테이션이 붙은 클래스를 모두 찾습니다.
//        return reflections.getTypesAnnotatedWith(Entity::class.java)
//    }



    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        collectAsyncEventListener()
        collectEntities()
    }

    fun findListeners(serviceName : String) : Pair<Method, Any>? {
        return listeners[serviceName]
    }

    fun findEntity(entityName : String) : Any? {
        return entities[entityName]
    }
}