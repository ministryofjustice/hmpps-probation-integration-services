package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.Outcome
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import java.time.LocalDate
import java.time.ZonedDateTime

object SentenceGenerator {
    fun generateSentence(
        event: Event,
        startDate: ZonedDateTime,
        disposalType: ReferenceData,
        custody: Custody? = null,
        endDate: ZonedDateTime? = null,
        terminationDate: ZonedDateTime? = null,
        entryLength: Long? = null,
        entryLengthUnits: ReferenceData? = null,
        lengthInDays: Long? = null,
        terminationReason: ReferenceData? = null,
        upw: Boolean = true,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(
        event,
        custody,
        startDate,
        disposalType,
        endDate,
        terminationDate,
        entryLength,
        entryLengthUnits,
        lengthInDays,
        terminationReason,
        upw,
        active,
        softDeleted,
        id
    )

    fun generateEvent(
        person: Person,
        inBreach: Boolean = false,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(person, inBreach, LocalDate.now(), active, softDeleted, id)

    fun generateOrderManager(
        event: Event,
        staff: Staff,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = OrderManager(event, staff, active, softDeleted, id)

    fun generateCourtAppearance(
        event: Event,
        outcome: Outcome,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = CourtAppearance(event, outcome, softDeleted, id)

    fun generateCustody(
        disposal: Disposal,
        custodialStatus: ReferenceData,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Custody(disposal, custodialStatus, softDeleted, id)

    val MAIN_OFFENCE = SentenceGenerator.generateOffence("Main Offence")
    val ADDITIONAL_OFFENCE = SentenceGenerator.generateOffence("Additional Offence")
    fun generateOffence(
        description: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Offence(id, description)

    fun generateMainOffence(
        event: Event,
        offence: Offence,
        date: LocalDate,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = MainOffence(id, event, offence, date, softDeleted)

    fun generateAdditionalOffence(
        event: Event,
        offence: Offence,
        date: LocalDate,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = AdditionalOffence(event, offence, date, softDeleted, id)
}
