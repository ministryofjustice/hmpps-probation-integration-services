package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMapping

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(ProviderGenerator.DEFAULT_PROVIDER)
        save(StaffGenerator.PDUHEAD)
        save(StaffGenerator.DEFAULT_PDUSTAFF_USER)
        save(ProviderGenerator.DEFAULT_BOROUGH)
        save(ProviderGenerator.DEFAULT_DISTRICT)
        save(LimitedAccessGenerator.LAO_DEFAULT_USER)
        save(LimitedAccessGenerator.LAO_EXCLUDED_USER)
        save(LimitedAccessGenerator.LAO_RESTRICTED_USER)
        save(LimitedAccessGenerator.LAO_EXCLUDED_PERSON)
        save(LimitedAccessGenerator.LAO_RESTRICTED_PERSON)
        save(LimitedAccessGenerator.LAO_EXCLUSION)
        save(LimitedAccessGenerator.LAO_RESTRICTION)

        createOfficeLocationsAndDistricts()

        save(ProviderGenerator.DEFAULT_TEAM)
        save(ProviderGenerator.TEAM_ENDED_OR_NULL_LOCATIONS)

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
        StaffGenerator.DEFAULT_EXCLUDED = StaffGenerator.generateStaff(
            StaffGenerator.DEFAULT.code,
            StaffGenerator.DEFAULT.forename,
            StaffGenerator.DEFAULT.surname,
            listOf(ProviderGenerator.DEFAULT_TEAM),
            ProviderGenerator.DEFAULT_PROVIDER,
            StaffGenerator.DEFAULT.middleName,
            StaffGenerator.DEFAULT.user,
            StaffGenerator.DEFAULT.id
        )
        StaffGenerator.DEFAULT_RESTRICTED = StaffGenerator.generateStaff(
            StaffGenerator.DEFAULT.code,
            StaffGenerator.DEFAULT.forename,
            StaffGenerator.DEFAULT.surname,
            listOf(ProviderGenerator.DEFAULT_TEAM),
            ProviderGenerator.DEFAULT_PROVIDER,
            StaffGenerator.DEFAULT.middleName,
            StaffGenerator.DEFAULT.user,
            StaffGenerator.DEFAULT.id
        )
        save(StaffGenerator.DEFAULT)
        save(StaffGenerator.DEFAULT_STAFF_USER)
        save(StaffGenerator.DEFAULT_EXCLUDED)
        save(StaffGenerator.DEFAULT_RESTRICTED)
        save(StaffGenerator.DEFAULT_EXCLUDED_STAFF_USER)
        save(StaffGenerator.DEFAULT_RESTRICTED_STAFF_USER)

        save(PersonGenerator.DEFAULT_PERSON)
        save(PersonGenerator.PERSON_ENDED_TEAM_LOCATION)
        save(PersonGenerator.DEFAULT_CM)
        save(PersonGenerator.CM_ENDED_TEAM_LOCATION)

        val person = save(PersonGenerator.generatePerson("N123456"))
        save(PersonGenerator.generateManager(person))

        createForAddingLicenceConditions()

        createCaseloadData()
    }

    private fun createForAddingLicenceConditions() {
        saveAll(
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
        saveCvlMappings(ReferenceDataGenerator.CVL_MAPPINGS)
    }

    private fun createOfficeLocationsAndDistricts() {
        saveAll(
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
        saveAll(
            CaseloadGenerator.TEAM1,
            CaseloadGenerator.TEAM2,
            CaseloadGenerator.STAFF1,
            CaseloadGenerator.STAFF2,
            CaseloadGenerator.CASELOAD_ROLE_OM_1,
            CaseloadGenerator.CASELOAD_ROLE_OM_2,
            CaseloadGenerator.CASELOAD_ROLE_OM_3,
            CaseloadGenerator.CASELOAD_ROLE_OM_4,
            CaseloadGenerator.CASELOAD_ROLE_OS_1
        )
    }

    private fun saveCvlMappings(mappings: List<CvlMapping>) {
        mappings.forEach {
            saveAll(it.mainCategory, it.subCategory, it)
        }
    }
}
