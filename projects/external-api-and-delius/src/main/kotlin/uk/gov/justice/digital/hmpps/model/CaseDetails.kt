package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import java.time.LocalDate
import java.time.temporal.ChronoUnit.YEARS

data class CaseDetails(
    val nomsId: String?,
    val name: Name,
    val dateOfBirth: LocalDate,
    val gender: String,
    val courtAppearance: Appearance?,
    val sentence: SentenceSummary?,
    val responsibleProvider: ResponsibleProvider?,
    val ogrsScore: Long?,
    val rsrScore: Double?,
    val limitedAccess: LimitedAccessDetail?
) {
    val age = YEARS.between(dateOfBirth, LocalDate.now())
    val ageAtRelease = sentence?.expectedReleaseDate?.let { YEARS.between(dateOfBirth, it) }
}

data class Appearance(
    val date: LocalDate,
    val court: Court
)

data class SentenceSummary(
    val expectedReleaseDate: LocalDate?
)

data class Court(val name: String)
data class ResponsibleProvider(val code: String, val name: String)

data class LimitedAccessDetail(
    val excludedFrom: List<LimitedAccess.ExcludedFrom>,
    val exclusionMessage: String?,
    val restrictedTo: List<LimitedAccess.RestrictedTo>,
    val restrictionMessage: String?,
)

sealed interface LimitedAccess {
    val email: String

    data class RestrictedTo(override val email: String) : LimitedAccess
    data class ExcludedFrom(override val email: String) : LimitedAccess
}

fun Person.name() =
    Name(forename, surname, listOfNotNull(secondName, thirdName).joinToString(" ").takeIf { it.isNotBlank() })
