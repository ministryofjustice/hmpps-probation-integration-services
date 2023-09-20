package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.data.generator.DomainEventGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEventRepository
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import uk.gov.justice.digital.hmpps.service.enhancement.NotificationEnhancer

@ExtendWith(MockitoExtension::class)
class DomainEventServiceTest {

    @Mock
    private lateinit var domainEventRepository: DomainEventRepository

    @Mock
    private lateinit var notificationPublisher: NotificationPublisher

    @Mock
    private lateinit var notificationEnhancer: NotificationEnhancer

    private lateinit var service: DomainEventService

    @BeforeEach
    fun setup() {
        service = DomainEventService(
            batchSize = 50,
            objectMapper = jacksonObjectMapper().findAndRegisterModules(),
            domainEventRepository = domainEventRepository,
            notificationPublisher = notificationPublisher,
            notificationEnhancer = notificationEnhancer
        )
    }

    @Test
    fun `messages can be read and published`() {
        val entities = listOf(DomainEventGenerator.generate("registration-added"))
        whenever(domainEventRepository.findAll(any<Pageable>())).thenReturn(PageImpl(entities))

        val count = service.publishBatch()

        verify(notificationPublisher).publish(argThat { eventType == "probation-case.registration.added" })
        assertThat(count, equalTo(1))
    }

    @Test
    fun `multiple messages can be published`() {
        val entities = listOf(
            DomainEventGenerator.generate("manual-ogrs"),
            DomainEventGenerator.generate("registration-added")
        )
        whenever(domainEventRepository.findAll(any<Pageable>())).thenReturn(PageImpl(entities))

        val count = service.publishBatch()

        verify(notificationPublisher, times(2)).publish(any())
        assertThat(count, equalTo(2))
    }

    @Test
    fun `nothing is published if any entity is invalid`() {
        val entities = listOf(
            DomainEventGenerator.generate("manual-ogrs"),
            DomainEventGenerator.generate("registration-added"),
            DomainEventGenerator.generate("{\"invalid-json\"}", "{\"invalid-json\"}")
        )
        whenever(domainEventRepository.findAll(any<Pageable>())).thenReturn(PageImpl(entities))

        assertThrows<JsonProcessingException> { service.publishBatch() }

        verify(notificationPublisher, never()).publish(any())
    }
}
