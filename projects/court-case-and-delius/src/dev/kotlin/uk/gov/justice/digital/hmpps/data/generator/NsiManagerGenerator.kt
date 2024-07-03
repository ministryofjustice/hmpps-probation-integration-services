package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.BREACH_NSIS
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiManager
import java.time.LocalDate

object NsiManagerGenerator {

    val ACTIVE =
        NsiManager (
            IdGenerator.id(),
            BREACH_NSIS,
            LocalDate.now(),
            endDate = null,
            StaffGenerator.ALLOCATED,
            TeamGenerator.DEFAULT,
            probationArea = CourtGenerator.PROBATION_AREA
        )

    val INACTIVE =
        NsiManager (
            IdGenerator.id(),
            BREACH_NSIS,
            LocalDate.now().minusDays(7),
            LocalDate.now().minusDays(1),
            StaffGenerator.ALLOCATED,
            TeamGenerator.DEFAULT,
            CourtGenerator.PROBATION_AREA
        )

}