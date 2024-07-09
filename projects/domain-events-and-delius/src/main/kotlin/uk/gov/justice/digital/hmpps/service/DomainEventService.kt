package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEvent
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEventRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import uk.gov.justice.digital.hmpps.service.enhancement.NotificationEnhancer
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

@Service
class DomainEventService(
    @Value("\${poller.batch-size:50}")
    private val batchSize: Int,
    private val objectMapper: ObjectMapper,
    private val domainEventRepository: DomainEventRepository,
    private val notificationPublisher: NotificationPublisher,
    private val notificationEnhancer: NotificationEnhancer,
    private val telemetryService: TelemetryService
) {
    fun getDeltas(): List<DomainEvent> = domainEventRepository.findAll(Pageable.ofSize(batchSize)).content

    fun deleteAll(deltas: List<DomainEvent>) = domainEventRepository.deleteAllByIdInBatch(deltas.map { it.id })

    @WithSpan
    fun notify(delta: DomainEvent) {
        val notification = notificationEnhancer.enhance(delta.asNotification())
        notificationPublisher.publish(notification)
        telemetryService.trackEvent(
            "DomainEventPublished",
            mapOf(
                "crn" to notification.message.personReference.findCrn().toString(),
                "eventType" to notification.eventType.toString(),
                "occurredAt" to ISO_ZONED_DATE_TIME.format(notification.message.occurredAt)
            )
        )
    }

    fun DomainEvent.asNotification() = Notification<HmppsDomainEvent>(
        message = objectMapper.readValue(messageBody),
        attributes = objectMapper.readValue(messageAttributes)
    )
}
