package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import java.time.LocalDate

object MainOffenceGenerator {
    val DEFAULT = generate(event = EventGenerator.DEFAULT, person = PersonGenerator.DEFAULT, offence = OffenceGenerator.DEFAULT)
    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        event: Event,
        person: Person,
        offence: Offence
    ) = MainOffence(
        id = id,
        date = LocalDate.now(),
        count = 1,
        event = event,
        offence = offence,
        person = person
    )
}

object DetailedOffenceGenerator {
    val DEFAULT = generate(code = "AA00000", description = "Murder", homeOfficeCode = "00100")
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

