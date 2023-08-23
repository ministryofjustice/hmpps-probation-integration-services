package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.hasSize
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistory
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.ReleaseRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest
open class PcstdIntegrationTestBase {

    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    internal lateinit var channelManager: HmppsChannelManager

    @Autowired
    internal lateinit var eventRepository: EventRepository

    @Autowired
    internal lateinit var personRepository: PersonRepository

    @Autowired
    internal lateinit var custodyHistoryRepository: CustodyHistoryRepository

    @Autowired
    internal lateinit var releaseRepository: ReleaseRepository

    @Autowired
    internal lateinit var recallRepository: RecallRepository

    @Autowired
    internal lateinit var contactRepository: ContactRepository

    @Autowired
    internal lateinit var contactAlertRepository: ContactAlertRepository

    @Autowired
    internal lateinit var prisonManagerRepository: PrisonManagerRepository

    @Autowired
    internal lateinit var personManagerRepository: PersonManagerRepository

    @Autowired
    internal lateinit var licenceConditionRepository: LicenceConditionRepository

    @MockBean
    internal lateinit var telemetryService: TelemetryService

    @MockBean
    internal lateinit var featureFlags: FeatureFlags

    internal fun getPersonId(nomsNumber: String) =
        personRepository.findByNomsNumberAndSoftDeletedIsFalse(nomsNumber).single().id

    internal fun getCustody(nomsNumber: String) =
        eventRepository.findActiveCustodialEvents(getPersonId(nomsNumber)).single().disposal!!.custody!!

    internal fun getCustodyHistory(custody: Custody) =
        custodyHistoryRepository.findAll().filter { it.custody.id == custody.id }

    internal fun getContacts(nomsNumber: String) =
        contactRepository.findAll().filter { it.person.id == getPersonId(nomsNumber) }

    internal fun getReleases(custody: Custody) =
        releaseRepository.findAll().filter { it.custody?.id == custody.id }

    internal fun getRecalls(custody: Custody) =
        recallRepository.findAll().filter { it.release.custody?.id == custody.id }

    internal fun getPrisonManagers(nomsNumber: String) =
        prisonManagerRepository.findAll().filter { it.personId == getPersonId(nomsNumber) }

    internal fun verifyRelease(custody: Custody, dateTime: ZonedDateTime) {
        val release = getReleases(custody).single()
        assertThat(
            release.date.withZoneSameInstant(EuropeLondon),
            equalTo(dateTime.truncatedTo(ChronoUnit.DAYS))
        )
        assertThat(release.type.code, equalTo(ReleaseTypeCode.ADULT_LICENCE.code))
    }

    internal fun verifyRecall(custody: Custody, dateTime: ZonedDateTime, rrc: RecallReason.Code) {
        val recall = getRecalls(custody).single()
        assertThat(
            recall.date.withZoneSameInstant(EuropeLondon),
            equalTo(dateTime.truncatedTo(ChronoUnit.DAYS))
        )
        assertThat(recall.reason.code, equalTo(rrc.value))
    }

    internal fun verifyContact(custody: Custody, type: ContactType.Code) {
        verifyContacts(custody, 1, type)
    }

    internal fun verifyContacts(custody: Custody, count: Int, vararg types: ContactType.Code) {
        val contacts = getContacts(custody.disposal.event.person.nomsNumber)
        assertThat(contacts, hasSize(count))
        assertThat(
            contacts.map { it.type.code },
            hasItems(*types.map { it.value }.toTypedArray())
        )
        if (ContactType.Code.CHANGE_OF_INSTITUTION in types) {
            val coi = contacts.first { it.type.code == ContactType.Code.CHANGE_OF_INSTITUTION.value }
            assertThat(coi.event?.id, equalTo(custody.disposal.event.id))
        }
    }

    internal fun verifyCustodyHistory(custody: Custody, vararg events: CustodyEventTester) {
        val custodyHistory = getCustodyHistory(custody)
        assertThat(custodyHistory, hasSize(events.size))
        assertThat(custodyHistory.map { it.eventTester() }, containsInAnyOrder(*events))
    }

    internal fun verifyTelemetry(vararg types: String, properties: () -> Map<String, String>) =
        types.forEach { verify(telemetryService).trackEvent(it, properties()) }

    internal data class CustodyEventTester(val type: CustodyEventTypeCode, val detail: String?)

    internal fun CustodyHistory.eventTester() =
        CustodyEventTester(CustodyEventTypeCode.entries.first { it.code == type.code }, detail)
}
