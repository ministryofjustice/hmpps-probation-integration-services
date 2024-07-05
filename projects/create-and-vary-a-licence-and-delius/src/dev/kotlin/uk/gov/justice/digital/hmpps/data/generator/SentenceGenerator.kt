package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.LocalDate

object SentenceGenerator {
    val EVENT_CREATE_LC = generateEvent("1", PersonGenerator.PERSON_CREATE_LC)
    val SENTENCE_CREATE_LC = generate(EVENT_CREATE_LC, ReferenceDataGenerator.RELEASED_STATUS)

    fun generateEvent(
        number: String,
        person: Person,
        disposal: Disposal? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(number, person, disposal, active, softDeleted, id)

    fun generate(
        event: Event,
        status: ReferenceData = ReferenceDataGenerator.RELEASED_STATUS,
        keyDates: List<KeyDate> = listOf(),
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
        disposalId: Long = IdGenerator.getAndIncrement(),
        endDate: LocalDate? = null,
        enteredEndDate: LocalDate? = null,
    ) = Custody(Disposal(event, active, softDeleted, endDate, enteredEndDate, disposalId), status, keyDates, softDeleted, id)

    fun generateKeyDate(
        custody: Custody,
        type: ReferenceData,
        date: LocalDate,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = KeyDate(custody, type, date, softDeleted, id)
}
