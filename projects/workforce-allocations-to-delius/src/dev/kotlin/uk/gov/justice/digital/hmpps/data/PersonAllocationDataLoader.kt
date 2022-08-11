package uk.gov.justice.digital.hmpps.data

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.ManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.data.generator.TransferReasonGenerator
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficerRepository

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
    private val requirementRepository: RequirementRepository,
    private val requirementManagerRepository: RequirementManagerRepository
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

        disposalRepository.save(DisposalGenerator.DEFAULT)
        RequirementManagerGenerator.DEFAULT = createRequirementWithManager(RequirementGenerator.DEFAULT)
        RequirementManagerGenerator.NEW = createRequirementWithManager(RequirementGenerator.NEW)
        RequirementManagerGenerator.HISTORIC = createRequirementWithManager(RequirementGenerator.HISTORIC)
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

    fun createEventWithManager(event: Event): OrderManager {
        eventRepository.save(event)
        return orderManagerRepository.save(
            OrderManagerGenerator.generate(
                eventId = event.id, startDateTime = ManagerGenerator.START_DATE_TIME
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
