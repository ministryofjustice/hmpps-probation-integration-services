package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.Offence
import java.time.LocalDate

object OffenceGenerator {
    val OFFENCE_ONE = generate("OFF1", "Murder - OFF1", "Murder", "Murder of spouse")
    val OFFENCE_TWO = generate("OFF2", "Burglary in a dwelling - OFF2", "Burglary in a dwelling", "Burglary (dwelling) with intent to commit, or the commission of, an offence triable only on indictment")

    fun generate(code: String, description: String, mainCategoryDescription: String, subCategoryDescription: String, id: Long = IdGenerator.getAndIncrement()) =
        Offence(code, description, mainCategoryDescription, subCategoryDescription, id)

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
