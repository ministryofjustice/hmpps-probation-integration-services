package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.DetailedOffence
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.ReferenceDataSet
import java.time.LocalDate

object DataGenerator {
    val COURT_CATEGORY_SET = ReferenceDataSet(IdGenerator.getAndIncrement(), "COURT CATEGORY")
    val COURT_CATEGORY = ReferenceData(IdGenerator.getAndIncrement(), "CS", "Summary Non-motoring", COURT_CATEGORY_SET)
    val EXISTING_OFFENCE = DetailedOffence(
        id = IdGenerator.getAndIncrement(),
        code = "AB06001",
        description = "Obstruct person acting in execution of the regulations - 09155",
        category = COURT_CATEGORY,
        homeOfficeCode = "091/55",
        homeOfficeDescription = "Obstruct person acting in execution of the regulations",
        legislation = "N/A",
        startDate = LocalDate.of(2006, 5, 12),
        endDate = null
    )
}
