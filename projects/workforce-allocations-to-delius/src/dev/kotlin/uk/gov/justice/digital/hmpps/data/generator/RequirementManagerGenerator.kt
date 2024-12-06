package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

object RequirementManagerGenerator : ManagerGenerator {
    var DEFAULT = generate(startDateTime = ZonedDateTime.now().minusMonths(1))
    var NEW = generate(requirementId = RequirementGenerator.NEW.id)
    var HISTORIC = generate(requirementId = RequirementGenerator.HISTORIC.id)
    fun generate(
        requirementId: Long = RequirementGenerator.DEFAULT.id,
        transferReasonId: Long = TransferReasonGenerator.COMPONENT.id,
        provider: Provider = ProviderGenerator.DEFAULT,
        team: Team = TeamGenerator.DEFAULT,
        staff: Staff = StaffGenerator.DEFAULT,
        startDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.AUDIT_USER.id,
        lastModifiedUserId: Long = UserGenerator.AUDIT_USER.id,
        version: Long = 0
    ) = RequirementManager(
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
