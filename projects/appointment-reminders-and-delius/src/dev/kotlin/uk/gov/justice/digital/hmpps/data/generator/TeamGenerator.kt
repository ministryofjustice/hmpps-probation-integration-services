package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.LONDON
import uk.gov.justice.digital.hmpps.entity.LocalAdminUnit
import uk.gov.justice.digital.hmpps.entity.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.entity.Team

object TeamGenerator {
    val DEFAULT = Team(
        id = id(),
        localAdminUnit = LocalAdminUnit(
            id = id(),
            probationDeliveryUnit = ProbationDeliveryUnit(
                id = id(),
                description = "Croydon",
                provider = LONDON
            )
        )
    )
}
