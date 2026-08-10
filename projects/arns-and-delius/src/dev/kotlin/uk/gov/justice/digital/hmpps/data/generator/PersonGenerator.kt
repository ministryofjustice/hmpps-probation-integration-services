package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Event
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT_PERSON = generate()
    val NO_EVENTS_PERSON = generate(crn = "B123456")

    fun generate(
        crn: String = "A123456",
        dateOfBirth: LocalDate = LocalDate.of(1984, 5, 12),
        gender: ReferenceData = ReferenceDataGenerator.MALE,
        events: List<Event> = emptyList(),
        id: Long = IdGenerator.getAndIncrement(),
    ) = Person(
        id = id,
        crn = crn,
        dateOfBirth = dateOfBirth,
        gender = gender,
        events = events.toMutableList(),
    )

    fun generatePersonWithOffences(): Person {
        val basePerson = generate()
        val eventOne = EventGenerator.generate(
            person = basePerson,
            convictionDate = LocalDate.of(2026, 1, 10),
        )
        val eventTwo = EventGenerator.generate(
            person = basePerson,
            convictionDate = LocalDate.of(2026, 2, 20),
        )
        val mainOffenceOne = MainOffenceGenerator.generate(
            event = eventOne,
            offence = OffenceGenerator.DEFAULT,
        )
        val mainOffenceTwo = MainOffenceGenerator.generate(
            event = eventTwo,
            offence = OffenceGenerator.SECOND_OFFENCE,
        )
        val disposalOne = DisposalGenerator.generate(
            event = eventOne,
            startDate = LocalDate.of(2026, 3, 1),
        )
        val disposalTwo = DisposalGenerator.generate(
            event = eventTwo,
            startDate = LocalDate.of(2026, 4, 15),
        )

        return generate(
            id = basePerson.id,
            crn = basePerson.crn,
            dateOfBirth = basePerson.dateOfBirth,
            gender = basePerson.gender,
            events = mutableListOf(
                Event(
                    id = eventOne.id,
                    person = basePerson,
                    mainOffence = mainOffenceOne,
                    disposal = disposalOne,
                    convictionDate = eventOne.convictionDate,
                ),
                Event(
                    id = eventTwo.id,
                    person = basePerson,
                    mainOffence = mainOffenceTwo,
                    disposal = disposalTwo,
                    convictionDate = eventTwo.convictionDate,
                ),
            ),
        )
    }
}