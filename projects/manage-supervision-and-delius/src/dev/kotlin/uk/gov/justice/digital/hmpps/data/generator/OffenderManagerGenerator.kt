package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import java.time.LocalDate

object OffenderManagerGenerator {

    val OFFENDER_MANAGER_ACTIVE =
        OffenderManager(IdGenerator.getAndIncrement(), PersonGenerator.OVERVIEW, null, 1234, null)
    val OFFENDER_MANAGER_INACTIVE =
        OffenderManager(IdGenerator.getAndIncrement(), PersonGenerator.OVERVIEW, null, 1234, LocalDate.now())
}