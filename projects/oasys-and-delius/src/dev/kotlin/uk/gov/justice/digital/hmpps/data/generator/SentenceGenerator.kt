package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.person.Person
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Institution
import uk.gov.justice.digital.hmpps.integration.delius.reference.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integration.delius.sentence.*
import java.time.ZonedDateTime

object SentenceGenerator {
    val INSTITUTION_TYPE = ReferenceDataGenerator.generate("INST1")
    val DEFAULT_INSTITUTION = generateInstitution("HMPDEF", nomisCdeCode = "DEF")
    val CUSTODY_STATUS = ReferenceDataGenerator.generate("C")
    val RELEASE_TYPE = ReferenceDataGenerator.generate("REL")
    val RECALL_REASON = generateRecallReason("REC")
    val CUSTODIAL_SENTENCE = generateCustodialSentence(PersonGenerator.CUSTODY_PERSON)
    val RELEASED_SENTENCE = generateCustodialSentence(PersonGenerator.RELEASED_PERSON)
    val RELEASE =
        generateRelease(RELEASED_SENTENCE, date = ZonedDateTime.now().minusDays(1), institution = DEFAULT_INSTITUTION)
    val RECALL = generateRecall(RELEASE)

    fun generateCustodialSentence(
        person: Person,
        status: ReferenceData = CUSTODY_STATUS,
        institution: Institution? = DEFAULT_INSTITUTION,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Custody(
        generateDisposal(generateEvent(person)),
        status,
        institution,
        emptyList(),
        softDeleted,
        id
    )

    fun generateDisposal(
        event: Event,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(event, active, softDeleted, id)

    fun generateEvent(
        person: Person,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(person, null, active, softDeleted, id)

    fun generateInstitution(
        code: String,
        description: String = "Description of $code",
        type: ReferenceData? = INSTITUTION_TYPE,
        name: String? = "Name of $code",
        nomisCdeCode: String? = "NOM$code",
        establishment: Boolean = true,
        private: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Institution(code, description, type, name, nomisCdeCode, establishment, private, id)

    fun generateRelease(
        custody: Custody,
        type: ReferenceData = RELEASE_TYPE,
        date: ZonedDateTime = ZonedDateTime.now(),
        institution: Institution? = null,
        notes: String? = null,
        softDeleted: Boolean = false,
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Release(custody, type, date, institution, notes, null, softDeleted, createdDateTime, id)

    fun generateRecall(
        release: Release,
        date: ZonedDateTime = ZonedDateTime.now(),
        reason: RecallReason = RECALL_REASON,
        notes: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Recall(release, date, reason, notes, softDeleted, id)

    fun generateRecallReason(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = RecallReason(code, description, id)
}