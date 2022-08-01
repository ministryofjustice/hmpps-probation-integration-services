package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.data.generator.TransferReasonGenerator
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficerRepository

@Component
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
        personRepository.save(PersonGenerator.DEFAULT)
        PersonManagerGenerator.DEFAULT = personManagerRepository.save(PersonManagerGenerator.DEFAULT)
        ResponsibleOfficerGenerator.DEFAULT = responsibleOfficerRepository.save(ResponsibleOfficerGenerator.generate())

        transferReasonRepository.saveAll(listOf(TransferReasonGenerator.CASE_ORDER, TransferReasonGenerator.COMPONENT))

        eventRepository.save(EventGenerator.DEFAULT)
        orderManagerRepository.save(OrderManagerGenerator.DEFAULT)

        disposalRepository.save(RequirementGenerator.DEFAULT.disposal)
        requirementRepository.save(RequirementGenerator.DEFAULT)
        requirementManagerRepository.save(RequirementManagerGenerator.DEFAULT)
    }
}
