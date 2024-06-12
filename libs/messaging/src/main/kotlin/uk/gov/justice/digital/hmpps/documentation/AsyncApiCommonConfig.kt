package uk.gov.justice.digital.hmpps.documentation

import org.openfolder.kotlinasyncapi.springweb.service.AsyncApiExtension
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AsyncApiCommonConfig {
    @Bean
    fun asyncApiCommonInfo() = AsyncApiExtension.builder {
        info {
            title("Async API Reference")
            version("1.0")
            contact {
                name("Probation Integration Team")
                email("probation-integration-team@digital.justice.gov.uk")
                url("https://mojdt.slack.com/archives/C02HQ4M2YQN") // #probation-integration-tech Slack channel
            }
            license {
                name("MIT")
                url("https://github.com/ministryofjustice/hmpps-probation-integration-services/blob/main/LICENSE")
            }
        }
        externalDocs {
            url("https://ministryofjustice.github.io/hmpps-probation-integration-services/tech-docs/")
        }
        defaultContentType("application/json")
    }
}
