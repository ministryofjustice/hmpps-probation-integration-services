package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.LOCATION_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.STATUS_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.ReleaseRepository
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.nomsNumber
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

@SpringBootTest
internal class PrisonCustodyStatusToDeliusIntegrationTest {

    @Value("\${messaging.consumer.queue}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

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
    private lateinit var contactAlertRepository: ContactAlertRepository

    @Autowired
    private lateinit var prisonManagerRepository: PrisonManagerRepository

    @Autowired
    private lateinit var personManagerRepository: PersonManagerRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `release a prisoner`() {
        // given a prisoner who is in custody
        val nomsNumber = MessageGenerator.PRISONER_RELEASED.additionalInformation.nomsNumber()
        assertTrue(getCustody(nomsNumber).isInCustody())

        // when they are released
        val notification = Notification(
            message = MessageGenerator.PRISONER_RELEASED,
            attributes = MessageAttributes("prison-offender-events.prisoner.released")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)

        // then they are no longer in custody
        val custody = getCustody(nomsNumber)
        assertFalse(custody.isInCustody())

        // and the release information is recorded correctly
        val release = getReleases(custody).single()
        assertThat(release.createdDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(
            release.date.withZoneSameInstant(EuropeLondon),
            equalTo(notification.message.occurredAt.truncatedTo(DAYS))
        )
        assertThat(release.person.nomsNumber, equalTo(notification.message.additionalInformation.nomsNumber()))

        // and the history is recorded correctly
        val custodyHistory = getCustodyHistory(custody)
        assertThat(custodyHistory, hasSize(2))
        assertThat(custodyHistory.map { it.type.code }, hasItems(STATUS_CHANGE.code, LOCATION_CHANGE.code))
        assertThat(custodyHistory.map { it.detail }, hasItems("Released on Licence", "Test institution (COMMUN)"))

        // and a contact is recorded
        val contact = getContacts(nomsNumber).single()
        assertThat(contact.type.code, equalTo(ContactType.Code.RELEASE_FROM_CUSTODY.value))
        assertThat(contact.notes, equalTo("Release Type: description of ADL"))

        // and telemetry is updated
        verify(telemetryService).trackEvent(
            "PrisonerReleased",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0001AA",
                "institution" to "WSI",
                "reason" to "RELEASED",
                "nomisMovementReasonCode" to "NCS",
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
        val notification = Notification(
            message = MessageGenerator.PRISONER_RECEIVED,
            attributes = MessageAttributes("prison-offender-events.prisoner.received")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)

        // then they are now in custody
        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())

        // and the recall information is recorded correctly
        val recall = getRecalls(custody).single()
        assertThat(recall.createdDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(
            recall.date.withZoneSameInstant(EuropeLondon),
            equalTo(notification.message.occurredAt.truncatedTo(DAYS))
        )

        // and the history is recorded correctly
        val custodyHistory = getCustodyHistory(custody)
        assertThat(custodyHistory, hasSize(2))
        assertThat(custodyHistory.map { it.type.code }, hasItems(STATUS_CHANGE.code, LOCATION_CHANGE.code))
        assertThat(custodyHistory.map { it.detail }, hasItems("Recall added in custody ", "Test institution (WSIHMP)"))

        // and a prison manager is created
        val prisonManager = getPrisonManagers(nomsNumber).single()
        assertThat(prisonManager.date, isCloseTo(ZonedDateTime.now()))
        assertThat(prisonManager.allocationReason.code, equalTo("AUT"))

        // and contacts are recorded
        val contacts = getContacts(nomsNumber)
        assertThat(contacts, hasSize(2))
        assertThat(
            contacts.map { it.type.code },
            hasItems(
                ContactType.Code.BREACH_PRISON_RECALL.value,
                ContactType.Code.CHANGE_OF_INSTITUTION.value
            )
        )
        val coi = contacts.first { it.type.code == ContactType.Code.CHANGE_OF_INSTITUTION.value }
        assertThat(coi.event?.id, equalTo(custody.disposal.event.id))

        // and telemetry is updated
        verify(telemetryService).trackEvent(
            "PrisonerRecalled",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0002AA",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "nomisMovementReasonCode" to "R1",
                "details" to "ACTIVE IN:ADM-L"
            )
        )
    }

    @Test
    fun `a person died in custody alerts manager`() {
        val person = PersonGenerator.DIED

        val notification = Notification(
            message = MessageGenerator.PRISONER_DIED,
            attributes = MessageAttributes("prison-offender-events.prisoner.released")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            "PrisonerDied",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to person.nomsNumber,
                "institution" to "WSI",
                "reason" to "RELEASED",
                "nomisMovementReasonCode" to "DEC"
            )
        )

        val dus = contactRepository.findAll().firstOrNull { it.person.id == person.id }
        assertNotNull(dus!!)
        assertThat(dus.type.code, equalTo(ContactType.Code.DIED_IN_CUSTODY.value))
        assertTrue(dus.alert!!)
        val personManager = personManagerRepository.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id)
        assertThat(dus.teamId, equalTo(personManager.team.id))
        assertThat(dus.staffId, equalTo(personManager.staff.id))
        val alert = contactAlertRepository.findAll().firstOrNull { it.contactId == dus.id }
        assertNotNull(alert)
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
}
