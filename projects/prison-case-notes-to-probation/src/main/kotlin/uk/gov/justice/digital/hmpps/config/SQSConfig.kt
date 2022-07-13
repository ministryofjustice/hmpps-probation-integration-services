package uk.gov.justice.digital.hmpps.config

import com.amazon.sqs.javamessaging.ProviderConfiguration
import com.amazon.sqs.javamessaging.SQSConnectionFactory
import com.amazon.sqs.javamessaging.SQSSession
import io.sentry.Sentry
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.support.destination.DynamicDestinationResolver
import javax.jms.ConnectionFactory
import javax.jms.Queue
import javax.jms.Session

@Configuration
@ConditionalOnMissingClass("uk.gov.justice.digital.hmpps.config.ArtemisConfig")
class SQSConfig {
    @Bean
    fun connectionFactory(): ConnectionFactory = SQSConnectionFactory(ProviderConfiguration())

    @Bean
    fun destinationResolver(@Value("\${aws.sqs.account-id}") accountId: String?) =
        // Adds support for accessing SQS queues in a different AWS account
        if (accountId == null) DynamicDestinationResolver()
        else object : DynamicDestinationResolver() {
            override fun resolveQueue(session: Session, queueName: String): Queue =
                (session as SQSSession).createQueue(queueName, accountId)
        }

    @Bean
    fun jmsListenerContainerFactory(
        configurer: DefaultJmsListenerContainerFactoryConfigurer
    ): DefaultJmsListenerContainerFactory {
        val factory = DefaultJmsListenerContainerFactory()
        configurer.configure(factory, connectionFactory())
        factory.setSessionTransacted(false) // SQS does not support transactions
        factory.setErrorHandler { Sentry.captureException(it) }
        return factory
    }
}
