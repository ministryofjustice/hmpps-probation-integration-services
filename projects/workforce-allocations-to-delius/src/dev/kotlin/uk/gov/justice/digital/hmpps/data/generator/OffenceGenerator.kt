package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewAdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewMainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.Offence

object OffenceGenerator {
    val MAIN_OFFENCE_TYPE = generateOffence("A main offence")
    val ADDITIONAL_OFFENCE_TYPE = generateOffence("An additional offence")

    val CASE_VIEW_MAIN_OFFENCE_TYPE =
        caseViewOffence("Case View Main Offence", "Main Offence Category", "Main Offence Sub Category")
    val CASE_VIEW_ADDITIONAL_OFFENCE_TYPE = caseViewOffence("Case View Additional Offence")
    val CASE_VIEW_MAIN_OFFENCE = forCaseViewMainOffence()
    val CASE_VIEW_ADDITIONAL_OFFENCE = forCaseViewAdditionalOffence()

    fun generateOffence(
        description: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Offence(id, description)

    fun caseViewOffence(
        description: String,
        mainCategory: String = "Offence Main Category",
        subCategory: String = "Offence Sub Category",
        id: Long = IdGenerator.getAndIncrement()
    ) = CaseViewOffence(id, description, mainCategory, subCategory)

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

    fun forCaseViewMainOffence(
        offence: CaseViewOffence = CASE_VIEW_MAIN_OFFENCE_TYPE,
        eventId: Long = EventGenerator.CASE_VIEW.id,
        id: Long = IdGenerator.getAndIncrement()
    ) = CaseViewMainOffence(id, eventId, offence)

    fun forCaseViewAdditionalOffence(
        offence: CaseViewOffence = CASE_VIEW_ADDITIONAL_OFFENCE_TYPE,
        eventId: Long = EventGenerator.CASE_VIEW.id,
        id: Long = IdGenerator.getAndIncrement()
    ) = CaseViewAdditionalOffence(id, eventId, offence)
}
