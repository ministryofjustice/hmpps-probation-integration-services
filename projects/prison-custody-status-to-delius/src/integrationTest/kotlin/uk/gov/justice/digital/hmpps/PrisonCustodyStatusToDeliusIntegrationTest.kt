package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.BREACH_PRISON_RECALL
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.RELEASE_FROM_CUSTODY
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.custody.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.history.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.responsibleofficer.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.LOCATION_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.STATUS_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseRepository
import uk.gov.justice.digital.hmpps.jms.convertSendAndWait
import uk.gov.justice.digital.hmpps.listener.nomsNumber
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles("integration-test")
internal class PrisonCustodyStatusToDeliusIntegrationTest {

    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var custodyHistoryRepository: CustodyHistoryRepository

    @Autowired
    private lateinit var releaseRepository: ReleaseRepository

    @Autowired
    private lateinit var recallRepository: RecallRepository

    @Autowired
    private lateinit var contactRepository: ContactRepository

    @Autowired
    private lateinit var prisonManagerRepository: PrisonManagerRepository

    @Autowired
    private lateinit var responsibleOfficerRepository: ResponsibleOfficerRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `release a prisoner`() {
        // given a prisoner who is in custody
        val nomsNumber = MessageGenerator.PRISONER_RELEASED.additionalInformation.nomsNumber()
        assertTrue(getCustody(nomsNumber).isInCustody())

        // when they are released
        val message = MessageGenerator.PRISONER_RELEASED
        jmsTemplate.convertSendAndWait(queueName, message)

        // then they are no longer in custody
        val custody = getCustody(nomsNumber)
        assertFalse(custody.isInCustody())

        // and the release information is recorded correctly
        val release = getReleases(custody).single()
        assertThat(release.createdDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(release.date.withZoneSameInstant(EuropeLondon), equalTo(message.occurredAt))
        assertThat(release.person.nomsNumber, equalTo(message.additionalInformation.nomsNumber()))

        // and the history is recorded correctly
        val custodyHistory = getCustodyHistory(custody)
        assertThat(custodyHistory, hasSize(2))
        assertThat(custodyHistory.map { it.type.code }, hasItems(STATUS_CHANGE.code, LOCATION_CHANGE.code))
        assertThat(custodyHistory.map { it.detail }, hasItems("Released on Licence", "Test institution (COMMUN)"))

        // and a contact is recorded
        val contact = getContacts(nomsNumber).single()
        assertThat(contact.type.code, equalTo(RELEASE_FROM_CUSTODY.code))
        assertThat(contact.notes, equalTo("Release Type: description of ADL"))

        // and telemetry is updated
        verify(telemetryService).trackEvent(
            "PrisonerReleased",
            mapOf(
                "nomsNumber" to "A0001AA",
                "institution" to "WSI",
                "reason" to "RELEASED",
                "details" to "Movement reason code NCS"
            )
        )
    }

    @Test
    fun `recall a prisoner`() {
        // given a prisoner who is not in custody
        val nomsNumber = MessageGenerator.PRISONER_RECEIVED.additionalInformation.nomsNumber()
        assertFalse(getCustody(nomsNumber).isInCustody())

        // when they are recalled
        val message = MessageGenerator.PRISONER_RECEIVED
        jmsTemplate.convertSendAndWait(queueName, message)

        // then they are now in custody
        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())

        // and the recall information is recorded correctly
        val recall = getRecalls(custody).single()
        assertThat(recall.createdDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(recall.date.withZoneSameInstant(EuropeLondon), equalTo(message.occurredAt))

        // and the history is recorded correctly
        val custodyHistory = getCustodyHistory(custody)
        assertThat(custodyHistory, hasSize(2))
        assertThat(custodyHistory.map { it.type.code }, hasItems(STATUS_CHANGE.code, LOCATION_CHANGE.code))
        assertThat(custodyHistory.map { it.detail }, hasItems("Recall added in custody ", "Test institution (WSI)"))

        // and a prison manager is created
        val prisonManager = getPrisonManagers(nomsNumber).single()
        assertThat(prisonManager.date, isCloseTo(ZonedDateTime.now()))
        assertThat(prisonManager.allocationReason.code, equalTo("AUT"))
        // and the responsible officer is updated
        val responsibleOfficer = getResponsibleOfficers(nomsNumber).single()
        assertThat(responsibleOfficer.prisonManager?.id, equalTo(prisonManager.id))
        assertThat(responsibleOfficer.communityManager, nullValue())

        // and contacts are recorded
        val contacts = getContacts(nomsNumber)
        assertThat(contacts, hasSize(2))
        assertThat(contacts.map { it.type.code }, hasItems(BREACH_PRISON_RECALL.code, RESPONSIBLE_OFFICER_CHANGE.code))

        // and telemetry is updated
        verify(telemetryService).trackEvent(
            "PrisonerRecalled",
            mapOf(
                "nomsNumber" to "A0002AA",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "details" to "ACTIVE IN:ADM-L"
            )
        )
    }

    private fun getPersonId(nomsNumber: String) =
        personRepository.findByNomsNumberAndSoftDeletedIsFalse(nomsNumber).single().id

    private fun getCustody(nomsNumber: String) =
        eventRepository.findActiveCustodialEvents(getPersonId(nomsNumber)).single().disposal!!.custody!!

    private fun getCustodyHistory(custody: Custody) =
        custodyHistoryRepository.findAll().filter { it.custody.id == custody.id }

    private fun getContacts(nomsNumber: String) =
        contactRepository.findAll().filter { it.person.id == getPersonId(nomsNumber) }

    private fun getReleases(custody: Custody) =
        releaseRepository.findAll().filter { it.custody?.id == custody.id }

    private fun getRecalls(custody: Custody) =
        recallRepository.findAll().filter { it.release.custody?.id == custody.id }

    private fun getPrisonManagers(nomsNumber: String) =
        prisonManagerRepository.findAll().filter { it.personId == getPersonId(nomsNumber) }

    private fun getResponsibleOfficers(nomsNumber: String) =
        responsibleOfficerRepository.findAll().filter { it.personId == getPersonId(nomsNumber) }
}
