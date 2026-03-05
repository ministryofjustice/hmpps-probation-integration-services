package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            TestData.ReferenceData.PREV_ADDRESS_STATUS,
            TestData.ReferenceData.MAIN_ADDRESS_STATUS,
            TestData.ReferenceData.EMERGENCY_CONTACT_TYPE,
            TestData.ReferenceData.EMERGENCY_CONTACT_TYPE,
            TestData.ReferenceData.HOURS,
            TestData.ReferenceData.COMMUNITY_ORDER,
            TestData.ReferenceData.REQUIREMENT_CATEGORY,
            TestData.ReferenceData.REQUIREMENT_SUBCATEGORY,
            TestData.ReferenceData.LICENCE_CONDITION_CATEGORY,
            TestData.ReferenceData.LICENCE_CONDITION_SUBCATEGORY,
            TestData.TeamData.OFFICE,
            TestData.TeamData.TEAM,
            TestData.StaffData.STAFF,
            TestData.StaffData.STAFF_WITHOUT_USER,
            TestData.PersonData.DEFAULT,
            TestData.PersonData.BASIC,
            TestData.AddressData.MAIN_ADDRESS,
            TestData.AddressData.PREV_ADDRESS,
            TestData.EmergencyContactData.EMERGENCY_CONTACT,
            TestData.SentenceData.EVENT,
            TestData.SentenceData.DISPOSAL,
            TestData.SentenceData.REQUIREMENT,
            TestData.SentenceData.LICENCE_CONDITION,
        )
    }
}
