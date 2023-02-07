package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData

object PersonGenerator {
    val DEFAULT = generate("T123456", "A00123Y", ReferenceDataGenerator.TIER_2)

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
        ProviderGenerator.generateTeam("N03PRE"),
        ProviderGenerator.generateStaff("N03PRE1", "Previous", "Manager"),
        active = false
    )
    val DELETED = generate(
        ProviderGenerator.generateTeam("N03DEL"),
        ProviderGenerator.generateStaff("N03DEL1", "Deleted", "Manager"),
        softDeleted = true
    )
    val DEFAULT = generate(ProviderGenerator.DEFAULT_TEAM, ProviderGenerator.DEFAULT_STAFF)

    val ALL = listOf(DEFAULT, PREVIOUS, DELETED)

    fun generate(
        team: Team = ProviderGenerator.DEFAULT_TEAM,
        staff: Staff = ProviderGenerator.DEFAULT_STAFF,
        person: Person = PersonGenerator.DEFAULT,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, team, staff, active, softDeleted, id)
}
