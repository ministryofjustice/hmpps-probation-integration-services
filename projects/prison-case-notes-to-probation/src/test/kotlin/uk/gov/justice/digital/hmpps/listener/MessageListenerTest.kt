package uk.gov.justice.digital.hmpps.listener

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteMessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNote
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonOffenderEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class MessageListenerTest {

    @Mock
    private lateinit var prisonCaseNotesClient: PrisonCaseNotesClient

    @Mock
    private lateinit var deliusService: DeliusService

    @Mock
    private lateinit var telemetryService: TelemetryService

    @InjectMocks
    private lateinit var messageListener: MessageListener

    @Test
    fun `when case note not found - noop`() {
        whenever(
            prisonCaseNotesClient.getCaseNote(
                CaseNoteMessageGenerator.NOT_FOUND.offenderId,
                CaseNoteMessageGenerator.NOT_FOUND.caseNoteId!!
            )
        ).thenReturn(null)

        assertDoesNotThrow { messageListener.receive(Notification(message = CaseNoteMessageGenerator.NOT_FOUND)) }
        verify(telemetryService, never()).trackEvent(eq("CaseNoteMerge"), any(), any())
        verify(deliusService, never()).mergeCaseNote(any())
    }

    @Test
    fun `get case note from NOMIS has blank text`() {
        val prisonCaseNote = PrisonCaseNote(
            "1",
            1L,
            "1",
            "type",
            "subType",
            creationDateTime = ZonedDateTime.now(),
            occurrenceDateTime = ZonedDateTime.now(),
            authorName = "bob",
            text = "",
            amendments = listOf()
        )
        whenever(
            prisonCaseNotesClient.getCaseNote(
                CaseNoteMessageGenerator.EXISTS_IN_DELIUS.offenderId,
                CaseNoteMessageGenerator.EXISTS_IN_DELIUS.caseNoteId!!
            )
        ).thenReturn(prisonCaseNote)
        messageListener.receive(Notification(message = CaseNoteMessageGenerator.EXISTS_IN_DELIUS))
        verify(deliusService, times(0)).mergeCaseNote(any())
    }

    @Test
    fun `when prisoner being transferred noop`() {
        val prisonCaseNote = PrisonCaseNote(
            "1",
            1L,
            "1",
            "type",
            "subType",
            creationDateTime = ZonedDateTime.now(),
            occurrenceDateTime = ZonedDateTime.now(),
            locationId = "TRN",
            authorName = "bob",
            text = "Prisoner being transferred",
            amendments = listOf()
        )
        whenever(
            prisonCaseNotesClient.getCaseNote(
                CaseNoteMessageGenerator.EXISTS_IN_DELIUS.offenderId,
                CaseNoteMessageGenerator.EXISTS_IN_DELIUS.caseNoteId!!
            )
        ).thenReturn(prisonCaseNote)

        messageListener.receive(Notification(message = CaseNoteMessageGenerator.EXISTS_IN_DELIUS))
        verify(deliusService, never()).mergeCaseNote(any())
        verify(telemetryService).trackEvent(eq("CaseNoteIgnored"), any(), any())
    }

    @Test
    fun `get case note from NOMIS has null caseNoteId`() {
        val prisonOffenderEvent = Notification(message = PrisonOffenderEvent("1", null, 0))
        messageListener.receive(prisonOffenderEvent)
        verify(deliusService, times(0)).mergeCaseNote(any())
        verify(prisonCaseNotesClient, times(0)).getCaseNote(any(), any())
    }
}
