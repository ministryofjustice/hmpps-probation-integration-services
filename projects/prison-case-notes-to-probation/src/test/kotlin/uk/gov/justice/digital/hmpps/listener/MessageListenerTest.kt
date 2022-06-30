package uk.gov.justice.digital.hmpps.listener

import feign.FeignException
import feign.Request
import feign.Request.Body
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.config.TelemetryService
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteMessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNote
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonOffenderEvent
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
    fun `feign exceptions are propogated`() {
        whenever(
            prisonCaseNotesClient.getCaseNote(
                CaseNoteMessageGenerator.NOT_FOUND.offenderId,
                CaseNoteMessageGenerator.NOT_FOUND.caseNoteId!!
            )
        ).thenThrow(
            FeignException.NotFound(
                "Error Message",
                Request.create(Request.HttpMethod.GET, "localhost:8500", mapOf(), null as Body?, null),
                null, mapOf()
            )
        )

        assertThrows<FeignException.NotFound> { messageListener.receive(CaseNoteMessageGenerator.NOT_FOUND) }
    }

    @Test
    fun `get case note from NOMIS has blank text`() {
        val prisonCaseNote = PrisonCaseNote(
            1L,
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
        messageListener.receive(CaseNoteMessageGenerator.EXISTS_IN_DELIUS)
        verify(deliusService, times(0)).mergeCaseNote(any())
    }

    @Test
    fun `get case note from NOMIS has null caseNoteId`() {
        val prisonOffenderEvent = PrisonOffenderEvent("1", null, 0, "type")
        messageListener.receive(prisonOffenderEvent)
        verify(deliusService, times(0)).mergeCaseNote(any())
        verify(prisonCaseNotesClient, times(0)).getCaseNote(any(), any())
    }
}
