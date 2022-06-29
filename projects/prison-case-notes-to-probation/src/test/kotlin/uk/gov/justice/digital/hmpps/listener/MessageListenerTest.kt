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
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.config.TelemetryService
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteMessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient

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
}
