package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.conviction.ConvictionRequirements
import uk.gov.justice.digital.hmpps.api.model.conviction.PssRequirement
import uk.gov.justice.digital.hmpps.api.model.keyValueOf
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.ConvictionEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.ConvictionRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.getByEventId
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.PssRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.PssRequirement as PssRequirementEntity

@Service
class RequirementService(
    private val personRepository: PersonRepository,
    private val convictionEventRepository: ConvictionEventRepository,
    private val convictionRequirementRepository: ConvictionRequirementRepository,
    private val pssRequirementRepository: PssRequirementRepository
) {
    fun getRequirementsByConvictionId(
        crn: String,
        convictionId: Long,
        includeInactive: Boolean,
        includeDeleted: Boolean
    ): ConvictionRequirements {

        val person = personRepository.getPerson(crn)
        val event = convictionEventRepository.getByEventId(convictionId, person.id)

        return ConvictionRequirements(
            convictionRequirementRepository
                .getRequirements(event.id, includeInactive, includeDeleted)
                .map { it.toRequirementModel() }
        )
    }

    fun getPssRequirementsByConvictionId(crn: String, convictionId: Long): List<PssRequirement> {
        val event = getEventForPersonByCrnAndEventId(crn, convictionId)

        return getPssRequirements(event.disposal?.custody?.id)
    }

    fun getEventForPersonByCrnAndEventId(crn: String, convictionId: Long): Event {
        val person = personRepository.getPerson(crn)
        return convictionEventRepository.getByEventId(convictionId, person.id)
    }

    fun getPssRequirements(custodyId: Long?) = if (custodyId == null) {
        listOf()
    } else {
        pssRequirementRepository.findAllByCustodyId(custodyId).map {
            it.toPssRequirement()
        }
    }
}

fun PssRequirementEntity.toPssRequirement(): PssRequirement =
    PssRequirement(
        mainCategory?.let { mainCategory.keyValueOf() },
        subCategory?.let { subCategory.keyValueOf()},
        active
    )



