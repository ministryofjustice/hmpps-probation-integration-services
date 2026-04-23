package uk.gov.justice.digital.hmpps.config

import com.amazon.sqs.javamessaging.AmazonSQSExtendedAsyncClient
import com.amazon.sqs.javamessaging.ExtendedAsyncClientConfiguration
import io.awspring.cloud.autoconfigure.core.AwsClientBuilderConfigurer
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.payloadoffloading.S3BackedPayloadStore
import software.amazon.payloadoffloading.S3Dao
import uk.gov.justice.digital.hmpps.logging.Logger.logger

@Configuration
@Conditional(AwsCondition::class)
@ConditionalOnProperty("messaging.large-message.bucket")
class ExtendedSqsConfig(
    private val awsClientBuilderConfigurer: AwsClientBuilderConfigurer,
    @Value("\${messaging.large-message.bucket}") private val bucketName: String
) {
    @Bean
    @Primary
    fun extendedSqsAsyncClient(): SqsAsyncClient {
        val sqsBase = awsClientBuilderConfigurer.configure(SqsAsyncClient.builder()).build()
        val s3AsyncClient = awsClientBuilderConfigurer.configure(S3AsyncClient.builder()).build()
        val extendedConfig = ExtendedAsyncClientConfiguration()
            .withPayloadSupportEnabled(s3AsyncClient, bucketName)
        return AmazonSQSExtendedAsyncClient(sqsBase, extendedConfig)
    }

    @Bean
    fun largeMessagePayloadStore(): S3BackedPayloadStore {
        val s3Client = awsClientBuilderConfigurer.configure(S3Client.builder()).build()
        logger().info("Using S3 bucket {} for large message payload storage", bucketName)
        return S3BackedPayloadStore(
            S3Dao(s3Client),
            bucketName
        )
    }

    @Bean(name = ["defaultSqsListenerContainerFactory"])
    fun sqsListenerContainerFactory(sqsAsyncClient: SqsAsyncClient): SqsMessageListenerContainerFactory<Any> =
        SqsMessageListenerContainerFactory.builder<Any>()
            .sqsAsyncClient(sqsAsyncClient)
            .build()
}