package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData

object PersonGenerator {
    val DEFAULT = generate("T123456", "A0123BY", ReferenceDataGenerator.TIER_2)
    val HANDOVER = generate("H123456", "A1024BY")
    val NO_MAPPA = generate("X123456", "A1024BX")
    val UPDATE_HANDOVER_AND_START = generate("H123457", "A2048BY")
    val CREATE_HANDOVER_AND_START = generate("H123458", "A4096BY")
    val CREATE_SENTENCE_CHANGED = generate("H123459", "A4096CY")
    val PERSON_NOT_FOUND = generate("H123410", "A4096DY")
    val PERSON_MULTIPLE_CUSTODIAL = generate("H123412", "A4096DX")

    fun generate(
        crn: String,
        nomsId: String? = null,
        currentTier: ReferenceData? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(
        crn,
        nomsId,
        currentTier,
        softDeleted,
        listOf(),
        id
    )
}

object PersonManagerGenerator {
    val PREVIOUS = generate(
        person = PersonGenerator.DEFAULT,
        team = ProviderGenerator.generateTeam("N03PRE"),
        staff = ProviderGenerator.generateStaff("N03PRE1", "Previous", "Manager"),
        active = false
    )
    val DELETED = generate(
        person = PersonGenerator.DEFAULT,
        team = ProviderGenerator.generateTeam("N03DEL"),
        staff = ProviderGenerator.generateStaff("N03DEL1", "Deleted", "Manager"),
        softDeleted = true
    )
    val DEFAULT = generate(
        person = PersonGenerator.DEFAULT,
        team = ProviderGenerator.DEFAULT_TEAM,
        staff = ProviderGenerator.DEFAULT_STAFF
    )

    val ALL = listOf(DEFAULT, PREVIOUS, DELETED)

    fun generate(
        person: Person,
        provider: ProbationArea = ProviderGenerator.DEFAULT_PROVIDER,
        team: Team = ProviderGenerator.DEFAULT_TEAM,
        staff: Staff = ProviderGenerator.DEFAULT_STAFF,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, team, staff, provider, active, softDeleted, id)
}
