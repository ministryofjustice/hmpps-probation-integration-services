package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import java.time.LocalDate

object MainOffenceGenerator {
    val DEFAULT =
        generate(event = EventGenerator.DEFAULT, person = PersonGenerator.DEFAULT, offence = OffenceGenerator.DEFAULT, detailedOffence = DetailedOffenceGenerator.DEFAULT)

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        event: Event,
        person: Person,
        offence: Offence,
        detailedOffence: DetailedOffence? = null,
    ) = MainOffence(
        id = id,
        date = LocalDate.now(),
        count = 1,
        event = event,
        offence = offence,
        person = person,
        detailedOffence = detailedOffence
    )
}

object DetailedOffenceGenerator {
    val DEFAULT = generate(code = "AA00000", description = "Murder", homeOfficeCode = "00100")
    val SECOND_OFFENCE = generate(code = "TN42001", description = "Second Offence", homeOfficeCode = "00101")
    val THIRD_OFFENCE = generate(code = "ZZ00120", description = "Third Offence", homeOfficeCode = "00200")
    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        homeOfficeCode: String
    ) = DetailedOffence(
        id = id,
        code = code,
        description = description,
        homeOfficeCode = homeOfficeCode,
        startDate = LocalDate.now(),
        endDate = null,
        homeOfficeDescription = "Home Office Description",
        category = null,
        legislation = "Legislation"
    )
}

object OffenceGenerator {
    val DEFAULT = generate(code = "00100", description = "Murder")
    val SECOND_OFFENCE = generate(code = "00101", description = "Second Offence")
    val THIRD_OFFENCE = generate(code = "00200", description = "Third Offence")
    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String
    ) = Offence(
        id = id,
        code = code,
        description = description
    )
}

