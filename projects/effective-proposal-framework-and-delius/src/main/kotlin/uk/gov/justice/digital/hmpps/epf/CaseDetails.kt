package uk.gov.justice.digital.hmpps.epf

import uk.gov.justice.digital.hmpps.epf.entity.Person
import java.time.LocalDate
import java.time.temporal.ChronoUnit.YEARS

data class CaseDetails(
    val nomsId: String?,
    val name: Name,
    val dateOfBirth: LocalDate,
    val gender: String,
    val courtAppearance: Appearance?,
    val sentence: Sentence?,
    val responsibleProvider: Provider?,
    val ogrsScore: Long?,
    val rsrScore: Double?,
    val limitedAccess: LimitedAccessDetail?
) {
    val age
        get() = YEARS.between(dateOfBirth, LocalDate.now())

    val ageAtRelease
        get() = sentence?.expectedReleaseDate?.let { YEARS.between(dateOfBirth, it) }
}

data class Name(val forename: String, val middleName: String?, val surname: String)
data class Appearance(
    val date: LocalDate,
    val court: Court
)

data class Sentence(
    val expectedReleaseDate: LocalDate?
)

data class Court(val name: String)
data class Provider(val code: String, val name: String)

data class LimitedAccessDetail(
    val excludedFrom: List<LimitedAccess.ExcludedFrom>,
    val restrictedTo: List<LimitedAccess.RestrictedTo>
)

sealed interface LimitedAccess {
    val email: String

    data class RestrictedTo(override val email: String) : LimitedAccess
    data class ExcludedFrom(override val email: String) : LimitedAccess
}

fun Person.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
