package uk.gov.justice.digital.hmpps.config

import org.openfolder.kotlinasyncapi.springweb.service.AsyncApiExtension
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable

@Configuration
class AsyncApiConfig {
    @Bean
    fun asyncApiExtension(asyncApiCommonInfo: AsyncApiExtension) = AsyncApiExtension.builder(order = 1) {
        info.title("court-case-and-delius")
        servers {
            server("dev") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-dev-court-case-and-delius-queue")
                protocol("sqs")
            }
            server("preprod") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-preprod-court-case-and-delius-queue")
                protocol("sqs")
            }
            server("prod") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-prod-court-case-and-delius-queue")
                protocol("sqs")
            }
        }
        externalDocs {
            url("https://ministryofjustice.github.io/hmpps-probation-integration-services/tech-docs/projects/court-case-and-delius/")
        }
    }

    @Bean
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10
        executor.maxPoolSize = 100
        executor.queueCapacity = 50
        executor.setThreadNamePrefix("async-")
        executor.setTaskDecorator { runnable: Runnable? ->
            DelegatingSecurityContextRunnable(
                runnable
            )
        }
        return executor
    }
}
