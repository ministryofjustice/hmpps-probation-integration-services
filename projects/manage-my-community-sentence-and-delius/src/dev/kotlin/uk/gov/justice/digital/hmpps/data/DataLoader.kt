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
            TestData.ReferenceData.DAYS,
            TestData.ReferenceData.APPOINTMENT_CONTACT_TYPE,
            TestData.ReferenceData.NON_APPOINTMENT_CONTACT_TYPE,
            TestData.ReferenceData.COMMUNITY_ORDER,
            TestData.ReferenceData.UPW_REQUIREMENT_CATEGORY,
            TestData.ReferenceData.UPW_REQUIREMENT_SUBCATEGORY,
            TestData.ReferenceData.RAR_REQUIREMENT_CATEGORY,
            TestData.ReferenceData.RAR_REQUIREMENT_SUBCATEGORY,
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
            TestData.RequirementData.REQUIREMENT,
            TestData.RequirementData.UPW_REQUIREMENT,
            TestData.RequirementData.RAR_REQUIREMENT,
            TestData.LicenceConditionData.LICENCE_CONDITION,
            TestData.UnpaidWorkData.UNPAID_WORK_DETAILS,
            TestData.UnpaidWorkData.UNPAID_WORK_APPOINTMENT,
            TestData.RarData.RAR_NSI,
            TestData.RarData.RAR_CONTACT_1,
            TestData.RarData.RAR_CONTACT_2,
            TestData.RarData.RAR_CONTACT_3_SAME_DAY,
            TestData.RarData.RAR_CONTACT_NOT_ATTENDED,
            TestData.AppointmentData.FUTURE_1,
            TestData.AppointmentData.FUTURE_2,
            TestData.AppointmentData.NON_APPOINTMENT,
            TestData.AppointmentData.PAST_1,
            TestData.AppointmentData.PAST_2,
        )
    }
}
