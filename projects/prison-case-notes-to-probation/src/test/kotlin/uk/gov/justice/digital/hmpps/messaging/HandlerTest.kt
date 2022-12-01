package uk.gov.justice.digital.hmpps.messaging

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
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.prepMessage
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {

    @Mock
    private lateinit var prisonCaseNotesClient: PrisonCaseNotesClient

    @Mock
    private lateinit var deliusService: DeliusService

    @Mock
    private lateinit var telemetryService: TelemetryService

    @InjectMocks
    private lateinit var handler: Handler

    @Test
    fun `when case note not found - noop`() {
        val message = prepMessage(CaseNoteMessageGenerator.NOT_FOUND).message
        whenever(prisonCaseNotesClient.getCaseNote(URI.create(message.detailUrl!!)))
            .thenReturn(null)

        assertDoesNotThrow { handler.handle(Notification(message = message)) }
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
        val message = prepMessage(CaseNoteMessageGenerator.EXISTS_IN_DELIUS).message
        whenever(prisonCaseNotesClient.getCaseNote(URI.create(message.detailUrl!!))).thenReturn(prisonCaseNote)
        handler.handle(Notification(message = message))
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
        val message = prepMessage(CaseNoteMessageGenerator.EXISTS_IN_DELIUS).message
        whenever(prisonCaseNotesClient.getCaseNote(URI.create(message.detailUrl!!))).thenReturn(prisonCaseNote)

        handler.handle(Notification(message = message))
        verify(deliusService, never()).mergeCaseNote(any())
        verify(telemetryService).trackEvent(eq("CaseNoteIgnored"), any(), any())
    }

    @Test
    fun `get case note from NOMIS has null caseNoteId`() {
        val message = prepMessage(CaseNoteMessageGenerator.EXISTS_IN_DELIUS).message
        val prisonOffenderEvent = Notification(message = message.copy(additionalInformation = AdditionalInformation()))
        handler.handle(prisonOffenderEvent)
        verify(deliusService, times(0)).mergeCaseNote(any())
        verify(prisonCaseNotesClient, times(0)).getCaseNote(any())
    }
}
