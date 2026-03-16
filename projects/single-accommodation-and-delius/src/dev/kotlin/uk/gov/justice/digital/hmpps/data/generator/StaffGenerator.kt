package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.Team
import java.time.LocalDate

object StaffGenerator {
    val DEFAULT = Staff(
        id = IdGenerator.getAndIncrement(),
        code = "N00A001",
        forename = "Probation",
        middleName = "PO",
        surname = "Officer",
        user = null,
        teams = listOf(TeamGenerator.DEFAULT)
    )
}

object TeamGenerator {
    val DEFAULT = Team(
        id = IdGenerator.getAndIncrement(),
        code = "N00T01",
        description = "Team 1",
        startDate = LocalDate.of(2000, 1, 1),
        endDate = null
    )
}

object ProviderGenerator {
    val DEFAULT = Provider(
        id = IdGenerator.getAndIncrement(),
        code = "N00",
        description = "NPS"
    )
}
