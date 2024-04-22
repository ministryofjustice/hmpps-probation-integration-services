package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class ThreadPoolConfig(
    @Value("\${n}") private val numRequests: Int,
    @Value("\${c}") private val poolSize: Int,
) {
    @Bean
    fun threadPoolTaskExecutor() = ThreadPoolTaskExecutor().also {
        it.queueCapacity = numRequests
        it.corePoolSize = poolSize
        it.maxPoolSize = poolSize
    }
}
