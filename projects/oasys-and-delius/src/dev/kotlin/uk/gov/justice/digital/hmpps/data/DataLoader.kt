package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
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
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.Custody
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.ZonedDateTime

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        referenceData()
        providerData()
        personData()
        registrationData()
        custodialData()
    }

    fun referenceData() {
        entityManager.saveAll(
            FLAG,
            LEVEL,
            CATEGORY,
            DEFAULT_TYPE,
            ANOTHER_TYPE,
            PersonGenerator.GENDER,
            PersonGenerator.ETHNICITY,
            PersonGenerator.LANGUAGE,
            PersonGenerator.RELIGION
        )
    }

    fun providerData() {
        entityManager.saveAll(
            DEFAULT_PROVIDER,
            DEFAULT_TEAM,
            UNALLOCATED_STAFF,
            JOHN_SMITH
        )
    }

    fun personData() {
        entityManager.saveAll(REGISTERED_PERSON, RELEASED_PERSON, CUSTODY_PERSON, DETAILED_PERSON)
        entityManager.flush()
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
        entityManager.saveAll(registration1, review1, registration2, review2)
    }

    fun custodialData() {
        entityManager.saveAll(
            SentenceGenerator.INSTITUTION_TYPE,
            SentenceGenerator.DEFAULT_INSTITUTION,
            SentenceGenerator.CUSTODY_STATUS,
            SentenceGenerator.RELEASE_TYPE,
            SentenceGenerator.RECALL_REASON
        )
        persistCustody(SentenceGenerator.CUSTODIAL_SENTENCE)
        persistCustody(SentenceGenerator.RELEASED_SENTENCE)
        entityManager.saveAll(SentenceGenerator.RELEASE, SentenceGenerator.RECALL)
    }

    fun persistCustody(custody: Custody) {
        entityManager.saveAll(custody.disposal.event, custody.disposal, custody)
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach(::persist)
}
