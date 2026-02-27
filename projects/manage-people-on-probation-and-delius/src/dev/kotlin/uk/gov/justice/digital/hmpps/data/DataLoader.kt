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
            TestData.Users.STAFF,
            TestData.Users.USER,
            TestData.Users.USER_WITHOUT_STAFF,
            TestData.Person.PERSON_1,
            TestData.Person.PERSON_2,
            TestData.Caseload.CASELOAD_FOR_PERSON_1,
            TestData.Caseload.CASELOAD_FOR_PERSON_2,
            TestData.OfficeLocation.DEFAULT,
            TestData.Provider.DEFAULT,
            TestData.Teams.DEFAULT,
            TestData.Requirement.Category.RAR,
            TestData.Requirement.Category.UPW,
            TestData.Requirement.RAR,
            TestData.Requirement.UNPAID_WORK,
            TestData.Contact.Type.PLANNED_OFFICE_VISIT,
            TestData.Contact.Type.DRUG_TEST_APPOINTMENT,
            TestData.Contact.Type.EMAIL_TEXT_FROM_POP,
            TestData.Contact.Outcome.COMPLIED,
            TestData.Contact.Outcome.NOT_COMPLIED,
            TestData.Contact.UPCOMING_1,
            TestData.Contact.UPCOMING_2,
            *TestData.Contact.REQUIRING_OUTCOME.toTypedArray(),
            *TestData.Contact.WITH_OUTCOME.toTypedArray(),
        )
    }
}
