package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Team
import java.time.LocalDate

object OffenderManagerGenerator {

    val TEAM = Team("N07T02", "OMU B", IdGenerator.getAndIncrement())

    val OFFENDER_MANAGER_ACTIVE =
        OffenderManager(
            IdGenerator.getAndIncrement(),
            PersonGenerator.OVERVIEW,
            ContactGenerator.DEFAULT_PROVIDER,
            TEAM,
            ContactGenerator.STAFF_1,
            null
        )
    val OFFENDER_MANAGER_INACTIVE =
        OffenderManager(
            IdGenerator.getAndIncrement(),
            PersonGenerator.OVERVIEW,
            ContactGenerator.DEFAULT_PROVIDER,
            TEAM,
            ContactGenerator.STAFF_1,
            LocalDate.now()
        )
}