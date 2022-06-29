package uk.gov.justice.digital.hmpps.listener

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.config.TelemetryService
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteMessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNote
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class MessageListenerTest {

    @Mock
    private lateinit var prisonCaseNotesClient: PrisonCaseNotesClient

    @Mock
    private lateinit var deliusService: DeliusService

    @Mock
    private lateinit var telemetryService: TelemetryService

    @InjectMocks
    lateinit var messageListener: MessageListener

    @Test
    fun `unable to get case note from NOMIS`() {
        whenever(prisonCaseNotesClient.getCaseNote(any(), any())).thenThrow(ResponseStatusException::class.java)
        assertThat(messageListener.receive(CaseNoteMessageGenerator.EXISTS_IN_DELIUS), equalTo(Unit))
    }

    @Test
    fun `get case note from NOMIS was null`() {
        whenever(prisonCaseNotesClient.getCaseNote(any(), any())).thenReturn(null)
        assertThat(messageListener.receive(CaseNoteMessageGenerator.EXISTS_IN_DELIUS), equalTo(Unit))
    }

    @Test
    fun `get case note from NOMIS has blank text`() {
        val caseNote = PrisonCaseNote(
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
        whenever(prisonCaseNotesClient.getCaseNote(any(), any())).thenReturn(caseNote)
        assertThat(messageListener.receive(CaseNoteMessageGenerator.EXISTS_IN_DELIUS), equalTo(Unit))
    }
}
