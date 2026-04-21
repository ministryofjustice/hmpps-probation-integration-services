package uk.gov.justice.digital.hmpps.config

import com.amazon.sqs.javamessaging.AmazonSQSExtendedAsyncClient
import com.amazon.sqs.javamessaging.ExtendedAsyncClientConfiguration
import io.awspring.cloud.sqs.config.SqsBootstrapConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sqs.SqsAsyncClient

@Configuration
@Conditional(AwsCondition::class)
@ConditionalOnProperty("messaging.large-message.bucket")
@AutoConfigureBefore(SqsBootstrapConfiguration::class)
class ExtendedSqsConfig(
    private val s3Client: S3Client,
    @Value("\${messaging.large-message.bucket}") private val bucketName: String
) {
    @Bean
    @Primary
    fun extendedSqsAsyncClient(@Qualifier("sqsAsyncClient") sqsAsyncClient: SqsAsyncClient): SqsAsyncClient {
        val s3AsyncClient = S3AsyncClient.builder().build()
        val extendedConfig = ExtendedAsyncClientConfiguration()
            .withPayloadSupportEnabled(s3AsyncClient, bucketName)
        return AmazonSQSExtendedAsyncClient(sqsAsyncClient, extendedConfig)
    }
}