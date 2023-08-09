package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.data.repository.ReferenceDataSetRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
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
    private val probationAreaRepository: ProbationAreaRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(AuditUser(IdGenerator.getAndIncrement(), deliusDbUsername))
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        createReferenceData()
        createReleasablePerson()
        createRecallablePerson()
        createPerson(PersonGenerator.DIED)
        createMatchablePerson()
        createNewCustodyPerson()
        createRecalledPerson()
    }

    private fun createReferenceData() {
        businessInteractionRepository.saveAll(BusinessInteractionGenerator.ALL.values)
        probationAreaRepository.save(ProbationAreaGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)
        staffRepository.save(StaffGenerator.UNALLOCATED)
        referenceDataSetRepository.save(ReferenceDataSetGenerator.RELEASE_TYPE)
        referenceDataRepository.saveAll(ReferenceDataGenerator.RELEASE_TYPE.values)
        referenceDataSetRepository.save(ReferenceDataSetGenerator.CUSTODIAL_STATUS)
        referenceDataRepository.saveAll(ReferenceDataGenerator.CUSTODIAL_STATUS.values)
        referenceDataSetRepository.save(ReferenceDataSetGenerator.CUSTODY_EVENT_TYPE)
        referenceDataRepository.saveAll(ReferenceDataGenerator.CUSTODY_EVENT_TYPE.values)
        referenceDataSetRepository.save(ReferenceDataSetGenerator.TRANSFER_STATUS)
        referenceDataRepository.saveAll(ReferenceDataGenerator.TRANSFER_STATUS.values)
        referenceDataSetRepository.save(ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON.set)
        referenceDataRepository.save(ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON)
        referenceDataSetRepository.save(ReferenceDataGenerator.PERSON_MANAGER_ALLOCATION_REASON.set)
        referenceDataRepository.save(ReferenceDataGenerator.PERSON_MANAGER_ALLOCATION_REASON)
        referenceDataSetRepository.save(ReferenceDataGenerator.PRISON_MANAGER_ALLOCATION_REASON.set)
        referenceDataRepository.save(ReferenceDataGenerator.PRISON_MANAGER_ALLOCATION_REASON)
        recallReasonRepository.saveAll(ReferenceDataGenerator.RECALL_REASON.values)
        contactTypeRepository.saveAll(ReferenceDataGenerator.CONTACT_TYPE.values)
        institutionRepository.saveAll(listOf(InstitutionGenerator.DEFAULT, InstitutionGenerator.MOVED_TO))
        probationAreaRepository.save(InstitutionGenerator.DEFAULT.probationArea!!)
        val team = teamRepository.save(TeamGenerator.allStaff(InstitutionGenerator.DEFAULT.probationArea!!))
        staffRepository.save(StaffGenerator.unallocated(team))
        institutionRepository.saveAll(InstitutionGenerator.STANDARD_INSTITUTIONS.values)
        probationAreaRepository.saveAll(InstitutionGenerator.STANDARD_INSTITUTIONS.values.mapNotNull { it.probationArea })
        val teams = teamRepository.saveAll(
            InstitutionGenerator.STANDARD_INSTITUTIONS.values
                .mapNotNull { it.probationArea }
                .map { TeamGenerator.allStaff(it) }
        )
        staffRepository.saveAll(teams.map { StaffGenerator.unallocated(it) })
    }

    private fun createReleasablePerson() {
        createPerson(PersonGenerator.RELEASABLE)
        createEvent(EventGenerator.custodialEvent(PersonGenerator.RELEASABLE, InstitutionGenerator.DEFAULT))
    }

    private fun createRecallablePerson() {
        createPerson(PersonGenerator.RECALLABLE)
        createEvent(
            EventGenerator.previouslyReleasedEvent(
                person = PersonGenerator.RECALLABLE,
                institution = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!,
                releaseDate = MessageGenerator.PRISONER_RECEIVED.occurredAt.minusMonths(6),
                lengthInDays = 999
            )
        )
    }

    private fun createMatchablePerson() {
        createPerson(PersonGenerator.MATCHABLE)
        createEvent(EventGenerator.custodialEvent(PersonGenerator.MATCHABLE, InstitutionGenerator.DEFAULT))
    }

    private fun createNewCustodyPerson() {
        createPerson(PersonGenerator.NEW_CUSTODY)
        createEvent(
            EventGenerator.custodialEvent(
                PersonGenerator.NEW_CUSTODY,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN]!!,
                CustodialStatusCode.SENTENCED_IN_CUSTODY
            )
        )
    }

    private fun createRecalledPerson() {
        createPerson(PersonGenerator.RECALLED)
        createEvent(
            EventGenerator.previouslyRecalledEvent(
                PersonGenerator.RECALLED,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN]!!,
                CustodialStatusCode.RECALLED,
                recallDate = ZonedDateTime.parse("2023-08-04T08:09:36.649098+01:00")
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
}
