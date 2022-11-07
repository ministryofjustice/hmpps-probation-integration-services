package uk.gov.justice.digital.hmpps.data

import UserGenerator
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.repository.EventRepository
import uk.gov.justice.digital.hmpps.data.repository.OffenderRepository
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
    private val offenderRepository: OffenderRepository
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()
        offenderRepository.save(NsiGenerator.DEFAULT_NSI.offender)
        eventRepository.save(NsiGenerator.DEFAULT_EVENT)
        nsiRepository.save(NsiGenerator.DEFAULT_NSI)
    }
}
