package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*

object ProviderGenerator {
    val DEFAULT_INSTITUTION = generateNomisInstitution(code = "LDN")
    val INSTITUTION_NO_TEAM = generateNomisInstitution(code = "MDL")

    val DEFAULT_AREA = generateProbationArea()
    val AREA_NO_TEAM = generateProbationArea(code = "MDL", institution = INSTITUTION_NO_TEAM)
    val DEFAULT_TEAM = generateTeam("N03DEF")
    val CSN_TEAM = generateTeam("LDNCSN")
    var DEFAULT_STAFF = generateStaff("N03DEF1", "John", "Smith", "James", probationAreaId = DEFAULT_AREA.id)
    var EXISTING_CSN_STAFF = generateStaff("LDNA001", "Terry", "Nutkins", "James", probationAreaId = DEFAULT_AREA.id)

    fun generateProbationArea(
        id: Long = IdGenerator.getAndIncrement(),
        code: String = "LDN",
        description: String = "London",
        institution: Institution? = DEFAULT_INSTITUTION
    ) = ProbationArea(id, code, description, institution)

    fun generateTeam(
        code: String,
        description: String = "Team $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(id, code, description)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        middleName: String? = null,
        id: Long = IdGenerator.getAndIncrement(),
        probationAreaId: Long
    ) = Staff(code, forename, surname, middleName, null, probationAreaId, id)

    fun generateStaffUser(
        username: String,
        staff: Staff,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(username, staff, id)

    fun generateNomisInstitution(
        id: Long = IdGenerator.getAndIncrement(),
        code: String
    ) = Institution(id, code)
}
