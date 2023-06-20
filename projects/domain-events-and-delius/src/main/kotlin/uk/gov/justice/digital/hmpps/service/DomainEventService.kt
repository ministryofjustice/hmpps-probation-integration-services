package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEvent
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.MessageAttribute
import uk.gov.justice.digital.hmpps.integrations.delius.toSnsAttributes
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

@Service
class DomainEventService(
    @Value("\${poller.batch-size:50}")
    private val batchSize: Int,
    private val objectMapper: ObjectMapper,
    private val domainEventRepository: DomainEventRepository,
    private val notificationPublisher: NotificationPublisher
) {
    @Transactional
    fun publishBatch(): Int {
        val deltas = domainEventRepository.findAll(Pageable.ofSize(batchSize)).content
        deltas.forEach { notificationPublisher.publish(it.asNotification()) }
        domainEventRepository.deleteAllByIdInBatch(deltas.map { it.id })
        return deltas.size
    }

    fun DomainEvent.asNotification() = Notification<HmppsDomainEvent>(
        message = objectMapper.readValue(messageBody),
        attributes = try {
            objectMapper.readValue<Map<String, MessageAttribute>>(messageAttributes).toSnsAttributes()
        } catch (e: JacksonException) {
            objectMapper.readValue(messageAttributes)
        }
    )
}
