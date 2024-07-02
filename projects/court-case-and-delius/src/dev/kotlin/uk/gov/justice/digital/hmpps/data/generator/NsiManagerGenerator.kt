package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.BREACH_NSIS
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiManager
import java.time.LocalDate

object NsiManagerGenerator {

    private val NSIS = BREACH_NSIS

    val ACTIVE =
        NsiManager (
            IdGenerator.id(),
            NSIS,
            LocalDate.now()
        )

    val INACTIVE =
        NsiManager (
            IdGenerator.id(),
            NSIS,
            LocalDate.now().minusDays(7),
            LocalDate.now().minusDays(1)
        )

}