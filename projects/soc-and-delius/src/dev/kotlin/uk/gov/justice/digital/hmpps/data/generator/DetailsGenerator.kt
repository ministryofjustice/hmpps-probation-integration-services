package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.DetailDistrict
import uk.gov.justice.digital.hmpps.entity.DetailPerson
import uk.gov.justice.digital.hmpps.entity.DetailProbationArea
import uk.gov.justice.digital.hmpps.entity.DetailStaff
import uk.gov.justice.digital.hmpps.entity.PersonManager
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.Team
import java.time.LocalDate

object DetailsGenerator {
    val RELIGION = ReferenceData(
        IdGenerator.getAndIncrement(),
        "Jedi",
        "Jedi"
    )
    val PERSON = DetailPerson(
        IdGenerator.getAndIncrement(),
        "X012773",
        "1231235",
        "1111111111111",
        RELIGION,
        listOf(),
        LocalDate.now().minusYears(18),
        "Smith",
        "Bob",
        "Richard",
        "Clive"
    )

    val DEFAULT_PA = DetailProbationArea(true, "London", "LDN", IdGenerator.getAndIncrement())
    val DISTRICT = DetailDistrict(true, "KK", "Kings Cross", IdGenerator.getAndIncrement())
    val STAFF = DetailStaff("Simon", "Smith", "James", IdGenerator.getAndIncrement())
    val TEAM = Team(IdGenerator.getAndIncrement(), "CODE", DEFAULT_PA, DISTRICT)

    val PERSON_MANAGER = PersonManager(IdGenerator.getAndIncrement(), PERSON, DEFAULT_PA.id, STAFF, TEAM)
}
