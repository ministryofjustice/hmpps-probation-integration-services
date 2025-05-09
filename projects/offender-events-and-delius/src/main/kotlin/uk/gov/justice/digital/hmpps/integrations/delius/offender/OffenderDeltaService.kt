package uk.gov.justice.digital.hmpps.integrations.delius.offender

import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import java.time.temporal.ChronoUnit

@Service
class OffenderDeltaService(
    @Value("\${offender-events.batch-size:50}")
    private val batchSize: Int,
    private val repository: OffenderDeltaRepository,
    private val contactRepository: ContactRepository,
    private val notificationPublisher: NotificationPublisher,
    private val telemetryService: TelemetryService
) {
    fun getDeltas(): List<OffenderDelta> = repository.findAll(Pageable.ofSize(batchSize)).content

    fun deleteAll(deltas: List<OffenderDelta>) = repository.deleteAllByIdInBatch(deltas.map { it.id })

    @WithSpan("POLL offender_delta", kind = SpanKind.SERVER)
    fun notify(delta: OffenderDelta) {
        delta.asNotifications().forEach {
            notificationPublisher.publish(it)
            telemetryService.trackEvent(
                "OffenderEventPublished",
                mapOf(
                    "crn" to it.message.crn,
                    "eventType" to it.eventType!!,
                    "occurredAt" to ISO_ZONED_DATE_TIME.format(it.message.eventDatetime),
                    "notification" to it.toString(),
                )
            )
        }
    }

    fun OffenderDelta.asNotifications(): List<Notification<OffenderEvent>> {
        fun sourceToEventType(): String? = when (sourceTable) {
            "ALIAS" -> "OFFENDER_ALIAS_CHANGED"
            "CONTACT" -> if (contactRepository.findById(sourceRecordId).isEmpty) "CONTACT_DELETED" else "CONTACT_CHANGED"
            "DEREGISTRATION" -> "OFFENDER_REGISTRATION_DEREGISTERED"
            "DISPOSAL" -> "SENTENCE_CHANGED"
            "EVENT" -> "CONVICTION_CHANGED"
            "MANAGEMENT_TIER_EVENT" -> null
            "MERGE_HISTORY" -> "OFFENDER_MERGED"
            "OFFENDER" -> "OFFENDER_DETAILS_CHANGED"
            "OFFICER" -> "OFFENDER_OFFICER_CHANGED"
            "OGRS_ASSESSMENT" -> "OFFENDER_OGRS_ASSESSMENT_CHANGED"
            "REGISTRATION" -> if ("DELETE" == action) "OFFENDER_REGISTRATION_DELETED" else "OFFENDER_REGISTRATION_CHANGED"
            "RQMNT" -> "SENTENCE_ORDER_REQUIREMENT_CHANGED"
            else -> "${sourceTable}_CHANGED"
        }

        return if (offender != null) {
            sourceToEventType()?.let {
                val oe = OffenderEvent(
                    offender.id,
                    offender.crn,
                    offender.nomsNumber,
                    sourceRecordId,
                    dateChanged.truncatedTo(ChronoUnit.SECONDS)
                )
                val list: MutableList<Notification<OffenderEvent>> = mutableListOf()
                if (sourceTable in listOf("ALIAS", "OFFENDER", "OFFENDER_MANAGER", "OFFENDER_ADDRESS", "OFFICER")) {
                    list += Notification(oe, MessageAttributes("OFFENDER_CHANGED"))
                }
                list += Notification(oe, MessageAttributes(it))
                list
            } ?: emptyList()
        } else {
            emptyList()
        }
    }
}
