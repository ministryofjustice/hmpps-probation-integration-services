package uk.gov.justice.digital.hmpps.config

import com.amazon.sqs.javamessaging.AmazonSQSExtendedAsyncClient
import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient
import com.amazon.sqs.javamessaging.ExtendedAsyncClientConfiguration
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration
import io.awspring.cloud.sqs.config.SqsBootstrapConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsClient

@Configuration
@Conditional(AwsCondition::class)
@ConditionalOnProperty("messaging.large-message.bucket")
@AutoConfigureBefore(SqsBootstrapConfiguration::class)
class ExtendedSqsConfig(
    private val s3Client: S3Client,
    @Value("\${messaging.large-message.bucket}") private val bucketName: String
) {
    @Bean
    fun extendedSqsClient(sqsClient: SqsClient): AmazonSQSExtendedClient {
        val config = ExtendedClientConfiguration()
            .withPayloadSupportEnabled(s3Client, bucketName)
        return AmazonSQSExtendedClient(sqsClient, config)
    }

    @Bean
    @Primary
    fun extendedSqsAsyncClient(@Lazy sqsAsyncClient: SqsAsyncClient): SqsAsyncClient {
        val s3AsyncClient = S3AsyncClient.builder().build()
        val extendedConfig = ExtendedAsyncClientConfiguration()
            .withPayloadSupportEnabled(s3AsyncClient, bucketName)
        return AmazonSQSExtendedAsyncClient(sqsAsyncClient, extendedConfig)
    }
}