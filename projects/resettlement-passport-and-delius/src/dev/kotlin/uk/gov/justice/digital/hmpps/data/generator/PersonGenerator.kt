package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PersonManager
import uk.gov.justice.digital.hmpps.entity.Staff

object PersonGenerator {
    val DEFAULT = generate("X123123", "A1234YZ")
    val DEFAULT_MANAGER = generateManager(staff = ProviderGenerator.DEFAULT_STAFF)

    fun generate(
        crn: String,
        noms: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) =
        Person(
            id,
            crn,
            noms,
            softDeleted
        )

    fun generateManager(
        person: Person = PersonGenerator.DEFAULT,
        staff: Staff,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, staff, softDeleted, active, id)
}
