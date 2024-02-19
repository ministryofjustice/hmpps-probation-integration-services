package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val entityManager: EntityManager,
    private val auditUserRepository: AuditUserRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(ape: ApplicationReadyEvent) {
        entityManager.run {
            persist(ContactTypeGenerator.MANAGEMENT_OVERSIGHT_RECALL)
            persist(ContactTypeGenerator.RECOMMENDATION_DELETED)
            persist(ContactOutcomeGenerator.DECISION_TO_RECALL)
            persist(ContactOutcomeGenerator.DECISION_NOT_TO_RECALL)
            persist(PersonGenerator.RECOMMENDATION_STARTED)
            persist(PersonGenerator.DECISION_TO_RECALL)
            persist(PersonGenerator.DECISION_NOT_TO_RECALL)
            persist(PersonGenerator.RECOMMENDATION_DELETED)

            persist(PersonGenerator.DEFAULT_PROVIDER)
            persist(PersonGenerator.DEFAULT_LAU)
            persist(PersonGenerator.DEFAULT_TEAM)
            persist(PersonGenerator.DEFAULT_STAFF)
            persist(PersonGenerator.RECOMMENDATION_STARTED.manager!!)
            persist(PersonGenerator.DECISION_TO_RECALL.manager!!)
            persist(PersonGenerator.DECISION_NOT_TO_RECALL.manager!!)
            persist(PersonGenerator.RECOMMENDATION_DELETED.manager!!)

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
            persist(EventGenerator.CASE_SUMMARY.disposal!!.secondEntryLengthUnit)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.entryLengthUnit)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.type)
            persist(EventGenerator.CASE_SUMMARY.disposal!!)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.status)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.sentenceExpiryDate.single().type)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.sentenceExpiryDate.single())
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.licenceExpiryDate.single().type)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.custody!!.licenceExpiryDate.single())
            persist(EventGenerator.CASE_SUMMARY.disposal!!.licenceConditions[0].mainCategory)
            persist(EventGenerator.CASE_SUMMARY.disposal!!.licenceConditions[0])
            persist(ContactGenerator.DEFAULT_OUTCOME)
            persist(ContactGenerator.DEFAULT_TYPE)
            persist(ContactGenerator.SYSTEM_GENERATED_TYPE)
            persist(ContactGenerator.DEFAULT)
            persist(ContactGenerator.SYSTEM_GENERATED)
            persist(ContactGenerator.FUTURE)
            persist(ContactGenerator.PAST)
            persist(ContactGenerator.WITH_DOCUMENTS)
            ContactGenerator.WITH_DOCUMENTS.documents.forEach { persist(it) }

            persist(UserGenerator.TEST_USER1)
            persist(UserGenerator.TEST_USER2)
            persist(PersonGenerator.EXCLUDED)
            persist(PersonGenerator.EXCLUDED.exclusions[0])
            persist(PersonGenerator.RESTRICTED)
            persist(PersonGenerator.RESTRICTED.restrictions[0])
            persist(PersonGenerator.NO_ACCESS_LIMITATIONS)
            persist(UserGenerator.WITHOUT_STAFF)
            persist(UserGenerator.WITH_STAFF.staff)
            persist(UserGenerator.WITH_STAFF)
            persist(UserGenerator.USER_DETAILS.staff)
            persist(UserGenerator.USER_DETAILS)
        }
    }
}
