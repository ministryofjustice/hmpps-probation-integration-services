package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import java.time.ZonedDateTime

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
            DocumentGenerator.FINAL_DOCUMENT,
            MainOffenceGenerator.DEFAULT_MAIN_OFFENCE,
            AdditionalOffenceGenerator.DEFAULT_ADDITIONAL_OFFENCE,
            UserGenerator.TEST_USER,
        )
        businessInteractions()
    }
    private fun businessInteractions() {
        saveAll(
            BusinessInteractionCode.entries.map {
                BusinessInteraction(
                    IdGenerator.getAndIncrement(),
                    it.code,
                    ZonedDateTime.now()
                )
            }
        )
    }
}