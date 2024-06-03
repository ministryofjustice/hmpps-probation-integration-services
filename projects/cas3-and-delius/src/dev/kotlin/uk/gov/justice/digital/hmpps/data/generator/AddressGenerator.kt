package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonAddress
import java.time.LocalDate

object AddressGenerator {
    val PERSON_2_ADDRESS = generate(PersonGenerator.PERSON_2_CRN.id, LocalDate.of(2023, 12, 12))

    fun generate(
        personId: Long,
        startDate: LocalDate
    ) = PersonAddress(
        0,
        personId,
        AddressRDGenerator.MAIN_ADDRESS_TYPE,
        AddressRDGenerator.MAIN_ADDRESS_STATUS,
        "A street",
        "A county",
        "A country",
        "a postcode",
        startDate = startDate
    )
}
