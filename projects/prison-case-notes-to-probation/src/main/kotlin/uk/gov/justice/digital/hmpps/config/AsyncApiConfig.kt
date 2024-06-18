package uk.gov.justice.digital.hmpps.config

import org.openfolder.kotlinasyncapi.springweb.service.AsyncApiExtension
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AsyncApiConfig {
    @Bean
    fun asyncApiExtension(asyncApiCommonInfo: AsyncApiExtension) = AsyncApiExtension.builder(order = 1) {
        info.title("prison-case-notes-to-probation")
        info.description("Import Prison Case Notes to the Delius Contact Log")
        servers {
            server("dev") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-dev-prison-case-notes-to-probation-queue")
                protocol("sqs")
            }
            server("preprod") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-preprod-prison-case-notes-to-probation-queue")
                protocol("sqs")
            }
            server("prod") {
                url("https://sqs.eu-west-2.amazonaws.com/754256621582/probation-integration-prod-prison-case-notes-to-probation-queue")
                protocol("sqs")
            }
        }
        externalDocs {
            url("https://ministryofjustice.github.io/hmpps-probation-integration-services/tech-docs/projects/prison-case-notes-to-probation/")
        }
    }
}
