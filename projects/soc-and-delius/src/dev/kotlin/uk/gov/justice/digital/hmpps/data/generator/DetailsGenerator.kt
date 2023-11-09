package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.DetailDistrict
import uk.gov.justice.digital.hmpps.entity.DetailPerson
import uk.gov.justice.digital.hmpps.entity.DetailProbationArea
import uk.gov.justice.digital.hmpps.entity.DetailRelease
import uk.gov.justice.digital.hmpps.entity.DetailStaff
import uk.gov.justice.digital.hmpps.entity.Institution
import uk.gov.justice.digital.hmpps.entity.PersonManager
import uk.gov.justice.digital.hmpps.entity.Recall
import uk.gov.justice.digital.hmpps.entity.RecallReason
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

    val INSTITUTION = Institution(IdGenerator.getAndIncrement(), "HMP-LDN")

    val RELEASE_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "RSN1", "Release reason")

    val RELEASE = DetailRelease(
        IdGenerator.getAndIncrement(),
        KeyDateGenerator.CUSTODY.id,
        INSTITUTION,
        null,
        releaseType = RELEASE_TYPE,
        LocalDate.now()
    )

    val RECALL_REASON = RecallReason(IdGenerator.getAndIncrement(), "REC1", "Recall reason")

    val RECALL = Recall(
        IdGenerator.getAndIncrement(),
        RELEASE,
        LocalDate.now(),
        RECALL_REASON
    )

    val DEFAULT_PA = DetailProbationArea(true, "London", "LDN", IdGenerator.getAndIncrement())
    val DISTRICT = DetailDistrict(true, "KK", "Kings Cross", IdGenerator.getAndIncrement())
    val STAFF = DetailStaff("Simon", "Smith", "James", IdGenerator.getAndIncrement())
    val TEAM = Team(IdGenerator.getAndIncrement(), "CODE", DEFAULT_PA, DISTRICT)

    val PERSON_MANAGER = PersonManager(IdGenerator.getAndIncrement(), PERSON, DEFAULT_PA.id, STAFF, TEAM)
}
