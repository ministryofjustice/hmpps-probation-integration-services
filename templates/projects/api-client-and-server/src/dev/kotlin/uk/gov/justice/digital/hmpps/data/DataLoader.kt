package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.user.UserRepository
import UserGenerator

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val userRepository: UserRepository,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        // Perform dev/test database setup here, using JPA repositories and generator classes...
    }
}
