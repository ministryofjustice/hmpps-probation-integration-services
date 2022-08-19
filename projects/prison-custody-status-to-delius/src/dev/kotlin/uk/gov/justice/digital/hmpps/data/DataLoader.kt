package uk.gov.justice.digital.hmpps.data

import IdGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.config.ServiceContext
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.ReferenceDataSetRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.institution.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.user.User
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    @Value("\${delius.db.username}") private val deliusDbUsername: String,
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val referenceDataSetRepository: ReferenceDataSetRepository,
    private val recallReasonRepository: RecallReasonRepository,
    private val institutionRepository: InstitutionRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val disposalRepository: DisposalRepository,
    private val disposalTypeRepository: DisposalTypeRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(User(IdGenerator.getAndIncrement(), deliusDbUsername))
        serviceContext.setUp()
        businessInteractionRepository.saveAll(BusinessInteractionGenerator.ALL.values)

        referenceDataSetRepository.save(ReferenceDataSetGenerator.RELEASE_TYPE)
        referenceDataRepository.saveAll(ReferenceDataGenerator.RELEASE_TYPE.values)
        referenceDataSetRepository.save(ReferenceDataSetGenerator.CUSTODIAL_STATUS)
        referenceDataRepository.saveAll(ReferenceDataGenerator.CUSTODIAL_STATUS.values)
        recallReasonRepository.saveAll(ReferenceDataGenerator.RECALL_REASON.values)
        institutionRepository.save(InstitutionGenerator.DEFAULT)

        val releasablePerson = PersonGenerator.RELEASABLE
        personRepository.save(releasablePerson)

        val releasableEvent = EventGenerator.custodialEvent(releasablePerson, InstitutionGenerator.DEFAULT)
        eventRepository.save(releasableEvent)
        disposalTypeRepository.save(releasableEvent.disposal!!.disposalType)
        disposalRepository.save(releasableEvent.disposal!!)
    }
}
