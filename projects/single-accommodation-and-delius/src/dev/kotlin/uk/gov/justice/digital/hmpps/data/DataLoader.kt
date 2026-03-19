package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(ReferenceDataGenerator.GENDER_MALE)
        save(ReferenceDataGenerator.EXP_RELEASE_DATE_TYPE)
        save(ProviderGenerator.DEFAULT)
        save(TeamGenerator.DEFAULT)
        save(PersonGenerator.DEFAULT)
        save(PersonGenerator.EXCLUDED)
        save(PersonGenerator.RESTRICTED)
        save(StaffGenerator.DEFAULT)
        save(UserGenerator.DEFAULT)
        save(PersonManagerGenerator.DEFAULT)
        save(PersonManagerGenerator.EXCLUDED)
        save(PersonManagerGenerator.RESTRICTED)
        save(RegisterTypeGenerator.HIGH_ROSH)
        save(EventGenerator.DEFAULT)
        save(DisposalGenerator.DEFAULT)
        save(CustodyGenerator.DEFAULT)
        save(KeyDateGenerator.EXPECTED_RELEASE)
        save(RegistrationGenerator.DEFAULT)
        save(LimitedAccessGenerator.RESTRICTED_USER)
        save(LimitedAccessGenerator.EXCLUDED_CASE)
        save(LimitedAccessGenerator.RESTRICTED_CASE)
        save(LimitedAccessGenerator.generateExclusion(LimitedAccessGenerator.EXCLUDED_CASE))
        save(LimitedAccessGenerator.generateRestriction(LimitedAccessGenerator.RESTRICTED_CASE))
    }
}
