package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import java.time.LocalDate

object MainOffenceGenerator {
    val DEFAULT =
        generate(event = EventGenerator.DEFAULT, person = PersonGenerator.DEFAULT, offence = OffenceGenerator.DEFAULT)

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
    val DEFAULT = generate(code = "TN42001", description = "62 Treason Acts 1351-1842", homeOfficeCode = "06200")
    val OTHER = generate(code = "ZZ00120", description = "91.2 Other Public Health offences", homeOfficeCode = "09166")
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
    val DEFAULT = generate(code = "06200", description = "High treason and other offences against Treason Acts")
    val OTHER = generate(
        code = "09166",
        description = "Triable either way offences under The Food Hygiene (Wales) Regulations 2006"
    )

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

