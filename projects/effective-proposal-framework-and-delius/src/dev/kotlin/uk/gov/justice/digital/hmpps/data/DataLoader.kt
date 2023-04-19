package uk.gov.justice.digital.hmpps.data

import UserGenerator
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.epf.entity.ReferenceData
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
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
            PersonGenerator.DEFAULT_GENDER,
            PersonGenerator.DEFAULT,
            SentenceGenerator.DEFAULT_COURT,
            SentenceGenerator.DEFAULT_EVENT,
            SentenceGenerator.DEFAULT_SENTENCE,
            SentenceGenerator.DEFAULT_COURT_APPEARANCE,
            ProviderGenerator.DEFAULT,
            ManagerGenerator.DEFAULT_PERSON_MANAGER,
            ManagerGenerator.DEFAULT_RESPONSIBLE_OFFICER,
        )
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
}
