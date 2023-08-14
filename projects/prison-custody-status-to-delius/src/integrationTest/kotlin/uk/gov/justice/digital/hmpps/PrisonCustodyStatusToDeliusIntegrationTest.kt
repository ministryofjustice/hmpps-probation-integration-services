package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.NotificationGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.LOCATION_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.STATUS_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.ReleaseRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
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

    @Autowired
    private lateinit var licenceConditionRepository: LicenceConditionRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `release a prisoner`() {
        // given a prisoner who is in custody
        val nomsNumber = NotificationGenerator.PRISONER_RELEASED.message.personReference.findNomsNumber()!!
        assertTrue(getCustody(nomsNumber).isInCustody())

        // when they are released
        val notification = NotificationGenerator.PRISONER_RELEASED
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
        assertThat(release.person.nomsNumber, equalTo(notification.message.personReference.findNomsNumber()))

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
            "Released",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0001AA",
                "institution" to "WSI",
                "reason" to "RELEASED",
                "movementReason" to "NCS",
                "movementType" to "Released"
            )
        )
    }

    @Test
    fun `recall a prisoner`() {
        // given a prisoner who is not in custody
        val nomsNumber = NotificationGenerator.PRISONER_RECEIVED.message.personReference.findNomsNumber()!!
        assertFalse(getCustody(nomsNumber).isInCustody())

        // when they are recalled
        val notification = NotificationGenerator.PRISONER_RECEIVED
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
        assertThat(contacts, hasSize(4))
        assertThat(
            contacts.map { it.type.code },
            hasItems(
                ContactType.Code.BREACH_PRISON_RECALL.value,
                ContactType.Code.CHANGE_OF_INSTITUTION.value,
                ContactType.Code.COMPONENT_TERMINATED.value
            )
        )
        val coi = contacts.first { it.type.code == ContactType.Code.CHANGE_OF_INSTITUTION.value }
        assertThat(coi.event?.id, equalTo(custody.disposal.event.id))

        licenceConditionRepository.findAll().filter {
            it.disposal.id == custody.disposal.id
        }.forEach {
            assertNotNull(it.terminationDate)
            assertThat(it.terminationReason?.code, equalTo("TEST"))
        }

        // and telemetry is updated
        verify(telemetryService).trackEvent(
            "Recalled",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0002AA",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "movementReason" to "R1",
                "movementType" to "Received"
            )
        )
    }

    @Test
    fun `when a prisoner is matched`() {
        val person = PersonGenerator.MATCHABLE
        val before = getCustody(person.nomsNumber)
        assertThat(before.institution?.code, equalTo(InstitutionGenerator.DEFAULT.code))

        val notification = NotificationGenerator.PRISONER_MATCHED
        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(person.nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.institution?.code, equalTo(InstitutionGenerator.MOVED_TO.code))

        val contacts = getContacts(person.nomsNumber)
        assertThat(contacts, hasSize(1))
        assertThat(
            contacts.map { it.type.code },
            hasItems(
                ContactType.Code.CHANGE_OF_INSTITUTION.value
            )
        )
        val coi = contacts.first { it.type.code == ContactType.Code.CHANGE_OF_INSTITUTION.value }
        assertThat(coi.event?.id, equalTo(custody.disposal.event.id))

        verify(telemetryService).trackEvent(
            "LocationUpdated",
            mapOf(
                "occurredAt" to ZonedDateTime.parse("2023-07-31T09:26:39+01:00[Europe/London]").toString(),
                "nomsNumber" to PersonGenerator.MATCHABLE.nomsNumber,
                "institution" to InstitutionGenerator.MOVED_TO.nomisCdeCode!!,
                "reason" to "TRANSFERRED",
                "movementReason" to "INT",
                "movementType" to "Received"
            )
        )
    }

    @Test
    fun `a person died in custody alerts manager`() {
        val person = PersonGenerator.DIED

        val notification = NotificationGenerator.PRISONER_DIED
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            "Died",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to person.nomsNumber,
                "institution" to "WSI",
                "reason" to "RELEASED",
                "movementReason" to "DEC",
                "movementType" to "Released"
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

    @Test
    fun `receieve a new custodial sentence`() {
        val nomsNumber = NotificationGenerator.PRISONER_NEW_CUSTODY.message.personReference.findNomsNumber()!!
        val before = getCustody(nomsNumber)
        assertThat(before.status.code, equalTo(CustodialStatusCode.SENTENCED_IN_CUSTODY.code))

        val notification = NotificationGenerator.PRISONER_NEW_CUSTODY
        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertThat(custody.status.code, equalTo(CustodialStatusCode.IN_CUSTODY.code))

        // No recall is created (already in custody)
        val recall = getRecalls(custody).singleOrNull()
        assertNull(recall)

        val custodyHistory = getCustodyHistory(custody)
        assertThat(custodyHistory, hasSize(2))
        assertThat(custodyHistory.map { it.type.code }, hasItems(STATUS_CHANGE.code, LOCATION_CHANGE.code))
        assertThat(custodyHistory.map { it.detail }, hasItems("In custody ", "Test institution (WSIHMP)"))

        val prisonManager = getPrisonManagers(nomsNumber).single()
        assertThat(prisonManager.date, isCloseTo(ZonedDateTime.now()))
        assertThat(prisonManager.allocationReason.code, equalTo("AUT"))

        val contacts = getContacts(nomsNumber)
        assertThat(contacts, hasSize(1))
        assertThat(
            contacts.map { it.type.code },
            hasItems(
                ContactType.Code.CHANGE_OF_INSTITUTION.value
            )
        )
        val coi = contacts.first { it.type.code == ContactType.Code.CHANGE_OF_INSTITUTION.value }
        assertThat(coi.event?.id, equalTo(custody.disposal.event.id))

        verify(telemetryService).trackEvent(
            "StatusUpdated",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0004AA",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "movementReason" to "N",
                "movementType" to "Received"
            )
        )

        verify(telemetryService).trackEvent(
            "LocationUpdated",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0004AA",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "movementReason" to "N",
                "movementType" to "Received"
            )
        )
    }

    @Test
    fun `receieve a prisoner already recalled in delius`() {
        val nomsNumber = NotificationGenerator.PRISONER_RECALLED.message.personReference.findNomsNumber()!!

        val notification = NotificationGenerator.PRISONER_RECALLED
        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertThat(custody.status.code, equalTo(CustodialStatusCode.IN_CUSTODY.code))

        val custodyHistory = getCustodyHistory(custody)
        assertThat(custodyHistory, hasSize(2))
        assertThat(custodyHistory.map { it.type.code }, hasItems(STATUS_CHANGE.code, LOCATION_CHANGE.code))
        assertThat(custodyHistory.map { it.detail }, hasItems("In custody ", "Test institution (WSIHMP)"))

        val prisonManager = getPrisonManagers(nomsNumber).single()
        assertThat(prisonManager.date, isCloseTo(ZonedDateTime.now()))
        assertThat(prisonManager.allocationReason.code, equalTo("AUT"))

        val contacts = getContacts(nomsNumber)
        assertThat(contacts, hasSize(1))
        assertThat(
            contacts.map { it.type.code },
            hasItems(
                ContactType.Code.CHANGE_OF_INSTITUTION.value
            )
        )
        val coi = contacts.first { it.type.code == ContactType.Code.CHANGE_OF_INSTITUTION.value }
        assertThat(coi.event?.id, equalTo(custody.disposal.event.id))

        verify(telemetryService).trackEvent(
            "StatusUpdated",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0006AA",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "movementReason" to "24",
                "movementType" to "Received"
            )
        )

        verify(telemetryService).trackEvent(
            "LocationUpdated",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0006AA",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "movementReason" to "24",
                "movementType" to "Received"
            )
        )
    }

    @Test
    fun `hospital release when released on licence in delius`() {
        val notification = NotificationGenerator.PRISONER_HOSPITAL_RELEASED
        val nomsNumber = notification.message.personReference.findNomsNumber()!!
        assertFalse(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.institution?.code, equalTo(InstitutionCode.OTHER_SECURE_UNIT.code))

        val recall = getRecalls(custody).single()
        assertThat(recall.createdDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(
            recall.date.withZoneSameInstant(EuropeLondon),
            equalTo(notification.message.occurredAt.truncatedTo(DAYS))
        )

        val custodyHistory = getCustodyHistory(custody)
        assertThat(custodyHistory, hasSize(2))
        assertThat(custodyHistory.map { it.type.code }, hasItems(STATUS_CHANGE.code, LOCATION_CHANGE.code))
        assertThat(custodyHistory.map { it.detail }, hasItems("Transfer to/from Hospital", "Test institution (XXX056)"))

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

        verify(telemetryService).trackEvent(
            "Recalled",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0005AA",
                "reason" to "RELEASED",
                "movementReason" to "HO",
                "movementType" to "Released"
            )
        )

        verify(telemetryService).trackEvent(
            "StatusUpdated",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0005AA",
                "reason" to "RELEASED",
                "movementReason" to "HO",
                "movementType" to "Released"
            )
        )

        verify(telemetryService).trackEvent(
            "LocationUpdated",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0005AA",
                "reason" to "RELEASED",
                "movementReason" to "HO",
                "movementType" to "Released"
            )
        )
    }

    @Test
    fun `hospital release when in custody in delius`() {
        val notification = NotificationGenerator.PRISONER_HOSPITAL_IN_CUSTODY
        val nomsNumber = notification.message.personReference.findNomsNumber()!!
        assertTrue(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.institution?.code, equalTo(InstitutionGenerator.MOVED_TO.code))

        val custodyHistory = getCustodyHistory(custody)
        assertThat(custodyHistory, hasSize(1))
        assertThat(custodyHistory.map { it.type.code }, hasItems(LOCATION_CHANGE.code))
        assertThat(custodyHistory.map { it.detail }, hasItems("Test institution (SWIHMP)"))

        val contacts = getContacts(nomsNumber)
        assertThat(contacts, hasSize(1))
        assertThat(
            contacts.map { it.type.code },
            hasItems(
                ContactType.Code.CHANGE_OF_INSTITUTION.value
            )
        )
        val coi = contacts.first { it.type.code == ContactType.Code.CHANGE_OF_INSTITUTION.value }
        assertThat(coi.event?.id, equalTo(custody.disposal.event.id))

        verify(telemetryService).trackEvent(
            "LocationUpdated",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0007AA",
                "institution" to "SWI",
                "reason" to "RELEASED_TO_HOSPITAL",
                "movementReason" to "HQ",
                "movementType" to "Released"
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
}
