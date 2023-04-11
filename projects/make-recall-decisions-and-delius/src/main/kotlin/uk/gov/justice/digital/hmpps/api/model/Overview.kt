package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Release
import java.time.LocalDate

data class Overview(
    val personalDetails: PersonalDetailsOverview,
    val registerFlags: List<String>,
    val lastRelease: Release?,
    val activeConvictions: List<Conviction>
) {
    data class Release(
        val releaseDate: LocalDate,
        val recallDate: LocalDate?
    )
    data class Conviction(
        val number: String,
        val sentence: Sentence?,
        val mainOffence: Offence,
        val additionalOffences: List<Offence>
    )
}

fun Release.dates() = Overview.Release(date, recall?.date)
fun List<Event>.singleCustody() = mapNotNull { it.disposal?.custody }.singleOrNull()
fun Event.toConviction() = Overview.Conviction(
    number = number,
    mainOffence = Offence(mainOffence.date, mainOffence.offence.code, mainOffence.offence.description),
    additionalOffences = additionalOffences.map { Offence(it.date, it.offence.code, it.offence.description) },
    sentence = disposal?.let {
        Sentence(
            description = it.type.description,
            length = it.entryLength,
            lengthUnits = it.entryLengthUnit?.description,
            isCustodial = it.custody != null,
            custodialStatusCode = it.custody?.status?.code,
            sentenceExpiryDate = it.custody?.sentenceExpiryDate?.date,
            licenceExpiryDate = it.custody?.licenceExpiryDate?.date
        )
    }
)
