package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.User

object ResponsibleOfficerGenerator {

    val DEFAULT_RO_STAFF = Staff(
        id = IdGenerator.getAndIncrement(),
        code = "N00A001",
        firstName = "Probation",
        middleName = "PO",
        surname = "Officer",
        user = null
    )

    val DEFAULT_RO_USER = User(
        id = IdGenerator.getAndIncrement(),
        username = "officer",
        staff = DEFAULT_RO_STAFF
    )

    val DEFAULT_OFFENDER_MANAGER = OffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        staff = DEFAULT_RO_STAFF
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