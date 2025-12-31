package uk.gov.justice.digital.hmpps.data

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.user.AuditUser

@Component
class DataLoader(
    @Value("\${delius.db.username}") private val deliusDbUsername: String,
    dataManager: DataManager
) : BaseDataLoader(dataManager) {
    override fun systemUser() = AuditUser(IdGenerator.getAndIncrement(), deliusDbUsername)

    override fun setupData() {
        save(ProbationAreaGenerator.DEFAULT)
        save(StaffGenerator.DEFAULT)
        save(TeamGenerator.DEFAULT)
        save(PersonGenerator.generate("A000001"))
        save(ReferenceDataSetGenerator.TIER)
        save(ReferenceDataSetGenerator.TIER_CHANGE_REASON)
        save(ReferenceDataGenerator.generate("UD0", ReferenceDataSetGenerator.TIER))
        save(ReferenceDataGenerator.generate("UD2", ReferenceDataSetGenerator.TIER))
        save(ReferenceDataGenerator.generate("UC2", ReferenceDataSetGenerator.TIER))
        save(ReferenceDataGenerator.generate("ATS", ReferenceDataSetGenerator.TIER_CHANGE_REASON))
        save(ContactTypeGenerator.TIER_UPDATE)
    }
}
