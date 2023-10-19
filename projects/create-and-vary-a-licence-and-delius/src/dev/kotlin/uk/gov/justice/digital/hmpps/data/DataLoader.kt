package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMapping
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

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
        entityManager.persist(ProviderGenerator.DEFAULT_PROVIDER)
        entityManager.persist(StaffGenerator.PDUHEAD)
        entityManager.persist(StaffGenerator.DEFAULT_PDUSTAFF_USER)
        entityManager.persist(ProviderGenerator.DEFAULT_BOROUGH)
        entityManager.persist(ProviderGenerator.DEFAULT_DISTRICT)
        entityManager.persist(ProviderGenerator.DEFAULT_TEAM)

        StaffGenerator.DEFAULT = StaffGenerator.generateStaff(
            StaffGenerator.DEFAULT.code,
            StaffGenerator.DEFAULT.forename,
            StaffGenerator.DEFAULT.surname,
            listOf(ProviderGenerator.DEFAULT_TEAM),
            StaffGenerator.DEFAULT.middleName,
            StaffGenerator.DEFAULT.user,
            StaffGenerator.DEFAULT.id
        )
        entityManager.persist(StaffGenerator.DEFAULT)

        entityManager.persist(StaffGenerator.DEFAULT_STAFF_USER)
        entityManager.flush()

        entityManager.persist(PersonGenerator.DEFAULT_PERSON)
        entityManager.persist(PersonGenerator.DEFAULT_CM)

        val person = PersonGenerator.generatePerson("N123456").also(entityManager::persist)
        PersonGenerator.generateManager(person).also(entityManager::persist)
        entityManager.persistAll(
            AddressGenerator.ADDRESS_STATUS_MAIN,
            AddressGenerator.ADDRESS_STATUS_PREVIOUS,
            AddressGenerator.ADDRESS_STATUS_OTHER,
            AddressGenerator.ADDRESS_MAIN,
            AddressGenerator.ADDRESS_PREVIOUS,
            AddressGenerator.ADDRESS_OTHER,
            AddressGenerator.ADDRESS_DELETED
        )

        createForAddingLicenceConditions()
    }

    private fun createForAddingLicenceConditions() {
        entityManager.persistAll(
            SentenceGenerator.SENTENCE_TYPE_SC,
            ReferenceDataGenerator.DATASET_LC_SUB_CAT,
            ReferenceDataGenerator.LC_STANDARD_CATEGORY,
            ReferenceDataGenerator.LC_STANDARD_SUB_CATEGORY,
            ReferenceDataGenerator.LC_BESPOKE_CATEGORY,
            ReferenceDataGenerator.LC_BESPOKE_SUB_CATEGORY,
            ReferenceDataGenerator.CONTACT_TYPE_LPOP,
            PersonGenerator.PERSON_CREATE_LC,
            SentenceGenerator.EVENT_CREATE_LC,
            SentenceGenerator.SENTENCE_CREATE_LC,
            PersonGenerator.generateManager(PersonGenerator.PERSON_CREATE_LC)
        )
        entityManager.saveCvlMappings(ReferenceDataGenerator.CVL_MAPPINGS)
    }

    private fun EntityManager.persistAll(vararg entities: Any) {
        entities.forEach { persist(it) }
    }

    private fun EntityManager.saveCvlMappings(mappings: List<CvlMapping>) {
        mappings.forEach {
            persistAll(it.mainCategory, it.subCategory, it)
        }
    }
}
