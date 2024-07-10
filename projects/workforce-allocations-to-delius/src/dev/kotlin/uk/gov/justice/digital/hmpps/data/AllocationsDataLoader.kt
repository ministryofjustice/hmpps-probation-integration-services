package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator.ADD_EVENT_ALLOCATION
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator.ADD_PERSON_ALLOCATION
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator.CREATE_COMPONENT_TRANSFER
import uk.gov.justice.digital.hmpps.data.repository.*
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamRepository
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class AllocationsDataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val datasetRepository: DatasetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val offenceRepository: OffenceRepository,
    private val requirementMainCategoryRepository: RequirementMainCategoryRepository,
    private val requirementAdditionalMainCategoryRepository: RequirementAdditionalMainCategoryRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val courtReportTypeRepository: CourtReportTypeRepository,
    private val providerRepository: ProviderRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val staffUserRepository: StaffUserRepository,
    private val personAllocationDataLoader: PersonAllocationDataLoader,
    private val caseViewDataLoader: CaseViewDataLoader,
    private val registerTypeRepository: RegisterTypeRepository,
    private val limitedAccessDataLoader: LimitedAccessDataLoader,
    private val registrationDataLoader: RegistrationDataLoader,
    private val existingAllocationsDataLoader: ExistingAllocationsDataLoader,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        businessInteractionRepository.saveAll(
            listOf(ADD_PERSON_ALLOCATION, ADD_EVENT_ALLOCATION, CREATE_COMPONENT_TRANSFER)
        )

        datasetRepository.saveAll(
            listOf(
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
        )

        referenceDataRepository.saveAll(
            listOf(
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
        )

        offenceRepository.saveAll(
            listOf(
                OffenceGenerator.MAIN_OFFENCE_TYPE,
                OffenceGenerator.ADDITIONAL_OFFENCE_TYPE
            )
        )

        requirementMainCategoryRepository.save(RequirementMainCategoryGenerator.DEFAULT)
        requirementAdditionalMainCategoryRepository.save(RequirementAdditionalMainCategoryGenerator.DEFAULT)
        registerTypeRepository.save(RegisterTypeGenerator.DEFAULT)

        contactTypeRepository.saveAll(
            listOf(
                ContactTypeGenerator.OFFENDER_MANAGER_TRANSFER,
                ContactTypeGenerator.ORDER_SUPERVISOR_TRANSFER,
                ContactTypeGenerator.RESPONSIBLE_OFFICER_CHANGE,
                ContactTypeGenerator.SENTENCE_COMPONENT_TRANSFER,
                ContactTypeGenerator.INITIAL_APPOINTMENT_IN_OFFICE,
                ContactTypeGenerator.INITIAL_APPOINTMENT_ON_DOORSTEP,
                ContactTypeGenerator.INITIAL_APPOINTMENT_HOME_VISIT,
                ContactTypeGenerator.INITIAL_APPOINTMENT_BY_VIDEO,
                ContactTypeGenerator.CASE_ALLOCATION_DECISION_EVIDENCE,
                ContactTypeGenerator.CASE_ALLOCATION_SPO_OVERSIGHT
            )
        )

        courtReportTypeRepository.save(CourtReportTypeGenerator.DEFAULT)

        providerRepository.save(ProviderGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)
        staffRepository.saveAll(listOf(StaffGenerator.DEFAULT, StaffGenerator.STAFF_FOR_INACTIVE_EVENT))

        teamRepository.save(TeamGenerator.ALLOCATION_TEAM)
        staffRepository.save(StaffGenerator.STAFF_WITH_USER)
        staffUserRepository.save(StaffGenerator.STAFF_WITH_USER.user!!)
        staffRepository.save(StaffGenerator.INACTIVE_STAFF)
        staffUserRepository.save(StaffGenerator.INACTIVE_STAFF.user!!)
        staffRepository.save(StaffGenerator.SPO_STAFF)
        staffUserRepository.save(StaffGenerator.SPO_STAFF.user!!)

        personAllocationDataLoader.loadData()
        caseViewDataLoader.loadData()
        limitedAccessDataLoader.loadData()
        registrationDataLoader.loadData()
        existingAllocationsDataLoader.loadData()
    }
}
