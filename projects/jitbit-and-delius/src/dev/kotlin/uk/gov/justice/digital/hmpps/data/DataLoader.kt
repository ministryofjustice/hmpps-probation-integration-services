package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.LaoGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(UserGenerator.LIMITED_ACCESS_USER)
        saveAll(
            listOf(
                PersonGenerator.DEFAULT, PersonGenerator.BASIC, PersonGenerator.EXCLUSION,
                PersonGenerator.RESTRICTION, PersonGenerator.RESTRICTION_EXCLUSION
            )
        )
        save(AddressGenerator.MAIN_STATUS)
        save(AddressGenerator.DEFAULT)
        saveAll(listOf(LaoGenerator.EXCLUSION, LaoGenerator.BOTH_EXCLUSION))
        saveAll(listOf(LaoGenerator.RESTRICTION, LaoGenerator.BOTH_RESTRICTION))
    }
}
