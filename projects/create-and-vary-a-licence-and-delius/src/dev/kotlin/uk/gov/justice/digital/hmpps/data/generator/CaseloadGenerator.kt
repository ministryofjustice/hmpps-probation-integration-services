package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.ManagedOffender
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.entity.Caseload
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.service.asStaff
import uk.gov.justice.digital.hmpps.service.asTeam
import java.time.LocalDate

object CaseloadGenerator {

    val STAFF1 = StaffGenerator.generateStaff("STCDE01", "Bob", "Smith")
    val STAFF2 = StaffGenerator.generateStaff("STCDE02", "Joe", "Bloggs")

    val TEAM1 = ProviderGenerator.generateTeam("N02BDT")

    val CASELOAD_ROLE_OM_1 = generateCaseload(
        staff = STAFF1,
        team = DEFAULT_TEAM,
        crn = "crn0001",
        firstName = "John",
        secondName = "x",
        surname = "Brown",
        allocationDate = LocalDate.of(2024, 1, 8),
        roleCode = Caseload.CaseloadRole.OFFENDER_MANAGER.value
    )

    val CASELOAD_ROLE_OM_2 = generateCaseload(
        staff = STAFF1,
        team = DEFAULT_TEAM,
        crn = "crn0022",
        firstName = "Jane",
        secondName = "y",
        surname = "Doe",
        allocationDate = LocalDate.of(2024, 1, 9),
        roleCode = Caseload.CaseloadRole.OFFENDER_MANAGER.value
    )

    val CASELOAD_ROLE_OM_3 = generateCaseload(
        staff = STAFF2,
        team = DEFAULT_TEAM,
        crn = "crn0077",
        firstName = "Ano",
        secondName = "no",
        surname = "mys",
        allocationDate = LocalDate.of(2024, 1, 10),
        roleCode = Caseload.CaseloadRole.OFFENDER_MANAGER.value
    )

    val CASELOAD_ROLE_OM_4 = generateCaseload(
        staff = STAFF2,
        team = TEAM1,
        crn = "crn0078",
        firstName = "Joe",
        secondName = "Denis",
        surname = "Doe",
        allocationDate = LocalDate.of(2024, 1, 10),
        roleCode = Caseload.CaseloadRole.OFFENDER_MANAGER.value
    )

    val CASELOAD_ROLE_OS_1 = generateCaseload(
        staff = STAFF2,
        team = DEFAULT_TEAM,
        crn = "crn0088",
        firstName = "Robert",
        secondName = "Alan",
        surname = "Brown",
        roleCode = Caseload.CaseloadRole.ORDER_SUPERVISOR.value
    )

    val MANAGED_OFFENDER = generateManagedOffender(CASELOAD_ROLE_OM_1, STAFF1, ProviderGenerator.DEFAULT_TEAM)

    fun generateCaseload(
        staff: Staff,
        team: Team,
        allocationDate: LocalDate? = LocalDate.now(),
        roleCode: String,
        crn: String,
        firstName: String,
        secondName: String?,
        surname: String,
        startDate: LocalDate? = LocalDate.now(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Caseload(
        staff,
        team,
        allocationDate,
        roleCode,
        crn,
        firstName,
        secondName,
        surname,
        startDate,
        id
    )

    fun generateManagedOffender(
        caseload: Caseload,
        staff: Staff,
        team: Team
    ) = ManagedOffender(
        caseload.crn,
        Name(caseload.firstName, caseload.secondName, caseload.surname),
        caseload.allocationDate,
        staff.asStaff(),
        team.asTeam()
    )
}
