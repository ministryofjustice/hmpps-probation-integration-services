package uk.gov.justice.digital.hmpps.integrations.delius.offender

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

@Service
class OffenderDeltaService(
    @Value("\${offender-events.batch-size:50}")
    private val batchSize: Int,
    private val repository: OffenderDeltaRepository,
    private val notificationPublisher: NotificationPublisher
) {
    @Transactional
    fun checkAndSendEvents(): Int {
        val deltas = repository.findAll(Pageable.ofSize(batchSize)).content
        deltas.asSequence()
            .flatMap { it.asNotifications() }
            .forEach { notificationPublisher.publish(it) }
        repository.deleteAllByIdInBatch(deltas.map { it.id })
        return deltas.size
    }

    fun OffenderDelta.asNotifications(): List<Notification<OffenderEvent>> {

        fun sourceToEventType(sourceTable: String, action: String): String = when (sourceTable) {
            "ALIAS" -> "OFFENDER_ALIAS_CHANGED"
            "DEREGISTRATION" -> "OFFENDER_REGISTRATION_DEREGISTERED"
            "DISPOSAL" -> "SENTENCE_CHANGED"
            "EVENT" -> "CONVICTION_CHANGED"
            "MANAGEMENT_TIER_EVENT" -> "OFFENDER_MANAGEMENT_TIER_CALCULATION_REQUIRED"
            "MERGE_HISTORY" -> "OFFENDER_MERGED"
            "OFFENDER" -> "OFFENDER_DETAILS_CHANGED"
            "OFFICER" -> "OFFENDER_OFFICER_CHANGED"
            "OGRS_ASSESSMENT" -> "OFFENDER_OGRS_ASSESSMENT_CHANGED"
            "REGISTRATION" -> if ("DELETE" == action) "OFFENDER_REGISTRATION_DELETED" else "OFFENDER_REGISTRATION_CHANGED"
            "RQMNT" -> "SENTENCE_ORDER_REQUIREMENT_CHANGED"
            else -> "${sourceTable}_CHANGED"
        }

        val oe = OffenderEvent(offender.id, offender.crn, offender.nomsNumber, sourceRecordId, dateChanged)
        val list: MutableList<Notification<OffenderEvent>> = mutableListOf()
        if (sourceTable in listOf("ALIAS", "OFFENDER", "OFFENDER_MANAGER", "OFFENDER_ADDRESS", "OFFICER")) {
            list += Notification(oe, MessageAttributes("OFFENDER_CHANGED"))
        }
        list += Notification(oe, MessageAttributes(sourceToEventType(sourceTable, action)))
        return list
    }
}
