package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.DetailPerson
import java.time.LocalDate

data class Detail(
    val name: Name,
    val dateOfBirth: LocalDate,
    val crn: String,
    val nomisId: String?,
    val pncNumber: String?,
    val ldu: String,
    val probationArea: String,
    val offenderManager: Name,
    val mainOffence: String? = null,
    val religion: String? = null,
    val keyDates: List<KeyDate> = listOf(),
    val releaseDate: LocalDate? = null,
    val releaseLocation: String? = null
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String
)

data class KeyDate(
    val code: String,
    val description: String,
    val data: LocalDate
)

fun DetailPerson.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
