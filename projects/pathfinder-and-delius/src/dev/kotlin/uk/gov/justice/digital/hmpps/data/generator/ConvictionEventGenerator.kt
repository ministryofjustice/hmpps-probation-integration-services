package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.entity.ConvictionEventEntity
import uk.gov.justice.digital.hmpps.entity.ConvictionEventPerson
import uk.gov.justice.digital.hmpps.entity.Disposal
import uk.gov.justice.digital.hmpps.entity.DisposalType
import uk.gov.justice.digital.hmpps.entity.MainOffence
import uk.gov.justice.digital.hmpps.entity.Offence
import java.time.LocalDate
object ConvictionEventGenerator {
    val PERSON = ConvictionEventPerson(
        IdGenerator.getAndIncrement(),
        "X012772",
        "1231234"
    )
    val OFFENCE_MAIN = Offence(
        IdGenerator.getAndIncrement(),
        "C01",
        "Murder"
    )
    val OFFENCE_OTHER = Offence(
        IdGenerator.getAndIncrement(),
        "C02",
        "Stealing a kitten"
    )
    val DEFAULT_EVENT = ConvictionEventEntity(
        IdGenerator.getAndIncrement(),
        LocalDate.now(),
        PERSON
    )
    val MAIN_OFFENCE = MainOffence(
        IdGenerator.getAndIncrement(),
        DEFAULT_EVENT,
        OFFENCE_MAIN
    )
    val OTHER_OFFENCE = AdditionalOffence(
        IdGenerator.getAndIncrement(),
        DEFAULT_EVENT,
        OFFENCE_OTHER
    )
    val DISPOSAL_TYPE = DisposalType(
        IdGenerator.getAndIncrement(),
        "Prison"
    )
    val DISPOSAL = Disposal(
        IdGenerator.getAndIncrement(),
        DISPOSAL_TYPE,
        DEFAULT_EVENT
    )
}
