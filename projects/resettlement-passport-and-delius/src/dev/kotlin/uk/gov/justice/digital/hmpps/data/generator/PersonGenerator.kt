package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT = generate(
        "X123123",
        "A1234YZ",
        "Fred",
        "Williams",
        LocalDate.of(1982, 8, 19),
        "020 346 7982",
        "07452819463",
        "freddy@justice.co.uk"
    )
    val DEFAULT_MANAGER = generateManager(DEFAULT)
    val CREATE_APPOINTMENT = generate("C123456", null, "James", "Brown", LocalDate.of(1990, 5, 12))

    val RP9_CONTACT_TYPE = generateContactType("RP9", "RP9")
    val RP10_CONTACT_TYPE = generateContactType("RP10", "RP10")
    fun generate(
        crn: String,
        noms: String? = null,
        firstName: String,
        surname: String,
        dateOfBirth: LocalDate,
        telephoneNumber: String? = null,
        mobileNumber: String? = null,
        emailAddress: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(
        id,
        crn,
        noms,
        firstName,
        surname,
        dateOfBirth,
        telephoneNumber,
        mobileNumber,
        emailAddress,
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

    fun generateContactType(code: String, description: String) = CaseNoteType(
        id = IdGenerator.getAndIncrement(),
        code = code,
        description = description
    )
}
