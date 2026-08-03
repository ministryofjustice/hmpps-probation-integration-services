package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(PersonGenerator.PERSON1)
        save(PersonGenerator.PERSON2)
        save(RegistrationGenerator.HOIE_TYPE)
        save(RegistrationGenerator.HOME_OFFICE_INTEREST)
    }
}
