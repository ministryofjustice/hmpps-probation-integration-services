package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.service.notify.NotificationClient

@Configuration
class NotificationClientConfig {
    @Bean
    fun notificationClient(@Value("\${govuk-notify.api-key}") apiKey: String) = NotificationClient(apiKey)
}
