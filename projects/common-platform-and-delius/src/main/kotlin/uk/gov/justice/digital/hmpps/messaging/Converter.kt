package uk.gov.justice.digital.hmpps.messaging

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClientUtil.RESERVED_ATTRIBUTE_NAMES
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.payloadoffloading.S3AsyncDao
import software.amazon.payloadoffloading.S3BackedPayloadStoreAsync
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonTypeRef
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification

@Primary
@Component
class Converter(
    objectMapper: ObjectMapper,
    s3AsyncClient: S3AsyncClient,
    @Value("\${spring.cloud.aws.sqs.extended.bucket}") private val bucketName: String
) : NotificationConverter<CommonPlatformHearing>(objectMapper) {
    private val payloadStore = S3BackedPayloadStoreAsync(S3AsyncDao(s3AsyncClient), bucketName)

    override fun getMessageType() = CommonPlatformHearing::class

    // We need to manually unwrap the SQS message from the wrapped SNS notification, as the extended lib doesn't support this yet.
    // TODO remove this fromMessage function once https://github.com/awslabs/amazon-sqs-java-extended-client-lib/issues/175 is resolved
    override fun fromMessage(message: String): Notification<CommonPlatformHearing>? {
        val notification = objectMapper.readValue(message, jacksonTypeRef<Notification<String>>())
        return if (notification.attributes.keys.none { it in RESERVED_ATTRIBUTE_NAMES }) {
            super.fromMessage(message)
        } else {
            super.fromMessage(
                objectMapper.writeValueAsString(
                    notification.copy(
                        message = payloadStore.getOriginalPayload(notification.message).join(),
                        attributes = MessageAttributes(
                            notification.attributes.filterKeys { it !in RESERVED_ATTRIBUTE_NAMES }.toMutableMap()
                        )
                    )
                )
            )
        }
    }
}