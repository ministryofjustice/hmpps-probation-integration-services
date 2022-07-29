package uk.gov.justice.digital.hmpps.data

import UserGenerator
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.ServiceContext
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.repository.DatasetRepository
import uk.gov.justice.digital.hmpps.data.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.data.repository.StaffTeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffTeam
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val datasetRepository: DatasetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val providerRepository: ProviderRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val staffTeamRepository: StaffTeamRepository,
    private val personAllocationDataLoader: PersonAllocationDataLoader
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()

        datasetRepository.saveAll(
            listOf(
                DatasetGenerator.OM_ALLOCATION_REASON,
                DatasetGenerator.ORDER_ALLOCATION_REASON,
                DatasetGenerator.RM_ALLOCATION_REASON,
                DatasetGenerator.TRANSFER_STATUS
            )
        )

        referenceDataRepository.saveAll(
            listOf(
                ReferenceDataGenerator.INITIAL_OM_ALLOCATION,
                ReferenceDataGenerator.INITIAL_ORDER_ALLOCATION,
                ReferenceDataGenerator.INITIAL_RM_ALLOCATION,
                ReferenceDataGenerator.PENDING_TRANSFER
            )
        )

        contactTypeRepository.saveAll(
            listOf(
                ContactTypeGenerator.OFFENDER_MANAGER_TRANSFER,
                ContactTypeGenerator.ORDER_SUPERVISOR_TRANSFER,
                ContactTypeGenerator.RESPONSIBLE_OFFICER_CHANGE,
                ContactTypeGenerator.SENTENCE_COMPONENT_TRANSFER,
            )
        )

        providerRepository.save(ProviderGenerator.DEFAULT)
        var team = teamRepository.save(TeamGenerator.DEFAULT)
        var staff = staffRepository.save(StaffGenerator.DEFAULT)
        staffTeamRepository.save(StaffTeam(staff.id, team.id))

        team = teamRepository.save(TeamGenerator.generate("N02ABS"))
        staff = staffRepository.save(StaffGenerator.generate("N02ABS1", "Brian", "Jones"))
        staffTeamRepository.save(StaffTeam(staff.id, team.id))

        personAllocationDataLoader.loadData()
    }
}