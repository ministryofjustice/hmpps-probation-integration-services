package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.OffenderManager
import uk.gov.justice.digital.hmpps.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.entity.User

object ResponsibleOfficerGenerator {
    val DEFAULT_USER = User(
        id = IdGenerator.getAndIncrement(),
        staff = StaffGenerator.DEFAULT_STAFF,
        username = "BillyKid",
    )

    val DEFAULT_OFFENDER_MANAGER = OffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT,
        probationArea = ProbationAreaGenerator.DEFAULT_PROBATION_AREA,
        staff = StaffGenerator.DEFAULT_STAFF,
    )

    val DEFAULT_RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT,
        offenderManager = DEFAULT_OFFENDER_MANAGER,
    )
}
