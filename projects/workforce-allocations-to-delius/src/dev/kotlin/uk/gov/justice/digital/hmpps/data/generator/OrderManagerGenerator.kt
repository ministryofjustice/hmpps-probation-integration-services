package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import UserGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator.build
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

object OrderManagerGenerator {
    var DEFAULT = generate(startDateTime = ZonedDateTime.of(2022, 7, 1, 10, 30, 0, 0, EuropeLondon))

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        eventId: Long = EventGenerator.DEFAULT.id,
        transferReasonId: Long = TransferReasonGenerator.CASE_ORDER.id,
        provider: Provider = ProviderGenerator.DEFAULT,
        team: Team = TeamGenerator.DEFAULT,
        staff: Staff = StaffGenerator.DEFAULT,
        startDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.APPLICATION_USER.id,
        lastModifiedUserId: Long = UserGenerator.APPLICATION_USER.id,
        version: Long = 0
    ) = OrderManager(
        id,
        eventId,
        transferReasonId
    ).build(
        provider,
        team,
        staff,
        startDateTime,
        createdDateTime,
        lastModifiedDateTime,
        createdUserId,
        lastModifiedUserId,
        version
    ) as OrderManager
}
