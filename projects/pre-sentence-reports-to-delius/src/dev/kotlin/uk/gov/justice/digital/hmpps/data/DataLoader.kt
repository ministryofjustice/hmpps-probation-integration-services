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
            EventGenerator.DEFAULT_EVENT,
            CourtAppearanceGenerator.DEFAULT_COURT_APPEARANCE,
            CourtReportGenerator.DEFAULT_COURT_REPORT,
            ReferenceDataGenerator.DEFAULT_TITLE,
            ReferenceDataGenerator.DEFAULT_ADDRESS_STATUS,
            PersonGenerator.DEFAULT_PERSON,
            PersonAddressGenerator.DEFAULT_PERSON_ADDRESS,
            DocumentGenerator.DEFAULT_DOCUMENT,
            MainOffenceGenerator.DEFAULT_MAIN_OFFENCE,
            AdditionalOffenceGenerator.DEFAULT_ADDITIONAL_OFFENCE
        )
    }
}