package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(ProbationAreaGenerator.DEFAULT_PROBATION_AREA)
        save(ProbationAreaGenerator.HOME_PROBATION_AREA)
        save(StaffGenerator.DEFAULT_STAFF)
        save(ResponsibleOfficerGenerator.DEFAULT_USER)
        save(ResponsibleOfficerGenerator.DEFAULT_OFFENDER_MANAGER)
        save(ResponsibleOfficerGenerator.DEFAULT_RESPONSIBLE_OFFICER)
        save(OfficeLocationGenerator.DEFAULT_OFFICE_LOCATION)

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
        <<<<<<< HEAD
        save(StaffGenerator.PRISON_STAFF)
        save(PersonGenerator.PRISON_MANAGED)
        save(PersonGenerator.NO_PREFERRED_ADDRESS)
        save(ResponsibleOfficerGenerator.PRISON_OFFENDER_MANAGER)
        save(ResponsibleOfficerGenerator.PRISON_RESPONSIBLE_OFFICER)

        save(StaffGenerator.NO_PREFERRED_ADDRESS_STAFF)
        save(ResponsibleOfficerGenerator.NO_PREFERRED_ADDRESS_OFFENDER_MANAGER)
        save(ResponsibleOfficerGenerator.NO_PREFERRED_ADDRESS_RESPONSIBLE_OFFICER)
        ====== =
        >>>>>>> 91e97ddcff72e5ee657d43059ffba63723839cc6
    }
}
