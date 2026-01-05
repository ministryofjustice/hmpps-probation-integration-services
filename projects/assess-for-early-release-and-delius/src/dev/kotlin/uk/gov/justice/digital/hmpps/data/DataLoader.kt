package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(ProviderGenerator.DEFAULT_PROVIDER)
        save(StaffGenerator.PDUHEAD)
        save(StaffGenerator.DEFAULT_PDUSTAFF_USER)
        save(ProviderGenerator.DEFAULT_BOROUGH)
        save(ProviderGenerator.DEFAULT_DISTRICT)

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
        save(StaffGenerator.DEFAULT)

        save(StaffGenerator.DEFAULT_STAFF_USER)

        save(PersonGenerator.DEFAULT_PERSON)
        save(PersonGenerator.PERSON_ENDED_TEAM_LOCATION)
        save(PersonGenerator.DEFAULT_CM)
        save(PersonGenerator.CM_ENDED_TEAM_LOCATION)

        val person = save(PersonGenerator.generatePerson("N123456"))
        save(PersonGenerator.generateManager(person))

        createCaseloadData()
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
            CaseloadGenerator.STAFF1,
            CaseloadGenerator.STAFF2,
            CaseloadGenerator.CASELOAD_ROLE_OM_1,
            CaseloadGenerator.CASELOAD_ROLE_OM_2,
            CaseloadGenerator.CASELOAD_ROLE_OM_3,
            CaseloadGenerator.CASELOAD_ROLE_OM_4,
            CaseloadGenerator.CASELOAD_ROLE_OS_1
        )
    }
}
