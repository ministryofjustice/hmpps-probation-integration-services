package uk.gov.justice.digital.hmpps.integrations.tier

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTier
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierId
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrnAndSoftDeletedIsFalse
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getByCodeAndSetName

@Service
class TierService(
    private val tierClient: TierClient,
    private val personRepository: PersonRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val managementTierRepository: ManagementTierRepository,
) {
    fun updateTier(crn: String, calculationId: String) {
        writeTierUpdate(crn, calculationId)
    }

    private fun writeTierUpdate(crn: String, calculationId: String) {
        val tierCalculation = tierClient.getTierCalculation(crn, calculationId)
        val tier = referenceDataRepository.getByCodeAndSetName(tierCalculation.tierScore, "TIER")
        val person = personRepository.getByCrnAndSoftDeletedIsFalse(crn)
        val changeReason = referenceDataRepository.getByCodeAndSetName("ATS", "TIER CHANGE REASON")
        managementTierRepository.save(
            ManagementTier(
                id = ManagementTierId(
                    offenderId = person.id,
                    tierId = tier.id,
                    dateChanged = tierCalculation.calculationDate
                ),
                tierChangeReasonId = changeReason.id
            )
        )
    }
}
