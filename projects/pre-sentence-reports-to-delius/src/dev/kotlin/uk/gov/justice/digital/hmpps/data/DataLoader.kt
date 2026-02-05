package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER
    override fun setupData() {
        print("DocumentUUID:${DocumentGenerator.DEFAULT_DOCUMENT.externalReference}")
        saveAll(
            EventGenerator.DEFAULT_EVENT,
            CourtAppearanceGenerator.DEFAULT_COURT_APPEARANCE,
            CourtReportGenerator.DEFAULT_COURT_REPORT,
            ReferenceDataGenerator.DEFAULT_TITLE,
            ReferenceDataGenerator.DEFAULT_ADDRESS_STATUS,
            ReferenceDataGenerator.DEFAULT_STATUS,
            PersonGenerator.DEFAULT_PERSON,
            PersonAddressGenerator.DEFAULT_PERSON_ADDRESS,
            DocumentGenerator.DEFAULT_DOCUMENT
        )
    }
}