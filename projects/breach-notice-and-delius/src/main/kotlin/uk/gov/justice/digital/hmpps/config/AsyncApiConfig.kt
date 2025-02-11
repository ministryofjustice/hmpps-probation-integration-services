package uk.gov.justice.digital.hmpps.config

import com.asyncapi.kotlinasyncapi.context.service.AsyncApiExtension
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AsyncApiConfig {
    @Bean
    fun asyncApiExtension() = AsyncApiExtension.builder(order = 1) {
        info.title("breach-notice-and-delius")
        servers {
            server("dev") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-dev-breach-notice-and-delius-queue")
                protocol("sqs")
            }
            server("preprod") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-preprod-breach-notice-and-delius-queue")
                protocol("sqs")
            }
            server("prod") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-prod-breach-notice-and-delius-queue")
                protocol("sqs")
            }
        }
        externalDocs {
            url("https://ministryofjustice.github.io/hmpps-probation-integration-services/tech-docs/projects/breach-notice-and-delius/")
        }
    }
}
