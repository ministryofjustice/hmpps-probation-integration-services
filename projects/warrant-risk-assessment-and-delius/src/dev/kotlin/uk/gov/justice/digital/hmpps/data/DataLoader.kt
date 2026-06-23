package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        // Perform dev/test database setup here, using the `save()` and `saveAll()` methods
        save(ProbationAreaGenerator.DEFAULT_PROBATION_AREA)
        save(ProbationAreaGenerator.HOME_PROBATION_AREA)
        save(StaffGenerator.DEFAULT_STAFF)
        save(ResponsibleOfficerGenerator.DEFAULT_USER)
        save(ResponsibleOfficerGenerator.DEFAULT_OFFENDER_MANAGER)
        save(ResponsibleOfficerGenerator.DEFAULT_RESPONSIBLE_OFFICER)
        save(OfficeLocationGenerator.DEFAULT_OFFICE_LOCATION)

    }
}
