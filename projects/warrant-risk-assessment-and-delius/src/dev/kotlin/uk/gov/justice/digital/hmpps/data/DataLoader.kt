package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(PersonGenerator.TITLE)
        save(PersonGenerator.DEFAULT)
        save(PersonGenerator.NO_OPTIONAL_FIELDS)
        save(AddressGenerator.MAIN_STATUS)
        save(AddressGenerator.POSTAL_STATUS)
        save(AddressGenerator.MAIN_ADDRESS)
        save(AddressGenerator.POSTAL_ADDRESS)
        save(AddressGenerator.END_DATED_ADDRESS)
        save(PersonalContactGenerator.CURRENT_EMPLOYER_TYPE)
        save(PersonalContactGenerator.EMPLOYER_ADDRESS)
        save(PersonalContactGenerator.DEFAULT_EMPLOYER)
        save(PersonalContactGenerator.ENDED_EMPLOYER)
        save(ContactGenerator.HOME_VISIT_TYPE)
        save(ContactGenerator.OLDER_HOME_VISIT)
        save(ContactGenerator.LAST_HOME_VISIT)

        save(RegistrationGenerator.MAPPA_TYPE)
        save(RegistrationGenerator.MAPPA_TYPE_M2)
        save(RegistrationGenerator.MAPPA_REGISTRATION)
        save(RegistrationGenerator.OLDER_MAPPA_REGISTRATION)

    }
}
