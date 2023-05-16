package uk.gov.justice.digital.hmpps.data

import UserGenerator
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val userRepository: UserRepository,
    private val em: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        em.saveAll(
            ProbationAreaGenerator.DEFAULT_PA,
            ProbationAreaGenerator.DEFAULT_BOROUGH,
            ProbationAreaGenerator.DEFAULT_LDU,
            CourtAppearanceGenerator.DEFAULT_CA_TYPE,
            CourtAppearanceGenerator.DEFAULT_COURT,
            CourtAppearanceGenerator.DEFAULT_PERSON,
            CourtAppearanceGenerator.DEFAULT_EVENT,
            CourtAppearanceGenerator.DEFAULT_CA
        )
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
}
