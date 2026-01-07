package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.EXCLUDED_CASE
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.FULL_ACCESS_USER
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.LIMITED_ACCESS_USER
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.RESTRICTED_CASE
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.UNLIMITED_ACCESS
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(FULL_ACCESS_USER, LIMITED_ACCESS_USER)
        saveAll(UNLIMITED_ACCESS, EXCLUDED_CASE, RESTRICTED_CASE)
        save(LimitedAccessGenerator.generateExclusion(EXCLUDED_CASE))
        save(LimitedAccessGenerator.generateRestriction(RESTRICTED_CASE))
    }
}
