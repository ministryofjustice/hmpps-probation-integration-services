package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.ReferralEndType
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles("integration-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class ReferAndMonitorIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var nsiRepository: NsiRepository

    @Autowired
    lateinit var statusHistoryRepo: NsiStatusHistoryRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var enforcementRepository: EnforcementRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Test
    @Order(1)
    fun `session appointment feedback submitted failed to comply`() {
        val scheduled = contactRepository.findById(ContactGenerator.CRSAPT_NON_COMPLIANT.id).orElseThrow()
        assertNull(scheduled.outcome)

        val nsi = nsiRepository.findById(scheduled.nsiId!!).orElseThrow()
        assertThat(nsi.rarCount, equalTo(2))

        val notification = prepNotification(
            notification("session-appointment-feedback-submitted-non-compliant"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            "SessionAppointmentSubmitted",
            mapOf(
                "crn" to "T140223",
                "appointmentId" to "1",
                "referralId" to "68df9f6c-3fcb-4ec6-8fcf-96551cd9b080",
                "referralReference" to "FE4536C"
            )
        )

        val expectedOutcome = ContactGenerator.OUTCOMES[ContactOutcome.Code.FAILED_TO_COMPLY.value]!!
        val appointment = contactRepository.findById(ContactGenerator.CRSAPT_NON_COMPLIANT.id).orElseThrow()
        assertThat(appointment.outcome?.code, equalTo(expectedOutcome.code))
        assertThat(appointment.attended, equalTo(expectedOutcome.attendance))
        assertThat(appointment.complied, equalTo(expectedOutcome.compliantAcceptable))
        assertThat(appointment.hoursCredited, equalTo(null))
        assertTrue(appointment.enforcement!!)

        val enforcement = enforcementRepository.findAll().firstOrNull { it.contact.id == appointment.id }
        assertNotNull(enforcement)
        val referContact = contactRepository.findAll().firstOrNull {
            it.person.id == PersonGenerator.DEFAULT.id && it.type.code == ContactType.Code.REFER_TO_PERSON_MANAGER.value
        }
        assertNotNull(referContact!!)
        assertThat(referContact.notes, containsString("Enforcement Action: ${enforcement!!.action!!.description}"))

        val event = eventRepository.findById(appointment.eventId!!).orElseThrow()
        assertThat(event.ftcCount, equalTo(1))

        assertThat(nsi.rarCount, equalTo(2))

        val reviewCreated = contactRepository.countEnforcementUnderReview(
            event.id,
            ContactType.Code.REVIEW_ENFORCEMENT_STATUS.value,
            event.breachEnd
        ) > 0
        assertTrue(reviewCreated)
    }

    @Test
    @Order(2)
    fun `session appointment feedback submitted complied`() {
        val scheduled = contactRepository.findById(ContactGenerator.CRSAPT_COMPLIANT.id).orElseThrow()
        assertNull(scheduled.outcome)

        val notification = prepNotification(
            notification("session-appointment-feedback-submitted-compliant"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification, Duration.ofSeconds(180))

        verify(telemetryService).trackEvent(
            "SessionAppointmentSubmitted",
            mapOf(
                "crn" to "T140223",
                "referralId" to "68df9f6c-3fcb-4ec6-8fcf-96551cd9b080",
                "appointmentId" to "2",
                "referralReference" to "FE4536C"
            )
        )

        val expectedOutcome = ContactGenerator.OUTCOMES[ContactOutcome.Code.COMPLIED.value]!!
        val appointment = contactRepository.findById(ContactGenerator.CRSAPT_COMPLIANT.id).orElseThrow()
        assertThat(appointment.outcome?.code, equalTo(expectedOutcome.code))
        assertThat(appointment.attended, equalTo(expectedOutcome.attendance))
        assertThat(appointment.complied, equalTo(expectedOutcome.compliantAcceptable))
        assertThat(appointment.hoursCredited, equalTo(0.75))
        assertNull(appointment.enforcement)

        val enforcement = enforcementRepository.findAll().firstOrNull { it.contact.id == appointment.id }
        assertNull(enforcement)
        val referContacts = contactRepository.findAll().filter {
            it.person.id == PersonGenerator.DEFAULT.id && it.type.code == ContactType.Code.REFER_TO_PERSON_MANAGER.value
        }
        // one created in the previous test
        assertThat(referContacts.count(), equalTo(1))

        // one ftc from previous test
        val event = eventRepository.findById(appointment.eventId!!).orElseThrow()
        assertThat(event.ftcCount, equalTo(1))

        val nsi = nsiRepository.findById(appointment.nsiId!!).orElseThrow()
        assertThat(nsi.rarCount, equalTo(1))
    }

    @Test
    @Order(3)
    fun `referral end submitted`() {
        val nsi = nsiRepository.findById(NsiGenerator.END_PREMATURELY.id).orElseThrow()
        assertThat(nsi.status.code, equalTo(NsiStatus.Code.IN_PROGRESS.value))
        assertNull(nsi.actualEndDate)
        assertNull(nsi.outcome)

        val futureAppt = ContactGenerator.generate(
            type = ContactGenerator.TYPES[ContactType.Code.CRSAPT.value]!!,
            date = LocalDate.now().plusDays(7),
            startTime = ZonedDateTime.now().plusDays(7),
            nsi = nsi,
            person = nsi.person,
            id = 0
        ).let { contactRepository.save(it) }

        val notification = prepNotification(
            notification("referral-prematurely-ended"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            "ReferralEnded",
            mapOf(
                "crn" to "T140223",
                "referralId" to "f56c5f7c-632f-4cad-a1b3-693541cb5f22",
                "referralUrn" to "urn:hmpps:interventions-referral:68df9f6c-3fcb-4ec6-8fcf-96551cd9b080",
                "endDate" to "2023-02-23T15:29:54.197Z[Europe/London]",
                "endType" to "PREMATURELY_ENDED"
            )
        )

        val saved = nsiRepository.findById(NsiGenerator.END_PREMATURELY.id).orElseThrow()
        assertThat(saved.status.code, equalTo(NsiStatus.Code.END.value))
        assertThat(
            saved.actualEndDate?.withZoneSameInstant(EuropeLondon),
            equalTo(
                ZonedDateTime.parse("2023-02-23T15:29:54Z").withZoneSameInstant(EuropeLondon)
            )
        )
        assertThat(saved.outcome?.code, equalTo(ReferralEndType.PREMATURELY_ENDED.outcome))
        assertFalse(saved.active)

        assertTrue(contactRepository.findById(futureAppt.id).isEmpty)

        val sh = statusHistoryRepo.findAll().firstOrNull { it.nsiId == nsi.id }
        assertNotNull(sh)
        assertThat(sh!!.statusId, equalTo(saved.status.id))
        assertThat(sh.date, equalTo(saved.actualEndDate))

        val contacts = contactRepository.findAll().filter { it.nsiId == nsi.id }
        assertTrue(contacts have ContactType.Code.COMPLETED.value)
        assertTrue(contacts have ContactType.Code.NSI_TERMINATED.value)
    }

    @Test
    fun `fuzzy search referral end submitted`() {
        val nsi = nsiRepository.findById(NsiGenerator.FUZZY_SEARCH.id).orElseThrow()
        assertNull(nsi.actualEndDate)
        assertNull(nsi.outcome)

        val notification = prepNotification(
            notification("fuzzy-search-referral-ended"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification, Duration.ofSeconds(180))

        verify(telemetryService).trackEvent(
            "ReferralEnded",
            mapOf(
                "crn" to "F123456",
                "referralId" to "f56c5f7c-632f-4cad-a1b3-693541cb5f22",
                "referralUrn" to "urn:hmpps:interventions-referral:71df9f6c-3fcb-4ec6-8fcf-96551cd9b080",
                "endDate" to "2023-02-23T15:29:54.197Z[Europe/London]",
                "endType" to "CANCELLED"
            )
        )

        val saved = nsiRepository.findById(NsiGenerator.FUZZY_SEARCH.id).orElseThrow()
        assertThat(saved.status.code, equalTo(NsiStatus.Code.END.value))
        assertThat(
            saved.actualEndDate?.withZoneSameInstant(EuropeLondon),
            equalTo(
                ZonedDateTime.parse("2023-02-23T15:29:54Z").withZoneSameInstant(EuropeLondon)
            )
        )
        assertThat(saved.outcome?.code, equalTo(ReferralEndType.CANCELLED.outcome))
        assertFalse(saved.active)
    }

    private infix fun List<Contact>.have(type: String) = any { it.type.code == type }
}
