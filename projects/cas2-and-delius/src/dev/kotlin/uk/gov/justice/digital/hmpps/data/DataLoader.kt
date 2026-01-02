package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.BUSINESS_INTERACTION
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.CONTACT_TYPES
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.MANAGER
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.PERSON
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            BUSINESS_INTERACTION,
            PERSON,
            MANAGER,
            *CONTACT_TYPES,
        )
    }
}
