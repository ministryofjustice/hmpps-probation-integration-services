package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.UserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val userRepository: UserRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        userRepository.save(UserGenerator.USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        userRepository.save(UserGenerator.TEST_USER)
        userRepository.save(UserGenerator.INACTIVE_USER)
    }
}
