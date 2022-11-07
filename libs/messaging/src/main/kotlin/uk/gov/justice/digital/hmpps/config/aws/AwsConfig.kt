package uk.gov.justice.digital.hmpps.config.aws

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.messaging.core.NotificationMessagingTemplate
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter

@Configuration
@ConditionalOnProperty(prefix = "aws", name = ["region", "accessKeyId", "secretAccessKey"])
class AwsConfig(val awsConfigProperties: AwsConfigProperties) {

    @Bean
    fun amazonSns(): AmazonSNS {
        val builder = AmazonSNSClientBuilder.standard()
        if (awsConfigProperties.endpoint != null) {
            builder.withEndpointConfiguration(
                AwsClientBuilder.EndpointConfiguration(awsConfigProperties.endpoint, awsConfigProperties.region)
            )
        }
        return builder.build()
    }

    @Bean
    fun notificationTemplate(objectMapper: ObjectMapper, amazonSns: AmazonSNS): NotificationMessagingTemplate {
        val notificationMessagingTemplate = NotificationMessagingTemplate(amazonSns)
        val mappingJackson2MessageConverter = MappingJackson2MessageConverter()
        mappingJackson2MessageConverter.isStrictContentTypeMatch = false
        mappingJackson2MessageConverter.serializedPayloadClass = String::class.java
        mappingJackson2MessageConverter.objectMapper = objectMapper
        notificationMessagingTemplate.messageConverter = mappingJackson2MessageConverter
        notificationMessagingTemplate.setDefaultDestinationName(awsConfigProperties.topicName)
        return notificationMessagingTemplate
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "aws")
data class AwsConfigProperties(
    val endpoint: String?,
    val region: String?,
    val topicName: String?,
)
