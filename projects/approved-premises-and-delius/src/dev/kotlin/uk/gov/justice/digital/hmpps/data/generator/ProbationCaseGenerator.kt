package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator.COM_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator.COM_TEAM
import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator.COM_UNALLOCATED
import uk.gov.justice.digital.hmpps.integrations.delius.person.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.time.LocalDate

object ProbationCaseGenerator {
    val COM_PROVIDER = ProbationAreaGenerator.generate(code = "N01", description = "N01 Description")
    val BOROUGH = generateBorough("B1", "Borough 1")

    val COM_LDU = generateLdu("N01LDU", "COM LDU")
    val COM_TEAM = generateComTeam("N01COM", "Community Manager Team", COM_LDU)
    val COM_UNALLOCATED = StaffGenerator.generate("Unallocated", "N01COMU")
    val CASE_COMPLEX =
        generate(
            "C246139",
            "James",
            "Brown",
            LocalDate.of(1979, 3, 12),
            "John",
            "Jack",
            "A5671YZ",
            null,
            ReferenceDataGenerator.GENDER_MALE,
            ReferenceDataGenerator.ETHNICITY_WHITE,
            ReferenceDataGenerator.NATIONALITY_BRITISH,
            ReferenceDataGenerator.RELIGION_OTHER,
            ReferenceDataGenerator.GENDER_IDENTITY_PNS,
            currentExclusion = true,
            currentRestriction = true,
        )
    val CASE_SIMPLE = generate("S517283", "Teresa", "Green", LocalDate.of(1987, 8, 2))
    val CASE_X320741 = generate(
        crn = "X320741",
        forename = "Aadland",
        surname = "Bertrand",
        dateOfBirth = LocalDate.of(1987, 8, 2),
        nomsId = "A1234AI",
        gender = ReferenceDataGenerator.GENDER_MALE,
        ethnicity = ReferenceDataGenerator.ETHNICITY_WHITE,
        nationality = ReferenceDataGenerator.NATIONALITY_BRITISH,
        religion = ReferenceDataGenerator.RELIGION_OTHER,
        genderIdentity = ReferenceDataGenerator.GENDER_IDENTITY_PNS,
    )
    val CASE_X320811 = generate(
        crn = "X320811",
        forename = "E2E Person",
        surname = "AdHoc Bookings",
        dateOfBirth = LocalDate.of(1987, 8, 2),
        nomsId = "A1234AI",
        gender = ReferenceDataGenerator.GENDER_MALE,
        ethnicity = ReferenceDataGenerator.ETHNICITY_WHITE,
        nationality = ReferenceDataGenerator.NATIONALITY_BRITISH,
        religion = ReferenceDataGenerator.RELIGION_OTHER,
        genderIdentity = ReferenceDataGenerator.GENDER_IDENTITY_PNS,
    )
    val CASE_LAO_EXCLUSION =
        generate(
            crn = "X400000",
            forename = "Elliot Exclusion",
            surname = "Erickson",
            dateOfBirth = LocalDate.of(1979, 4, 11),
            nomsId = "A1235AI",
            gender = ReferenceDataGenerator.GENDER_MALE,
            ethnicity = ReferenceDataGenerator.ETHNICITY_WHITE,
            nationality = ReferenceDataGenerator.NATIONALITY_BRITISH,
            religion = ReferenceDataGenerator.RELIGION_OTHER,
            genderIdentity = ReferenceDataGenerator.GENDER_IDENTITY_PNS,
            currentExclusion = true,
            currentRestriction = false,
        )
    val CASE_LAO_RESTRICTED =
        generate(
            crn = "X400001",
            forename = "Reginald Restricted",
            surname = "Robinson",
            dateOfBirth = LocalDate.of(1981, 1, 1),
            nomsId = "A1236AI",
            gender = ReferenceDataGenerator.GENDER_MALE,
            ethnicity = ReferenceDataGenerator.ETHNICITY_WHITE,
            nationality = ReferenceDataGenerator.NATIONALITY_BRITISH,
            religion = ReferenceDataGenerator.RELIGION_OTHER,
            genderIdentity = ReferenceDataGenerator.GENDER_IDENTITY_PNS,
            currentExclusion = false,
            currentRestriction = true,
        )

    fun generate(
        crn: String,
        forename: String,
        surname: String,
        dateOfBirth: LocalDate,
        secondName: String? = null,
        thirdName: String? = null,
        nomsId: String? = null,
        pnc: String? = null,
        gender: ReferenceData? = null,
        ethnicity: ReferenceData? = null,
        nationality: ReferenceData? = null,
        religion: ReferenceData? = null,
        genderIdentity: ReferenceData? = null,
        genderIdentityDescription: String? = null,
        currentExclusion: Boolean = false,
        currentRestriction: Boolean = false,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = ProbationCase(
        crn,
        nomsId,
        pnc,
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
        genderIdentityDescription,
        currentExclusion,
        currentRestriction,
        listOf(),
        softDeleted,
        id,
    )

    fun generateBorough(code: String, description: String) = Borough(IdGenerator.getAndIncrement(), code, description)
    fun generateLdu(code: String, description: String = "LDU of $code", id: Long = IdGenerator.getAndIncrement()) =
        Ldu(code, description, BOROUGH, id)

    fun generateComTeam(code: String, description: String, ldu: Ldu, id: Long = IdGenerator.getAndIncrement()) =
        CommunityManagerTeam(code, description, ldu, LocalDate.now(), null, id)

    fun generateManager(
        pc: ProbationCase,
        team: CommunityManagerTeam = COM_TEAM,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = CommunityManager(pc, team, active, softDeleted, id)
}

fun ProbationCase.asPerson() = PersonGenerator.generate(crn, id)
fun CommunityManagerTeam.asTeam() = Team(id, code, description, COM_PROVIDER, null, ldu)
fun CommunityManager.asPersonManager() =
    PersonManagerGenerator.generate(person.asPerson(), COM_TEAM.asTeam(), COM_UNALLOCATED, id)
