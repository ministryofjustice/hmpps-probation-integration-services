package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.AI_PREVIOUS_CRN
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_DISABILITY_CONDITION
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_DISABILITY_TYPE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_ETHNICITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_NATIONALITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_RELIGION
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.INVALID_MAPPA_LEVEL
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
                persist(ReferenceDataGenerator.DATASET_TYPE_OTHER)
                persist(ReferenceDataGenerator.DATASET_TYPE_GENDER)
                persist(ReferenceDataGenerator.DATASET_TYPE_ETHNICITY)
                persist(ReferenceDataGenerator.DATASET_TYPE_ADDRESS_STATUS)
                persist(ReferenceDataGenerator.RD_MALE)
                persist(ReferenceDataGenerator.RD_FEMALE)
                persist(RegistrationGenerator.CHILD_CONCERNS_TYPE)
                persist(RegistrationGenerator.generate(RegistrationGenerator.CHILD_CONCERNS_TYPE))
                persist(RegistrationGenerator.CHILD_PROTECTION_TYPE)
                persist(RegistrationGenerator.generate(RegistrationGenerator.CHILD_PROTECTION_TYPE))
                persist(RegistrationGenerator.SERIOUS_FURTHER_OFFENCE_TYPE)
                persist(RegistrationGenerator.WARRANT_SUMMONS_TYPE)
                persist(RegistrationGenerator.generate(RegistrationGenerator.SERIOUS_FURTHER_OFFENCE_TYPE))
                persist(RegistrationGenerator.generate(RegistrationGenerator.WARRANT_SUMMONS_TYPE))
                persist(RegistrationGenerator.MAPPA_TYPE)
                RegistrationGenerator.CATEGORIES.values.forEach(::persist)
                RegistrationGenerator.LEVELS.values.forEach(::persist)
                persist(RD_RELIGION)
                persist(RD_NATIONALITY)
                persist(AI_PREVIOUS_CRN)
                persist(RD_DISABILITY_TYPE)
                persist(RD_DISABILITY_CONDITION)
                persist(RD_ADDRESS_STATUS)
                persist(RD_ETHNICITY)
                persist(INVALID_MAPPA_LEVEL)
                persist(DataGenerator.DEFAULT_PROVIDER)
                persist(DataGenerator.DEFAULT_TEAM)
                persist(DataGenerator.JOHN_SMITH)
                persist(DataGenerator.JS_USER)
                persist(DataGenerator.PERSON)
                persist(DataGenerator.PERSON_2)
                persist(DataGenerator.PERSON_MANAGER)
                persist(DataGenerator.PERSON_MANAGER_2)
                persist(PersonGenerator.generateAddress(PersonGenerator.DEFAULT))
                persist(DataGenerator.OFFENCE)
                persist(DataGenerator.COURT)
                persist(DataGenerator.COURT_APPEARANCE_TYPE)
                persist(DataGenerator.COURT_APPEARANCE_PLEA)
                persist(DataGenerator.DISPOSAL_TYPE)
                persist(DataGenerator.MONTHS)
                persist(DataGenerator.LENGTH_UNIT_NA)
                persist(DataGenerator.EVENT)
                persist(DataGenerator.EVENT.disposal)
                persist(DataGenerator.EVENT.mainOffence)
                DataGenerator.EVENT.additionalOffences.forEach { persist(it) }
                DataGenerator.EVENT.courtAppearances.forEach { persist(it) }
                persist(DataGenerator.EVENT_NON_APP_LENGTH_UNIT)
                persist(DataGenerator.EVENT_NON_APP_LENGTH_UNIT.disposal)
                persist(DataGenerator.EVENT_NON_APP_LENGTH_UNIT.mainOffence)
                DataGenerator.EVENT_NON_APP_LENGTH_UNIT.additionalOffences.forEach { persist(it) }
                DataGenerator.EVENT_NON_APP_LENGTH_UNIT.courtAppearances.forEach { persist(it) }
                persist(
                    RegistrationGenerator.generate(
                        RegistrationGenerator.MAPPA_TYPE,
                        RegistrationGenerator.CATEGORIES[Category.M2.name],
                        RegistrationGenerator.LEVELS[Level.M1.name],
                        reviewDate = LocalDate.now().plusMonths(6),
                        notes = "Mappa Detail for ${DataGenerator.PERSON.crn}"
                    )
                )
                persist(
                    RegistrationGenerator.generate(
                        RegistrationGenerator.MAPPA_TYPE,
                        RegistrationGenerator.CATEGORIES[Category.M2.name],
                        INVALID_MAPPA_LEVEL,
                        person = DataGenerator.PERSON_2,
                        reviewDate = LocalDate.now().plusMonths(6),
                        notes = "Invalid mappa level ${DataGenerator.PERSON_2.crn}"
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
