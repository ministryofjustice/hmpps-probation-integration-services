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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.CourtCaseNoteMessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

const val COURT_CASE_NOTE_MERGED = "CourtCaseNoteMerged"

@SpringBootTest
class CaseNotesIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val caseNoteRepository: CaseNoteRepository,
    private val wireMockserver: WireMockServer
) {

    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @MockitoSpyBean
    lateinit var air: AuditedInteractionRepository

    @Test
    fun `create a new court case note successfully`() {
        channelManager.getChannel(queueName).publishAndWait(
            prepMessage(CourtCaseNoteMessageGenerator.NEW, wireMockserver.port())
        )

        val caseNote = caseNoteRepository.findByExternalReferenceAndOffenderIdAndSoftDeletedIsFalse(
            "1111",
            PersonGenerator.NEW_TO_PROBATION.id
        )
        assertThat(caseNote!!.notes, equalTo("Some new notes about the court case."))

        verify(telemetryService).trackEvent(eq(COURT_CASE_NOTE_MERGED), anyMap(), anyMap())
    }

    @Test
    fun `replace a court case note successfully`() {
        val message = CourtCaseNoteMessageGenerator.EXISTS.copy(occurredAt = ZonedDateTime.now())

        channelManager.getChannel(queueName).publishAndWait(
            prepMessage(message, wireMockserver.port())
        )

        val caseNote = caseNoteRepository.findByExternalReferenceAndOffenderIdAndSoftDeletedIsFalse(
            "2222",
            PersonGenerator.CURRENTLY_MANAGED.id
        )
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
