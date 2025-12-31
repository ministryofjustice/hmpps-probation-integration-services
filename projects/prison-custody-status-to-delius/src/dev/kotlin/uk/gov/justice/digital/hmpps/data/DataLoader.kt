package uk.gov.justice.digital.hmpps.data

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.user.AuditUser
import java.time.ZoneId
import java.time.ZonedDateTime

@Component
class DataLoader(
    @Value("\${delius.db.username}") private val deliusDbUsername: String,
    dataManager: DataManager
) : BaseDataLoader(dataManager) {
    override fun systemUser() = AuditUser(IdGenerator.getAndIncrement(), deliusDbUsername)

    override fun setupData() {
        createReferenceData()
        createReleasablePerson(PersonGenerator.RELEASABLE)
        createRecallablePerson()
        createPersonToDie()
        createMatchablePerson()
        createNewCustodyPerson()
        createRecalledPerson()
        createHospitalReleased()
        createHospitalInCustody()
        createTemporaryAbsenceReturnFromRotl()
        createIrcReleased()
        createIrcInCustody()
        createReleasablePerson(PersonGenerator.RELEASABLE_ECSL_ACTIVE)
        createReleasablePerson(PersonGenerator.RELEASABLE_ECSL_INACTIVE)
        createAbsconded()
        createEtrInCustody()
        createEcslircInCustody()
        createReleasablePerson(PersonGenerator.RELEASE_HDC)
    }

    private fun createReferenceData() {
        saveAll(BusinessInteractionGenerator.ALL.values)
        save(ProbationAreaGenerator.DEFAULT)
        save(TeamGenerator.DEFAULT)
        save(StaffGenerator.UNALLOCATED)
        saveAll(
            listOf(
                ReferenceDataSetGenerator.RELEASE_TYPE,
                ReferenceDataSetGenerator.CUSTODIAL_STATUS,
                ReferenceDataSetGenerator.CUSTODY_EVENT_TYPE,
                ReferenceDataSetGenerator.TRANSFER_STATUS,
                ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON.set,
                ReferenceDataGenerator.PERSON_MANAGER_ALLOCATION_REASON.set,
                ReferenceDataGenerator.PRISON_MANAGER_ALLOCATION_REASON.set,
                ReferenceDataSetGenerator.ACCEPTED_DECISION,
                ReferenceDataSetGenerator.LICENCE_AREA_TRANSFER_REJECTION_REASON,
                ReferenceDataSetGenerator.AUTO_TRANSFER_REASON,
                ReferenceDataSetGenerator.DOMAIN_EVENT_TYPE
            )
        )
        saveAll(
            ReferenceDataGenerator.RELEASE_TYPE.values +
                ReferenceDataGenerator.CUSTODIAL_STATUS.values +
                ReferenceDataGenerator.CUSTODY_EVENT_TYPE.values +
                ReferenceDataGenerator.TRANSFER_STATUS.values +
                listOf(
                    ReferenceDataGenerator.PERSON_MANAGER_ALLOCATION_REASON,
                    ReferenceDataGenerator.PRISON_MANAGER_ALLOCATION_REASON,
                    ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON,
                    ReferenceDataGenerator.LC_REJECTED_DECISION,
                    ReferenceDataGenerator.LC_REJECTED_REASON,
                    ReferenceDataGenerator.AUTO_TRANSFER,
                    ReferenceDataGenerator.LC_TERMINATED_DOMAIN_EVENT
                )
        )
        saveAll(ReferenceDataGenerator.RECALL_REASON.values)
        saveAll(ReferenceDataGenerator.CONTACT_TYPE.values)
        saveAll(
            listOf(
                InstitutionGenerator.DEFAULT,
                InstitutionGenerator.MOVED_TO,
                InstitutionGenerator.MOVED_TO_WITH_POM
            )
        )
        saveAll(
            listOf(
                InstitutionGenerator.DEFAULT.probationArea!!,
                InstitutionGenerator.MOVED_TO_WITH_POM.probationArea!!
            )
        )
        val team = save(TeamGenerator.allStaff(InstitutionGenerator.DEFAULT.probationArea!!))
        val teamSwi = save(TeamGenerator.allStaff(InstitutionGenerator.MOVED_TO.probationArea!!))
        save(StaffGenerator.unallocated(teamSwi))
        val teamBir =
            save(TeamGenerator.allStaff(InstitutionGenerator.MOVED_TO_WITH_POM.probationArea!!))
        val prisonManager = PrisonManager(
            0,
            PersonGenerator.MATCHABLE_WITH_POM.id,
            ZonedDateTime.of(2023, 1, 1, 1, 0, 0, 0, ZoneId.systemDefault()),
            ReferenceDataGenerator.AUTO_TRANSFER,
            StaffGenerator.UNALLOCATED,
            teamBir,
            InstitutionGenerator.MOVED_TO_WITH_POM.probationArea!!,
            false
        )
        val prisonManager1 = PrisonManager(
            0,
            PersonGenerator.MATCHABLE_WITH_POM.id,
            ZonedDateTime.of(2023, 2, 1, 1, 0, 0, 0, ZoneId.systemDefault()),
            ReferenceDataGenerator.AUTO_TRANSFER,
            StaffGenerator.UNALLOCATED,
            teamBir,
            InstitutionGenerator.MOVED_TO_WITH_POM.probationArea!!,
            false
        )
        saveAll(listOf(prisonManager, prisonManager1))
        save(StaffGenerator.unallocated(team))
        save(StaffGenerator.unallocated(teamBir))
        saveAll(InstitutionGenerator.STANDARD_INSTITUTIONS.values)
        saveAll(InstitutionGenerator.STANDARD_INSTITUTIONS.values.mapNotNull { it.probationArea })
        val teams = saveAll(
            InstitutionGenerator.STANDARD_INSTITUTIONS.values
                .mapNotNull { it.probationArea }
                .map { TeamGenerator.allStaff(it) }
        )
        saveAll(teams.map { StaffGenerator.unallocated(it) })
    }

    private fun createPersonToDie() {
        createPerson(PersonGenerator.DIED)
        createEvent(EventGenerator.custodialEvent(PersonGenerator.DIED, InstitutionGenerator.DEFAULT))
    }

    private fun createReleasablePerson(person: Person) {
        createPerson(person)
        createEvent(EventGenerator.custodialEvent(person, InstitutionGenerator.DEFAULT))
    }

    private fun createRecallablePerson() {
        createPerson(PersonGenerator.RECALLABLE)
        val event = EventGenerator.previouslyReleasedEvent(
            person = PersonGenerator.RECALLABLE,
            institution = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY],
            releaseDate = NotificationGenerator.PRISONER_RECEIVED.message.occurredAt.minusMonths(6),
            lengthInDays = 999
        )
        createEvent(event)
        val conditions = listOf(LicenceConditionGenerator.generate(event), LicenceConditionGenerator.generate(event))
        conditions.forEach {
            save(it.mainCategory)
            save(it)
        }
    }

    private fun createMatchablePerson() {
        createPerson(PersonGenerator.MATCHABLE)
        createEvent(EventGenerator.custodialEvent(PersonGenerator.MATCHABLE, InstitutionGenerator.DEFAULT))
        createPerson(PersonGenerator.MATCHABLE_WITH_POM)
        createEvent(EventGenerator.custodialEvent(PersonGenerator.MATCHABLE_WITH_POM, InstitutionGenerator.DEFAULT))
    }

    private fun createNewCustodyPerson() {
        createPerson(PersonGenerator.NEW_CUSTODY)
        createEvent(
            EventGenerator.custodialEvent(
                PersonGenerator.NEW_CUSTODY,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN],
                CustodialStatusCode.SENTENCED_IN_CUSTODY
            )
        )
    }

    private fun createRecalledPerson() {
        createPerson(PersonGenerator.RECALLED)
        createEvent(
            EventGenerator.previouslyRecalledEvent(
                PersonGenerator.RECALLED,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN],
                CustodialStatusCode.RECALLED,
                recallDate = ZonedDateTime.parse("2023-08-04T08:09:36.649098+01:00")
            )
        )
    }

    private fun createHospitalReleased() {
        createPerson(PersonGenerator.HOSPITAL_RELEASED)
        createEvent(
            EventGenerator.previouslyReleasedEvent(
                PersonGenerator.HOSPITAL_RELEASED,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY],
                releaseDate = NotificationGenerator.PRISONER_HOSPITAL_RELEASED.message.occurredAt.minusDays(7)
            )
        )
    }

    private fun createHospitalInCustody() {
        createPerson(PersonGenerator.HOSPITAL_IN_CUSTODY)
        createEvent(
            EventGenerator.custodialEvent(
                PersonGenerator.HOSPITAL_IN_CUSTODY,
                InstitutionGenerator.DEFAULT
            )
        )
    }

    private fun createTemporaryAbsenceReturnFromRotl() {
        createPerson(PersonGenerator.ROTL)
        createEvent(
            EventGenerator.previouslyReleasedEvent(
                PersonGenerator.ROTL,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY],
                releaseDate = NotificationGenerator.PRISONER_ROTL_RETURN.message.occurredAt.minusDays(7)
            )
        )
    }

    private fun createIrcReleased() {
        createPerson(PersonGenerator.IRC_RELEASED)
        createEvent(
            EventGenerator.previouslyReleasedEvent(
                PersonGenerator.IRC_RELEASED,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY],
                releaseDate = NotificationGenerator.PRISONER_IRC_RELEASED.message.occurredAt.minusDays(7)
            )
        )
    }

    private fun createIrcInCustody() {
        createPerson(PersonGenerator.IRC_IN_CUSTODY)
        createEvent(
            EventGenerator.custodialEvent(
                PersonGenerator.IRC_IN_CUSTODY,
                InstitutionGenerator.DEFAULT
            )
        )
    }

    private fun createEtrInCustody() {
        createPerson(PersonGenerator.ETR_IN_CUSTODY)
        createEvent(
            EventGenerator.custodialEvent(
                PersonGenerator.ETR_IN_CUSTODY,
                InstitutionGenerator.DEFAULT
            )
        )
    }

    private fun createEcslircInCustody() {
        createPerson(PersonGenerator.ECSLIRC_IN_CUSTODY)
        createEvent(
            EventGenerator.custodialEvent(
                PersonGenerator.ECSLIRC_IN_CUSTODY,
                InstitutionGenerator.DEFAULT
            )
        )
    }

    private fun createPerson(person: Person) {
        save(person)
        save(PersonManagerGenerator.generate(person))
    }

    private fun createEvent(event: Event) {
        save(event)
        save(event.disposal!!.type)
        save(event.disposal!!)
        save(event.disposal!!.custody!!)
        save(OrderManagerGenerator.generate(event))
    }

    private fun createAbsconded() {
        createPerson(PersonGenerator.ABSCONDED)
        createEvent(
            EventGenerator.previouslyReleasedEvent(
                PersonGenerator.ABSCONDED,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]
            )
        )
    }
}
