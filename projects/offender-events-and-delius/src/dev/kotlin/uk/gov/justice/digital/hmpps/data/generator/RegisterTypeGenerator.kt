package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegisterType

object RegisterTypeGenerator {

    val MAPPA = RegisterType(
        id = 1L,
        code = RegisterType.Code.MAPPA.value
    )
}