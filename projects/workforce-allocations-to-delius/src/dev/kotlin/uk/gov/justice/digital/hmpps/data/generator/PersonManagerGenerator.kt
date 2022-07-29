package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

object PersonManagerGenerator {
    var DEFAULT = generate(dateTime = ZonedDateTime.now().minusMonths(1))

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        personId: Long = PersonGenerator.DEFAULT.id,
        provider: Provider = ProviderGenerator.DEFAULT,
        team: Team = TeamGenerator.DEFAULT,
        staff: Staff = StaffGenerator.DEFAULT,
        dateTime: ZonedDateTime = ZonedDateTime.now(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.APPLICATION_USER.id,
        lastModifiedUserId: Long = UserGenerator.APPLICATION_USER.id,
        version: Long = 0
    ) = PersonManager(
        id,
        personId
    ).apply {
        this.provider = provider
        this.team = team
        this.staff = staff
        trustProviderTeam = team
        staffEmployee = staff
        startDate = dateTime
        this.createdDateTime = createdDateTime
        this.lastModifiedDateTime = lastModifiedDateTime
        this.createdUserId = createdUserId
        this.lastModifiedUserId = lastModifiedUserId
        this.version = version
    }
}