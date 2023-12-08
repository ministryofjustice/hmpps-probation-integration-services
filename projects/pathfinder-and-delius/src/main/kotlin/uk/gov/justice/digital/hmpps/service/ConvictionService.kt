package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.ConvictionEventRepository
import uk.gov.justice.digital.hmpps.model.BatchRequest
import uk.gov.justice.digital.hmpps.model.Conviction
import uk.gov.justice.digital.hmpps.model.ConvictionsContainer
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.PersonConviction

@Service
class ConvictionService(private val convictionEventRepository: ConvictionEventRepository) {
    fun getConvictions(batchRequest: BatchRequest): ConvictionsContainer {
        val convictions = convictionEventRepository.getAllByConvictionEventPersonCrnIn(batchRequest.crns)

        val personConvictions =
            convictions.groupBy { it.convictionEventPerson.crn }
                .map {
                    PersonConviction(
                        it.key,
                        it.value
                            .map { c ->
                                Conviction(
                                    c.convictionDate,
                                    c.disposal?.type?.description ?: "unknown",
                                    listOf(
                                        Offence(c.mainOffence!!.offence.description, true),
                                    ) + c.additionalOffences.map { o -> Offence(o.offence.description, false) },
                                )
                            },
                    )
                }
        return ConvictionsContainer(personConvictions)
    }
}
