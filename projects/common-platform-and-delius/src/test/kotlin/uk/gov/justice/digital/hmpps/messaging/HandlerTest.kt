package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonAddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.integrations.client.*
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Equality
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.InsertPersonResult
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var converter: NotificationConverter<CommonPlatformHearing>

    @Mock
    lateinit var personService: PersonService

    @Mock
    lateinit var probationSearchClient: ProbationSearchClient

    @Mock
    lateinit var notifier: Notifier

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `inserts records when probation search match is not found`() {
        whenever(probationSearchClient.match(any())).thenReturn(
            ProbationMatchResponse(
                matches = emptyList(),
                matchedBy = "NONE"
            )
        )
        whenever(personService.insertPerson(any(), any())).thenReturn(
            InsertPersonResult(
                person = PersonGenerator.DEFAULT,
                personManager = PersonManagerGenerator.DEFAULT,
                equality = Equality(id = 1L, personId = 1L, softDeleted = false),
                address = PersonAddressGenerator.MAIN_ADDRESS,
            )
        )

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(personService).insertPerson(any(), any())
        verify(notifier).caseCreated(any())
        verify(notifier).addressCreated(any())
    }

    @Test
    fun `does not insert person or address when match is found`() {
        val fakeMatchResponse = ProbationMatchResponse(
            matches = listOf(
                OffenderMatch(
                    offender = OffenderDetail(
                        otherIds = IDs(crn = "X123456", pncNumber = "00000000000Z"),
                        firstName = "Name",
                        surname = "Name",
                        dateOfBirth = LocalDate.of(1980, 1, 1)
                    )
                )
            ),
            matchedBy = "PNC"
        )

        whenever(probationSearchClient.match(any())).thenReturn(fakeMatchResponse)

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(personService, never()).insertPerson(any(), any())
        verify(notifier, never()).caseCreated(any())
        verify(notifier, never()).addressCreated(any())
    }

    @Test
    fun `When defendants with remanded in custody are not found then no inserts occur`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NO_REMAND)

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(personService, never()).insertPerson(any(), any())
        verify(notifier, never()).caseCreated(any())
        verify(notifier, never()).addressCreated(any())
    }

    @Test
    fun `When a defendant is missing name or dob then records are not inserted and probation-search is not performed`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NULL_FIELDS)

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(probationSearchClient, never()).match(any())
        verify(personService, never()).insertPerson(any(), any())
        verify(notifier, never()).caseCreated(any())
        verify(notifier, never()).addressCreated(any())
    }
}
