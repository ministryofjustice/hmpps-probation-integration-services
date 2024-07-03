package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime

const val CASE_NOTE_MERGED = "CaseNoteMerged"

@SpringBootTest
class CaseNotesIntegrationTest {

    @Value("\${messaging.consumer.queue}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

    @Autowired
    private lateinit var caseNoteRepository: CaseNoteRepository

    @Autowired
    private lateinit var staffRepository: StaffRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @SpyBean
    lateinit var air: AuditedInteractionRepository

    @Test
    fun `update an existing case note succesfully`() {
        val nomisCaseNote = PrisonCaseNoteGenerator.EXISTING_IN_BOTH

        channelManager.getChannel(queueName).publishAndWait(
            prepMessage(CaseNoteMessageGenerator.EXISTS_IN_DELIUS, wireMockserver.port())
        )

        val saved = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)

        assertThat(
            saved?.notes,
            stringContainsInOrder(
                nomisCaseNote.type,
                nomisCaseNote.subType,
                nomisCaseNote.text,
                nomisCaseNote.amendments[0].authorName,
                DeliusDateTimeFormatter.format(nomisCaseNote.amendments[0].creationDateTime),
                nomisCaseNote.amendments[0].additionalNoteText
            )
        )

        verify(telemetryService).trackEvent(eq(CASE_NOTE_MERGED), anyMap(), anyMap())

        verify(air, atLeastOnce()).save(any())
        val savedAudits = air.findAll()
        assertThat(savedAudits.size, greaterThan(0))
    }

    @Test
    fun `create a new case note succesfully`() {
        val nomisCaseNote = PrisonCaseNoteGenerator.NEW_TO_DELIUS
        val original = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)
        assertNull(original)

        channelManager.getChannel(queueName).publishAndWait(
            prepMessage(CaseNoteMessageGenerator.NEW_TO_DELIUS, wireMockserver.port())
        )

        verify(telemetryService).trackEvent(eq(CASE_NOTE_MERGED), anyMap(), anyMap())
        val saved = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)
        assertNotNull(saved)

        assertThat(
            saved!!.notes,
            stringContainsInOrder(nomisCaseNote.type, nomisCaseNote.subType, nomisCaseNote.text)
        )

        assertThat(
            saved.type.code,
            equalTo(CaseNoteNomisTypeGenerator.NEG.type.code)
        )

        assertThat(
            saved.eventId,
            equalTo(EventGenerator.CUSTODIAL_EVENT.id)
        )

        assertThat(
            saved.nsiId,
            equalTo(NsiGenerator.EVENT_CASE_NOTE_NSI.id)
        )

        val staff = staffRepository.findById(saved.staffId).orElseThrow()
        assertThat(staff.code, equalTo("${ProbationAreaGenerator.DEFAULT.code}B001"))

        assertThat(saved.createdByUserId, equalTo(UserGenerator.AUDIT_USER.id))
        assertThat(saved.lastModifiedUserId, equalTo(UserGenerator.AUDIT_USER.id))
        assertThat(saved.date, isCloseTo(ZonedDateTime.parse("2022-10-18T08:19:19.451579+01:00")))

        verify(air, atLeastOnce()).save(any())
        val savedAudits = air.findAll()
        assertThat(savedAudits.size, greaterThan(0))
    }

    @Test
    fun `case note not found - noop`() {
        channelManager.getChannel(queueName).publishAndWait(
            prepMessage(CaseNoteMessageGenerator.NOT_FOUND, wireMockserver.port())
        )

        verify(telemetryService, never()).trackEvent(eq(CASE_NOTE_MERGED), anyMap(), anyMap())
    }

    @Test
    fun `create a new case note for resettlement passport`() {
        val nomisCaseNote = PrisonCaseNoteGenerator.RESETTLEMENT_PASSPORT

        channelManager.getChannel(queueName).publishAndWait(
            prepMessage(CaseNoteMessageGenerator.RESETTLEMENT_PASSPORT, wireMockserver.port())
        )

        verify(telemetryService).trackEvent(eq(CASE_NOTE_MERGED), anyMap(), anyMap())
        val saved = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)
        assertNotNull(saved)

        assertThat(
            saved!!.notes,
            stringContainsInOrder(nomisCaseNote.type, nomisCaseNote.subType, nomisCaseNote.text)
        )

        assertThat(
            saved.type.code,
            equalTo(CaseNoteNomisTypeGenerator.RESETTLEMENT.type.code)
        )

        assertThat(
            saved.eventId,
            equalTo(EventGenerator.CUSTODIAL_EVENT.id)
        )

        val staff = staffRepository.findById(saved.staffId).orElseThrow()
        assertThat(staff.code, equalTo("${ProbationAreaGenerator.DEFAULT.code}B001"))
    }
}
