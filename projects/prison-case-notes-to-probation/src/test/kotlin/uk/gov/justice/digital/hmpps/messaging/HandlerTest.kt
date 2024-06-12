package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteMessageGenerator
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.StaffCodeExhaustedException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNote
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
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

    @Mock
    private lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @Mock
    private lateinit var featureFlags: FeatureFlags

    @InjectMocks
    private lateinit var handler: Handler

    @Test
    fun `when case note not found - noop`() {
        val message = prepMessage(CaseNoteMessageGenerator.NOT_FOUND).message
        whenever(prisonCaseNotesClient.getCaseNote(URI.create(message.detailUrl!!)))
            .thenReturn(null)

        assertDoesNotThrow { handler.handle(Notification(message = message)) }
        verify(telemetryService, never()).trackEvent(eq("CaseNoteMerged"), any(), any())
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

    @ParameterizedTest
    @ValueSource(
        strings = ["Alert Security. Do not share with offender and OCG Nominal - Do not share made active.",
            "Alert Security. Do not share with offender and OCG Nominal - Do not share made inactive."]
    )
    fun `when ocg alert text has been sent`(text: String) {
        val prisonCaseNote = PrisonCaseNote(
            "1",
            1L,
            "1",
            "type",
            "subType",
            creationDateTime = ZonedDateTime.now(),
            occurrenceDateTime = ZonedDateTime.now(),
            authorName = "bob",
            text = text,
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
        val prisonOffenderEvent = Notification(message = message.copy(additionalInformation = emptyMap()))
        handler.handle(prisonOffenderEvent)
        verify(deliusService, times(0)).mergeCaseNote(any())
        verify(prisonCaseNotesClient, times(0)).getCaseNote(any())
    }

    @Test
    fun `offender not found sends alert to telemetry without throwing exception`() {
        val prisonCaseNote = PrisonCaseNote(
            "1",
            1L,
            "1",
            "type",
            "subType",
            creationDateTime = ZonedDateTime.now(),
            occurrenceDateTime = ZonedDateTime.now(),
            locationId = "EXI",
            authorName = "bob",
            text = "Notes for an offender without noms number in delius",
            amendments = listOf()
        )
        val poe = Notification(prepMessage(CaseNoteMessageGenerator.NEW_TO_DELIUS).message)
        whenever(prisonCaseNotesClient.getCaseNote(URI.create(poe.message.detailUrl!!))).thenReturn(prisonCaseNote)
        whenever(deliusService.mergeCaseNote(any())).thenThrow(OffenderNotFoundException("A001"))

        handler.handle(poe)
        verify(telemetryService).trackEvent(eq("PRISON_CASE-NOTE_PUBLISHED_RECEIVED"), any(), any())
        verify(telemetryService).trackEvent(eq("CaseNoteMergeFailed"), any(), any())
    }

    @Test
    fun `non offender not found exceptions are thrown`() {
        val prisonCaseNote = PrisonCaseNote(
            "1",
            1L,
            "1",
            "type",
            "subType",
            creationDateTime = ZonedDateTime.now(),
            occurrenceDateTime = ZonedDateTime.now(),
            locationId = "EXI",
            authorName = "bob",
            text = "Notes for an exceptional case note",
            amendments = listOf()
        )
        val poe = Notification(prepMessage(CaseNoteMessageGenerator.NEW_TO_DELIUS).message)
        whenever(prisonCaseNotesClient.getCaseNote(URI.create(poe.message.detailUrl!!))).thenReturn(prisonCaseNote)
        whenever(deliusService.mergeCaseNote(any())).thenThrow(StaffCodeExhaustedException("A999"))

        assertThrows<StaffCodeExhaustedException> { handler.handle(poe) }
        verify(telemetryService).trackEvent(eq("PRISON_CASE-NOTE_PUBLISHED_RECEIVED"), any(), any())
        verify(telemetryService).trackEvent(eq("CaseNoteMergeFailed"), any(), any())
    }
}
