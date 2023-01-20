package uk.gov.justice.digital.hmpps.data

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtReportGenerator
import uk.gov.justice.digital.hmpps.data.generator.CustodyGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionalReportGenerator
import uk.gov.justice.digital.hmpps.data.generator.KeyDateGenerator
import uk.gov.justice.digital.hmpps.data.generator.ManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.OasysAssessmentGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.OgrsAssessmentGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TransferReasonGenerator
import uk.gov.justice.digital.hmpps.data.repository.CourtReportRepository
import uk.gov.justice.digital.hmpps.data.repository.CustodyRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.InstitutionalReportRepository
import uk.gov.justice.digital.hmpps.data.repository.KeyDateRepository
import uk.gov.justice.digital.hmpps.data.repository.MainOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs.OASYSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.registration.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.AdditionalOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff

@Component
@Profile("dev", "integration-test")
class PersonAllocationDataLoader(
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val eventRepository: EventRepository,
    private val orderManagerRepository: OrderManagerRepository,
    private val disposalRepository: DisposalRepository,
    private val disposalTypeRepository: DisposalTypeRepository,
    private val requirementRepository: RequirementRepository,
    private val requirementManagerRepository: RequirementManagerRepository,
    private val contactRepository: ContactRepository,
    private val keyDateRepository: KeyDateRepository,
    private val custodyRepository: CustodyRepository,
    private val institutionalReportRepository: InstitutionalReportRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val additionalOffenceRepository: AdditionalOffenceRepository,
    private val courtReportRepository: CourtReportRepository,
    private val oasysAssessmentRepository: OASYSAssessmentRepository,
    private val ogrsAssessmentRepository: OGRSAssessmentRepository,
    private val registrationRepository: RegistrationRepository
) {
    fun loadData() {
        transferReasonRepository.saveAll(listOf(TransferReasonGenerator.CASE_ORDER, TransferReasonGenerator.COMPONENT))

        val (dpm, dro) = createPersonWithManagers(PersonGenerator.DEFAULT)
        PersonManagerGenerator.DEFAULT = dpm
        ResponsibleOfficerGenerator.DEFAULT = dro

        val (npm, nro) = createPersonWithManagers(PersonGenerator.NEW_PM)
        PersonManagerGenerator.NEW = npm
        ResponsibleOfficerGenerator.NEW = nro

        val (hpm, hro) = createPersonWithManagers(PersonGenerator.HISTORIC_PM)
        PersonManagerGenerator.HISTORIC = hpm
        ResponsibleOfficerGenerator.HISTORIC = hro

        OrderManagerGenerator.DEFAULT = createEventWithManager(EventGenerator.DEFAULT)
        OrderManagerGenerator.NEW = createEventWithManager(EventGenerator.NEW)
        OrderManagerGenerator.HISTORIC = createEventWithManager(EventGenerator.HISTORIC)
        OrderManagerGenerator.INACTIVE_EVENT =
            createEventWithManager(EventGenerator.INACTIVE, StaffGenerator.STAFF_FOR_INACTIVE_EVENT)

        disposalTypeRepository.save(DisposalGenerator.DEFAULT.type)
        disposalRepository.saveAll(listOf(DisposalGenerator.DEFAULT, DisposalGenerator.INACTIVE))
        RequirementManagerGenerator.DEFAULT = createRequirementWithManager(RequirementGenerator.DEFAULT)
        RequirementManagerGenerator.NEW = createRequirementWithManager(RequirementGenerator.NEW)
        RequirementManagerGenerator.HISTORIC = createRequirementWithManager(RequirementGenerator.HISTORIC)

        custodyRepository.save(CustodyGenerator.DEFAULT)
        keyDateRepository.save(KeyDateGenerator.DEFAULT)
        institutionalReportRepository.save(InstitutionalReportGenerator.DEFAULT)

        contactRepository.save(ContactGenerator.INITIAL_APPOINTMENT)

        courtReportRepository.save(CourtReportGenerator.DEFAULT)

        registrationRepository.save(RegistrationGenerator.DEFAULT)
        ogrsAssessmentRepository.save(OgrsAssessmentGenerator.DEFAULT)
        oasysAssessmentRepository.save(OasysAssessmentGenerator.DEFAULT)
    }

    fun createPersonWithManagers(person: Person): Pair<PersonManager, ResponsibleOfficer> {
        personRepository.save(person)
        val pm = personManagerRepository.save(
            PersonManagerGenerator.generate(
                personId = person.id, startDateTime = ManagerGenerator.START_DATE_TIME
            )
        )
        val ro = responsibleOfficerRepository.save(
            ResponsibleOfficerGenerator.generate(
                personId = person.id, communityManager = pm, startDateTime = ManagerGenerator.START_DATE_TIME
            )
        )
        return Pair(pm, ro)
    }

    fun createEventWithManager(event: Event, staff: Staff? = null): OrderManager {
        eventRepository.save(event)
        mainOffenceRepository.save(OffenceGenerator.generateMainOffence(event = event))
        additionalOffenceRepository.save(OffenceGenerator.generateAdditionalOffence(event = event))
        return orderManagerRepository.save(
            OrderManagerGenerator.generate(
                eventId = event.id,
                startDateTime = ManagerGenerator.START_DATE_TIME,
                staff = staff ?: StaffGenerator.DEFAULT
            )
        )
    }

    fun createRequirementWithManager(requirement: Requirement): RequirementManager {
        requirementRepository.save(requirement)
        return requirementManagerRepository.save(
            RequirementManagerGenerator.generate(
                requirementId = requirement.id, startDateTime = ManagerGenerator.START_DATE_TIME
            )
        )
    }
}
