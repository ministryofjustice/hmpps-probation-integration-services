package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import java.time.LocalDate

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
)

data class Event(val number: String, val manager: Manager? = null)
data class Sentence(val type: String, val date: LocalDate, val length: String)

data class StaffMember(
    val code: String,
    val name: Name,
    val email: String? = null,
    val grade: String? = null,
)
data class Manager(val code: String, val name: Name, val teamCode: String, val grade: String? = null)

data class InitialAppointment(val date: LocalDate)

data class ProbationStatus(
    val status: ManagementStatus
) {
    val description = status.description
}

enum class ManagementStatus(
    val description: String
) {
    CURRENTLY_MANAGED("Currently managed"),
    PREVIOUSLY_MANAGED("Previously managed"),
    NEW_TO_PROBATION("New to probation"),
    UNKNOWN("Unknown")
}

enum class CaseType {
    LICENSE, // Typo of "LICENCE", but left as is to remain consistent with the legacy Workload Measurement Tool.
    CUSTODY,
    COMMUNITY,
    UNKNOWN
}

fun Person.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
fun Staff.name() = Name(forename, middleName, surname)
fun Staff.grade() = grade?.code?.let { Mappings.toAllocationsGradeCode[it] }
fun Staff.toManager(teamCode: String) = Manager(code, name(), teamCode, grade())
fun Staff.toStaffMember(email: String? = null) = StaffMember(code, name(), email, grade())
fun PersonManager.toManager() = staff.toManager(team.code)

object Mappings {
    val toAllocationsGradeCode: Map<String, String> = mapOf(
        "PSQ" to "PSO",
        "PSP" to "PQiP",
        "PSM" to "PO",
        "PSC" to "SPO"
    )
}
