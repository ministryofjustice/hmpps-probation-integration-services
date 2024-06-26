package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator.DEFAULT_OUTCOME
import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate

object ConvictionEventGenerator {
    val PERSON = ConvictionEventPerson(
        IdGenerator.getAndIncrement(),
        "X012772",
        "1231234"
    )
    val OFFENCE_MAIN_TYPE = Offence(
        IdGenerator.getAndIncrement(),
        "C01",
        "Murder of a person",
        "Murder"
    )
    val ADDITIONAL_OFFENCE_TYPE = Offence(
        IdGenerator.getAndIncrement(),
        "C02",
        "Stealing a kitten",
        "Theft"
    )
    val DEFAULT_EVENT = ConvictionEventEntity(
        IdGenerator.getAndIncrement(),
        LocalDate.now().minusDays(1),
        LocalDate.now().minusDays(2),
        PERSON
    )
    val INACTIVE_EVENT = ConvictionEventEntity(
        IdGenerator.getAndIncrement(),
        LocalDate.now(),
        LocalDate.now().minusDays(1),
        PERSON,
        active = false
    )
    val MAIN_OFFENCE = MainOffence(
        IdGenerator.getAndIncrement(),
        DEFAULT_EVENT,
        OFFENCE_MAIN_TYPE
    )
    val OTHER_OFFENCE = AdditionalOffence(
        IdGenerator.getAndIncrement(),
        DEFAULT_EVENT,
        ADDITIONAL_OFFENCE_TYPE
    )
    val DISPOSAL_TYPE = DisposalType(
        IdGenerator.getAndIncrement(),
        "Prison"
    )
    val DISPOSAL = Disposal(
        IdGenerator.getAndIncrement(),
        DISPOSAL_TYPE,
        DEFAULT_EVENT,
        LocalDate.now()
    )
    val COURT_APPEARANCE = ConvictionCourtAppearanceEntity(
        IdGenerator.getAndIncrement(),
        DEFAULT_EVENT,
        DEFAULT_OUTCOME,
        LocalDate.now()
    )

    val PERSON_2 = ConvictionEventPerson(
        IdGenerator.getAndIncrement(),
        "X11111",
        "1111111"
    )
    val EVENT_2 = ConvictionEventEntity(
        IdGenerator.getAndIncrement(),
        LocalDate.now(),
        LocalDate.now(),
        PERSON_2
    )
    val MAIN_OFFENCE_2 = MainOffence(
        IdGenerator.getAndIncrement(),
        EVENT_2,
        OFFENCE_MAIN_TYPE
    )
    val OTHER_OFFENCE_2 = AdditionalOffence(
        IdGenerator.getAndIncrement(),
        EVENT_2,
        ADDITIONAL_OFFENCE_TYPE
    )
    val DISPOSAL_2 = Disposal(
        IdGenerator.getAndIncrement(),
        DISPOSAL_TYPE,
        EVENT_2,
        LocalDate.now()
    )
}
