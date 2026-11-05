package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.DigestUtils
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.CommonPlatformHearing
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.*
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.TimeUnit

internal class LargeHearingMessageIntegrationTest @Autowired constructor(
    private val sqsAsyncClient: SqsAsyncClient,
    private val objectMapper: ObjectMapper,
    @Value($$"${spring.cloud.aws.sqs.extended.bucket}") private val bucketName: String
) : BaseIntegrationTest() {
    @Test
    fun `An S3-backed hearing larger than 1 MiB is retrieved and processed`() {
        // Given a large hearing message
        val hearing = MessageGenerator.COMMON_PLATFORM_EVENT.hearing
        val largeHearing = CommonPlatformHearing(
            hearing.copy(prosecutionCases = hearing.prosecutionCases.map { prosecutionCase ->
                prosecutionCase.copy(caseMarkers = List(12_000) {
                    mapOf(
                        "type" to "Example case marker",
                        "description" to "Additional case marker data for a large Common Platform hearing"
                    )
                })
            })
        )
        val payload = objectMapper.writeValueAsString(
            Notification(message = objectMapper.writeValueAsString(largeHearing))
        )
        // With message content in S3
        val getRequest = GetObjectRequest.builder().bucket(bucketName).key("message.json").build()
        mockS3GetObjectRequest(getRequest, payload)

        // When the message is received
        sendLargeMessage(payload)

        // Then a call is made to fetch the data from S3
        verify(s3AsyncClient).getObject(
            eq(getRequest),
            any<AsyncResponseTransformer<GetObjectResponse, ResponseBytes<GetObjectResponse>>>()
        )
    }

    private fun mockS3GetObjectRequest(request: GetObjectRequest, payload: String) {
        whenever(
            s3AsyncClient.getObject(
                eq(request),
                any<AsyncResponseTransformer<GetObjectResponse, ResponseBytes<GetObjectResponse>>>()
            )
        ).thenReturn(
            completedFuture(
                ResponseBytes.fromByteArray(
                    GetObjectResponse.builder().build(),
                    payload.toByteArray(Charsets.UTF_8)
                )
            )
        )
    }

    private fun sendLargeMessage(payload: String) {
        val payloadSize = payload.toByteArray(Charsets.UTF_8).size
        assertTrue(payloadSize > 1024 * 1024, "The hearing must exceed the SQS 1 MiB message limit")

        val queueUrl = "${wireMockServer.baseUrl()}/000000000000/$queueName"
        val receiptHandle = "large-message-receipt-handle"
        val pointer = objectMapper.writeValueAsString(
            listOf(
                "software.amazon.payloadoffloading.PayloadS3Pointer",
                mapOf("s3BucketName" to bucketName, "s3Key" to "message.json")
            )
        )
        wireMockServer.stubFor(
            post(anyUrl())
                .withHeader("X-Amz-Target", equalTo("AmazonSQS.ReceiveMessage"))
                .willReturn(
                    okJson(
                        """
                        {
                          "Messages": [{
                            "MessageId": "${UUID.randomUUID()}",
                            "ReceiptHandle": "$receiptHandle",
                            "Body": ${objectMapper.writeValueAsString(pointer)},
                            "MD5OfBody": "${DigestUtils.md5DigestAsHex(pointer.toByteArray(Charsets.UTF_8))}",
                            "MD5OfMessageAttributes": "${largePayloadAttributeChecksum(payloadSize)}",
                            "MessageAttributes": {
                              "ExtendedPayloadSize": {"DataType": "Number", "StringValue": "${payloadSize}"}
                            }
                          }]
                        }
                        """.trimIndent()
                    )
                )
        )
        val message = sqsAsyncClient.receiveMessage {
            it.queueUrl(queueUrl).maxNumberOfMessages(1).waitTimeSeconds(0)
        }.get(5, TimeUnit.SECONDS).messages().single()
        assertEquals(payload, message.body())
    }

    private fun largePayloadAttributeChecksum(payloadSize: Int): String {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            fun writeString(value: String) {
                val encoded = value.toByteArray(Charsets.UTF_8)
                output.writeInt(encoded.size)
                output.write(encoded)
            }
            writeString("ExtendedPayloadSize")
            writeString("Number")
            output.writeByte(1) // SQS uses the string transport type for Number attributes.
            writeString(payloadSize.toString())
        }
        return DigestUtils.md5DigestAsHex(bytes.toByteArray())
    }
}
