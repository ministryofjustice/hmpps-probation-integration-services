package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ManagerHistory
import uk.gov.justice.digital.hmpps.entity.ManagerHistoryPerson
import uk.gov.justice.digital.hmpps.entity.ManagerHistoryProbationArea
import java.time.LocalDate

object ManagerGenerator {
    val PERSON = ManagerHistoryPerson(IdGenerator.getAndIncrement(), "MH00001")
    val PERSON_2 = ManagerHistoryPerson(IdGenerator.getAndIncrement(), "MH00002")
    val PROBATION_AREA_1 = ManagerHistoryProbationArea(IdGenerator.getAndIncrement(), "M01", "Area 1")
    val PROBATION_AREA_2 = ManagerHistoryProbationArea(IdGenerator.getAndIncrement(), "M02", "Area 2")
    val PROBATION_AREA_3 = ManagerHistoryProbationArea(IdGenerator.getAndIncrement(), "M03", "Area 3")
    val PERSON_MANAGERS = listOf(
        ManagerHistory(
            IdGenerator.getAndIncrement(),
            PERSON,
            PROBATION_AREA_1,
            LocalDate.of(2000, 1, 1),
            LocalDate.of(2001, 1, 1)
        ),
        ManagerHistory(
            IdGenerator.getAndIncrement(),
            PERSON,
            PROBATION_AREA_2,
            LocalDate.of(2001, 1, 1),
            LocalDate.of(2002, 1, 1)
        ),
        ManagerHistory(IdGenerator.getAndIncrement(), PERSON, PROBATION_AREA_3, LocalDate.of(2002, 1, 1)),
        ManagerHistory(IdGenerator.getAndIncrement(), PERSON_2, PROBATION_AREA_1, LocalDate.of(2025, 5, 15)),
    )
}
