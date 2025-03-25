@file:Suppress("deprecation", "removal") // Copying io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration
package uk.gov.justice.digital.hmpps.client

import com.amazon.sqs.javamessaging.AmazonSQSExtendedAsyncClient
import io.awspring.cloud.autoconfigure.AwsAsyncClientCustomizer
import io.awspring.cloud.autoconfigure.core.AwsClientBuilderConfigurer
import io.awspring.cloud.autoconfigure.core.AwsClientCustomizer
import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails
import io.awspring.cloud.autoconfigure.sqs.SqsAsyncClientCustomizer
import io.awspring.cloud.autoconfigure.sqs.SqsProperties
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder

@Configuration
class SqsExtendedClientConfig {
    @Bean
    @Primary
    fun sqsAsyncClient(
        sqsProperties: SqsProperties,
        awsClientBuilderConfigurer: AwsClientBuilderConfigurer,
        configurer: ObjectProvider<AwsClientCustomizer<SqsAsyncClientBuilder>>,
        connectionDetails: ObjectProvider<AwsConnectionDetails>,
        sqsAsyncClientCustomizers: ObjectProvider<SqsAsyncClientCustomizer>,
        awsAsyncClientCustomizers: ObjectProvider<AwsAsyncClientCustomizer>
    ): SqsAsyncClient = AmazonSQSExtendedAsyncClient(
        awsClientBuilderConfigurer.configureAsyncClient<SqsAsyncClientBuilder?>(
            SqsAsyncClient.builder(),
            sqsProperties,
            connectionDetails.getIfAvailable(),
            configurer.getIfAvailable(),
            sqsAsyncClientCustomizers.orderedStream(),
            awsAsyncClientCustomizers.orderedStream()
        ).build()
    )
}