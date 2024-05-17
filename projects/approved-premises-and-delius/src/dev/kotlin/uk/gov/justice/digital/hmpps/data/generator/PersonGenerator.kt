package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.time.LocalDate
import java.time.ZonedDateTime

object PersonGenerator {
    val DEFAULT = generate(crn = "A000001")
    val PERSON_INACTIVE_EVENT = generate(crn = "A000002")
    val EVENT = generateEvent("7", DEFAULT.id)
    val ANOTHER_EVENT = generateEvent("8", DEFAULT.id)
    val INACTIVE_EVENT = generateEvent("6", PERSON_INACTIVE_EVENT.id, active = false)
    val PERSON_WITH_BOOKING = generate(crn = "B000001")

    fun generate(
        crn: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(id = id, crn = crn)

    fun generateEvent(
        number: String,
        personId: Long,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(id, number, personId, active, softDeleted)

    fun generateRegistration(
        person: Person,
        type: RegisterType,
        date: LocalDate,
        category: ReferenceData? = null,
        level: ReferenceData? = null,
        softDeleted: Boolean = false,
        deregistered: Boolean = false,
        lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Registration(person.id, type, category, level, date, softDeleted, deregistered, lastUpdatedDateTime, id)
}

object PersonManagerGenerator {
    fun generate(
        person: Person,
        team: Team = TeamGenerator.generate(),
        staff: Staff = StaffGenerator.generate(teams = listOf(team)),
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(
        id = id,
        personId = person.id,
        staff = staff,
        team = team
    )
}
