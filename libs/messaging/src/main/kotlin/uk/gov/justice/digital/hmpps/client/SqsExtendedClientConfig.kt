@file:Suppress("deprecation", "removal") // Copying io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration
package uk.gov.justice.digital.hmpps.client

import com.amazon.sqs.javamessaging.AmazonSQSExtendedAsyncClient
import com.amazon.sqs.javamessaging.ExtendedAsyncClientConfiguration
import io.awspring.cloud.autoconfigure.AwsAsyncClientCustomizer
import io.awspring.cloud.autoconfigure.core.AwsClientBuilderConfigurer
import io.awspring.cloud.autoconfigure.core.AwsClientCustomizer
import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails
import io.awspring.cloud.autoconfigure.s3.properties.S3Properties
import io.awspring.cloud.autoconfigure.sqs.SqsAsyncClientCustomizer
import io.awspring.cloud.autoconfigure.sqs.SqsProperties
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder

@Configuration
class SqsExtendedClientConfig {

    lateinit var sqsAsyncClient: SqsAsyncClient

    @Bean
    @Primary
    fun sqsAsyncClient(
        sqsProperties: SqsProperties,
        awsClientBuilderConfigurer: AwsClientBuilderConfigurer,
        configurer: ObjectProvider<AwsClientCustomizer<SqsAsyncClientBuilder>>,
        connectionDetails: ObjectProvider<AwsConnectionDetails>,
        sqsAsyncClientCustomizers: ObjectProvider<SqsAsyncClientCustomizer>,
        awsAsyncClientCustomizers: ObjectProvider<AwsAsyncClientCustomizer>,
        s3Client: S3AsyncClient
    ): SqsAsyncClient {

        sqsAsyncClient = AmazonSQSExtendedAsyncClient(
            awsClientBuilderConfigurer.configureAsyncClient(
                SqsAsyncClient.builder(),
                sqsProperties,
                connectionDetails.getIfAvailable(),
                configurer.getIfAvailable(),
                sqsAsyncClientCustomizers.orderedStream(),
                awsAsyncClientCustomizers.orderedStream()
            ).build(),
            ExtendedAsyncClientConfiguration().withPayloadSupportEnabled(s3Client, "large-common-platform-messages")
        )

        return sqsAsyncClient
    }

    @Bean
    fun s3AsyncClient(
        s3Properties: S3Properties,
        awsClientBuilderConfigurer: AwsClientBuilderConfigurer,
        configurer: ObjectProvider<AwsClientCustomizer<S3AsyncClientBuilder>>,
        connectionDetails: ObjectProvider<AwsConnectionDetails>,
        s3ClientCustomizers: ObjectProvider<S3AsyncClientCustomizer>,
        awsSyncClientCustomizers: ObjectProvider<AwsAsyncClientCustomizer>
    ): S3AsyncClient {
        val builder = awsClientBuilderConfigurer.configureAsyncClient(
            S3AsyncClient.builder().forcePathStyle(true),
            s3Properties,
            connectionDetails.getIfAvailable(),
            configurer.getIfAvailable(),
            s3ClientCustomizers.orderedStream(),
            awsSyncClientCustomizers.orderedStream()
        )
        return builder.build()
    }
}

@FunctionalInterface
interface S3AsyncClientCustomizer : io.awspring.cloud.autoconfigure.AwsClientCustomizer<S3AsyncClientBuilder?>
