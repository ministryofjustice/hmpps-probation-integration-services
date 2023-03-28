package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Release
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate
import java.time.ZonedDateTime

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
            entryLength = 6,
            entryLengthUnit = ReferenceData(IdGenerator.getAndIncrement(), "M", "Months")
        )
        val custody = Custody(
            id = IdGenerator.getAndIncrement(),
            disposal = disposal,
            status = ReferenceData(IdGenerator.getAndIncrement(), "B", "Released on licence")
        )
        custody.set(Custody::sentenceExpiryDate, custody.keyDate("SED", LocalDate.of(2023, 1, 1)))
        custody.set(Custody::licenceExpiryDate, custody.keyDate("LED", LocalDate.of(2024, 1, 1)))
        disposal.set(Disposal::custody, custody)
        event.set(Event::disposal, disposal)
        event.set(Event::mainOffence, mainOffence(event))
        event.set(Event::additionalOffences, listOf(additionalOffence(event)))
        return event
    }

    fun mainOffence(event: Event? = null) = MainOffence(
        id = IdGenerator.getAndIncrement(),
        event = event,
        offence = Offence(IdGenerator.getAndIncrement(), "Offence description")
    )

    fun additionalOffence(event: Event? = null) = AdditionalOffence(
        id = IdGenerator.getAndIncrement(),
        event = event,
        offence = Offence(IdGenerator.getAndIncrement(), "Additional offence description")
    )

    fun Custody.keyDate(type: String, date: LocalDate) = KeyDate(
        id = IdGenerator.getAndIncrement(),
        custody = this,
        type = ReferenceData(IdGenerator.getAndIncrement(), type, "Description of $type"),
        date = date
    )

    fun Custody.release(): Release {
        val release = Release(
            id = IdGenerator.getAndIncrement(),
            custodyId = id,
            date = ZonedDateTime.now().minusMonths(6)
        )
        release.set(
            Release::recall,
            Recall(
                id = IdGenerator.getAndIncrement(),
                release = release,
                date = ZonedDateTime.now().minusMonths(3)
            )
        )
        return release
    }
}
