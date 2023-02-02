package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.OffenderGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderRepository
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class PoeDataLoader(
    private val userRepository: UserRepository,
    private val offenderRepository: OffenderRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        offenderRepository.save(OffenderGenerator.DEFAULT)
    }
}
