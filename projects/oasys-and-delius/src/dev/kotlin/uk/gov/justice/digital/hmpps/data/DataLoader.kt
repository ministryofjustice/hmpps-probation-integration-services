package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DETAILED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.EMPTY_REQUIREMENTS_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.MIXED_ORDERS_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.NO_DISPOSAL_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.NO_EVENTS_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REGISTERED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.STANDALONE_ONLY_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.UPW_ONLY_PERSON
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
        saveAll(
            REGISTERED_PERSON,
            DETAILED_PERSON,
            STANDALONE_ONLY_PERSON,
            UPW_ONLY_PERSON,
            NO_EVENTS_PERSON,
            MIXED_ORDERS_PERSON,
            NO_DISPOSAL_PERSON,
            EMPTY_REQUIREMENTS_PERSON
        )
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
            SentenceGenerator.RESTRICTIVE_MAIN_CATEGORY,
            SentenceGenerator.UPW_MAIN_CATEGORY,
            SentenceGenerator.NON_STANDALONE_MAIN_CATEGORY
        )

        saveAll(
            SentenceGenerator.STANDALONE_EVENT,
            SentenceGenerator.STANDALONE_DISPOSAL,
            SentenceGenerator.STANDALONE_REQUIREMENT,
            SentenceGenerator.UPW_ONLY_EVENT,
            SentenceGenerator.UPW_ONLY_DISPOSAL,
            SentenceGenerator.UPW_ONLY_REQUIREMENT,
            SentenceGenerator.MIXED_EVENT_STANDALONE,
            SentenceGenerator.MIXED_DISPOSAL_STANDALONE,
            SentenceGenerator.MIXED_REQUIREMENT_STANDALONE,
            SentenceGenerator.MIXED_EVENT_NON_STANDALONE,
            SentenceGenerator.MIXED_DISPOSAL_NON_STANDALONE,
            SentenceGenerator.MIXED_REQUIREMENT_NON_STANDALONE,
            SentenceGenerator.NO_DISPOSAL_EVENT,
            SentenceGenerator.EMPTY_REQUIREMENTS_EVENT,
            SentenceGenerator.EMPTY_REQUIREMENTS_DISPOSAL
        )
    }
}
