package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.integrations.delius.*
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT_PERSON =
        generatePerson("A000001", false, IdGenerator.getAndIncrement(), "John", "Doe", "07745612923", "john@doe.com")
    val DEFAULT_COM = generatePersonManager(DEFAULT_PERSON, DEFAULT_PROVIDER, DEFAULT_TEAM, DEFAULT_STAFF)
    val PREVIOUS_EVENT = generateEvent(DEFAULT_PERSON, LocalDate.now().minusDays(7))
    val DEFAULT_EVENT = generateEvent(DEFAULT_PERSON, LocalDate.now().minusDays(5))

    fun generatePerson(
        crn: String,
        softDeleted: Boolean,
        id: Long,
        firstName: String,
        lastName: String,
        mobile: String,
        email: String
    ) =
        Person(crn, softDeleted, id, firstName, lastName, mobile, email)

    fun generatePersonManager(
        person: Person,
        provider: Provider,
        team: Team,
        staff: Staff,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, provider, team, staff, active, softDeleted, id)

    fun generateEvent(
        person: Person,
        referralDate: LocalDate,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(person, referralDate, active, softDeleted, id)
}