package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator.ADD_EVENT_ALLOCATION
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator.ADD_PERSON_ALLOCATION
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator.CREATE_COMPONENT_TRANSFER
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(
    private val personAllocationDataLoader: PersonAllocationDataLoader,
    private val caseViewDataLoader: CaseViewDataLoader,
    private val limitedAccessDataLoader: LimitedAccessDataLoader,
    private val registrationDataLoader: RegistrationDataLoader,
    private val existingAllocationsDataLoader: ExistingAllocationsDataLoader,
    dataManager: DataManager
) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(ADD_PERSON_ALLOCATION, ADD_EVENT_ALLOCATION, CREATE_COMPONENT_TRANSFER)

        saveAll(
            DatasetGenerator.CUSTODY_STATUS,
            DatasetGenerator.IREPORTTYPE,
            DatasetGenerator.THROUGHCARE_DATE_TYPE,
            DatasetGenerator.OM_ALLOCATION_REASON,
            DatasetGenerator.ORDER_ALLOCATION_REASON,
            DatasetGenerator.RM_ALLOCATION_REASON,
            DatasetGenerator.TRANSFER_STATUS,
            DatasetGenerator.OFFICER_GRADE,
            DatasetGenerator.UNITS,
            DatasetGenerator.GENDER,
            DatasetGenerator.REQUIREMENT_SUB_CATEGORY,
            DatasetGenerator.ADDRESS_TYPE,
            DatasetGenerator.ADDRESS_STATUS,
            DatasetGenerator.COURT_APPEARANCE_TYPE,
            DatasetGenerator.REGISTER_TYPE_FLAG
        )

        saveAll(
            ReferenceDataGenerator.CUSTODY_STATUS,
            ReferenceDataGenerator.KEY_DATE_EXP_REL_DATE,
            ReferenceDataGenerator.INITIAL_OM_ALLOCATION,
            ReferenceDataGenerator.INITIAL_ORDER_ALLOCATION,
            ReferenceDataGenerator.INITIAL_RM_ALLOCATION,
            ReferenceDataGenerator.INS_RPT_PAR,
            ReferenceDataGenerator.PENDING_TRANSFER,
            ReferenceDataGenerator.PSQ_GRADE,
            ReferenceDataGenerator.UNIT_MONTHS,
            ReferenceDataGenerator.GENDER_MALE,
            ReferenceDataGenerator.REQUIREMENT_SUB_CATEGORY,
            ReferenceDataGenerator.ADDRESS_TYPE,
            ReferenceDataGenerator.ADDRESS_STATUS_MAIN,
            ReferenceDataGenerator.ADDRESS_STATUS_PREVIOUS,
            ReferenceDataGenerator.SENTENCE_APPEARANCE
        )

        saveAll(
            OffenceGenerator.MAIN_OFFENCE_TYPE,
            OffenceGenerator.ADDITIONAL_OFFENCE_TYPE
        )

        save(RequirementMainCategoryGenerator.DEFAULT)
        save(RequirementAdditionalMainCategoryGenerator.DEFAULT)
        save(RegisterTypeGenerator.DEFAULT)

        saveAll(
            ContactTypeGenerator.OFFENDER_MANAGER_TRANSFER,
            ContactTypeGenerator.ORDER_SUPERVISOR_TRANSFER,
            ContactTypeGenerator.RESPONSIBLE_OFFICER_CHANGE,
            ContactTypeGenerator.SENTENCE_COMPONENT_TRANSFER,
            ContactTypeGenerator.INITIAL_APPOINTMENT_IN_OFFICE,
            ContactTypeGenerator.INITIAL_APPOINTMENT_ON_DOORSTEP,
            ContactTypeGenerator.INITIAL_APPOINTMENT_HOME_VISIT,
            ContactTypeGenerator.INITIAL_APPOINTMENT_BY_VIDEO,
            ContactTypeGenerator.CASE_ALLOCATION_SPO_OVERSIGHT
        )

        save(CourtReportTypeGenerator.DEFAULT)

        save(ProviderGenerator.DEFAULT)
        save(TeamGenerator.DEFAULT)
        saveAll(StaffGenerator.DEFAULT, StaffGenerator.STAFF_FOR_INACTIVE_EVENT)

        save(TeamGenerator.ALLOCATION_TEAM)
        save(StaffGenerator.STAFF_WITH_USER)
        save(StaffGenerator.STAFF_WITH_USER.user!!)
        save(StaffGenerator.INACTIVE_STAFF)
        save(StaffGenerator.INACTIVE_STAFF.user!!)
        save(StaffGenerator.SPO_STAFF)
        save(StaffGenerator.SPO_STAFF.user!!)

        personAllocationDataLoader.loadData()
        caseViewDataLoader.loadData()
        limitedAccessDataLoader.loadData()
        registrationDataLoader.loadData()
        existingAllocationsDataLoader.loadData()
    }
}
