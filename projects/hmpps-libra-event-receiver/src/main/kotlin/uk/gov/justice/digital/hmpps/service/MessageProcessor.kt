package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.core.JsonProcessingException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.crimeportalgateway.messaging.MessageNotifier
import uk.gov.justice.digital.hmpps.crimeportalgateway.messaging.MessageParser
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest

@Service
class MessageProcessor(
    private val messageParser: MessageParser<ExternalDocumentRequest>,
    private val messageNotifier: MessageNotifier,
    private val telemetryService: TelemetryService,
) {
    @Throws(JsonProcessingException::class)
    fun process(message: String) {
        val externalDocumentRequest = messageParser.parseMessage(message, ExternalDocumentRequest::class.java)

        val documents = externalDocumentRequest.documentWrapper.document
        trackCourtListReceipt(documents)

        return documents
            .stream()
            .flatMap { document: Document ->
                document.data.job.sessions
                    .stream()
            }.flatMap { session: Session ->
                session.blocks.stream()
            }.flatMap { block: Block ->
                block.cases.stream()
            }.forEach {
                log.debug("Sending {}", it.caseNo)
                messageNotifier.send(it)
            }
    }

    private fun trackCourtListReceipt(documents: List<Document>) {
        documents
            .stream()
            .map { it.info }
            .distinct()
            .forEach { info: Info ->
                run {
                    log.debug("Track court list event $info")
                    telemetryService.trackCourtListEvent(info)
                }
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
