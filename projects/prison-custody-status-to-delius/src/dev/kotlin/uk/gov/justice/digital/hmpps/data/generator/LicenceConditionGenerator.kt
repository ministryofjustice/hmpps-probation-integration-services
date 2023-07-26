package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionCategory

object LicenceConditionGenerator {
    val DEFAULT = generate(EventGenerator.nonCustodialEvent(PersonGenerator.RELEASABLE))

    fun generate(event: Event) = LicenceCondition(
        id = IdGenerator.getAndIncrement(),
        disposal = event.disposal!!,
        mainCategory = LicenceConditionCategory(IdGenerator.getAndIncrement(), "TEST")
    )
}
