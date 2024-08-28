package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.IdentifierType
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.entity.KeyDate
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.model.Custody
import uk.gov.justice.digital.hmpps.model.Offence

@Service
class ConvictionService(
    private val convictionEventRepository: ConvictionEventRepository,
    private val custodyRepository: CustodyRepository
) {
    fun getConvictions(value: String, type: IdentifierType, activeOnly: Boolean): ConvictionsContainer {
        val convictions = when (type) {
            IdentifierType.CRN -> if (activeOnly) {
                convictionEventRepository.getAllByConvictionEventPersonCrnAndActiveIsTrue(
                    value
                )
            } else {
                convictionEventRepository.getAllByConvictionEventPersonCrn(value)
            }

            IdentifierType.NOMS -> if (activeOnly) {
                convictionEventRepository.getAllByConvictionEventPersonNomsNumberAndActiveIsTrue(
                    value
                )
            } else {
                convictionEventRepository.getAllByConvictionEventPersonNomsNumber(value)
            }
        }
        val convictionModels = mutableListOf<Conviction>()
        convictions.map { convictionEventEntity ->
            val custody = convictionEventEntity.disposal?.let { custodyRepository.getCustodyByDisposalId(it.id) }
            val offences = mutableListOf<Offence>()
            convictionEventEntity.mainOffence?.let {
                offences.add(
                    Offence(
                        convictionEventEntity.mainOffence.id,
                        convictionEventEntity.mainOffence.offence.description,
                        convictionEventEntity.mainOffence.offence.mainCategoryDescription,
                        true
                    )
                )
            }
            offences.addAll(
                convictionEventEntity.additionalOffences.map {
                    Offence(
                        it.id,
                        it.offence.description,
                        it.offence.mainCategoryDescription,
                        false
                    )
                }
            )
            convictionModels.add(
                Conviction(
                    convictionEventEntity.id,
                    convictionEventEntity.convictionDate,
                    convictionEventEntity.referralDate,
                    convictionEventEntity.disposal?.type?.description ?: "unknown",
                    convictionEventRepository.findLatestCourtAppearanceOutcome(convictionEventEntity.id) ?: "unknown",
                    offences,
                    convictionEventEntity.disposal?.asModel(custody),
                    active = convictionEventEntity.active
                )
            )
        }

        return ConvictionsContainer(convictionModels)
    }
}

private fun Disposal.asModel(custody: uk.gov.justice.digital.hmpps.entity.Custody?) =
    Sentence(id, startDate, expectedEndDate(), custody?.custodyModel())

private fun ReferenceData.custodialStatus() = CustodyStatus(code, description)
private fun uk.gov.justice.digital.hmpps.entity.Custody.custodyModel() =
    Custody(prisonerNumber, status.custodialStatus(), keyDates.map { it.toModel() })

private fun KeyDate.toModel() = uk.gov.justice.digital.hmpps.model.KeyDate(type.code, type.description, date)
