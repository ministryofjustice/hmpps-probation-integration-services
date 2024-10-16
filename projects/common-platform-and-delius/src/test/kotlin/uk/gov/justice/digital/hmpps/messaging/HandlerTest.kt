package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.integrations.client.*
import uk.gov.justice.digital.hmpps.integrations.delius.entity.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var converter: NotificationConverter<CommonPlatformHearing>

    @Mock
    lateinit var personService: PersonService

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var probationSearchClient: ProbationSearchClient

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `message is logged to telemetry`() {
        whenever(probationSearchClient.match(any())).thenReturn(ProbationMatchResponse(matches = emptyList(), matchedBy = "NONE"))
        whenever(personService.generateCrn()).thenReturn("A000001")
        whenever(referenceDataRepository.findByCode("M")).thenReturn(ReferenceDataGenerator.GENDER_MALE)
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
    }

    @Test
    fun `exception thrown when age is under 10 years old`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_VALIDATION_ERROR)
        val exception = assertThrows<IllegalArgumentException> {
            handler.handle(notification)
        }
        assert(exception.message!!.contains("Date of birth would indicate person is under ten years old"))
    }

    @Test
    fun `exception thrown prosecution cases is empty`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NO_CASES)
        val exception = assertThrows<IllegalArgumentException> {
            handler.handle(notification)
        }
        assert(exception.message!!.contains("No prosecution cases found"))
    }
}
