package uk.gov.justice.digital.hmpps.data

import UserGenerator
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ContactOutcomeGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.repository.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.provider.StaffRepository
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val staffRepository: StaffRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository
) : CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()

        staffRepository.save(StaffGenerator.DEFAULT)

        contactTypeRepository.saveAll(
            listOf(
                ContactTypeGenerator.RECOMMENDATION_STARTED,
                ContactTypeGenerator.MANAGEMENT_OVERSIGHT_RECALL
            )
        )
        contactOutcomeRepository.saveAll(
            listOf(
                ContactOutcomeGenerator.DECISION_TO_RECALL,
                ContactOutcomeGenerator.DECISION_NOT_TO_RECALL
            )
        )
        personRepository.saveAll(
            listOf(
                PersonGenerator.RECOMMENDATION_STARTED,
                PersonGenerator.DECISION_TO_RECALL,
                PersonGenerator.DECISION_NOT_TO_RECALL
            )
        )
        personManagerRepository.saveAll(
            listOf(
                PersonGenerator.RECOMMENDATION_STARTED.manager!!,
                PersonGenerator.DECISION_TO_RECALL.manager!!,
                PersonGenerator.DECISION_NOT_TO_RECALL.manager!!
            )
        )
    }
}
