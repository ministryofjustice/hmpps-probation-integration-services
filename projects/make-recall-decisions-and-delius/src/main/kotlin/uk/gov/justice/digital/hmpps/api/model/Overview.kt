package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Release
import java.time.LocalDate
import java.time.ZonedDateTime

data class Overview(
    val personalDetails: PersonalDetailsOverview,
    val registerFlags: List<String>,
    val lastRelease: Release?,
    val activeConvictions: List<Conviction>
) {
    data class Release(
        val releaseDate: ZonedDateTime,
        val recallDate: ZonedDateTime?
    )
    data class Conviction(
        val number: String,
        val sentence: Sentence?,
        val mainOffence: String,
        val additionalOffences: List<String>
    )
    data class Sentence(
        val description: String,
        val length: Long?,
        val lengthUnits: String?,
        val isCustodial: Boolean,
        val custodialStatusCode: String?,
        val licenceExpiryDate: LocalDate?,
        val sentenceExpiryDate: LocalDate?
    )
}

fun Release.dates() = Overview.Release(date, recall?.date)
fun Event.toConviction() = Overview.Conviction(
    number = number,
    mainOffence = mainOffence.offence.description,
    additionalOffences = additionalOffences.map { it.offence.description },
    sentence = disposal?.let {
        Overview.Sentence(
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
