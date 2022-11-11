package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()

        referenceDataRepository.save(ApprovedPremisesGenerator.DEFAULT.code)
        referenceDataRepository.save(ApprovedPremisesGenerator.NO_STAFF.code)
        referenceDataRepository.save(StaffGenerator.STAFF_GRADE)
        approvedPremisesRepository.save(ApprovedPremisesGenerator.DEFAULT)
        approvedPremisesRepository.save(ApprovedPremisesGenerator.NO_STAFF)
        teamRepository.save(TeamGenerator.APPROVED_PREMISES_TEAM)
        teamRepository.save(TeamGenerator.APPROVED_PREMISES_TEAM_WITH_NO_STAFF)
        teamRepository.save(TeamGenerator.NON_APPROVED_PREMISES_TEAM)
        staffRepository.save(StaffGenerator.generate("Key-worker", listOf(TeamGenerator.APPROVED_PREMISES_TEAM), listOf(ApprovedPremisesGenerator.DEFAULT)))
        staffRepository.save(StaffGenerator.generate("Not key-worker", listOf(TeamGenerator.APPROVED_PREMISES_TEAM), emptyList()))
        staffRepository.save(StaffGenerator.generate("Not key-worker and not in AP team", listOf(TeamGenerator.NON_APPROVED_PREMISES_TEAM), emptyList()))
    }
}

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long>
interface TeamRepository : JpaRepository<Team, Long>
