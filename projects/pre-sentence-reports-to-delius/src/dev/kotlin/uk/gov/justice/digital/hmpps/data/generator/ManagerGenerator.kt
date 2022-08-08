package uk.gov.justice.digital.hmpps.data.generator

import UserGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ManagerBaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

interface ManagerGenerator {
    companion object {
        val START_DATE_TIME: ZonedDateTime =
            ZonedDateTime.of(2022, 7, 1, 10, 30, 0, 0, EuropeLondon)
    }

    fun ManagerBaseEntity.build(
        provider: Provider = ProviderGenerator.DEFAULT,
        team: Team = TeamGenerator.DEFAULT,
        staff: Staff = StaffGenerator.DEFAULT,
        dateTime: ZonedDateTime = START_DATE_TIME,
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.APPLICATION_USER.id,
        lastModifiedUserId: Long = UserGenerator.APPLICATION_USER.id,
        version: Long? = null
    ) = apply {
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
