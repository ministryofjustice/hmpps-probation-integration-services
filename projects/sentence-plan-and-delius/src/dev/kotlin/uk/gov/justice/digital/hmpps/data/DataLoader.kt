package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            ReferenceDataGenerator.TIER_1,
            ReferenceDataGenerator.TC_STATUS_CUSTODY,
            ReferenceDataGenerator.TC_STATUS_NO_CUSTODY,
            ProviderGenerator.DEFAULT_AREA,
            ProviderGenerator.DEFAULT_TEAM,
            ProviderGenerator.DEFAULT_STAFF,
            PersonGenerator.DEFAULT,
            PersonManagerGenerator.DEFAULT,
            EventGenerator.DISPOSAL_TYPE,
            EventGenerator.REQUIREMENT_CAT_F,
            EventGenerator.REQUIREMENT_CAT_W,
            EventGenerator.DEFAULT_EVENT,
            EventGenerator.DEFAULT_DISPOSAL,
            EventGenerator.DEFAULT_CUSTODY,
            PersonGenerator.NON_CUSTODIAL,
            PersonManagerGenerator.NON_CUSTODIAL_MANAGER,
            EventGenerator.NON_CUSTODIAL_EVENT,
            EventGenerator.NON_CUSTODIAL_DISPOSAL,
            EventGenerator.NON_CUSTODIAL_CUSTODY,
            EventGenerator.FIRST_APPT_CT,
            EventGenerator.FIRST_APPT_CONTACT,
            EventGenerator.REQUIREMENT_1,
            EventGenerator.REQUIREMENT_2,
            EventGenerator.UPW_DETAILS_1,
            EventGenerator.UPW_DETAILS_2,
            EventGenerator.UPW_APPOINTMENT_1,
            EventGenerator.UPW_APPOINTMENT_2,
            EventGenerator.RAR_CONTACT_1,
            EventGenerator.RAR_CONTACT_2,
            CaseloadGenerator.generateCaseload(PersonGenerator.DEFAULT, ProviderGenerator.DEFAULT_STAFF),
        )
        val defaultUser = save(ProviderGenerator.generateStaffUser("Default", ProviderGenerator.DEFAULT_STAFF))
        save(PersonGenerator.generateExclusion(PersonGenerator.NON_CUSTODIAL, defaultUser))
    }
}
