package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficer
import java.time.ZonedDateTime

object ResponsibleOfficerGenerator {
    var DEFAULT = generate()
    var NEW = generate(personId = PersonGenerator.NEW_PM.id, PersonManagerGenerator.NEW)
    var HISTORIC = generate(personId = PersonGenerator.HISTORIC_PM.id, PersonManagerGenerator.HISTORIC)

    fun generate(
        personId: Long = PersonGenerator.DEFAULT.id,
        communityManager: PersonManager? = PersonManagerGenerator.DEFAULT,
        prisonManager: PrisonManager? = null,
        startDateTime: ZonedDateTime = communityManager?.startDate ?: ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.AUDIT_USER.id,
        lastModifiedUserId: Long = UserGenerator.AUDIT_USER.id,
        version: Long = 0
    ) = ResponsibleOfficer(
        personId,
        communityManager,
        prisonManager,
        startDateTime,
        endDateTime,
        version,
        createdUserId,
        lastModifiedUserId,
        createdDateTime,
        lastModifiedDateTime
    )
}
