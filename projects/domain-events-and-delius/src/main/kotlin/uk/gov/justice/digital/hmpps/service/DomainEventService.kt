package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEvent
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEventRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import uk.gov.justice.digital.hmpps.service.enhancement.NotificationEnhancer

@Service
class DomainEventService(
    @Value("\${poller.batch-size:50}")
    private val batchSize: Int,
    private val objectMapper: ObjectMapper,
    private val domainEventRepository: DomainEventRepository,
    private val notificationPublisher: NotificationPublisher,
    private val notificationEnhancer: NotificationEnhancer
) {
    @Transactional
    fun publishBatch(): Int {
        val deltas = domainEventRepository.findAll(Pageable.ofSize(batchSize)).content
        val notifications = deltas.map { notificationEnhancer.enhance(it.asNotification()) }
        notifications.forEach { notificationPublisher.publish(it) }
        domainEventRepository.deleteAllByIdInBatch(deltas.map { it.id })
        return deltas.size
    }

    fun DomainEvent.asNotification() = Notification<HmppsDomainEvent>(
        message = objectMapper.readValue(messageBody),
        attributes = objectMapper.readValue(messageAttributes)
    )
}
