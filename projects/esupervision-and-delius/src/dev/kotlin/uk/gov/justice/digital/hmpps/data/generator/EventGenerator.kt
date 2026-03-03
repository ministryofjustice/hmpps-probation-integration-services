package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.event.EventEntity
import uk.gov.justice.digital.hmpps.entity.event.offence.MainOffence
import uk.gov.justice.digital.hmpps.entity.event.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.event.sentence.DisposalType
import java.time.LocalDate

object EventGenerator {
    val COMMUNITY_ORDER = DisposalType(id(), "ORA Community Order (24 Months)")
    val CUSTODY = DisposalType(id(), "ORA Adult Custody (inc PSS)")

    val EVENT_1 = generateEvent(
        number = 1,
        referralDate = LocalDate.of(2025, 12, 1),
        disposal = Disposal(id(), LocalDate.of(2025, 12, 1), CUSTODY),
        mainOffence = MainOffence(id(), OffenceGenerator.BURGLARY),
        active = false,
    )
    val EVENT_2 = generateEvent(
        number = 2,
        referralDate = LocalDate.of(2026, 3, 1),
        disposal = Disposal(id(), LocalDate.of(2026, 3, 1), COMMUNITY_ORDER),
        mainOffence = MainOffence(id(), OffenceGenerator.BURGLARY)
    )
    val EVENT_3 = generateEvent(
        number = 3,
        referralDate = LocalDate.of(2026, 3, 1),
        disposal = null,
        mainOffence = MainOffence(id(), OffenceGenerator.BURGLARY)
    )

    fun generateEvent(
        number: Int,
        person: Person = DEFAULT_PERSON,
        referralDate: LocalDate,
        disposal: Disposal?,
        mainOffence: MainOffence,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = EventEntity(id, number.toString(), person, referralDate, disposal, mainOffence, active, softDeleted)
}
