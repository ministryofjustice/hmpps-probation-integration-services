package uk.gov.justice.digital.hmpps.integrations.delius.offender

import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.service.DomainEventService
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
    private val offenderDeltaRepository: OffenderDeltaRepository,
    private val contactRepository: ContactRepository,
    private val notificationPublisher: NotificationPublisher,
    private val telemetryService: TelemetryService,
    private val domainEventService: DomainEventService,
    private val registrationRepository: RegistrationRepository,
) {
    fun getDeltas(): List<OffenderDelta> = offenderDeltaRepository.findAll(Pageable.ofSize(batchSize)).content

    fun deleteAll(deltas: List<OffenderDelta>) = offenderDeltaRepository.deleteAllByIdInBatch(deltas.map { it.id })

    @WithSpan("POLL offender_delta", kind = SpanKind.SERVER)
    internal fun notify(notification: Notification<OffenderEvent>) {
        notificationPublisher.publish(notification)
        telemetryService.trackEvent(
            "OffenderEventPublished",
            mapOf(
                "crn" to notification.message.crn,
                "eventType" to notification.eventType!!,
                "occurredAt" to ISO_ZONED_DATE_TIME.format(notification.message.eventDatetime),
                "notification" to notification.toString(),
            )
        )
    }

    internal fun prepare(delta: OffenderDelta): List<Notification<OffenderEvent>> =
        delta
            .also(::handleDomainEvents)
            .asNotifications()

    fun OffenderDelta.asNotifications(): List<Notification<OffenderEvent>> {
        fun sourceToEventType(): String? = when (sourceTable) {
            "ALIAS" -> "OFFENDER_ALIAS_CHANGED"
            "CONTACT" -> if (!contactRepository.existsByIdAndSoftDeletedFalse(sourceRecordId)) "CONTACT_DELETED" else "CONTACT_CHANGED"
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

    private fun handleDomainEvents(delta: OffenderDelta) {
        if (!isContactDomainEventCandidate(delta)) return

        val offender = delta.offender ?: return

        val contactId = delta.sourceRecordId

        // ignore hard-deleted contacts or non-visor contacts
        if (!contactRepository.existsByIdAndVisorContactTrue(contactId)) return

        val category = resolveMappaCategory(offender.id)

        if (!contactRepository.existsByIdAndSoftDeletedFalse(contactId)) {
            domainEventService.publishContactDeleted(
                crn = offender.crn,
                contactId = contactId,
                category = category,
                occurredAt = delta.dateChanged
            )
        } else {
            domainEventService.publishContactUpdated(
                crn = offender.crn,
                contactId = contactId,
                category = category,
                occurredAt = delta.dateChanged
            )
        }
    }

    private fun isContactDomainEventCandidate(delta: OffenderDelta): Boolean =
        delta.sourceTable == "CONTACT"

    private fun resolveMappaCategory(offenderId: Long): Int {
        val registration = registrationRepository
            .findByPersonIdAndTypeCodeOrderByIdDesc(
                offenderId,
                RegisterType.Code.MAPPA.value
            )
            .firstOrNull()

        return when (registration?.category?.code) {
            "M1" -> 1
            "M2" -> 2
            "M3" -> 3
            "M4" -> 4
            else -> 0
        }
    }
}
