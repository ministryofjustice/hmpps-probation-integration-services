package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Event

object SentenceGenerator {
    val SENTENCE_TYPE_SC = generateSentenceType("SC")
    val EVENT_CREATE_LC = generateEvent("1", PersonGenerator.PERSON_CREATE_LC)
    val SENTENCE_CREATE_LC = generate(EVENT_CREATE_LC)

    fun generateSentenceType(sentenceType: String, id: Long = IdGenerator.getAndIncrement()) =
        DisposalType(sentenceType, id)

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
        type: DisposalType = SENTENCE_TYPE_SC,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(event, type, active, softDeleted, id)
}
