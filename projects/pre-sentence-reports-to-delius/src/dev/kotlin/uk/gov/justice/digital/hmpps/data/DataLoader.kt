package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(UserGenerator.DOCUMENT_USER)
        save(Provider(IdGenerator.getAndIncrement(), "N00", "NPS London"))
        saveAll(listOf(BusinessInteractionGenerator.UPLOAD_DOCUMENT))
        save(PersonGenerator.DEFAULT)
        save(CourtReportGenerator.DEFAULT_EVENT)
        save(CourtReportGenerator.DEFAULT_CA)
        save(CourtReportGenerator.DEFAULT)
        save(DocumentGenerator.DEFAULT)
    }
}
