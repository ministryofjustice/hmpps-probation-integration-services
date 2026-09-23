package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.service.entity.Person
import uk.gov.justice.digital.hmpps.service.entity.StaffUser
import java.time.LocalDate
import java.time.ZonedDateTime

object PersonGenerator {
    val DEFAULT = generate("X123123")
    val NON_CUSTODIAL = generate("X123124", exclusionMessage = "Exclusion Applied")

    fun generate(
        crn: String,
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(
        listOf(),
        "Banner",
        "David",
        "Bruce",
        "",
        id,
        "NOMISID",
        crn,
        LocalDate.now().minusYears(18),
        ReferenceDataGenerator.TIER_1,
        exclusionMessage,
        restrictionMessage,
        softDeleted
    )

    fun generateExclusion(
        person: Person,
        user: StaffUser,
        start: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person.asLimitedAccessPerson(), user.asLimitedAccessUser(), start, endDateTime, id)

    fun generateRestriction(
        person: Person,
        user: StaffUser,
        start: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person.asLimitedAccessPerson(), user.asLimitedAccessUser(), start, endDateTime, id)

    fun Person.asLimitedAccessPerson() = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)
}
