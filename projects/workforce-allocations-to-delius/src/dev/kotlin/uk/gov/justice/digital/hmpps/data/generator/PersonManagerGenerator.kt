package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator.build
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

object PersonManagerGenerator {
    var DEFAULT = generate(startDateTime = ManagerGenerator.START_DATE_TIME)
    var NEW = generate(startDateTime = ManagerGenerator.START_DATE_TIME, personId = PersonGenerator.NEW_PM.id)
    var HISTORIC = generate(startDateTime = ManagerGenerator.START_DATE_TIME, personId = PersonGenerator.HISTORIC_PM.id)

    fun generate(
        personId: Long = PersonGenerator.DEFAULT.id,
        provider: Provider = ProviderGenerator.DEFAULT,
        team: Team = TeamGenerator.DEFAULT,
        staff: Staff = StaffGenerator.DEFAULT,
        startDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.AUDIT_USER.id,
        lastModifiedUserId: Long = UserGenerator.AUDIT_USER.id,
        version: Long = 0
    ) = PersonManager(
        personId
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
    ) as PersonManager
}
