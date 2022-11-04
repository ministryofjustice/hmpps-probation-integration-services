package uk.gov.justice.digital.hmpps.data

import UserGenerator
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.repository.EventRepository
import uk.gov.justice.digital.hmpps.data.repository.StandardReferenceListRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.NsiRepository
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val nsiRepository: NsiRepository,
    private val eventRepository: EventRepository,
    private val standardReferenceListRepository: StandardReferenceListRepository
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()
        eventRepository.save(NsiGenerator.EVENT)
        standardReferenceListRepository.save(NsiGenerator.OUTCOME)
        standardReferenceListRepository.save(NsiGenerator.NSI_STATUS)
        nsiRepository.save(NsiGenerator.BREACH_DETAILS_NSI)
    }
}
