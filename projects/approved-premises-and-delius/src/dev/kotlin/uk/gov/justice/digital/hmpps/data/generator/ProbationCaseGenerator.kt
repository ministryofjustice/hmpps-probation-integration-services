package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator.COM_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator.COM_TEAM
import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator.COM_UNALLOCATED
import uk.gov.justice.digital.hmpps.integrations.delius.person.CommunityManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.CommunityManagerTeam
import uk.gov.justice.digital.hmpps.integrations.delius.person.Ldu
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCase
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.time.LocalDate

object ProbationCaseGenerator {
    val COM_PROVIDER = ProbationAreaGenerator.generate("N01")
    val COM_LDU = generateLdu("N01LDU", "COM LDU")
    val COM_TEAM = generateComTeam("N01COM", "Community Manager Team", COM_LDU)
    val COM_UNALLOCATED = StaffGenerator.generate("Unallocated", "N01COMU")
    val CASE_COMPLEX = generate(
        "C246139",
        "James",
        "Brown",
        LocalDate.of(1979, 3, 12),
        "John",
        "Jack",
        "A5671YZ",
        ReferenceDataGenerator.GENDER_MALE,
        ReferenceDataGenerator.ETHNICITY_WHITE,
        ReferenceDataGenerator.NATIONALITY_BRITISH,
        ReferenceDataGenerator.RELIGION_OTHER,
        ReferenceDataGenerator.GENDER_IDENTITY_PNS,
        currentExclusion = true,
        currentRestriction = true
    )
    val CASE_SIMPLE = generate("S517283", "Teresa", "Green", LocalDate.of(1987, 8, 2))

    fun generate(
        crn: String,
        forename: String,
        surname: String,
        dateOfBirth: LocalDate,
        secondName: String? = null,
        thirdName: String? = null,
        nomsId: String? = null,
        gender: ReferenceData? = null,
        ethnicity: ReferenceData? = null,
        nationality: ReferenceData? = null,
        religion: ReferenceData? = null,
        genderIdentity: ReferenceData? = null,
        currentExclusion: Boolean = false,
        currentRestriction: Boolean = false,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = ProbationCase(
        crn,
        nomsId,
        forename,
        secondName,
        thirdName,
        surname,
        dateOfBirth,
        gender,
        ethnicity,
        nationality,
        religion,
        genderIdentity,
        currentExclusion,
        currentRestriction,
        listOf(),
        softDeleted,
        id
    )

    fun generateLdu(code: String, description: String = "LDU of $code", id: Long = IdGenerator.getAndIncrement()) =
        Ldu(code, description, id)

    fun generateComTeam(code: String, description: String, ldu: Ldu, id: Long = IdGenerator.getAndIncrement()) =
        CommunityManagerTeam(code, description, ldu, id)

    fun generateManager(
        pc: ProbationCase,
        team: CommunityManagerTeam = COM_TEAM,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = CommunityManager(pc, team, active, softDeleted, id)
}

fun ProbationCase.asPerson() = PersonGenerator.generate(crn, id)
fun CommunityManagerTeam.asTeam() = Team(id, code, description, COM_PROVIDER, null, ldu.id)
fun CommunityManager.asPersonManager() =
    PersonManagerGenerator.generate(person.asPerson(), COM_TEAM.asTeam(), COM_UNALLOCATED, id)
