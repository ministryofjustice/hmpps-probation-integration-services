package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.api.model.RecommendationModel.ConvictionDetails
import uk.gov.justice.digital.hmpps.api.model.RecommendationModel.ExtendedSentence
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event
import java.time.LocalDate

data class RecommendationModel(
    val personalDetails: PersonalDetailsOverview,
    val mainAddress: Address?,
    val lastRelease: Release?,
    val lastReleasedFromInstitution: Institution?,
    val mappa: Mappa?,
    val activeConvictions: List<Conviction>,
    val activeCustodialConvictions: List<ConvictionDetails>,
) {
    data class Institution(
        val name: String?,
        val description: String,
    )

    data class ConvictionDetails(
        val number: String,
        val sentence: ExtendedSentence?,
        val mainOffence: Offence,
        val additionalOffences: List<Offence>,
    )

    data class ExtendedSentence(
        val description: String,
        val length: Long?,
        val lengthUnits: String?,
        val secondLength: Long?,
        val secondLengthUnits: String?,
        val isCustodial: Boolean,
        val custodialStatusCode: String?,
        val startDate: LocalDate?,
        val licenceExpiryDate: LocalDate?,
        val sentenceExpiryDate: LocalDate?,
    )
}

fun Event.toConvictionDetails() =
    ConvictionDetails(
        number = number,
        mainOffence = Offence(mainOffence.date, mainOffence.offence.code, mainOffence.offence.description),
        additionalOffences = additionalOffences.map { Offence(it.date, it.offence.code, it.offence.description) },
        sentence =
            disposal?.let {
                ExtendedSentence(
                    description = it.type.description,
                    length = it.entryLength,
                    lengthUnits = it.entryLengthUnit?.description,
                    secondLength = it.secondEntryLength,
                    secondLengthUnits = it.secondEntryLengthUnit?.description,
                    isCustodial = it.custody != null,
                    custodialStatusCode = it.custody?.status?.code,
                    startDate = it.startDate,
                    sentenceExpiryDate = it.custody?.sentenceExpiryDate?.singleOrNull()?.date,
                    licenceExpiryDate = it.custody?.licenceExpiryDate?.singleOrNull()?.date,
                )
            },
    )
