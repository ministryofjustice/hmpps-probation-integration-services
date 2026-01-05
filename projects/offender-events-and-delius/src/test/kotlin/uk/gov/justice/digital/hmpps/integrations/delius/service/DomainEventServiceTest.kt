package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEvent
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.objectMapper
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class DomainEventServiceTest {

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var domainEventRepository: DomainEventRepository

    private lateinit var domainEventService: DomainEventService

    private val occurredAt = ZonedDateTime.parse("2025-12-15T17:18:30Z")

    @BeforeEach
    fun setUp() {
        domainEventService = DomainEventService(
            objectMapper = objectMapper,
            referenceDataRepository = referenceDataRepository,
            domainEventRepository = domainEventRepository
        )
    }

    @Test
    fun `publishContactUpdated persists MAPPA updated domain event`() {
        // given
        val eventTypeRef = mock<ReferenceData>()

        whenever(
            referenceDataRepository.findByCode(
                "probation-case.mappa-information.updated",
                "DOMAIN EVENT TYPE"
            )
        ).thenReturn(eventTypeRef)

        // when
        domainEventService.publishContactUpdated(
            crn = "X972222",
            contactId = 2510101288L,
            category = 0,
            occurredAt = occurredAt
        )

        // then
        val captor = ArgumentCaptor.forClass(DomainEvent::class.java)
        verify(domainEventRepository).save(captor.capture())

        val saved = captor.value
        assertEquals(eventTypeRef, saved.type)

        val body = objectMapper.readTree(saved.messageBody)

        assertEquals(
            "probation-case.mappa-information.updated",
            body["eventType"].asText()
        )
        assertEquals(
            "MAPPS information has been updated in NDelius",
            body["description"].asText()
        )
        assertEquals(
            "X972222",
            body["personReference"]["identifiers"][0]["value"].asText()
        )
        assertEquals(
            2510101288L,
            body["additionalInformation"]["contactId"].asLong()
        )
        assertEquals(
            0,
            body["additionalInformation"]["mapps"]["category"].asInt()
        )

        val attributes = objectMapper.readTree(saved.messageAttributes)
        assertEquals(
            "probation-case.mappa-information.updated",
            attributes["eventType"]["Value"].asText()
        )
    }

    @Test
    fun `publishContactDeleted persists MAPPA deleted domain event including previousCrn`() {
        // given
        val eventTypeRef = mock<ReferenceData>()

        whenever(
            referenceDataRepository.findByCode(
                "probation-case.mappa-information.deleted",
                "DOMAIN EVENT TYPE"
            )
        ).thenReturn(eventTypeRef)

        // when
        domainEventService.publishContactDeleted(
            crn = "X972222",
            contactId = 2510101297L,
            category = 4,
            occurredAt = occurredAt
        )

        // then
        val captor = ArgumentCaptor.forClass(DomainEvent::class.java)
        verify(domainEventRepository).save(captor.capture())

        val saved = captor.value
        assertEquals(eventTypeRef, saved.type)

        val body = objectMapper.readTree(saved.messageBody)

        assertEquals(
            "probation-case.mappa-information.deleted",
            body["eventType"].asText()
        )
        assertEquals(
            "MAPPS information has been deleted in NDelius",
            body["description"].asText()
        )
        assertEquals(
            2510101297L,
            body["additionalInformation"]["contactId"].asLong()
        )
        assertEquals(
            4,
            body["additionalInformation"]["mapps"]["category"].asInt()
        )
    }
}
