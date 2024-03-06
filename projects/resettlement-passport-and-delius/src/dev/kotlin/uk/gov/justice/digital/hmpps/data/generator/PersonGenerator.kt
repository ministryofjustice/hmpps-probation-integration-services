package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PersonManager
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.Team

object PersonGenerator {
    val DEFAULT = generate("X123123", "A1234YZ")
    val DEFAULT_MANAGER = generateManager(DEFAULT)
    val CREATE_APPOINTMENT = generate("C123456")

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
        person: Person,
        team: Team = ProviderGenerator.DEFAULT_TEAM,
        staff: Staff = ProviderGenerator.DEFAULT_STAFF,
        probationAreaId: Long = ProviderGenerator.DEFAULT_AREA.id,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, team, staff, probationAreaId, softDeleted, active, id)
}
