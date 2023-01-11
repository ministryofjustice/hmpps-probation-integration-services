package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.Offence

object OffenceGenerator {
    val MAIN_OFFENCE_TYPE = generateOffence("A main offence", "Main Offence Category", "Main Offence Sub Category")
    val ADDITIONAL_OFFENCE_TYPE = generateOffence("An additional offence")

    fun generateOffence(
        description: String,
        mainCategory: String = "Offence Main Category",
        subCategory: String = "Offence Sub Category",
        id: Long = IdGenerator.getAndIncrement()
    ): Offence =
        Offence(id, description, mainCategory, subCategory)

    fun generateMainOffence(
        offence: Offence = MAIN_OFFENCE_TYPE,
        event: Event = EventGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ): MainOffence = MainOffence(id, offence, event)

    fun generateAdditionalOffence(
        offence: Offence = ADDITIONAL_OFFENCE_TYPE,
        event: Event = EventGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ): AdditionalOffence = AdditionalOffence(id, offence, event)
}
