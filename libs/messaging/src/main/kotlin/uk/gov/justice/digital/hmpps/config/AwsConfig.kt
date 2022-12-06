package uk.gov.justice.digital.hmpps.config

import com.amazonaws.services.sns.AmazonSNS
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.messaging.core.NotificationMessagingTemplate
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter

@Configuration
class AwsConfig {
    @Bean
    @Conditional(AwsCondition::class)
    @ConditionalOnProperty("messaging.producer.topic")
    fun notificationMessagingTemplate(objectMapper: ObjectMapper, amazonSns: AmazonSNS): NotificationMessagingTemplate {
        val notificationMessagingTemplate = NotificationMessagingTemplate(amazonSns)
        val mappingJackson2MessageConverter = MappingJackson2MessageConverter()
        mappingJackson2MessageConverter.isStrictContentTypeMatch = false
        mappingJackson2MessageConverter.serializedPayloadClass = String::class.java
        mappingJackson2MessageConverter.objectMapper = objectMapper
        notificationMessagingTemplate.messageConverter = mappingJackson2MessageConverter
        return notificationMessagingTemplate
    }
}
