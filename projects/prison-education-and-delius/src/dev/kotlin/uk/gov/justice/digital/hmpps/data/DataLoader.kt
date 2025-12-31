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
        save(DataGenerator.PERSON)
        save(DataGenerator.STAFF_WITH_USER)
        save(DataGenerator.STAFF_WITH_USER.user)
        save(DataGenerator.COMMUNITY_MANAGER_WITH_USER)
        save(DataGenerator.MAIN_ADDRESS_TYPE)
        save(DataGenerator.MAIN_ADDRESS)
    }
}
