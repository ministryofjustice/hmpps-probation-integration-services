package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.OffenderManager
import uk.gov.justice.digital.hmpps.entity.PrisonOffenderManager
import uk.gov.justice.digital.hmpps.entity.ResponsibleOfficer

object ResponsibleOfficerGenerator {
    val DEFAULT_OFFENDER_MANAGER = OffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        probationArea = ProbationAreaGenerator.DEFAULT_PROBATION_AREA,
        staff = StaffGenerator.DEFAULT_PROBATION_STAFF,
        softDeleted = false
    )

    val DEFAULT_PROBATION_RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        offenderManager = DEFAULT_OFFENDER_MANAGER,
        prisonOffenderManager = null,
        endDate = null
    )

    val DEFAULT_PRISON_OFFENDER_MANAGER = PrisonOffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PERSON_IN_PRISON,
        probationArea = ProbationAreaGenerator.DEFAULT_PROBATION_AREA,
        staff = StaffGenerator.DEFAULT_PROBATION_STAFF, //this is ok for the test
        softDeleted = false,
        emailAddress = "om@prison.gov.uk",
        telephoneNumber = "01234567890"
    )

    val DEFAULT_PRISON_RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PERSON_IN_PRISON,
        offenderManager = null,
        prisonOffenderManager = DEFAULT_PRISON_OFFENDER_MANAGER,
        endDate = null
    )

}