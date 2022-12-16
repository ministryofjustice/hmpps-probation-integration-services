package uk.gov.justice.digital.hmpps.data

import UserGenerator
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.DEFAULT_CUSTODY
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateCustodialSentence
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateDisposal
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateEvent
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateOrderManager
import uk.gov.justice.digital.hmpps.data.repository.DatasetRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.data.repository.EventRepository
import uk.gov.justice.digital.hmpps.data.repository.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val datasetRepository: DatasetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val orderManagerRepository: OrderManagerRepository,
    private val disposalRepository: DisposalRepository,
    private val custodyRepository: CustodyRepository,
) : CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()

        datasetRepository.saveAll(
            listOf(
                ReferenceDataGenerator.DS_CUSTODY_STATUS,
                ReferenceDataGenerator.DS_KEY_DATE_TYPE
            )
        )
        referenceDataRepository.save(ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS)
        contactTypeRepository.save(ContactTypeGenerator.EDSS)

        personRepository.save(PersonGenerator.DEFAULT)

        val event = eventRepository.save(generateEvent(PersonGenerator.DEFAULT))
        orderManagerRepository.save(generateOrderManager(event))
        val disposal = disposalRepository.save(generateDisposal(event))
        val custody = custodyRepository.save(
            generateCustodialSentence(
                ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS,
                disposal,
                "38339A"
            )
        )
        DEFAULT_CUSTODY = custody
    }
}
