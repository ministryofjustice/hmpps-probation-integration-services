package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.person.RegisterType
import uk.gov.justice.digital.hmpps.entity.person.Registration
import java.time.LocalDate

object RegisterTypeGenerator {
    val HIGH_ROSH = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "RHRH",
        description = "High RoSH"
    )

    val NON_ROSH = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "HOIE",
        description = "Home Office Interest"
    )

    val REGISTER_SO = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "ARSO",
        description = "SEX_OFFENCE"
    )
}

object RegistrationGenerator {
    val SEX_OFFENCE_START_DATE: LocalDate = LocalDate.now().minusDays(7)

    val REGISTER_SO = Registration(
        id = IdGenerator.getAndIncrement(),
        type = RegisterTypeGenerator.REGISTER_SO,
        person = PersonGenerator.PERSON_SO,
        category = ReferenceDataGenerator.REGISTER_CATEGORY_M3,
        level = null,
        date = SEX_OFFENCE_START_DATE,
        nextReviewDate = SEX_OFFENCE_START_DATE.plusWeeks(2),
    )

    val REGISTER_SO_WITHOUT_CATEGORY = Registration(
        id = IdGenerator.getAndIncrement(),
        type = RegisterTypeGenerator.REGISTER_SO,
        person = PersonGenerator.PERSON_SO_WITHOUT_CATEGORY,
        category = null,
        level = null,
        date = SEX_OFFENCE_START_DATE,
        nextReviewDate = SEX_OFFENCE_START_DATE.plusWeeks(2),
    )
}
