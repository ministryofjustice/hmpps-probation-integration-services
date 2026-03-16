package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.Registration

object RegisterTypeGenerator {
    val HIGH_ROSH = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "RHRH",
        description = "High RoSH"
    )
}

object RegistrationGenerator {
    val DEFAULT = Registration(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.DEFAULT.id,
        type = RegisterTypeGenerator.HIGH_ROSH
    )
}
