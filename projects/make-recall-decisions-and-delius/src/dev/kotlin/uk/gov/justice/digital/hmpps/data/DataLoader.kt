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
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
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

        entityManager.persist(PersonGenerator.DEFAULT_PROVIDER)
        entityManager.persist(PersonGenerator.DEFAULT_LAU)
        entityManager.persist(PersonGenerator.DEFAULT_TEAM)
        entityManager.persist(PersonGenerator.DEFAULT_STAFF)
        entityManager.persist(PersonGenerator.RECOMMENDATION_STARTED.manager!!)
        entityManager.persist(PersonGenerator.DECISION_TO_RECALL.manager!!)
        entityManager.persist(PersonGenerator.DECISION_NOT_TO_RECALL.manager!!)

        entityManager.persist(PersonGenerator.CASE_SUMMARY.gender)
        entityManager.persist(PersonGenerator.CASE_SUMMARY.ethnicity)
        entityManager.persist(PersonGenerator.CASE_SUMMARY.primaryLanguage)
        entityManager.persist(PersonGenerator.CASE_SUMMARY)
        entityManager.persist(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS.status)
        entityManager.persist(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS)
        entityManager.persist(PersonManagerGenerator.CASE_SUMMARY.provider)
        entityManager.persist(PersonManagerGenerator.CASE_SUMMARY.team.district)
        entityManager.persist(PersonManagerGenerator.CASE_SUMMARY.team)
        entityManager.persist(PersonManagerGenerator.CASE_SUMMARY.staff)
        entityManager.persist(PersonManagerGenerator.CASE_SUMMARY)
        entityManager.persist(RegistrationGenerator.MAPPA.type)
        entityManager.persist(RegistrationGenerator.HIGH_ROSH.type)
        entityManager.persist(RegistrationGenerator.MAPPA)
        entityManager.persist(RegistrationGenerator.HIGH_ROSH)
        entityManager.persist(EventGenerator.CASE_SUMMARY)
        entityManager.persist(EventGenerator.CASE_SUMMARY.disposal!!.entryLengthUnit)
        entityManager.persist(EventGenerator.CASE_SUMMARY.disposal!!.type)
        entityManager.persist(EventGenerator.CASE_SUMMARY.disposal!!)
        entityManager.persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.status)
        entityManager.persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!)
        entityManager.persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.sentenceExpiryDate!!.type)
        entityManager.persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.sentenceExpiryDate)
        entityManager.persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.licenceExpiryDate!!.type)
        entityManager.persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.licenceExpiryDate)
    }
}
