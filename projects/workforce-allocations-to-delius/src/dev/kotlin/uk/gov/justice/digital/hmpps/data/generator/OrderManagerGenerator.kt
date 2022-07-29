package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

object OrderManagerGenerator {
    var DEFAULT = generate(dateTime = ZonedDateTime.now().minusMonths(1))

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        eventId: Long = EventGenerator.DEFAULT.id,
        transferReasonId: Long = TransferReasonGenerator.CASE_ORDER.id,
        provider: Provider = ProviderGenerator.DEFAULT,
        team: Team = TeamGenerator.DEFAULT,
        staff: Staff = StaffGenerator.DEFAULT,
        dateTime: ZonedDateTime = ZonedDateTime.now(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.APPLICATION_USER.id,
        lastModifiedUserId: Long = UserGenerator.APPLICATION_USER.id,
        version: Long = 0
    ) = OrderManager(
        id,
        eventId,
        transferReasonId
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
