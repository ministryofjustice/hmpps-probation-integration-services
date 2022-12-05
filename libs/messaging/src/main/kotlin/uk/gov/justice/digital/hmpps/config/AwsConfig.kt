package uk.gov.justice.digital.hmpps.config

import com.amazonaws.services.sns.AmazonSNS
import io.awspring.cloud.messaging.core.NotificationMessagingTemplate
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

@Configuration
class AwsConfig {
    @Bean
    @Conditional(AwsCondition::class)
    @ConditionalOnProperty("messaging.producer.topic")
    fun notificationMessagingTemplate(amazonSNS: AmazonSNS) = NotificationMessagingTemplate(amazonSNS)
}
