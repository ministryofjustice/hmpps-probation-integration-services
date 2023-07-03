package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.LicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Release
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate

object EventGenerator {
    val CASE_SUMMARY = custodialEvent(PersonGenerator.CASE_SUMMARY.id)

    fun custodialEvent(personId: Long): Event {
        val event = Event(
            id = IdGenerator.getAndIncrement(),
            personId = personId,
            number = "3",
            mainOffence = mainOffence(),
            additionalOffences = listOf(additionalOffence())
        )
        val disposal = Disposal(
            id = IdGenerator.getAndIncrement(),
            event = event,
            type = DisposalType(IdGenerator.getAndIncrement(), "Sentence type"),
            startDate = LocalDate.of(2021, 1, 1),
            entryLength = 6,
            entryLengthUnit = ReferenceData(IdGenerator.getAndIncrement(), "M", "Months"),
            secondEntryLength = 2,
            secondEntryLengthUnit = ReferenceData(IdGenerator.getAndIncrement(), "Y", "Years")
        )
        val custody = Custody(
            id = IdGenerator.getAndIncrement(),
            disposal = disposal,
            status = ReferenceData(IdGenerator.getAndIncrement(), "B", "Released on licence")
        )
        custody.set(Custody::sentenceExpiryDate, custody.keyDate("SED", LocalDate.of(2023, 1, 1)))
        custody.set(Custody::licenceExpiryDate, custody.keyDate("LED", LocalDate.of(2024, 1, 1)))
        disposal.set(Disposal::custody, custody)
        disposal.set(Disposal::licenceConditions, listOf(disposal.licenceCondition("TEST", "Freedom of movement")))
        event.set(Event::disposal, disposal)
        event.set(Event::mainOffence, mainOffence(event))
        event.set(Event::additionalOffences, listOf(additionalOffence(event)))
        return event
    }

    fun mainOffence(event: Event? = null) = MainOffence(
        id = IdGenerator.getAndIncrement(),
        event = event,
        date = LocalDate.now().minusYears(1),
        offence = Offence(IdGenerator.getAndIncrement(), "AA", "Offence description")
    )

    fun additionalOffence(event: Event? = null) = AdditionalOffence(
        id = IdGenerator.getAndIncrement(),
        event = event,
        date = LocalDate.now().minusYears(1),
        offence = Offence(IdGenerator.getAndIncrement(), "BB", "Additional offence description")
    )

    fun Custody.keyDate(type: String, date: LocalDate) = setOf(
        KeyDate(
            id = IdGenerator.getAndIncrement(),
            custody = this,
            type = ReferenceData(IdGenerator.getAndIncrement(), type, "Description of $type"),
            date = date
        )
    )

    fun Custody.release(): Release {
        val release = Release(
            id = IdGenerator.getAndIncrement(),
            custodyId = id,
            date = LocalDate.now().minusMonths(6),
            institution = Institution(IdGenerator.getAndIncrement(), "Test institution")
        )
        release.set(
            Release::recall,
            Recall(
                id = IdGenerator.getAndIncrement(),
                release = release,
                date = LocalDate.now().minusMonths(3)
            )
        )
        return release
    }

    fun Disposal.licenceCondition(code: String, description: String) = LicenceCondition(
        id = IdGenerator.getAndIncrement(),
        disposal = this,
        notes = "Test notes",
        mainCategory = LicenceConditionMainCategory(
            id = IdGenerator.getAndIncrement(),
            code = code,
            description = description
        ),
        subCategory = null
    )
}
