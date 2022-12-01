package uk.gov.justice.digital.hmpps.config.aws

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.messaging.core.NotificationMessagingTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter

@Configuration
@ConditionalOnProperty("messaging.producer.topic")
@ConditionalOnMissingClass("org.apache.activemq.ActiveMQConnectionFactory")
class AwsConfig {
    @Bean
    fun amazonSns(
        @Value("\${aws.endpoint}") awsEndpoint: String?,
        @Value("\${aws.region}") awsEndpointSigningRegion: String?
    ): AmazonSNS {
        val builder = AmazonSNSClientBuilder.standard()
        if (awsEndpoint != null) {
            builder.withEndpointConfiguration(EndpointConfiguration(awsEndpoint, awsEndpointSigningRegion))
        }
        return builder.build()
    }

    @Bean
    fun notificationTemplate(
        objectMapper: ObjectMapper,
        amazonSns: AmazonSNS,
        @Value("\${messaging.producer.topic}") topicArn: String
    ): NotificationMessagingTemplate {
        val notificationMessagingTemplate = NotificationMessagingTemplate(amazonSns)
        val mappingJackson2MessageConverter = MappingJackson2MessageConverter()
        mappingJackson2MessageConverter.isStrictContentTypeMatch = false
        mappingJackson2MessageConverter.serializedPayloadClass = String::class.java
        mappingJackson2MessageConverter.objectMapper = objectMapper
        notificationMessagingTemplate.messageConverter = mappingJackson2MessageConverter
        notificationMessagingTemplate.setDefaultDestinationName(topicArn)
        return notificationMessagingTemplate
    }
}
