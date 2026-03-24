package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.PrisonOffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.ResponsibleOfficer

object ResponsibleOfficerGenerator {

    val DEFAULT_OFFENDER_MANAGER = OffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT,
    )

    val DEFAULT_PRISON_OFFENDER_MANAGER = PrisonOffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PERSON_NO_REGISTRATIONS,
        staff = StaffGenerator.OFFICER_2,
    )

    val DEFAULT_RESPONSIBLE_OFFICER =
        ResponsibleOfficer(
            id = IdGenerator.getAndIncrement(),
            person = PersonGenerator.DEFAULT_PERSON,
            offenderManager = DEFAULT_OFFENDER_MANAGER,
            prisonOffenderManager = null,
            endDate = null
        )

    val DEFAULT_PRISON_RESPONSIBLE_OFFICER =
        ResponsibleOfficer(
            id = IdGenerator.getAndIncrement(),
            person = PersonGenerator.PERSON_NO_REGISTRATIONS,
            offenderManager = null,
            prisonOffenderManager = DEFAULT_PRISON_OFFENDER_MANAGER,
        )
}