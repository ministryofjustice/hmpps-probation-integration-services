package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Release
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Team
import java.time.LocalDate
import java.time.ZonedDateTime

data class Overview(
    val personalDetails: PersonalDetails,
    val communityManager: Manager?,
    val registerFlags: List<String>,
    val lastRelease: Release?,
    val activeConvictions: List<Conviction>
) {
    data class Manager(
        val staffCode: String,
        val name: Name,
        val provider: Provider,
        val team: Team
    ) {
        data class Provider(
            val code: String,
            val name: String
        )
        data class Team(
            val code: String,
            val name: String,
            val localAdminUnit: String,
            val telephone: String?,
            val email: String?
        )
    }
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

fun Team.toTeam() = Overview.Manager.Team(code, description, localAdminUnit = district.description, telephone, emailAddress)
fun Provider.toProvider() = Overview.Manager.Provider(code, description)
fun PersonManager.toManager() = Overview.Manager(
    staffCode = staff.code,
    name = staff.name(),
    team = team.toTeam(),
    provider = provider.toProvider()
)
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
