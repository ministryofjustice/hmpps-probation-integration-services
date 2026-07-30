package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Caseload

object CaseloadGenerator {
    val CL_DEFAULT_1 = Caseload(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT,
        staff = StaffGenerator.DEFAULT,
        team = TeamGenerator.DEFAULT,
        roleCode = "OM"
    )
    val CL_DEFAULT_2 = Caseload(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.EXCLUDED,
        staff = StaffGenerator.DEFAULT,
        team = TeamGenerator.DEFAULT,
        roleCode = "OM"
    )
    val CL_DEFAULT_3 = Caseload(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.RESTRICTED,
        staff = StaffGenerator.DEFAULT,
        team = TeamGenerator.DEFAULT,
        roleCode = "OM"
    )
    val CL_DEFAULT_4 = Caseload(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.WITH_NON_ROSH_REGISTRATION,
        staff = StaffGenerator.DEFAULT,
        team = TeamGenerator.DEFAULT,
        roleCode = "OM"
    )

    val CL_BOTH_DEFAULT_1 = Caseload(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT,
        staff = StaffGenerator.BOTH_TEAMS_STAFF,
        team = TeamGenerator.DEFAULT,
        roleCode = "OM"
    )
    val CL_BOTH_DEFAULT_2 = Caseload(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.EXCLUDED,
        staff = StaffGenerator.BOTH_TEAMS_STAFF,
        team = TeamGenerator.DEFAULT,
        roleCode = "OM"
    )
    val CL_BOTH_DEFAULT_3 = Caseload(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.RESTRICTED,
        staff = StaffGenerator.BOTH_TEAMS_STAFF,
        team = TeamGenerator.DEFAULT,
        roleCode = "OM"
    )
    val CL_BOTH_DEFAULT_4 = Caseload(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.WITH_NON_ROSH_REGISTRATION,
        staff = StaffGenerator.BOTH_TEAMS_STAFF,
        team = TeamGenerator.DEFAULT,
        roleCode = "OM"
    )
    val CL_BOTH_DEFAULT_5 = Caseload(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.TEAM,
        staff = StaffGenerator.BOTH_TEAMS_STAFF,
        team = TeamGenerator.DEFAULT,
        roleCode = "OM"
    )

    val CL_BOTH_OTHER_1 = Caseload(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.OTHER_TEAM,
        staff = StaffGenerator.BOTH_TEAMS_STAFF,
        team = TeamGenerator.OTHER_TEAM,
        roleCode = "OM"
    )
}