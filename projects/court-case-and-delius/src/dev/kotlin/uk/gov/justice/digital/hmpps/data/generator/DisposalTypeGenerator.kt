package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.DisposalType

object DisposalTypeGenerator {

    val CURFEW_ORDER =
        DisposalType(IdGenerator.getAndIncrement(), "Curfew Order", "SP", cja2003Order = false, legacyOrder = true, 2)
}