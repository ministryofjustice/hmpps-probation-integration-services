package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(UserGenerator.DEFAULT)
        save(UserGenerator.RESTRICTED)
        save(PersonGenerator.DEFAULT)
        save(PersonGenerator.EXCLUDED)
        save(PersonGenerator.RESTRICTED)
        save(LimitedAccessGenerator.EXCLUSION)
        save(LimitedAccessGenerator.RESTRICTION)
    }
}
