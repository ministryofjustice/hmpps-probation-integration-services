package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(OffenderGenerator.DEFAULT)
        save(ContactGenerator.DEFAULT)
        save(ContactGenerator.DELETED)
        save(ContactGenerator.DELETED_VISOR)
        save(ContactGenerator.VISOR)

        save(DatasetGenerator.DOMAIN_EVENT_TYPE)
        saveAll(DomainEventTypeGenerator.MAPPA_UPDATED, DomainEventTypeGenerator.MAPPA_DELETED)

        save(RegisterTypeGenerator.MAPPA)
        save(DatasetGenerator.MAPPA_CATEGORY)
        save(ReferenceDataGenerator.MAPPA_CATEGORY)
        save(
            RegistrationGenerator.mappaRegistration(
                OffenderGenerator.DEFAULT.id,
                ReferenceDataGenerator.MAPPA_CATEGORY,
                RegisterTypeGenerator.MAPPA
            )
        )
    }
}
