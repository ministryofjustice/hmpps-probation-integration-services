package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator.build
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

object PersonManagerGenerator {
    var DEFAULT = generate(
        startDateTime = ManagerGenerator.START_DATE_TIME,
        allocationReason = ReferenceDataGenerator.INITIAL_OM_ALLOCATION
    )
    var NEW = generate(
        startDateTime = ManagerGenerator.START_DATE_TIME,
        personId = PersonGenerator.NEW_PM.id,
        allocationReason = ReferenceDataGenerator.INITIAL_OM_ALLOCATION
    )
    var HISTORIC = generate(
        startDateTime = ManagerGenerator.START_DATE_TIME,
        personId = PersonGenerator.HISTORIC_PM.id,
        allocationReason = ReferenceDataGenerator.INITIAL_OM_ALLOCATION
    )

    fun generate(
        personId: Long = PersonGenerator.DEFAULT.id,
        provider: Provider = ProviderGenerator.DEFAULT,
        team: Team = TeamGenerator.DEFAULT,
        staff: Staff = StaffGenerator.DEFAULT,
        startDateTime: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.AUDIT_USER.id,
        lastModifiedUserId: Long = UserGenerator.AUDIT_USER.id,
        allocationReason: ReferenceData,
        version: Long = 0
    ) = PersonManager(
        personId
    ).build(
        provider,
        team,
        staff,
        startDateTime,
        endDateTime,
        createdDateTime,
        lastModifiedDateTime,
        createdUserId,
        lastModifiedUserId,
        allocationReason,
        version
    ) as PersonManager
}
