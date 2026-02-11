package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import java.time.ZonedDateTime

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            BusinessInteraction(
                IdGenerator.getAndIncrement(),
                BusinessInteractionCode.ADD_CONTACT.code,
                ZonedDateTime.now()
            ),
            BusinessInteraction(
                IdGenerator.getAndIncrement(),
                BusinessInteractionCode.UPDATE_CONTACT.code,
                ZonedDateTime.now()
            ),
            ContactTypeGenerator.CT_ESPCHI,
            ProviderGenerator.DEFAULT_PROVIDER,
            ProviderGenerator.DEFAULT_PDU,
            ProviderGenerator.DEFAULT_LDU,
            ProviderGenerator.DEFAULT_TEAM,
            ProviderGenerator.DEFAULT_STAFF,
            ProviderGenerator.DEFAULT_STAFF_USER,
            PersonGenerator.DEFAULT_PERSON,
            PersonGenerator.DEFAULT_COM,
            PersonGenerator.PREVIOUS_EVENT,
            PersonGenerator.DEFAULT_EVENT,
            PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1,
            PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_2,
            PersonGenerator.generatePersonManager(PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1),
            PersonGenerator.generatePersonManager(PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_2),
            ContactGenerator.CONTACT_TO_REVIEW,
            ContactGenerator.CONTACT_TO_UPDATE,
            ContactGenerator.CONTACT_TO_UPDATE_EXPIRY
        )
    }
}
