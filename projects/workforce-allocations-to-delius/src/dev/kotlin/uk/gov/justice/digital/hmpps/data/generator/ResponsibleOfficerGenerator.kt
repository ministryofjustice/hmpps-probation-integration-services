package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficer
import java.time.ZonedDateTime

object ResponsibleOfficerGenerator {
    var DEFAULT = generate()

    fun generate(
        personId: Long = PersonGenerator.DEFAULT.id,
        communityManager: PersonManager? = PersonManagerGenerator.DEFAULT,
        prisonManager: PrisonManager? = null,
        startDateTime: ZonedDateTime = communityManager?.startDate ?: ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.APPLICATION_USER.id,
        lastModifiedUserId: Long = UserGenerator.APPLICATION_USER.id,
        version: Long = 0
    ) = ResponsibleOfficer(
        id,
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
