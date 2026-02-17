package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEvent
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.objectMapper
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class DomainEventServiceTest {

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var domainEventRepository: DomainEventRepository

    @Mock
    lateinit var telemetryService: TelemetryService

    private lateinit var domainEventService: DomainEventService

    private val occurredAt = ZonedDateTime.parse("2025-12-15T17:18:30Z")

    @BeforeEach
    fun setUp() {
        domainEventService = DomainEventService(
            objectMapper = objectMapper,
            referenceDataRepository = referenceDataRepository,
            domainEventRepository = domainEventRepository,
            telemetryService = telemetryService
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

        doAnswer { it.arguments[0] }.whenever(domainEventRepository).save(any())

        val contactId = id()
        // when
        domainEventService.publishContactUpdated(
            crn = "X972222",
            contactId = contactId,
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
            body["eventType"].asString()
        )
        assertEquals(
            "MAPPA information has been updated in NDelius",
            body["description"].asString()
        )
        assertEquals(
            "X972222",
            body["personReference"]["identifiers"][0]["value"].asString()
        )
        assertEquals(
            contactId,
            body["additionalInformation"]["contactId"].asLong()
        )
        assertEquals(
            0,
            body["additionalInformation"]["mappa"]["category"].asInt()
        )

        val attributes = objectMapper.readTree(saved.messageAttributes)
        assertEquals(
            "probation-case.mappa-information.updated",
            attributes["eventType"]["Value"].asString()
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

        doAnswer { it.arguments[0] }.whenever(domainEventRepository).save(any())

        val contactId = id()

        // when
        domainEventService.publishContactDeleted(
            crn = "X972222",
            contactId = contactId,
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
            body["eventType"].asString()
        )
        assertEquals(
            "MAPPA information has been deleted in NDelius",
            body["description"].asString()
        )
        assertEquals(
            contactId,
            body["additionalInformation"]["contactId"].asLong()
        )
        assertEquals(
            4,
            body["additionalInformation"]["mappa"]["category"].asInt()
        )
    }
}
