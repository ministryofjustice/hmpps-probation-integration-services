package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator
import uk.gov.justice.digital.hmpps.data.generator.LaoGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.model.Category
import uk.gov.justice.digital.hmpps.model.Level
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager,
    private val transactionTemplate: TransactionTemplate,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
        auditUserRepository.save(UserGenerator.LIMITED_ACCESS_USER)
    }

    override fun onApplicationEvent(applicationReadyEvent: ApplicationReadyEvent) {
        transactionTemplate.execute {
            with(entityManager) {
                persist(DataGenerator.DEFAULT_PROVIDER)
                persist(DataGenerator.DEFAULT_TEAM)
                persist(DataGenerator.JOHN_SMITH)
                persist(DataGenerator.JS_USER)
                persist(DataGenerator.PERSON)
                persist(DataGenerator.PERSON_MANAGER)
                persist(DataGenerator.OFFENCE)
                persist(DataGenerator.COURT)
                persist(DataGenerator.COURT_APPEARANCE_TYPE)
                persist(DataGenerator.COURT_APPEARANCE_PLEA)
                persist(DataGenerator.DISPOSAL_TYPE)
                persist(DataGenerator.MONTHS)
                persist(DataGenerator.EVENT)
                persist(DataGenerator.EVENT.disposal)
                persist(DataGenerator.EVENT.mainOffence)
                DataGenerator.EVENT.additionalOffences.forEach { persist(it) }
                DataGenerator.EVENT.courtAppearances.forEach { persist(it) }
                persist(RegistrationGenerator.CHILD_CONCERNS_TYPE)
                persist(RegistrationGenerator.generate(RegistrationGenerator.CHILD_CONCERNS_TYPE))
                persist(RegistrationGenerator.CHILD_PROTECTION_TYPE)
                persist(RegistrationGenerator.generate(RegistrationGenerator.CHILD_PROTECTION_TYPE))
                persist(RegistrationGenerator.SERIOUS_FURTHER_OFFENCE_TYPE)
                persist(RegistrationGenerator.generate(RegistrationGenerator.SERIOUS_FURTHER_OFFENCE_TYPE))
                persist(RegistrationGenerator.MAPPA_TYPE)
                persist(RegistrationGenerator.DATASET_TYPE_OTHER)
                persist(RegistrationGenerator.DATASET_TYPE_GENDER)
                persist(RegistrationGenerator.REFDATA_MALE)
                persist(RegistrationGenerator.REFDATA_FEMALE)
                RegistrationGenerator.CATEGORIES.values.forEach(::persist)
                RegistrationGenerator.LEVELS.values.forEach(::persist)
                persist(
                    RegistrationGenerator.generate(
                        RegistrationGenerator.MAPPA_TYPE,
                        RegistrationGenerator.CATEGORIES[Category.M2.name],
                        RegistrationGenerator.LEVELS[Level.M1.name],
                        reviewDate = LocalDate.now().plusMonths(6),
                        notes = "Mappa Detail for ${DataGenerator.PERSON.crn}"
                    )
                )
                persist(PersonGenerator.EXCLUSION)
                persist(PersonGenerator.RESTRICTION)
                persist(PersonGenerator.RESTRICTION_EXCLUSION)
            }
        }
        loadLaoData()
    }

    private fun loadLaoData() {
        transactionTemplate.execute {
            with(entityManager) {
                merge(LaoGenerator.EXCLUSION)
                merge(LaoGenerator.RESTRICTION)
                merge(LaoGenerator.BOTH_EXCLUSION)
                merge(LaoGenerator.BOTH_RESTRICTION)
            }
        }
    }
}
