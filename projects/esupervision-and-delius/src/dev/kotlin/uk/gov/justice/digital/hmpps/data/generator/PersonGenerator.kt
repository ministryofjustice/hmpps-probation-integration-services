package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT_PERSON = generatePerson("A000001")
    val PERSON_CONTACT_DETAILS_1 = generatePerson("A000002")
    val PERSON_CONTACT_DETAILS_2 = generatePerson("A000003")
    val NO_ACTIVE_EVENT_PERSON = generatePerson("A000004")
    val FALLBACK_EVENT_PERSON = generatePerson("A000005")
    val PUNCTUATION_IN_NAME = generatePerson("A000006", "Joe", "O'Neil")
    val SENSITIVE_CONTACT = generatePerson("A000007")

    val DEFAULT_COM = generatePersonManager(DEFAULT_PERSON)
    val NO_ACTIVE_EVENT_COM = generatePersonManager(NO_ACTIVE_EVENT_PERSON)
    val FALLBACK_EVENT_COM = generatePersonManager(FALLBACK_EVENT_PERSON)
    val SENSITIVE_CONTACT_MANAGER = generatePersonManager(SENSITIVE_CONTACT)

    fun generatePerson(
        crn: String,
        firstName: String = "John",
        lastName: String = "Doe",
        id: Long = id()
    ) = Person(id, crn, LocalDate.of(1985, 10, 1), firstName, lastName, "07123456789", "john@example.com")

    fun generatePersonManager(
        person: Person,
        provider: Provider = DEFAULT_PROVIDER,
        team: Team = DEFAULT_TEAM,
        staff: Staff = DEFAULT_STAFF,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = id()
    ) = PersonManager(person, provider, team, staff, active, softDeleted, id)
}
