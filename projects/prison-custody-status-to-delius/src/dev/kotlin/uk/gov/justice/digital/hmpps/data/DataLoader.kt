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
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.ReleaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.entity.TeamRepository
import uk.gov.justice.digital.hmpps.user.AuditUser
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

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

        val releasablePerson = PersonGenerator.RELEASABLE
        personRepository.save(releasablePerson)
        val releasableEvent = EventGenerator.custodialEvent(releasablePerson, InstitutionGenerator.DEFAULT)
        eventRepository.save(releasableEvent)
        disposalTypeRepository.save(releasableEvent.disposal!!.type)
        disposalRepository.save(releasableEvent.disposal!!)
        custodyRepository.save(releasableEvent.disposal!!.custody!!)
        orderManagerRepository.save(OrderManagerGenerator.generate(releasableEvent))

        val recallablePerson = PersonGenerator.RECALLABLE
        personRepository.save(recallablePerson)
        val recallableEvent = EventGenerator.previouslyReleasedEvent(
            person = recallablePerson,
            institution = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!,
            releaseDate = MessageGenerator.PRISONER_RECEIVED.occurredAt.minusMonths(6),
            lengthInDays = 999
        )
        eventRepository.save(recallableEvent)
        disposalTypeRepository.save(recallableEvent.disposal!!.type)
        disposalRepository.save(recallableEvent.disposal!!)
        custodyRepository.save(recallableEvent.disposal!!.custody!!)
        releaseRepository.save(recallableEvent.disposal!!.custody!!.mostRecentRelease()!!)
        orderManagerRepository.save(OrderManagerGenerator.generate(recallableEvent))
        personManagerRepository.save(PersonManagerGenerator.generate(recallablePerson))

        personRepository.save(PersonGenerator.DIED)
        personManagerRepository.save(PersonManagerGenerator.generate(PersonGenerator.DIED))

        personRepository.save(PersonGenerator.MOVEABLE)
        personManagerRepository.save(PersonManagerGenerator.generate(PersonGenerator.MOVEABLE))
        val moveableEvent = EventGenerator.custodialEvent(PersonGenerator.MOVEABLE, InstitutionGenerator.DEFAULT)
        eventRepository.save(moveableEvent)
        disposalTypeRepository.save(moveableEvent.disposal!!.type)
        disposalRepository.save(moveableEvent.disposal!!)
        custodyRepository.save(moveableEvent.disposal!!.custody!!)
        orderManagerRepository.save(OrderManagerGenerator.generate(moveableEvent))
    }
}
