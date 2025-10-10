package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.COURT
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.COURT_APPEARANCE_PLEA
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.COURT_APPEARANCE_TYPE
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.DISPOSAL_TYPE
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.EVENT
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.LENGTH_UNIT_NA
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.OFFENCE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DATASET_TYPE_KEY_DATE
import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate
import java.time.ZonedDateTime

object SentenceGenerator {
    val RELEASE_DATE_TYPE = generateKeyDateType(KeyDate.Type.EXPECTED_RELEASE_DATE.code)
    val RELEASED_EVENT = generateEvent(person = PersonGenerator.WITH_RELEASE_DATE)
    val RELEASED_COURT_APPEARANCE = generateCourtAppearance(RELEASED_EVENT)
    val RELEASED_SENTENCE = generateSentence(RELEASED_EVENT)
    val RELEASED_CUSTODY = generateCustody(RELEASED_SENTENCE)
    val RELEASE_DATE = generateExpectedReleaseDate(RELEASED_CUSTODY, LocalDate.now().plusWeeks(6))

    fun generateSentence(
        event: Event,
        date: LocalDate = LocalDate.now().minusDays(1),
        type: DisposalType = DISPOSAL_TYPE,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(
        id = id,
        event = event,
        type = type,
        date = date,
        length = 6,
        lengthUnits = LENGTH_UNIT_NA
    )

    fun generateEvent(
        person: Person = PersonGenerator.DEFAULT,
        convictionDate: LocalDate = LocalDate.now().minusWeeks(1),
        number: String = "1",
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(
        id = id,
        person = person,
        number = number,
        convictionDate = convictionDate,
        mainOffence = MainOffence(
            id = IdGenerator.getAndIncrement(),
            date = LocalDate.now().minusMonths(1),
            count = 1,
            offence = OFFENCE,
        ),
        additionalOffences = listOf(),
        courtAppearances = listOf(),
    ).apply { mainOffence.set(MainOffence::event, this) }

    fun generateCourtAppearance(
        event: Event,
        court: Court = COURT,
        date: ZonedDateTime = ZonedDateTime.now().minusDays(1),
        type: ReferenceData = COURT_APPEARANCE_TYPE,
        plea: ReferenceData = COURT_APPEARANCE_PLEA,
        id: Long = IdGenerator.getAndIncrement()
    ) = CourtAppearance(id, date, event, court, type, plea)

    fun generateOgrsAssessment(
        date: LocalDate,
        score: Long?,
        event: Event = EVENT,
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
        dataset: Dataset = DATASET_TYPE_KEY_DATE,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(code, description, dataset, id)

    fun generateExpectedReleaseDate(
        custody: Custody,
        date: LocalDate = LocalDate.now(),
        type: ReferenceData = RELEASE_DATE_TYPE,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = KeyDate(custody, type, date, softDeleted, id)
}
