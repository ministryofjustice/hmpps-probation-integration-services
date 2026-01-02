package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(DataGenerator.COURT_CATEGORY_SET)
        save(DataGenerator.COURT_CATEGORY)
        save(DataGenerator.EXISTING_DETAILED_OFFENCE)
        save(DataGenerator.HIGH_LEVEL_OFFENCE)
    }
}
