package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.DetailPerson
import java.time.LocalDate

data class Detail(
    val name: Name,
    val dateOfBirth: LocalDate,
    val crn: String,
    val nomisId: String?,
    val pncNumber: String?,
    val offenderManager: Manager,
    val activeProbationManagedSentence: Boolean,
    val currentlyInPrison: Boolean,
    val mainOffence: String? = null,
    val profile: Profile? = null,
    val keyDates: List<KeyDate> = listOf(),
    val releaseDate: LocalDate? = null,
    val releaseReason: String? = null,
    val releaseLocation: String? = null,
    val lastRecallDate: LocalDate? = null,
    val recallReason: String? = null,
    val nsiRecallDate: LocalDate? = null,
    val nsiBreachDate: LocalDate? = null
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String
)

data class Manager(
    val name: Name,
    val team: Team,
    val provider: Provider
)

data class Provider(
    val code: String,
    val description: String
)

data class Ldu(val code: String, val name: String)

data class Team(
    val code: String,
    val localDeliveryUnit: Ldu
)

data class Profile(
    val nationality: String?,
    val religion: String?
)

data class KeyDate(
    val code: String,
    val description: String,
    val data: LocalDate
)

fun DetailPerson.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
