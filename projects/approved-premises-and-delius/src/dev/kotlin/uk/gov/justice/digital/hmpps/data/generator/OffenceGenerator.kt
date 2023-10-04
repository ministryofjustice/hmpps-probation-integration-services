package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.Offence
import java.time.LocalDate

object OffenceGenerator {
    val OFFENCE_ONE = generate("OFF1", "Offence One")
    val OFFENCE_TWO = generate("OFF2", "Offence Two")

    fun generate(code: String, description: String, id: Long = IdGenerator.getAndIncrement()) =
        Offence(code, description, id)

    fun generateMainOffence(
        event: Event,
        offence: Offence,
        date: LocalDate,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = MainOffence(event, offence, date, softDeleted, id)

    fun generateAdditionalOffence(
        event: Event,
        offence: Offence,
        date: LocalDate,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = AdditionalOffence(event, offence, date, softDeleted, id)
}
