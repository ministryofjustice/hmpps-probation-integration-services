package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event

data class Conviction(
    val number: String,
    val sentence: Sentence?,
    val mainOffence: Offence,
    val additionalOffences: List<Offence>,
)

fun Event.toConviction() =
    Conviction(
        number = number,
        mainOffence = Offence(mainOffence.date, mainOffence.offence.code, mainOffence.offence.description),
        additionalOffences = additionalOffences.map { Offence(it.date, it.offence.code, it.offence.description) },
        sentence =
            disposal?.let {
                Sentence(
                    description = it.type.description,
                    length = it.entryLength,
                    lengthUnits = it.entryLengthUnit?.description,
                    isCustodial = it.custody != null,
                    custodialStatusCode = it.custody?.status?.code,
                    sentenceExpiryDate = it.custody?.sentenceExpiryDate?.singleOrNull()?.date,
                    licenceExpiryDate = it.custody?.licenceExpiryDate?.singleOrNull()?.date,
                )
            },
    )
