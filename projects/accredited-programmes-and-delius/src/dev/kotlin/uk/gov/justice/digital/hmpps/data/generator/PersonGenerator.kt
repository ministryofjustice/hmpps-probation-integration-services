package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PersonCrn
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.staff.Manager
import java.time.LocalDate

object PersonGenerator {
    fun generate(crn: String, gender: ReferenceData, ethnicity: ReferenceData, manager: Manager? = null) = Person(
        id = IdGenerator.getAndIncrement(),
        crn = crn,
        forename = "Forename",
        secondName = "MiddleName",
        thirdName = null,
        surname = "Surname",
        dateOfBirth = LocalDate.now().minusYears(45).minusMonths(6),
        exclusionMessage = "Exclusion message",
        restrictionMessage = "Restriction message",
        gender = gender,
        ethnicity = ethnicity,
        manager = manager,
    )

    fun Person.toCrn() = PersonCrn(id, crn, softDeleted)
}
