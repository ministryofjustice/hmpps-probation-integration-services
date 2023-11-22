package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.Outcome
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.Nsi
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
    ) = CourtAppearance(event, outcome, DocumentEntityGenerator.COURT.courtId, softDeleted, id)

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

    fun generateRequirement(
        id: Long = IdGenerator.getAndIncrement(),
        disposal: Disposal
    ) = Requirement(
        disposal,
        ReferenceDataGenerator.REQUIREMENT_MAIN_CAT,
        ReferenceDataGenerator.REQUIREMENT_SUB_CAT,
        ReferenceDataGenerator.AD_REQUIREMENT_MAIN_CAT,
        ReferenceDataGenerator.AD_REQUIREMENT_SUB_CAT,
        LocalDate.now(),
        id = id
    )

    fun generateLicenseCondition(
        id: Long = IdGenerator.getAndIncrement(),
        disposal: Disposal
    ) = LicenceCondition(
        disposal,
        LocalDate.now(),
        ReferenceDataGenerator.LIC_COND_MAIN_CAT,
        ReferenceDataGenerator.LIC_COND_SUB_CAT,
        "Licence Condition notes",
        id
    )

    fun generateBreachNsi(disposal: Disposal) = Nsi(
        disposal.event.person.id,
        disposal.event.id,
        ReferenceDataGenerator.NSI_TYPE,
        null,
        ReferenceDataGenerator.NSI_BREACH_OUTCOME,
        LocalDate.now(),
        LocalDate.now(),
        LocalDate.now()
    )
}
