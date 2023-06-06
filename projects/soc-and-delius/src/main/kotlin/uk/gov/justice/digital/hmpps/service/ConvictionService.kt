package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.IdentifierType
import uk.gov.justice.digital.hmpps.entity.ConvictionEventRepository
import uk.gov.justice.digital.hmpps.model.Conviction
import uk.gov.justice.digital.hmpps.model.ConvictionsContainer
import uk.gov.justice.digital.hmpps.model.Offence

@Service
class ConvictionService(private val convictionEventRepository: ConvictionEventRepository) {
    fun getConvictions(value: String, type: IdentifierType): ConvictionsContainer {
        val convictions = when (type) {
            IdentifierType.CRN -> convictionEventRepository.getAllByConvictionEventPersonCrn(value)
            IdentifierType.NOMS -> convictionEventRepository.getAllByConvictionEventPersonNomsNumber(value)
        }
        val convictionModels = mutableListOf<Conviction>()
        convictions.map { convictionEventEntity ->
            val offences = mutableListOf<Offence>()
            offences.add(Offence(convictionEventEntity.mainOffence!!.offence.description, true))
            convictionEventEntity.additionalOffences.forEach { offences.add(Offence(it.offence.description, false)) }
            convictionModels.add(
                Conviction(
                    convictionEventEntity.convictionDate,
                    convictionEventEntity.disposal?.type?.description ?: "unknown",
                    offences
                )
            )
        }

        return ConvictionsContainer(convictionModels)
    }
}
