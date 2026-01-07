package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.CONTACT
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.CONTACT_OUTCOME_TYPE
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.CONTACT_TYPE
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.MAPPA_CONTACT
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.JS_USER
import uk.gov.justice.digital.hmpps.data.generator.LaoGenerator.generateRestriction
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.AI_PREVIOUS_CRN
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_DISABILITY_CONDITION
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_DISABILITY_TYPE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_ETHNICITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_NATIONALITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_RELIGION
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.INVALID_MAPPA_LEVEL
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.RELEASED_COURT_APPEARANCE
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.RELEASED_CUSTODY
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.RELEASED_EVENT
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.RELEASED_SENTENCE
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.model.Category
import uk.gov.justice.digital.hmpps.model.Level
import uk.gov.justice.digital.hmpps.user.AuditUser
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(UserGenerator.LIMITED_ACCESS_USER)
        save(ReferenceDataGenerator.DATASET_TYPE_OTHER)
        save(ReferenceDataGenerator.DATASET_TYPE_GENDER)
        save(ReferenceDataGenerator.DATASET_TYPE_ETHNICITY)
        save(ReferenceDataGenerator.DATASET_TYPE_ADDRESS_STATUS)
        save(ReferenceDataGenerator.RD_MALE)
        save(ReferenceDataGenerator.RD_FEMALE)
        save(RegistrationGenerator.CHILD_CONCERNS_TYPE)
        save(RegistrationGenerator.generate(RegistrationGenerator.CHILD_CONCERNS_TYPE))
        save(RegistrationGenerator.CHILD_PROTECTION_TYPE)
        save(RegistrationGenerator.generate(RegistrationGenerator.CHILD_PROTECTION_TYPE))
        save(RegistrationGenerator.SERIOUS_FURTHER_OFFENCE_TYPE)
        save(RegistrationGenerator.WARRANT_SUMMONS_TYPE)
        save(RegistrationGenerator.generate(RegistrationGenerator.SERIOUS_FURTHER_OFFENCE_TYPE))
        save(RegistrationGenerator.generate(RegistrationGenerator.WARRANT_SUMMONS_TYPE))
        save(RegistrationGenerator.MAPPA_TYPE)
        RegistrationGenerator.CATEGORIES.values.forEach(::save)
        RegistrationGenerator.LEVELS.values.forEach(::save)
        save(RD_RELIGION)
        save(RD_NATIONALITY)
        save(AI_PREVIOUS_CRN)
        save(RD_DISABILITY_TYPE)
        save(RD_DISABILITY_CONDITION)
        save(RD_ADDRESS_STATUS)
        save(RD_ETHNICITY)
        save(INVALID_MAPPA_LEVEL)
        save(DataGenerator.DEFAULT_PROVIDER)
        save(DataGenerator.DEFAULT_PDU)
        save(DataGenerator.DEFAULT_LAU)
        save(DataGenerator.DEFAULT_TEAM)
        save(DataGenerator.JOHN_SMITH)
        save(DataGenerator.JS_USER)
        save(DataGenerator.PERSON)
        save(DataGenerator.PERSON_2)
        save(DataGenerator.PERSON_MANAGER)
        save(DataGenerator.PERSON_MANAGER_2)
        save(PersonGenerator.generateAddress(PersonGenerator.DEFAULT))
        save(DataGenerator.OFFENCE)
        save(DataGenerator.COURT)
        save(DataGenerator.COURT_APPEARANCE_TYPE)
        save(DataGenerator.COURT_APPEARANCE_PLEA)
        save(DataGenerator.DISPOSAL_TYPE)
        save(DataGenerator.MONTHS)
        save(DataGenerator.LENGTH_UNIT_NA)
        save(DataGenerator.EVENT)
        save(DataGenerator.EVENT_NON_APP_LENGTH_UNIT)
        save(CONTACT_TYPE)
        save(CONTACT_OUTCOME_TYPE)
        save(CONTACT)
        save(MAPPA_CONTACT)
        save(
            RegistrationGenerator.generate(
                RegistrationGenerator.MAPPA_TYPE,
                RegistrationGenerator.CATEGORIES[Category.M2.name],
                RegistrationGenerator.LEVELS[Level.M1.name],
                reviewDate = LocalDate.now().plusMonths(6),
                notes = "Mappa Detail for ${DataGenerator.PERSON.crn}",
            )
        )
        save(
            RegistrationGenerator.generate(
                RegistrationGenerator.MAPPA_TYPE,
                RegistrationGenerator.CATEGORIES[Category.M2.name],
                INVALID_MAPPA_LEVEL,
                person = DataGenerator.PERSON_2,
                reviewDate = LocalDate.now().plusMonths(6),
                notes = "Invalid mappa level ${DataGenerator.PERSON_2.crn}"
            )
        )
        save(PersonGenerator.EXCLUSION)
        save(DataGenerator.EXCLUSION_PERSON_MANAGER)
        save(PersonGenerator.RESTRICTION)
        save(DataGenerator.RESTRICTION_PERSON_MANAGER)
        save(PersonGenerator.RESTRICTION_EXCLUSION)
        save(PersonGenerator.WITH_RELEASE_DATE)
        save(PersonGenerator.generateManager(PersonGenerator.WITH_RELEASE_DATE))
        save(ReferenceDataGenerator.DATASET_TYPE_KEY_DATE)
        save(SentenceGenerator.RELEASE_DATE_TYPE)
        save(RELEASED_EVENT)
        save(RELEASED_COURT_APPEARANCE)
        save(RELEASED_SENTENCE)
        save(RELEASED_CUSTODY)
        save(SentenceGenerator.RELEASE_DATE)

        generateCustodialEvent(PersonGenerator.EXCLUSION)
        generateCustodialEvent(PersonGenerator.RESTRICTION)

        save(SentenceGenerator.generateOgrsAssessment(LocalDate.now(), 3))

        loadLaoData()
    }

    private fun loadLaoData() {
        save(LaoGenerator.EXCLUSION)
        save(LaoGenerator.RESTRICTION)
        save(LaoGenerator.BOTH_EXCLUSION)
        save(LaoGenerator.BOTH_RESTRICTION)
        save(
            generateRestriction(
                PersonGenerator.RESTRICTION,
                user = AuditUser(JS_USER.id, JS_USER.username),
                endDateTime = LocalDateTime.now().plusDays(1)
            )
        )
    }

    private fun generateCustodialEvent(person: Person) {
        val event = save(SentenceGenerator.generateEvent(person))
        val disposal = save(SentenceGenerator.generateSentence(event))
        save(SentenceGenerator.generateCustody(disposal))
    }
}
