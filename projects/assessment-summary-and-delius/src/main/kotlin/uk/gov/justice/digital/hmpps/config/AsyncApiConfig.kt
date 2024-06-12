package uk.gov.justice.digital.hmpps.config

import org.openfolder.kotlinasyncapi.springweb.service.AsyncApiExtension
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AsyncApiConfig {
    @Bean
    fun asyncApiExtension(asyncApiCommonInfo: AsyncApiExtension) = AsyncApiExtension.builder(order = 1) {
        info.title("assessment-summary-and-delius")
        servers {
            server("dev") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-dev-assessment-summary-and-delius-queue")
                protocol("sqs")
            }
            server("preprod") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-preprod-assessment-summary-and-delius-queue")
                protocol("sqs")
            }
            server("prod") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-prod-assessment-summary-and-delius-queue")
                protocol("sqs")
            }
        }
        externalDocs {
            url("https://ministryofjustice.github.io/hmpps-probation-integration-services/tech-docs/projects/assessment-summary-and-delius/")
        }
    }
}
