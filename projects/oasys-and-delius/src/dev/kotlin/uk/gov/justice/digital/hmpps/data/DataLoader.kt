package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.CUSTODY_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DETAILED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REGISTERED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.RELEASED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.JOHN_SMITH
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.UNALLOCATED_STAFF
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.ANOTHER_TYPE
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.CATEGORY
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.DEFAULT_TYPE
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.FLAG
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.LEVEL
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.Custody
import java.time.ZonedDateTime

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        referenceData()
        providerData()
        personData()
        registrationData()
        custodialData()
    }

    fun referenceData() {
        saveAll(
            FLAG,
            LEVEL,
            CATEGORY,
            DEFAULT_TYPE,
            ANOTHER_TYPE,
            PersonGenerator.GENDER,
            PersonGenerator.ETHNICITY,
            PersonGenerator.LANGUAGE,
            PersonGenerator.RELIGION,
            PersonGenerator.MAIN_ADDRESS_STATUS
        )
    }

    fun providerData() {
        saveAll(
            DEFAULT_PROVIDER,
            DEFAULT_TEAM,
            UNALLOCATED_STAFF,
            JOHN_SMITH
        )
    }

    fun personData() {
        saveAll(REGISTERED_PERSON, RELEASED_PERSON, CUSTODY_PERSON, DETAILED_PERSON)
        save(PersonGenerator.DETAIL_ADDRESS)
    }

    fun registrationData() {
        val person = REGISTERED_PERSON.asPerson()
        val registration1 = RegistrationGenerator.generate(person)
        val review1 = RegistrationGenerator.generateReview(registration1)
        val registration2 = RegistrationGenerator.generate(
            person,
            ANOTHER_TYPE,
            staff = JOHN_SMITH,
            category = null,
            level = null,
            createdDateTime = ZonedDateTime.now().minusHours(1)
        )
        val review2 = RegistrationGenerator.generateReview(registration2)
        saveAll(registration1, review1, registration2, review2)
    }

    fun custodialData() {
        saveAll(
            SentenceGenerator.INSTITUTION_TYPE,
            SentenceGenerator.DEFAULT_INSTITUTION,
            SentenceGenerator.CUSTODY_STATUS,
            SentenceGenerator.RELEASE_TYPE,
            SentenceGenerator.RECALL_REASON
        )
        persistCustody(SentenceGenerator.CUSTODIAL_SENTENCE)
        persistCustody(SentenceGenerator.RELEASED_SENTENCE)
        saveAll(SentenceGenerator.RELEASE, SentenceGenerator.RECALL)
    }

    fun persistCustody(custody: Custody) {
        saveAll(custody.disposal.event, custody.disposal, custody)
    }
}
