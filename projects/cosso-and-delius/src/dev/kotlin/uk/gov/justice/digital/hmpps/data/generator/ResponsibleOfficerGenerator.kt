package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.OffenderManager
import uk.gov.justice.digital.hmpps.entity.PrisonOffenderManager
import uk.gov.justice.digital.hmpps.entity.ResponsibleOfficer

object ResponsibleOfficerGenerator {
    val DEFAULT_OFFENDER_MANAGER = OffenderManager(
        id = id(),
        person = PersonGenerator.DEFAULT_PERSON,
        probationArea = ProbationAreaGenerator.DEFAULT_PROBATION_AREA,
        staff = StaffGenerator.DEFAULT_PROBATION_STAFF,
    )

    val DEFAULT_PROBATION_RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = id(),
        person = PersonGenerator.DEFAULT_PERSON,
        offenderManager = DEFAULT_OFFENDER_MANAGER,
    )

    val DEFAULT_PRISON_OFFENDER_MANAGER = PrisonOffenderManager(
        id = id(),
        person = PersonGenerator.PERSON_IN_PRISON,
        probationArea = ProbationAreaGenerator.DEFAULT_PROBATION_AREA,
        staff = StaffGenerator.PRISON_OFFENDER_MANAGER_STAFF,
        emailAddress = "om@prison.gov.uk",
        telephoneNumber = "01234567890"
    )

    val DEFAULT_PRISON_RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = id(),
        person = PersonGenerator.PERSON_IN_PRISON,
        offenderManager = null,
        prisonOffenderManager = DEFAULT_PRISON_OFFENDER_MANAGER,
    )

    val PROBATION_MANAGER_WITHOUT_USER = OffenderManager(
        id = id(),
        person = PersonGenerator.PERSON_WITH_RESPONSIBLE_OFFICER_WITHOUT_USER,
        probationArea = ProbationAreaGenerator.DEFAULT_PROBATION_AREA,
        staff = StaffGenerator.PROBATION_STAFF_WITHOUT_USER,
    )

    val RESPONSIBLE_OFFICER_WITHOUT_USER = ResponsibleOfficer(
        id = id(),
        person = PersonGenerator.PERSON_WITH_RESPONSIBLE_OFFICER_WITHOUT_USER,
        offenderManager = PROBATION_MANAGER_WITHOUT_USER,
    )
}
