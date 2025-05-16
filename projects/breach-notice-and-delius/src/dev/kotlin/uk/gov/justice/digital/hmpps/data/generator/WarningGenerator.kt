package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DateTimeGenerator.zonedDateTime
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.DEFAULT_RQMNT
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.PSS_REQUIREMENT
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PSS_PERSON
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateContactOutcome
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateContactType
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateDataset
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateReferenceData
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.*
import java.time.ZonedDateTime

object WarningGenerator {
    val DS_BREACH_NOTICE_TYPE = generateDataset(Dataset.BREACH_NOTICE_TYPE)
    val DS_BREACH_REASON = generateDataset(Dataset.BREACH_REASON)
    val DS_BREACH_CONDITION_TYPE = generateDataset(Dataset.BREACH_CONDITION_TYPE)
    val DS_BREACH_SENTENCE_TYPE = generateDataset(Dataset.BREACH_SENTENCE_TYPE)

    val NOTICE_TYPES = listOf(
        generateNoticeType("BR", "Breach Warning"),
        generateNoticeType("FI", "Final Warning"),
        generateNoticeType("NS", selectable = false),
        generateNoticeType("FO", "Formal Warning"),
    )
    val BREACH_REASONS = listOf(
        generateReason("BR01"),
        generateReason("BR02"),
        generateReason("BR03"),
        generateReason("BR04", selectable = false),
    )

    val CONDITION_TYPES = listOf(
        generateCondition("2IN12"),
        generateCondition("3IN12"),
        generateCondition("3TOTAL"),
    )

    val SENTENCE_TYPES = listOf(
        generateSentenceType("CO", linkedCondition = CONDITION_TYPES.first { it.code == "2IN12" }),
        generateSentenceType("PSS", linkedCondition = CONDITION_TYPES.first { it.code == "2IN12" }),
        generateSentenceType("SDO", linkedCondition = CONDITION_TYPES.first { it.code == "2IN12" }),
        generateSentenceType("SSO", linkedCondition = CONDITION_TYPES.first { it.code == "3TOTAL" }),
        generateSentenceType("YO", linkedCondition = CONDITION_TYPES.first { it.code == "3IN12" }),
    )

    val ENFORCEABLE_CONTACT_TYPE = generateContactType("ENCT")
    val ENFORCEABLE_CONTACT_OUTCOME = generateContactOutcome("ENOC", enforceable = true)
    val ENFORCEABLE_CONTACTS = listOf(3, 1, 2).map {
        generateEnforceableContact(
            DEFAULT_PERSON,
            ENFORCEABLE_CONTACT_TYPE,
            dateTime = ZonedDateTime.of(2020, 1, it, 0, 0, 0, 0, EuropeLondon),
            requirement = DEFAULT_RQMNT,
            outcome = ENFORCEABLE_CONTACT_OUTCOME,
            description = "Enforceable Description",
            notes = "Some notes about the enforceable contact",
        )
    }

    val PSS_ENFORCEABLE_CONTACT = generateEnforceableContact(
        PSS_PERSON,
        ENFORCEABLE_CONTACT_TYPE,
        pssRequirement = PSS_REQUIREMENT,
        outcome = ENFORCEABLE_CONTACT_OUTCOME,
        description = "Enforceable Description",
        notes = "Some notes about the enforceable contact",
    )

    fun generateNoticeType(
        code: String,
        description: String = "Description of $code",
        dataset: Dataset = DS_BREACH_NOTICE_TYPE,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = generateReferenceData(dataset, code, description, selectable, setOf(), id)

    fun generateReason(
        code: String,
        description: String = "Description of $code",
        dataset: Dataset = DS_BREACH_REASON,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = generateReferenceData(dataset, code, description, selectable, setOf(), id)

    fun generateCondition(
        code: String,
        description: String = "Description of $code",
        dataset: Dataset = DS_BREACH_CONDITION_TYPE,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = generateReferenceData(dataset, code, description, selectable, setOf(), id)

    fun generateSentenceType(
        code: String,
        description: String = "Description of $code",
        dataset: Dataset = DS_BREACH_SENTENCE_TYPE,
        linkedCondition: ReferenceData,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = generateReferenceData(dataset, code, description, selectable, setOf(linkedCondition), id)

    fun generateEnforceableContact(
        person: Person,
        type: ContactType,
        dateTime: ZonedDateTime = zonedDateTime().minusDays(1),
        requirement: Requirement? = null,
        pssRequirement: PssRequirement? = null,
        staff: Staff = DEFAULT_STAFF,
        location: OfficeLocation? = DEFAULT_LOCATION,
        outcome: ContactOutcome?,
        description: String? = null,
        notes: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Contact(
        person,
        type,
        dateTime.toLocalDate(),
        dateTime,
        requirement?.disposal?.event ?: pssRequirement?.custody?.disposal?.event,
        requirement,
        pssRequirement,
        staff,
        location,
        outcome,
        description,
        notes,
        true,
        softDeleted,
        id
    )
}