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
        save(StaffGenerator.DEFAULT)
        save(UserGenerator.DEFAULT)
        save(PersonManagerGenerator.DEFAULT)
        save(RegisterTypeGenerator.HIGH_ROSH)
        save(EventGenerator.DEFAULT)
        save(DisposalGenerator.DEFAULT)
        save(CustodyGenerator.DEFAULT)
        save(KeyDateGenerator.EXPECTED_RELEASE)
        save(RegistrationGenerator.DEFAULT)
    }
}
