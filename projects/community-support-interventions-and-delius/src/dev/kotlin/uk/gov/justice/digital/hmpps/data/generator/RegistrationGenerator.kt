package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.RegisterType
import uk.gov.justice.digital.hmpps.entity.Registration

object RegistrationGenerator {
    val HOIE_TYPE = RegisterType(
        code = "HOIE",
        description = "Home Office Interest",
        colour = "Red",
        id = IdGenerator.getAndIncrement()
    )

    val HOME_OFFICE_INTEREST = Registration(
        personId = PersonGenerator.PERSON1.id,
        notes = "Some notes about Home Office Interest",
        type = HOIE_TYPE,
        deRegistered = false,
        softDeleted = false,
        id = IdGenerator.getAndIncrement()
    )
}

