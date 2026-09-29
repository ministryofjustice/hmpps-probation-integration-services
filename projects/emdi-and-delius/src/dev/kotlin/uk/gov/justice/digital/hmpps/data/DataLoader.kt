package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.ProbationDeliveryUnitGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            ProviderGenerator.DEFAULT,
            ProviderGenerator.LONDON,
            ProviderGenerator.NORTH,
            ProviderGenerator.INACTIVE
        )

        saveAll(
            ProbationDeliveryUnitGenerator.DEFAULT,
            ProbationDeliveryUnitGenerator.SECOND,
            ProbationDeliveryUnitGenerator.THIRD,
            ProbationDeliveryUnitGenerator.INACTIVE_PDU,
            ProbationDeliveryUnitGenerator.PDU_IN_INACTIVE_PROVIDER,
            ProbationDeliveryUnitGenerator.LONDON_PDU
        )
    }
}
