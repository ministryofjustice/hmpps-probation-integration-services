package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionCategory
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionManager
import uk.gov.justice.digital.hmpps.set

object LicenceConditionGenerator {
    val DEFAULT = generate(EventGenerator.nonCustodialEvent(PersonGenerator.RELEASABLE, "1"))

    fun generate(event: Event) = LicenceCondition(
        id = IdGenerator.getAndIncrement(),
        disposal = event.disposal!!,
        mainCategory = LicenceConditionCategory(IdGenerator.getAndIncrement(), "TEST", "Test description"),
        subCategory = null
    )
}

fun LicenceCondition.withManager(): LicenceCondition {
    val lcm = LicenceConditionManager(
        IdGenerator.getAndIncrement(),
        this,
        staffId = StaffGenerator.DEFAULT.id,
        teamId = TeamGenerator.DEFAULT.id,
        probationAreaId = ProbationAreaGenerator.DEFAULT.id
    )
    this.set(this::manager, lcm)
    return this
}
