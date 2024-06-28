package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.MutableLimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCase
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffUser
import java.time.LocalDateTime

object LimitedAccessGenerator {
    val FULL_ACCESS_USER = generateLaoUser(StaffGenerator.LAO_FULL_ACCESS_USER)
    val LIMITED_ACCESS_USER = generateLaoUser(StaffGenerator.LAO_RESTRICTED_USER)
    val EXCLUDED_CASE = generateLaoPerson(ProbationCaseGenerator.CASE_LAO_EXCLUSION, exclusionMessage = "This case has an exclusion")
    val RESTRICTED_CASE = generateLaoPerson(ProbationCaseGenerator.CASE_LAO_RESTRICTED, restrictionMessage = "This case has an restriction")

    fun generateLaoUser(staffUser: StaffUser) = LimitedAccessUser(staffUser.username, staffUser.id)

    fun generateLaoPerson(
        probationCase: ProbationCase,
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
    ) = MutableLimitedAccessPerson(probationCase.crn, exclusionMessage, restrictionMessage, probationCase.id)

    fun generateExclusion(
        person: LimitedAccessPerson,
        user: LimitedAccessUser = LIMITED_ACCESS_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Exclusion(person, user, endDateTime, id)

    fun generateRestriction(
        person: LimitedAccessPerson,
        user: LimitedAccessUser = FULL_ACCESS_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Restriction(person, user, endDateTime, id)
}
