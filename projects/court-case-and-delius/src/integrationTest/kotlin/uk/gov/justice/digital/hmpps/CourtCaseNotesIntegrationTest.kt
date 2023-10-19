package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.CourtCaseNoteMessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

const val COURT_CASE_NOTE_MERGED = "CourtCaseNoteMerged"

@SpringBootTest
class CaseNotesIntegrationTest {

    @Value("\${messaging.consumer.queue}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

    @Autowired
    private lateinit var caseNoteRepository: CaseNoteRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @SpyBean
    lateinit var air: AuditedInteractionRepository

    @Test
    fun `create a new court case note succesfully`() {
        channelManager.getChannel(queueName).publishAndWait(
            prepMessage(CourtCaseNoteMessageGenerator.NEW, wireMockserver.port())
        )

        val caseNote = caseNoteRepository.findByExternalReferenceAndOffenderIdAndSoftDeletedIsFalse("1111", PersonGenerator.NEW_TO_PROBATION.id)
        assertThat(caseNote!!.notes, equalTo("Some new notes about the court case."))

        verify(telemetryService).trackEvent(eq(COURT_CASE_NOTE_MERGED), anyMap(), anyMap())
    }

    @Test
    fun `replace a court case note succesfully`() {
        val message = CourtCaseNoteMessageGenerator.EXISTS.copy(occurredAt = ZonedDateTime.now())

        channelManager.getChannel(queueName).publishAndWait(
            prepMessage(message, wireMockserver.port())
        )

        val caseNote = caseNoteRepository.findByExternalReferenceAndOffenderIdAndSoftDeletedIsFalse("2222", PersonGenerator.CURRENTLY_MANAGED.id)
        assertThat(caseNote!!.notes, equalTo("Overwritten the existing notes about the court case."))

        verify(telemetryService).trackEvent(eq(COURT_CASE_NOTE_MERGED), anyMap(), anyMap())
    }

    @Test
    fun `court case note not found - noop`() {
        channelManager.getChannel(queueName).publishAndWait(
            prepMessage(CourtCaseNoteMessageGenerator.NOT_FOUND, wireMockserver.port())
        )

        verify(telemetryService, never()).trackEvent(eq(COURT_CASE_NOTE_MERGED), anyMap(), anyMap())
    }
}
