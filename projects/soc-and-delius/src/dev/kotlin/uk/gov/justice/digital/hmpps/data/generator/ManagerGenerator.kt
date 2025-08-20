package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator.generateLau
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator.generatePdu
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator.generateProbationArea
import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate

object ManagerGenerator {
    val PERSON = ManagerHistoryPerson(IdGenerator.getAndIncrement(), "MH00001")
    val PERSON_2 = ManagerHistoryPerson(IdGenerator.getAndIncrement(), "MH00002")
    val PROBATION_AREA_1 = generateProbationArea("M01", "Area 1")
    val PROBATION_AREA_2 = generateProbationArea("M02", "Area 2")
    val PROBATION_AREA_3 = generateProbationArea("M03", "Area 3")
    val PDU_1 = generatePdu("PD1", area = PROBATION_AREA_1)
    val PDU_2 = generatePdu("PD2", area = PROBATION_AREA_2)
    val PDU_3 = generatePdu("PD3", area = PROBATION_AREA_3)
    val LAU_1 = generateLau("LA1", pdu = PDU_1)
    val LAU_2 = generateLau("LA2", pdu = PDU_2)
    val LAU_3 = generateLau("LA3", pdu = PDU_3)
    val TEAM_1 = generateTeam("M01A", district = LAU_1)
    val TEAM_2 = generateTeam("M02A", district = LAU_2)
    val TEAM_3 = generateTeam("M03A", district = LAU_3)

    val PERSON_MANAGERS = listOf(
        ManagerHistory(
            IdGenerator.getAndIncrement(),
            PERSON,
            TEAM_1,
            LocalDate.of(2000, 1, 1),
            LocalDate.of(2001, 1, 1)
        ),
        ManagerHistory(
            IdGenerator.getAndIncrement(),
            PERSON,
            TEAM_1,
            LocalDate.of(2000, 1, 1),
            LocalDate.of(2002, 1, 1)
        ),
        ManagerHistory(
            IdGenerator.getAndIncrement(),
            PERSON,
            TEAM_1,
            LocalDate.of(2005, 1, 1),
            LocalDate.of(2007, 1, 1)
        ),
        ManagerHistory(
            IdGenerator.getAndIncrement(),
            PERSON,
            TEAM_2,
            LocalDate.of(2002, 1, 1),
            LocalDate.of(2003, 1, 1)
        ),
        ManagerHistory(
            IdGenerator.getAndIncrement(),
            PERSON,
            TEAM_2,
            LocalDate.of(2001, 1, 1),
            LocalDate.of(2002, 6, 1)
        ),
        ManagerHistory(
            IdGenerator.getAndIncrement(),
            PERSON,
            TEAM_3,
            LocalDate.of(2002, 1, 1),
            LocalDate.of(2002, 1, 1)
        ),
        ManagerHistory(IdGenerator.getAndIncrement(), PERSON, TEAM_3, LocalDate.of(2002, 1, 1)),
        ManagerHistory(IdGenerator.getAndIncrement(), PERSON_2, TEAM_1, LocalDate.of(2025, 5, 15)),
    )

    fun generateTeam(
        code: String,
        description: String = "Team $code",
        district: District,
        id: Long = IdGenerator.getAndIncrement()
    ) = ManagerHistoryTeam(code, description, district, district.borough.probationArea.id, id)
}
