package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.entity.audit.BusinessInteractionCode
import java.time.ZonedDateTime

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            BusinessInteraction(id(), BusinessInteractionCode.ADD_CONTACT.code, ZonedDateTime.now()),
            BusinessInteraction(id(), BusinessInteractionCode.UPDATE_CONTACT.code, ZonedDateTime.now()),
            ContactTypeGenerator.CT_ESPCHI,
            ProviderGenerator.DEFAULT_PROVIDER,
            ProviderGenerator.DEFAULT_PDU,
            ProviderGenerator.DEFAULT_LDU,
            ProviderGenerator.DEFAULT_TEAM,
            ProviderGenerator.DEFAULT_STAFF,
            ProviderGenerator.DEFAULT_STAFF_USER,
            PersonGenerator.DEFAULT_PERSON,
            PersonGenerator.DEFAULT_COM,
            PersonGenerator.PERSON_CONTACT_DETAILS_1,
            PersonGenerator.generatePersonManager(PersonGenerator.PERSON_CONTACT_DETAILS_1),
            PersonGenerator.PERSON_CONTACT_DETAILS_2,
            PersonGenerator.generatePersonManager(PersonGenerator.PERSON_CONTACT_DETAILS_2),
            OffenceGenerator.BURGLARY,
            EventGenerator.EVENT_1,
            EventGenerator.EVENT_2,
            EventGenerator.EVENT_3,
            ContactGenerator.CONTACT_TO_REVIEW,
            ContactGenerator.CONTACT_TO_UPDATE,
            ContactGenerator.CONTACT_TO_UPDATE_EXPIRY
        )
    }
}
