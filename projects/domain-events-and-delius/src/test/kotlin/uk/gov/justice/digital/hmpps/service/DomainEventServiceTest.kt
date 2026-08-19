package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order.asc
import tools.jackson.core.JacksonException
import uk.gov.justice.digital.hmpps.data.generator.DomainEventGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEventRepository
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import uk.gov.justice.digital.hmpps.service.enhancement.NotificationEnhancer
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.objectMapper

@ExtendWith(MockitoExtension::class)
class DomainEventServiceTest {

    @Mock
    private lateinit var domainEventRepository: DomainEventRepository

    @Mock
    private lateinit var notificationPublisher: NotificationPublisher

    @Mock
    private lateinit var notificationEnhancer: NotificationEnhancer

    @Mock
    private lateinit var telemetryService: TelemetryService

    private lateinit var service: DomainEventService

    @BeforeEach
    fun setup() {
        service = DomainEventService(
            batchSize = 50,
            objectMapper = objectMapper,
            domainEventRepository = domainEventRepository,
            notificationPublisher = notificationPublisher,
            notificationEnhancer = notificationEnhancer,
            telemetryService = telemetryService,
        )
    }

    @Test
    fun `messages are retrieved in ascending ID order`() {
        whenever(domainEventRepository.findAll(any<PageRequest>())).thenReturn(PageImpl(emptyList()))

        service.getDeltas()

        verify(domainEventRepository).findAll(PageRequest.of(0, 50, Sort.by(asc("id"))))
    }

    @Test
    fun `multiple messages can be published`() {
        whenever(notificationEnhancer.enhance(any())).thenAnswer { it.getArgument(0) }
        service.publishAll(
            listOf(
                DomainEventGenerator.generate("manual-ogrs"),
                DomainEventGenerator.generate("registration-added")
            )
        )

        verify(notificationPublisher, times(2)).publish(check {
            assertThat(it.attributes["eventSource"]?.value).isEqualTo("delius")
        })
    }

    @Test
    fun `nothing is published if any entity is invalid`() {
        assertThrows<JacksonException> {
            service.publishAll(
                listOf(
                    DomainEventGenerator.generate("manual-ogrs"),
                    DomainEventGenerator.generate(
                        """{"invalid-json"}""",
                        """{"invalid-json"}""",
                    )
                )
            )
        }

        verify(notificationPublisher, never()).publish(any())
    }
}
