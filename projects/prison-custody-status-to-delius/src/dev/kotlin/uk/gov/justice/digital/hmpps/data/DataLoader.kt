package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.data.repository.ReferenceDataSetRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionCategory
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.ReleaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.entity.TeamRepository
import uk.gov.justice.digital.hmpps.user.AuditUser
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.ZoneId
import java.time.ZonedDateTime

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    @Value("\${delius.db.username}") private val deliusDbUsername: String,
    private val auditUserRepository: AuditUserRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val referenceDataSetRepository: ReferenceDataSetRepository,
    private val recallReasonRepository: RecallReasonRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val institutionRepository: InstitutionRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val disposalTypeRepository: DisposalTypeRepository,
    private val disposalRepository: DisposalRepository,
    private val custodyRepository: CustodyRepository,
    private val orderManagerRepository: OrderManagerRepository,
    private val releaseRepository: ReleaseRepository,
    private val recallRepository: RecallRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val probationAreaRepository: ProbationAreaRepository,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val licenceConditionCategoryRepository: LicenceConditionCategoryRepository,
    private val prisonManagerRepository: PrisonManagerRepository,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(AuditUser(IdGenerator.getAndIncrement(), deliusDbUsername))
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
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
        businessInteractionRepository.saveAll(BusinessInteractionGenerator.ALL.values)
        probationAreaRepository.save(ProbationAreaGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)
        staffRepository.save(StaffGenerator.UNALLOCATED)
        referenceDataSetRepository.saveAll(
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
                ReferenceDataSetGenerator.AUTO_TRANSFER_REASON
            )
        )
        referenceDataRepository.saveAll(
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
                    ReferenceDataGenerator.AUTO_TRANSFER
                )
        )
        recallReasonRepository.saveAll(ReferenceDataGenerator.RECALL_REASON.values)
        contactTypeRepository.saveAll(ReferenceDataGenerator.CONTACT_TYPE.values)
        institutionRepository.saveAll(
            listOf(
                InstitutionGenerator.DEFAULT,
                InstitutionGenerator.MOVED_TO,
                InstitutionGenerator.MOVED_TO_WITH_POM
            )
        )
        probationAreaRepository.saveAll(
            listOf(
                InstitutionGenerator.DEFAULT.probationArea!!,
                InstitutionGenerator.MOVED_TO_WITH_POM.probationArea!!
            )
        )
        val team = teamRepository.save(TeamGenerator.allStaff(InstitutionGenerator.DEFAULT.probationArea!!))
        val teamBir =
            teamRepository.save(TeamGenerator.allStaff(InstitutionGenerator.MOVED_TO_WITH_POM.probationArea!!))
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
        prisonManagerRepository.saveAll(listOf(prisonManager, prisonManager1))
        staffRepository.save(StaffGenerator.unallocated(team))
        staffRepository.save(StaffGenerator.unallocated(teamBir))
        institutionRepository.saveAll(InstitutionGenerator.STANDARD_INSTITUTIONS.values)
        probationAreaRepository.saveAll(InstitutionGenerator.STANDARD_INSTITUTIONS.values.mapNotNull { it.probationArea })
        val teams = teamRepository.saveAll(
            InstitutionGenerator.STANDARD_INSTITUTIONS.values
                .mapNotNull { it.probationArea }
                .map { TeamGenerator.allStaff(it) }
        )
        staffRepository.saveAll(teams.map { StaffGenerator.unallocated(it) })
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
            licenceConditionCategoryRepository.save(it.mainCategory)
            licenceConditionRepository.save(it)
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
        personRepository.save(person)
        personManagerRepository.save(PersonManagerGenerator.generate(person))
    }

    private fun createEvent(event: Event) {
        eventRepository.save(event)
        disposalTypeRepository.save(event.disposal!!.type)
        disposalRepository.save(event.disposal!!)
        custodyRepository.save(event.disposal!!.custody!!)
        orderManagerRepository.save(OrderManagerGenerator.generate(event))
        val release = event.disposal?.custody?.mostRecentRelease()
        val recall = release?.recall
        if (recall != null) {
            release.recall = null
        }
        release?.also { releaseRepository.save(it) }
        recall?.also { recallRepository.save(it) }
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

interface LicenceConditionCategoryRepository : JpaRepository<LicenceConditionCategory, Long>
