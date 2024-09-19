package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
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

        createOfficeLocationsAndDistricts()

        entityManager.persist(ProviderGenerator.DEFAULT_TEAM)
        entityManager.persist(ProviderGenerator.TEAM_ENDED_OR_NULL_LOCATIONS)

        StaffGenerator.DEFAULT = StaffGenerator.generateStaff(
            StaffGenerator.DEFAULT.code,
            StaffGenerator.DEFAULT.forename,
            StaffGenerator.DEFAULT.surname,
            listOf(ProviderGenerator.DEFAULT_TEAM),
            ProviderGenerator.DEFAULT_PROVIDER,
            StaffGenerator.DEFAULT.middleName,
            StaffGenerator.DEFAULT.user,
            StaffGenerator.DEFAULT.id
        )
        entityManager.persist(StaffGenerator.DEFAULT)

        entityManager.persist(StaffGenerator.DEFAULT_STAFF_USER)
        entityManager.flush()

        entityManager.persist(PersonGenerator.DEFAULT_PERSON)
        entityManager.persist(PersonGenerator.PERSON_ENDED_TEAM_LOCATION)
        entityManager.persist(PersonGenerator.DEFAULT_CM)
        entityManager.persist(PersonGenerator.DEFAULT_PM)
        entityManager.persist(PersonGenerator.CM_ENDED_TEAM_LOCATION)

        val person = PersonGenerator.generatePerson("N123456").also(entityManager::persist)
        PersonGenerator.generateManager(person).also(entityManager::persist)

        createForAddingLicenceConditions()

        createCaseloadData()
    }

    private fun createForAddingLicenceConditions() {
        entityManager.persistAll(
            ReferenceDataGenerator.DATASET_LC_SUB_CAT,
            ReferenceDataGenerator.DATASET_LM_ALLOCATION_REASON,
            ReferenceDataGenerator.DATASET_CUSTODY_STATUS,
            ReferenceDataGenerator.DATASET_KEY_DATE_TYPE,
            ReferenceDataGenerator.LC_STANDARD_CATEGORY,
            ReferenceDataGenerator.LC_STANDARD_SUB_CATEGORY,
            ReferenceDataGenerator.LC_BESPOKE_CATEGORY,
            ReferenceDataGenerator.LC_BESPOKE_SUB_CATEGORY,
            ReferenceDataGenerator.CONTACT_TYPE_LPOP,
            ReferenceDataGenerator.DEFAULT_TRANSFER_REASON,
            ReferenceDataGenerator.INITIAL_ALLOCATION_REASON,
            ReferenceDataGenerator.PSS_COMMENCED_STATUS,
            ReferenceDataGenerator.RELEASED_STATUS,
            ReferenceDataGenerator.SENTENCE_EXPIRY_DATE_TYPE,
            PersonGenerator.PERSON_CREATE_LC,
            SentenceGenerator.EVENT_CREATE_LC,
            SentenceGenerator.SENTENCE_CREATE_LC.disposal,
            SentenceGenerator.SENTENCE_CREATE_LC,
            PersonGenerator.generateManager(PersonGenerator.PERSON_CREATE_LC)
        )
        entityManager.saveCvlMappings(ReferenceDataGenerator.CVL_MAPPINGS)
    }

    private fun createOfficeLocationsAndDistricts() {
        entityManager.persistAll(
            ProviderGenerator.DISTRICT_BRK,
            ProviderGenerator.DISTRICT_MKY,
            ProviderGenerator.DISTRICT_OXF,
            ProviderGenerator.LOCATION_BRK_1,
            ProviderGenerator.LOCATION_BRK_2,
            ProviderGenerator.LOCATION_ENDED,
            ProviderGenerator.LOCATION_NULL
        )
    }

    private fun createCaseloadData() {
        entityManager.persistAll(
            CaseloadGenerator.TEAM1,
            CaseloadGenerator.STAFF1,
            CaseloadGenerator.STAFF2,
            CaseloadGenerator.CASELOAD_ROLE_OM_1,
            CaseloadGenerator.CASELOAD_ROLE_OM_2,
            CaseloadGenerator.CASELOAD_ROLE_OM_3,
            CaseloadGenerator.CASELOAD_ROLE_OM_4,
            CaseloadGenerator.CASELOAD_ROLE_OS_1
        )
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
