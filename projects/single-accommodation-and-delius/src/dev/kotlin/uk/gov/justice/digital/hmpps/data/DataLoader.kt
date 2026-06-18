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
        save(TeamGenerator.OTHER_TEAM)
        save(PersonGenerator.DEFAULT)
        save(PersonGenerator.EXCLUDED)
        save(PersonGenerator.RESTRICTED)
        save(PersonGenerator.TEAM)
        save(PersonGenerator.OTHER_TEAM)
        save(RegisterTypeGenerator.NON_ROSH)
        save(PersonGenerator.WITH_NON_ROSH_REGISTRATION)
        save(StaffGenerator.DEFAULT)
        save(StaffGenerator.TEAM_STAFF)
        save(StaffGenerator.OTHER_TEAM_STAFF)
        save(StaffGenerator.BOTH_TEAMS_STAFF)
        save(UserGenerator.DEFAULT)
        save(UserGenerator.OTHER)
        save(RegisterTypeGenerator.HIGH_ROSH)
        save(EventGenerator.DEFAULT)
        save(DisposalGenerator.DEFAULT)
        save(CustodyGenerator.DEFAULT)
        save(KeyDateGenerator.EXPECTED_RELEASE)
        save(LimitedAccessGenerator.RESTRICTED_USER)
        save(LimitedAccessGenerator.generateExclusion(LimitedAccessGenerator.EXCLUDED_CASE))
        save(LimitedAccessGenerator.generateRestriction(LimitedAccessGenerator.RESTRICTED_CASE))
    }
}
