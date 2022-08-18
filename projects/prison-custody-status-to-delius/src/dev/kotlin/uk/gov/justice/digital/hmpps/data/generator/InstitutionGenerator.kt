package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.institution.Institution
import uk.gov.justice.digital.hmpps.listener.prisonId

object InstitutionGenerator {
    val DEFAULT = Institution(
        id = IdGenerator.getAndIncrement(),
        code = "TEST",
        nomisCdeCode = MessageGenerator.PRISONER_RELEASED.additionalInformation.prisonId(),
        establishment = true,
        selectable = true
    )
}
