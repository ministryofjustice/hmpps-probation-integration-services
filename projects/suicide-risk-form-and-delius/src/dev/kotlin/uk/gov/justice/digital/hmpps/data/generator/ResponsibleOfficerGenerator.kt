package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.ResponsibleOfficer

object ResponsibleOfficerGenerator {


    val DEFAULT_OFFENDER_MANAGER = OffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT,
    )

    val DEFAULT_RESPONSIBLE_OFFICER =
        ResponsibleOfficer(
            id = IdGenerator.getAndIncrement(),
            person = PersonGenerator.DEFAULT_PERSON,
            offenderManager = DEFAULT_OFFENDER_MANAGER,
            prisonOffenderManager = null,
            endDate = null
        )
}