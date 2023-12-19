package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEvent
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes

@Service
class DomainEventService(
    private val objectMapper: ObjectMapper,
    private val domainEventRepository: DomainEventRepository
) {
    fun publishEvents(hmppsDomainEvents: List<HmppsDomainEvent>) = domainEventRepository.saveAll(
        hmppsDomainEvents.map {
            DomainEvent(
                objectMapper.writeValueAsString(it),
                objectMapper.writeValueAsString(MessageAttributes(it.eventType))
            )
        }
    )
}
