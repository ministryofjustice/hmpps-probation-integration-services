package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator.build
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

object OrderManagerGenerator {
    var DEFAULT = generate(startDateTime = ManagerGenerator.START_DATE_TIME)
    var NEW = generate(eventId = EventGenerator.NEW.id, startDateTime = ManagerGenerator.START_DATE_TIME)
    var HISTORIC = generate(eventId = EventGenerator.HISTORIC.id, startDateTime = ManagerGenerator.START_DATE_TIME)
    var DELETED_EVENT =
        generate(eventId = EventGenerator.DELETED.id, startDateTime = ManagerGenerator.START_DATE_TIME.minusDays(3))
    var INACTIVE_EVENT = generate(
        eventId = EventGenerator.INACTIVE.id,
        startDateTime = ManagerGenerator.START_DATE_TIME.minusDays(2),
        staff = StaffGenerator.STAFF_FOR_INACTIVE_EVENT
    )
    var INITIAL_ALLOCATION = generate(
        startDateTime = ZonedDateTime.of(2024, 5, 7, 12, 0, 0, 0, EuropeLondon),
        eventId = EventGenerator.HAS_INITIAL_ALLOCATION.id,
        staff = StaffGenerator.ALLOCATED,
        team = with(TeamGenerator.TEAM_IN_LAU) {
            Team(id, code, district.borough.probationArea.id, description, endDate)
        }
    )
    var UNALLOCATED = generate(
        startDateTime = ZonedDateTime.of(2024, 5, 1, 12, 0, 0, 0, EuropeLondon),
        eventId = EventGenerator.HAS_INITIAL_ALLOCATION.id,
        team = with(TeamGenerator.TEAM_IN_LAU) {
            Team(id, code, district.borough.probationArea.id, description, endDate)
        }
    ).also {
        it.endDate = ManagerGenerator.START_DATE_TIME
    }

    fun generate(
        eventId: Long = EventGenerator.DEFAULT.id,
        transferReasonId: Long = TransferReasonGenerator.CASE_ORDER.id,
        provider: Provider = ProviderGenerator.DEFAULT,
        team: Team = TeamGenerator.DEFAULT,
        staff: Staff = StaffGenerator.DEFAULT,
        startDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.AUDIT_USER.id,
        lastModifiedUserId: Long = UserGenerator.AUDIT_USER.id,
        version: Long = 0
    ) = OrderManager(
        eventId = eventId,
        transferReasonId = transferReasonId
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
