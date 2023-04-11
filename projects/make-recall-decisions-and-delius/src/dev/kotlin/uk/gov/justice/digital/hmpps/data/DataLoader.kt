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
        entityManager.run {
            persist(StaffGenerator.DEFAULT)

            persist(ContactTypeGenerator.RECOMMENDATION_STARTED)
            persist(ContactTypeGenerator.MANAGEMENT_OVERSIGHT_RECALL)
            persist(ContactOutcomeGenerator.DECISION_TO_RECALL)
            persist(ContactOutcomeGenerator.DECISION_NOT_TO_RECALL)
            persist(PersonGenerator.RECOMMENDATION_STARTED)
            persist(PersonGenerator.DECISION_TO_RECALL)
            persist(PersonGenerator.DECISION_NOT_TO_RECALL)

            persist(PersonGenerator.DEFAULT_PROVIDER)
            persist(PersonGenerator.DEFAULT_LAU)
            persist(PersonGenerator.DEFAULT_TEAM)
            persist(PersonGenerator.DEFAULT_STAFF)
            persist(PersonGenerator.RECOMMENDATION_STARTED.manager!!)
            persist(PersonGenerator.DECISION_TO_RECALL.manager!!)
            persist(PersonGenerator.DECISION_NOT_TO_RECALL.manager!!)

            persist(PersonGenerator.CASE_SUMMARY.gender)
            persist(PersonGenerator.CASE_SUMMARY.ethnicity)
            persist(PersonGenerator.CASE_SUMMARY.primaryLanguage)
            persist(PersonGenerator.CASE_SUMMARY)
            persist(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS.status)
            persist(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS)
            persist(PersonManagerGenerator.CASE_SUMMARY.provider)
            persist(PersonManagerGenerator.CASE_SUMMARY.team.district)
            persist(PersonManagerGenerator.CASE_SUMMARY.team)
            persist(PersonManagerGenerator.CASE_SUMMARY.staff)
            persist(PersonManagerGenerator.CASE_SUMMARY)
            persist(RegistrationGenerator.MAPPA.type.flag)
            persist(RegistrationGenerator.MAPPA.type)
            persist(RegistrationGenerator.MAPPA.category)
            persist(RegistrationGenerator.MAPPA.level)
            persist(RegistrationGenerator.MAPPA)
            persist(RegistrationGenerator.HIGH_ROSH.type.flag)
            persist(RegistrationGenerator.HIGH_ROSH.type)
            persist(RegistrationGenerator.HIGH_ROSH)
            persist(EventGenerator.CASE_SUMMARY)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.entryLengthUnit)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.type)
            persist(EventGenerator.CASE_SUMMARY.disposal!!)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.status)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.sentenceExpiryDate!!.type)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.sentenceExpiryDate)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.licenceExpiryDate!!.type)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.licenceExpiryDate)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.licenceConditions[0].mainCategory)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.licenceConditions[0])

            persist(UserGenerator.TEST_USER1)
            persist(UserGenerator.TEST_USER2)
            persist(PersonGenerator.EXCLUDED)
            persist(PersonGenerator.EXCLUDED.exclusions[0])
            persist(PersonGenerator.RESTRICTED)
            persist(PersonGenerator.RESTRICTED.restrictions[0])
            persist(PersonGenerator.NO_ACCESS_LIMITATIONS)
        }
    }
}
