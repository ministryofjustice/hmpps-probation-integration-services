package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Disposal
import uk.gov.justice.digital.hmpps.entity.DisposalType
import java.time.LocalDate

object DisposalGenerator {
    val DISPOSAL = Disposal(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PERSON1,
        event = EventGenerator.EVENT,
        disposalType = DisposalTypeGenerator.DISPOSAL_TYPE,
        enteredNotionalEndDate = LocalDate.now().plusYears(1),
        notionalEndDate = LocalDate.now().plusMonths(11),
        active = true,
        softDeleted = false
    )

    val NOMS_DISPOSAL = Disposal(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PERSON2,
        event = EventGenerator.EVENT2,
        disposalType = DisposalTypeGenerator.DISPOSAL_TYPE,
        enteredNotionalEndDate = LocalDate.now().plusYears(1),
        notionalEndDate = LocalDate.now().plusMonths(11),
        active = true,
        softDeleted = false
    )
}


object DisposalTypeGenerator {
    val DISPOSAL_TYPE = DisposalType(
        id = IdGenerator.getAndIncrement(),
        code = "CJA",
        description = "Community Justice Order"
    )
}