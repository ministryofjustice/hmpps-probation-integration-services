package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateContactOutcome
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateContactType
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateDataset
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateReferenceData
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateRequirementMainCategory
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.set
import java.time.ZonedDateTime

object WarningGenerator {
    val DS_BREACH_NOTICE_TYPE = ReferenceDataGenerator.generateDataset(Dataset.BREACH_NOTICE_TYPE)
    val DS_BREACH_REASON = ReferenceDataGenerator.generateDataset(Dataset.BREACH_REASON)
    val DS_BREACH_CONDITION_TYPE = ReferenceDataGenerator.generateDataset(Dataset.BREACH_CONDITION_TYPE)
    val DS_BREACH_SENTENCE_TYPE = ReferenceDataGenerator.generateDataset(Dataset.BREACH_SENTENCE_TYPE)

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

    val DEFAULT_EVENT = generateEvent(DEFAULT_PERSON, "1")
    val DEFAULT_DISPOSAL = generateDisposal(DEFAULT_EVENT)

    val DEFAULT_RQMNT_CATEGORY = generateRequirementMainCategory("DRMC")
    val DS_REQUIREMENT_SUB_CATEOGORY = generateDataset(Dataset.REQUIREMENT_SUB_CATEGORY)
    val DEFAULT_RQMNT_SUB_CATEGORY = generateReferenceData(DS_REQUIREMENT_SUB_CATEOGORY, "DRSC")
    val DEFAULT_RQMNT = generateRequirement(DEFAULT_DISPOSAL, DEFAULT_RQMNT_CATEGORY, DEFAULT_RQMNT_SUB_CATEGORY)

    val ENFORCEABLE_CONTACT_TYPE = generateContactType("ENCT")
    val ENFORCEABLE_CONTACT_OUTCOME = generateContactOutcome("ENOC", enforceable = true)
    val DEFAULT_ENFORCEABLE_CONTACT = generateEnforceableContact(
        DEFAULT_PERSON,
        ENFORCEABLE_CONTACT_TYPE,
        requirement = DEFAULT_RQMNT,
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
        dateTime: ZonedDateTime = ZonedDateTime.now().minusDays(1),
        requirement: Requirement,
        team: Team = DEFAULT_TEAM,
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
        requirement.disposal.event,
        requirement,
        team,
        staff,
        location,
        outcome,
        description,
        notes,
        softDeleted,
        id
    )

    fun generateEvent(
        person: Person,
        eventNumber: String,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Event(person, eventNumber, null, active, softDeleted, id)

    fun generateDisposal(
        event: Event,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Disposal(event, active, softDeleted, id).also { event.set(Event::disposal, it) }

    fun generateRequirement(
        disposal: Disposal,
        mainCategory: RequirementMainCategory,
        subCategory: ReferenceData?,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Requirement(disposal, mainCategory, subCategory, active, softDeleted, id)
}