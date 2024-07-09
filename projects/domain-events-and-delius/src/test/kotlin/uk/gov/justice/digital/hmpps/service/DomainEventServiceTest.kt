package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.data.generator.DomainEventGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEventRepository
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import uk.gov.justice.digital.hmpps.service.enhancement.NotificationEnhancer
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

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
            objectMapper = jacksonObjectMapper().findAndRegisterModules(),
            domainEventRepository = domainEventRepository,
            notificationPublisher = notificationPublisher,
            notificationEnhancer = notificationEnhancer,
            telemetryService = telemetryService,
        )
    }

    @Test
    fun `multiple messages can be published`() {
        whenever(notificationEnhancer.enhance(any())).thenAnswer { it.getArgument(0) }
        service.notify(DomainEventGenerator.generate("manual-ogrs"))
        service.notify(DomainEventGenerator.generate("registration-added"))

        verify(notificationPublisher, times(2)).publish(any())
    }

    @Test
    fun `nothing is published if any entity is invalid`() {
        assertThrows<JsonProcessingException> {
            service.notify(
                DomainEventGenerator.generate(
                    "{\"invalid-json\"}",
                    "{\"invalid-json\"}"
                )
            )
        }

        verify(notificationPublisher, never()).publish(any())
    }
}
