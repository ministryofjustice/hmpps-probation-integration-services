package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableAsync
@Configuration
class ThreadConfig : WebMvcConfigurer {
    @Value("\${async-task-executor.threads.core:8}")
    private val coreThreads = 0

    @Value("\${async-task-executor.threads.max:64}")
    private val maxThreads = 0

    @Primary
    @Bean
    fun asyncTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = coreThreads
        executor.maxPoolSize = maxThreads
        executor.setThreadNamePrefix("AsyncTask: ")
        executor.setTaskDecorator { ContextRunnable(it) }
        executor.initialize()
        return executor
    }

    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        configurer.setTaskExecutor(asyncTaskExecutor())
    }
}

internal class ContextRunnable(
    private val delegate: Runnable
) : Runnable {

    private val securityContext: SecurityContext? =
        try {
            SecurityContextHolder.getContext()
        } catch (ignore: Exception) {
            null
        }

    private val requestContext: RequestAttributes? =
        try {
            RequestContextHolder.currentRequestAttributes()
        } catch (ignore: Exception) {
            null
        }

    override fun run() {
        val originalSecurityContext: SecurityContext = SecurityContextHolder.getContext()
        try {
            RequestContextHolder.setRequestAttributes(requestContext)
            SecurityContextHolder.setContext(securityContext)
            delegate.run()
        } finally {
            if (SecurityContextHolder.createEmptyContext() == originalSecurityContext) {
                SecurityContextHolder.clearContext()
            } else {
                SecurityContextHolder.setContext(originalSecurityContext)
            }
            RequestContextHolder.resetRequestAttributes()
        }
    }

    override fun toString(): String {
        return delegate.toString()
    }
}
