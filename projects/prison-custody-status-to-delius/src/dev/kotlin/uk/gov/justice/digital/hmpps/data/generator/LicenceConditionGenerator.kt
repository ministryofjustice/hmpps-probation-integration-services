package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.category.LicenceConditionCategory

object LicenceConditionGenerator {
    val DEFAULT = generate(EventGenerator.nonCustodialEvent(PersonGenerator.RELEASABLE))

    fun generate(event: Event) = LicenceCondition(
        id = IdGenerator.getAndIncrement(),
        disposal = event.disposal!!,
        mainCategory = LicenceConditionCategory(IdGenerator.getAndIncrement(), "TEST")
    )
}
