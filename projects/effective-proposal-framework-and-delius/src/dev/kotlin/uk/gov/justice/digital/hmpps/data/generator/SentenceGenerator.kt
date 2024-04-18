package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.epf.entity.*
import java.time.LocalDate

object SentenceGenerator {
    val DEFAULT_COURT = generateCourt()
    val RELEASE_DATE_TYPE = generateKeyDateType(KeyDate.Type.EXPECTED_RELEASE_DATE.code)

    val DEFAULT_EVENT = generateEvent()
    val DEFAULT_COURT_APPEARANCE = generateCourtAppearance(DEFAULT_EVENT)
    val DEFAULT_SENTENCE = generateSentence(DEFAULT_EVENT)

    val RELEASED_EVENT = generateEvent(person = PersonGenerator.WITH_RELEASE_DATE)
    val RELEASED_COURT_APPEARANCE = generateCourtAppearance(RELEASED_EVENT)
    val RELEASED_SENTENCE = generateSentence(RELEASED_EVENT)
    val RELEASED_CUSTODY = generateCustody(RELEASED_SENTENCE)
    val RELEASE_DATE = generateExpectedReleaseDate(RELEASED_CUSTODY, LocalDate.now().plusWeeks(6))

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
    ) = Custody(disposal, softDeleted, id)

    fun generateKeyDateType(
        code: String,
        description: String = "Key Date Type of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, description)

    fun generateExpectedReleaseDate(
        custody: Custody,
        date: LocalDate = LocalDate.now(),
        type: ReferenceData = RELEASE_DATE_TYPE,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = KeyDate(custody, type, date, softDeleted, id)
}
