package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.epf.entity.*
import java.time.LocalDate
import java.time.ZonedDateTime

object SentenceGenerator {
    val DEFAULT_COURT = generateCourt()

    val DEFAULT_EVENT = generateEvent()
    val DEFAULT_COURT_APPEARANCE = generateCourtAppearance(DEFAULT_EVENT)
    val DEFAULT_SENTENCE = generateSentence(DEFAULT_EVENT)

    val RELEASED_EVENT = generateEvent(person = PersonGenerator.RELEASED)
    val RELEASED_COURT_APPEARANCE = generateCourtAppearance(RELEASED_EVENT)
    val RELEASED_SENTENCE = generateSentence(RELEASED_EVENT)
    val RELEASED_CUSTODY = generateCustody(RELEASED_SENTENCE)
    val RELEASE = generateRelease(RELEASED_CUSTODY)

    fun generateSentence(
        event: Event,
        date: LocalDate = LocalDate.now().minusYears(1),
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(id, date, event)

    fun generateEvent(
        person: Person = PersonGenerator.DEFAULT,
        convictionDate: LocalDate = LocalDate.now(),
        number: String = "1",
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(id, number, person, convictionDate)

    fun generateCourtAppearance(
        event: Event,
        court: Court = DEFAULT_COURT,
        date: LocalDate = LocalDate.now().minusDays(1),
        id: Long = IdGenerator.getAndIncrement()
    ) = CourtAppearance(date, id, event, court)

    fun generateCourt(
        id: Long = IdGenerator.getAndIncrement(),
        name: String = "CourtName"
    ) = Court(id, name)

    fun generateOgrsAssessment(
        date: LocalDate,
        score: Long?,
        event: Event = DEFAULT_EVENT,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = OgrsAssessment(event.id, date, score, softDeleted, id)

    fun generateCustody(
        disposal: Disposal,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Custody(disposal, listOf(), softDeleted, id)

    fun generateRelease(
        custody: Custody,
        date: LocalDate = LocalDate.now(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Release(custody, date, createdDateTime, id)
}
