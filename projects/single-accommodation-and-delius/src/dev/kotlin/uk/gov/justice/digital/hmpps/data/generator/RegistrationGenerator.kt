package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.RegisterType

object RegisterTypeGenerator {
    val HIGH_ROSH = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "RHRH",
        description = "High RoSH"
    )
}

