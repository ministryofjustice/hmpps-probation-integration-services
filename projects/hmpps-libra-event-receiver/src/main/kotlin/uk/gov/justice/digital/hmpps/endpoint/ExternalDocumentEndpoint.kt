package uk.gov.justice.digital.hmpps.endpoint

import com.asyncapi.kotlinasyncapi.context.annotation.processor.MessageProcessor
import jakarta.annotation.PostConstruct
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.ws.server.endpoint.annotation.Endpoint
import org.springframework.ws.server.endpoint.annotation.PayloadRoot
import org.springframework.ws.server.endpoint.annotation.RequestPayload
import org.springframework.ws.server.endpoint.annotation.ResponsePayload
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.xml.DocumentUtils
import uk.gov.justice.magistrates.ack.AckType
import uk.gov.justice.magistrates.ack.Acknowledgement
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

@Endpoint
class ExternalDocumentEndpoint(
    @Value("#{'\${included-court-codes}'.split(',')}") private val includedCourts: Set<String>,
    @Value("\${min-dummy-court-room:50}") private val minDummyCourtRoom: Int,
    private val telemetryService: TelemetryService,
    // private val s3Service: S3Service,
    private val jaxbContext: JAXBContext,
    // private val validationSchema: Schema?,
    private val messageProcessor: MessageProcessor,
) {
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = REQUEST_LOCAL_NAME)
    @ResponsePayload
    fun processPayloadRootRequest(@RequestPayload request: ExternalDocumentRequest): Acknowledgement {
        return process(request)
    }

    @SoapAction("externalDocument")
    @ResponsePayload
    fun processRequestExternalDocument(@RequestPayload request: ExternalDocumentRequest): Acknowledgement {
        return process(request)
    }

    @SoapAction("")
    @ResponsePayload
    fun processRequest(@RequestPayload request: ExternalDocumentRequest): Acknowledgement {
        return process(request)
    }

    private fun process(request: ExternalDocumentRequest): Acknowledgement {
        CompletableFuture.supplyAsync<Any> { enqueueMessage(request) }
        return Acknowledgement().apply {
            ack = AckType().apply {
                messageComment = "MessageComment"
                messageStatus = SUCCESS_MESSAGE_STATUS
                timeStamp = LocalDateTime.now()
            }
        }
    }

    fun enqueueMessage(request: ExternalDocumentRequest) {
        val fileName = request.documents?.any?.let { DocumentUtils.getFileName(it) }

        val messageDetail =
            request.documents?.any?.let { DocumentUtils.getMessageDetail(it) }
                ?: kotlin.run {
                    val failedFileName =
                        fileName ?: "fail-" + DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())
                    s3Service.uploadMessage("$failedFileName.xml", marshal(request, validate = false))
                    return
                }

        val messageContent = marshal(request)
        s3Service.uploadMessage(messageDetail, messageContent)

        if (includedCourts.contains(messageDetail.courtCode) && messageDetail.courtRoom < minDummyCourtRoom) {
            messageProcessor.process(messageContent)
        }
    }

    private fun marshal(
        request: ExternalDocumentRequest,
        validate: Boolean = true,
    ): String {
        val marshaller: Marshaller = jaxbContext.createMarshaller()
        if (validate) {
            validationSchema?.let { marshaller.schema = it }
        }
        val sw = StringWriter()
        marshaller.marshal(request, sw)
        return sw.toString()
    }

    companion object {
        const val SUCCESS_MESSAGE_STATUS = "Success"
        const val NAMESPACE_URI = "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest"
        const val REQUEST_LOCAL_NAME = "ExternalDocumentRequest"
    }
}