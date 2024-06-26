package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.conviction.ConvictionRequirements
import uk.gov.justice.digital.hmpps.api.model.conviction.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.ConvictionEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.ConvictionRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.getByEventId
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.Requirement as RequirementEntity

@Service
class RequirementService(
    private val personRepository: PersonRepository,
    private val convictionEventRepository: ConvictionEventRepository,
    private val convictionRequirementRepository: ConvictionRequirementRepository
) {
    fun getRequirementsByConvictionId(
        crn: String,
        convictionId: Long,
        includeInactive: Boolean,
        includeDeleted: Boolean
    ): ConvictionRequirements {

        personRepository.getPerson(crn)
        val event = convictionEventRepository.getByEventId(convictionId)

        return ConvictionRequirements(
            convictionRequirementRepository
                .getRequirements(event.id, includeInactive, includeDeleted)
                .map { it.toRequirement() }
        )
    }

    fun RequirementEntity.toRequirement(): Requirement =
        Requirement(
            id,
            notes,
            commencementDate,
            startDate,
            terminationDate,
            expectedStartDate,
            expectedEndDate,
            active,
            subCategory?.let { KeyValue(it.code, it.description) },
            mainCategory?.let { KeyValue(it.code, it.description) },
            adMainCategory?.let { KeyValue(it.code, it.description) },
            adSubCategory?.let { KeyValue(it.code, it.description) },
            terminationReason?.let { KeyValue(it.code, it.description) },
            length
        )
}