package uk.gov.justice.digital.hmpps.integrations.delius.service

import uk.gov.justice.digital.hmpps.api.model.conviction.ConvictionRequirements
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.ConvictionRequirementRepository

class RequirementService(private val convictionRequirementRepository: ConvictionRequirementRepository) {

    fun getRequirementsByConvictionId(
        crn: String,
        convictionId: Long,
        includeInactive: Boolean,
        includeDeleted: Boolean
    ): ConvictionRequirements {

        return ConvictionRequirements(emptyList())
    }
}