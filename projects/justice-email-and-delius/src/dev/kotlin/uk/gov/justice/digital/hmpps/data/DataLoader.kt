package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.Data.BUSINESS_INTERACTIONS
import uk.gov.justice.digital.hmpps.data.generator.Data.CONTACT_TYPES
import uk.gov.justice.digital.hmpps.data.generator.Data.DUPLICATE_STAFF_1
import uk.gov.justice.digital.hmpps.data.generator.Data.DUPLICATE_STAFF_2
import uk.gov.justice.digital.hmpps.data.generator.Data.EVENT
import uk.gov.justice.digital.hmpps.data.generator.Data.MANAGER
import uk.gov.justice.digital.hmpps.data.generator.Data.MANAGER_STAFF
import uk.gov.justice.digital.hmpps.data.generator.Data.PERSON
import uk.gov.justice.digital.hmpps.data.generator.Data.STAFF
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            PERSON,
            EVENT,
            STAFF,
            STAFF.user,
            DUPLICATE_STAFF_1,
            DUPLICATE_STAFF_1.user,
            DUPLICATE_STAFF_2,
            DUPLICATE_STAFF_2.user,
            MANAGER_STAFF,
            MANAGER,
            *CONTACT_TYPES,
            *BUSINESS_INTERACTIONS,
        )
    }
}
