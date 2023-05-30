package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.epf.entity.Court
import uk.gov.justice.digital.hmpps.epf.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.epf.entity.Disposal
import uk.gov.justice.digital.hmpps.epf.entity.Event
import uk.gov.justice.digital.hmpps.epf.entity.Person
import java.time.LocalDate

object SentenceGenerator {
    val DEFAULT_EVENT = generateEvent()
    val DEFAULT_COURT = generateCourt()
    val DEFAULT_COURT_APPEARANCE = generateCourtAppearance()
    val DEFAULT_SENTENCE = generateSentence()

    fun generateSentence(
        event: Event = DEFAULT_EVENT,
        date: LocalDate = LocalDate.now().minusYears(1),
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(id, date, event)

    fun generateEvent(
        person: Person = PersonGenerator.DEFAULT,
        number: String = "1",
        firstReleaseDate: LocalDate? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(id, number, person, firstReleaseDate)

    fun generateCourtAppearance(
        event: Event = DEFAULT_EVENT,
        court: Court = DEFAULT_COURT,
        date: LocalDate = LocalDate.now().minusDays(1),
        id: Long = IdGenerator.getAndIncrement()
    ) = CourtAppearance(date, id, event, court)

    fun generateCourt(
        id: Long = IdGenerator.getAndIncrement(),
        name: String = "CourtName"
    ) = Court(id, name)
}
