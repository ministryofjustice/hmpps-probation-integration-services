package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.testcontainers.shaded.org.bouncycastle.oer.its.ieee1609dot2dot1.AdditionalParams.original
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffRepository
import uk.gov.justice.digital.hmpps.message.MessageAttribute
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import kotlin.text.Typography.notEqual

const val CASE_NOTE_MERGED = "CaseNoteMerged"

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@SpringBootTest
class CaseNotesIntegrationTest {

    @Autowired
    private lateinit var offenderRepository: OffenderRepository

    @Value("\${messaging.consumer.queue}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

    @Autowired
    private lateinit var caseNoteRepository: CaseNoteRepository

    @Autowired
    private lateinit var staffRepository: StaffRepository

    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @MockitoSpyBean
    lateinit var air: AuditedInteractionRepository

    @Order(1)
    @Test
    fun `update an existing case note succesfully`() {
        val nomisCaseNote = PrisonCaseNoteGenerator.EXISTING_IN_BOTH

        channelManager.getChannel(queueName).publishAndWait(
            prepNotification(CaseNoteMessageGenerator.EXISTS_IN_DELIUS, wireMockserver.port())
        )

        val saved =
            caseNoteRepository.findByExternalReference("${DeliusCaseNote.CASE_NOTE_URN_PREFIX}${nomisCaseNote.id}")

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

    @Order(2)
    @Test
    fun `create a new case note succesfully`() {
        val nomisCaseNote = PrisonCaseNoteGenerator.NEW_TO_DELIUS
        val original = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)
        assertNull(original)

        channelManager.getChannel(queueName).publishAndWait(
            prepNotification(CaseNoteMessageGenerator.NEW_TO_DELIUS, wireMockserver.port())
        )

        verify(telemetryService).trackEvent(eq(CASE_NOTE_MERGED), anyMap(), anyMap())
        val saved =
            caseNoteRepository.findByExternalReference("${DeliusCaseNote.CASE_NOTE_URN_PREFIX}${nomisCaseNote.id}")
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
        assertThat(staff.code, equalTo("${ProbationAreaGenerator.DEFAULT.code}B000"))

        assertThat(saved.createdByUserId, equalTo(UserGenerator.AUDIT_USER.id))
        assertThat(saved.lastModifiedUserId, equalTo(UserGenerator.AUDIT_USER.id))
        assertThat(saved.date, isCloseTo(ZonedDateTime.parse("2022-10-18T08:19:19.451579+01:00")))

        verify(air, atLeastOnce()).save(any())
        val savedAudits = air.findAll()
        assertThat(savedAudits.size, greaterThan(0))
    }

    @Order(3)
    @Test
    fun `case note not found - noop`() {
        channelManager.getChannel(queueName).publishAndWait(
            prepNotification(CaseNoteMessageGenerator.NOT_FOUND, wireMockserver.port())
        )

        verify(telemetryService, never()).trackEvent(eq(CASE_NOTE_MERGED), anyMap(), anyMap())
    }

    @Order(4)
    @Test
    fun `create a new case note for resettlement passport`() {
        val nomisCaseNote = PrisonCaseNoteGenerator.RESETTLEMENT_PASSPORT

        channelManager.getChannel(queueName).publishAndWait(
            prepNotification(CaseNoteMessageGenerator.RESETTLEMENT_PASSPORT, wireMockserver.port())
        )

        verify(telemetryService).trackEvent(eq(CASE_NOTE_MERGED), anyMap(), anyMap())
        val saved =
            caseNoteRepository.findByExternalReference("${DeliusCaseNote.CASE_NOTE_URN_PREFIX}${nomisCaseNote.id}")
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
        assertThat(staff.code, equalTo("${ProbationAreaGenerator.DEFAULT.code}B000"))
    }

    @Order(5)
    @Test
    fun `migrate case notes succesfully when noms number added`() {
        val offender = requireNotNull(offenderRepository.findByNomsIdAndSoftDeletedIsFalse("A4578BX"))
        val originals = caseNoteRepository.findAll().filter { it.offenderId == offender.id }
        assert(originals.isEmpty())

        channelManager.getChannel(queueName).publishAndWait(
            prepNotification(CaseNoteMessageGenerator.NOMS_NUMBER_ADDED, wireMockserver.port())
        )

        verify(telemetryService).trackEvent(
            eq("CaseNotesMigrated"),
            eq(
                mapOf(
                    "nomsId" to "A4578BX",
                    "cause" to "probation-case.prison-identifier.added",
                    "total" to "4",
                )
            ),
            anyMap()
        )
        val saved = caseNoteRepository.findAll().filter { it.offenderId == offender.id }
        assertThat(saved.size, equalTo(4))
    }

    @Order(6)
    @Test
    fun `case note not of interest - noop`() {
        val existing = CaseNoteMessageGenerator.EXISTS_IN_DELIUS
        channelManager.getChannel(queueName).publishAndWait(
            prepNotification(
                existing.copy(
                    attributes = MessageAttributes(existing.eventType!!).apply {
                        this["type"] = MessageAttribute("String", "NOTOF")
                        this["subType"] = MessageAttribute("String", "INTEREST")
                    }
                ), wireMockserver.port())
        )

        verify(telemetryService, never()).trackEvent(eq(CASE_NOTE_MERGED), anyMap(), anyMap())
    }

    @Order(7)
    @Test
    fun `create an active alert`() {
        val nomisCaseNote = PrisonCaseNoteGenerator.CREATED_ALERT
        val urn = "urn:hmpps:prisoner-alerts:active:${nomisCaseNote.alertUuid}"
        val original = caseNoteRepository.findByExternalReference(urn)
        assertNull(original)

        channelManager.getChannel(queueName).publishAndWait(
            prepNotification(AlertMessageGenerator.ALERT_CREATED, wireMockserver.port()),
        )

        val saved = caseNoteRepository.findByExternalReference(urn)
        assertNotNull(saved)

        assertThat(saved?.date?.toLocalDate(), equalTo(nomisCaseNote.activeFrom))
        assertThat(
            saved!!.notes,
            stringContainsInOrder("ALERT", "ACTIVE", nomisCaseNote.description)
        )

        val staff = staffRepository.findById(saved.staffId).orElseThrow()
        assertThat(staff.forename, equalTo("John"))
    }

    @Order(8)
    @Test
    fun `updated to deactivate alert`() {
        val nomisCaseNote = PrisonCaseNoteGenerator.UPDATED_ALERT
        val activeUrn = "urn:hmpps:prisoner-alerts:active:${nomisCaseNote.alertUuid}"
        val inactiveUrn = "urn:hmpps:prisoner-alerts:inactive:${nomisCaseNote.alertUuid}"
        val active = caseNoteRepository.findByExternalReference(activeUrn)
        val inactive = caseNoteRepository.findByExternalReference(inactiveUrn)
        assertNotNull(active)
        assertNull(inactive)

        channelManager.getChannel(queueName).publishAndWait(
            prepNotification(AlertMessageGenerator.ALERT_UPDATED, wireMockserver.port()),
        )

        val saved = caseNoteRepository.findByExternalReference(inactiveUrn)
        assertNotNull(saved)
        assert(active?.id != saved?.id)

        assertThat(saved?.date?.toLocalDate(), equalTo(nomisCaseNote.activeTo))
        assertThat(
            saved!!.notes,
            stringContainsInOrder("ALERT", "INACTIVE", nomisCaseNote.description)
        )

        val staff = staffRepository.findById(saved.staffId).orElseThrow()
        assertThat(staff.forename, equalTo("Jane"))
    }

    @Order(9)
    @Test
    fun `alert made inactive`() {
        val nomisCaseNote = PrisonCaseNoteGenerator.INACTIVE_ALERT
        val inactiveUrn = "urn:hmpps:prisoner-alerts:inactive:${nomisCaseNote.alertUuid}"
        val inactive = caseNoteRepository.findByExternalReference(inactiveUrn)

        assertNull(inactive)

        channelManager.getChannel(queueName).publishAndWait(
            prepNotification(AlertMessageGenerator.ALERT_INACTIVE, wireMockserver.port()),
        )

        val saved = caseNoteRepository.findByExternalReference(inactiveUrn)
        assertNotNull(saved)

        assertThat(saved?.date?.toLocalDate(), equalTo(nomisCaseNote.activeTo))
        assertThat(
            saved!!.notes,
            stringContainsInOrder("ALERT", "INACTIVE", nomisCaseNote.description)
        )

        val staff = staffRepository.findById(saved.staffId).orElseThrow()
        assertThat(staff.forename, equalTo("John"))
    }
}
