package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.OffenderManager
import uk.gov.justice.digital.hmpps.entity.PrisonOffenderManager
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

    val PRISON_OFFENDER_MANAGER = PrisonOffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PRISON_MANAGED,
        probationArea = ProbationAreaGenerator.HOME_PROBATION_AREA,
        staff = StaffGenerator.PRISON_STAFF,
    )

    val PRISON_RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PRISON_MANAGED,
        prisonOffenderManager = PRISON_OFFENDER_MANAGER,
    )

    val NO_PREFERRED_ADDRESS_OFFENDER_MANAGER = OffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.NO_PREFERRED_ADDRESS,
        probationArea = ProbationAreaGenerator.HOME_PROBATION_AREA,
        staff = StaffGenerator.NO_PREFERRED_ADDRESS_STAFF,
    )

    val NO_PREFERRED_ADDRESS_RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.NO_PREFERRED_ADDRESS,
        offenderManager = NO_PREFERRED_ADDRESS_OFFENDER_MANAGER,
    )

}
