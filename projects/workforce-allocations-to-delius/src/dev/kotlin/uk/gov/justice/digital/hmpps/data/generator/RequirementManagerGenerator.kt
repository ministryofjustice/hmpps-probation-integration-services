package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

object RequirementManagerGenerator : ManagerGenerator {
    var DEFAULT = generate(startDateTime = ZonedDateTime.now().minusMonths(1))

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        requirementId: Long = RequirementGenerator.DEFAULT.id,
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
    ) = RequirementManager(
        id,
        requirementId,
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
    ) as RequirementManager
}
