package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Team

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider("N01")
    val DEFAULT_TEAM = generateTeam("N01UAT")
    val UNALLOCATED_STAFF = generateStaff("N01UATU", "Unallocated", "Staff")
    val JOHN_SMITH = generateStaff("N01URT1", "John", "Smith")

    fun generateProvider(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Provider(code, description, id)

    fun generateTeam(
        code: String,
        provider: Provider = DEFAULT_PROVIDER,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, provider, id)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, id)
}