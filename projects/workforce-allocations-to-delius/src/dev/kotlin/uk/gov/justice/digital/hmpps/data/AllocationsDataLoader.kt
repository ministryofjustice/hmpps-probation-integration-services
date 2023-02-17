package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator.ADD_EVENT_ALLOCATION
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator.ADD_PERSON_ALLOCATION
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator.CREATE_COMPONENT_TRANSFER
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtReportTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegisterTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementMainCategoryGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.repository.CourtReportTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.DatasetRepository
import uk.gov.justice.digital.hmpps.data.repository.OffenceRepository
import uk.gov.justice.digital.hmpps.data.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.data.repository.RegisterTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.RequirementMainCategoryRepository
import uk.gov.justice.digital.hmpps.data.repository.StaffUserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamRepository
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class AllocationsDataLoader(
    private val userRepository: UserRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val datasetRepository: DatasetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val offenceRepository: OffenceRepository,
    private val requirementMainCategoryRepository: RequirementMainCategoryRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val courtReportTypeRepository: CourtReportTypeRepository,
    private val providerRepository: ProviderRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val staffUserRepository: StaffUserRepository,
    private val personAllocationDataLoader: PersonAllocationDataLoader,
    private val caseViewDataLoader: CaseViewDataLoader,
    private val registerTypeRepository: RegisterTypeRepository

) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
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
                DatasetGenerator.ADDRESS_STATUS
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
                ReferenceDataGenerator.ADDRESS_STATUS_PREVIOUS
            )
        )

        offenceRepository.saveAll(
            listOf(
                OffenceGenerator.MAIN_OFFENCE_TYPE,
                OffenceGenerator.ADDITIONAL_OFFENCE_TYPE
            )
        )

        requirementMainCategoryRepository.save(RequirementMainCategoryGenerator.DEFAULT)
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
                ContactTypeGenerator.INITIAL_APPOINTMENT_BY_VIDEO
            )
        )

        courtReportTypeRepository.save(CourtReportTypeGenerator.DEFAULT)

        providerRepository.save(ProviderGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)
        staffRepository.saveAll(listOf(StaffGenerator.DEFAULT, StaffGenerator.STAFF_FOR_INACTIVE_EVENT))

        teamRepository.save(TeamGenerator.ALLOCATION_TEAM)
        staffRepository.save(StaffGenerator.STAFF_WITH_USER)
        staffUserRepository.save(StaffGenerator.STAFF_WITH_USER.user!!)

        personAllocationDataLoader.loadData()
        caseViewDataLoader.loadData()
    }
}
