package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            DatasetGenerator.ADDRESS_STATUS,
            DatasetGenerator.ADDRESS_TYPE,
            AddressRDGenerator.CAS3_ADDRESS_TYPE,
            AddressRDGenerator.MAIN_ADDRESS_STATUS,
            AddressRDGenerator.PREV_ADDRESS_STATUS,
            BusinessInteractionGenerator.UPDATE_CONTACT,
            ContactTypeGenerator.EARS_CONTACT_TYPE,
            ContactTypeGenerator.EACA_CONTACT_TYPE,
            ContactTypeGenerator.EACO_CONTACT_TYPE,
            ContactTypeGenerator.EABP_CONTACT_TYPE,
            ContactTypeGenerator.EAAR_CONTACT_TYPE,
            ContactTypeGenerator.EADP_CONTACT_TYPE,
            PersonGenerator.PERSON_CRN,
            PersonGenerator.generatePersonManager(PersonGenerator.PERSON_CRN)
        )
        saveProviderDetails()
    }

    private fun saveProviderDetails() {
        saveAll(
            ProviderGenerator.DEFAULT_PROVIDER,
            ProviderGenerator.DEFAULT_TEAM,
            ProviderGenerator.DEFAULT_STAFF,
        )
    }
}