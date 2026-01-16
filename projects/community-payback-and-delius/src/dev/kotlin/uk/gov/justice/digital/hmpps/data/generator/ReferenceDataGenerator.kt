package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.*

object ReferenceDataGenerator {
    val GROUP_PLACEMENT_PROJECT_TYPE = generateReferenceData(
        code = "G",
        description = "Group Placement",
        datasetId = DatasetGenerator.UPW_PROJECT_TYPE_DATASET.id
    )
    val INDIVIDUAL_PLACEMENT_PROJECT_TYPE = generateReferenceData(
        code = "I",
        description = "Individual Placement",
        datasetId = DatasetGenerator.UPW_PROJECT_TYPE_DATASET.id
    )
    val INACTIVE_PROJECT_TYPE = generateReferenceData(
        code = "NA",
        description = "Individual Placement",
        datasetId = DatasetGenerator.UPW_PROJECT_TYPE_DATASET.id,
        selectable = false
    )
    val EXCELLENT_WORK_QUALITY = generateReferenceData(
        code = "EX",
        description = "Excellent",
        datasetId = DatasetGenerator.UPW_WORK_QUALITY_DATASET.id,
        selectable = true
    )
    val UNSATISFACTORY_WORK_QUALITY = generateReferenceData(
        code = "US",
        description = "Unsatisfactory",
        datasetId = DatasetGenerator.UPW_WORK_QUALITY_DATASET.id,
        selectable = true
    )
    val EXCELLENT_BEHAVIOUR = generateReferenceData(
        code = "EX",
        description = "Excellent",
        datasetId = DatasetGenerator.UPW_BEHAVIOUR_DATASET.id,
        selectable = true
    )
    val UNSATISFACTORY_BEHAVIOUR = generateReferenceData(
        code = "UN",
        description = "Unsatisfactory",
        datasetId = DatasetGenerator.UPW_BEHAVIOUR_DATASET.id,
        selectable = true
    )
    val NON_WORKING_DAY_CHRISTMAS = generateReferenceData(
        code = "251225",
        description = "Christmas Day",
        datasetId = DatasetGenerator.NON_WORKING_DAYS_DATASET.id,
        selectable = true
    )
    val NON_WORKING_DAY_NEW_YEAR = generateReferenceData(
        code = "010126",
        description = "New Year",
        datasetId = DatasetGenerator.NON_WORKING_DAYS_DATASET.id,
        selectable = true
    )
    val UPW_FREQUENCY_WEEKLY = generateReferenceData(
        code = "WE",
        description = "Weekly",
        datasetId = DatasetGenerator.UPW_FREQUENCY_DATASET.id
    )

    val UPW_APPOINTMENT_TYPE = generateContactType(ContactType.Code.UNPAID_WORK_APPOINTMENT.value)
    val REVIEW_ENFORCEMENT_STATUS_TYPE = generateContactType(ContactType.Code.REVIEW_ENFORCEMENT_STATUS.value)

    val ROM_ENFORCEMENT_ACTION = generateEnforcementAction(
        code = EnforcementAction.REFER_TO_PERSON_MANAGER,
        description = "Refer to Offender Manager",
        responseByPeriod = 7L,
        outstandingContactAction = true,
        contactType = UPW_APPOINTMENT_TYPE
    )

    val ATTENDED_COMPLIED_CONTACT_OUTCOME = generateContactOutcome(
        code = "A",
        description = "Attended - Complied",
        attended = true,
        complied = true,
        enforceable = false,
    )
    val FAILED_TO_ATTEND_CONTACT_OUTCOME = generateContactOutcome(
        code = "F",
        description = "Failed to Attend",
        attended = false,
        complied = false,
        enforceable = true,
    )

    val UPW_RQMNT_MAIN_CATEGORY = generateRequirementMainCategory(
        code = "W",
        description = "Unpaid Work"
    )

    val DEFAULT_DISPOSAL_TYPE = generateDisposalType(
        code = "100",
        description = "Community Order",
        preCja2003 = false,
        ftcLimit = 0
    )

    val UPW_DAY_MONDAY = generateUpwDay(
        weekDay = "MONDAY"
    )

    fun generateReferenceData(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        datasetId: Long,
        selectable: Boolean = true
    ) = ReferenceData(id, code, description, datasetId, selectable)

    fun generateEnforcementAction(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        responseByPeriod: Long,
        outstandingContactAction: Boolean,
        contactType: ContactType
    ) = EnforcementAction(id, code, description, responseByPeriod, outstandingContactAction, contactType)

    fun generateContactType(
        code: String,
        id: Long = IdGenerator.getAndIncrement(),
        nationalStandards: Boolean = true
    ) = ContactType(
        code,
        id,
        nationalStandards = nationalStandards
    )

    fun generateContactOutcome(
        code: String,
        description: String,
        attended: Boolean?,
        complied: Boolean?,
        enforceable: Boolean?,
        id: Long = IdGenerator.getAndIncrement(),
    ) = ContactOutcome(code, description, attended, complied, enforceable, id)

    fun generateRequirementMainCategory(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
    ) = RequirementMainCategory(id, code, description)

    fun generateDisposalType(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        ftcLimit: Long? = 3,
        preCja2003: Boolean = false
    ) = DisposalType(id, code, description, ftcLimit, preCja2003)

    fun generateUpwDay(
        id: Long = IdGenerator.getAndIncrement(),
        weekDay: String
    ) = UpwDay(id, weekDay)
}

object DatasetGenerator {
    val UPW_PROJECT_TYPE_DATASET = generateDataset(code = Dataset.UPW_PROJECT_TYPE)
    val UPW_WORK_QUALITY_DATASET = generateDataset(code = Dataset.UPW_WORK_QUALITY)
    val UPW_BEHAVIOUR_DATASET = generateDataset(code = Dataset.UPW_BEHAVIOUR)
    val NON_WORKING_DAYS_DATASET = generateDataset(code = Dataset.NON_WORKING_DAYS)
    val UPW_FREQUENCY_DATASET = generateDataset(code = Dataset.UPW_FREQUENCY)

    fun generateDataset(
        id: Long = IdGenerator.getAndIncrement(),
        code: String
    ) = Dataset(id, code)
}