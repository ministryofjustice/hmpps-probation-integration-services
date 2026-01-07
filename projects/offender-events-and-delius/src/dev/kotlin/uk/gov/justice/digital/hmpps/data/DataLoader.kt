package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.offender.Contact

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(OffenderGenerator.DEFAULT)
        save(Contact(id = 101))
        save(Contact(id = 102, softDeleted = true))
        save(Contact(id = 201, visorContact = true, softDeleted = true))
        save(Contact(id = 202, visorContact = true))

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
