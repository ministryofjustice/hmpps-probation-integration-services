package uk.gov.justice.digital.hmpps.data

import UserGenerator
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactOutcomeGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val entityManager: EntityManager,
    private val userRepository: UserRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    @Transactional
    override fun onApplicationEvent(ape: ApplicationReadyEvent) {
        entityManager.persist(StaffGenerator.DEFAULT)

        entityManager.persist(ContactTypeGenerator.RECOMMENDATION_STARTED)
        entityManager.persist(ContactTypeGenerator.MANAGEMENT_OVERSIGHT_RECALL)
        entityManager.persist(ContactOutcomeGenerator.DECISION_TO_RECALL)
        entityManager.persist(ContactOutcomeGenerator.DECISION_NOT_TO_RECALL)
        entityManager.persist(PersonGenerator.RECOMMENDATION_STARTED)
        entityManager.persist(PersonGenerator.DECISION_TO_RECALL)
        entityManager.persist(PersonGenerator.DECISION_NOT_TO_RECALL)

        entityManager.persist(PersonGenerator.RECOMMENDATION_STARTED.manager!!)
        entityManager.persist(PersonGenerator.DECISION_TO_RECALL.manager!!)
        entityManager.persist(PersonGenerator.DECISION_NOT_TO_RECALL.manager!!)

        entityManager.persist(PersonGenerator.CASE_SUMMARY.gender)
        entityManager.persist(PersonGenerator.CASE_SUMMARY.ethnicity)
        entityManager.persist(PersonGenerator.CASE_SUMMARY.primaryLanguage)
        entityManager.persist(PersonGenerator.CASE_SUMMARY)
        entityManager.persist(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS.status)
        entityManager.persist(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS)
    }
}
