package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationDeliveryUnit
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
    private val probationAreaRepository: ProbationAreaRepository,
    private val probationDeliveryUnitRepository: ProbationDeliveryUnitRepository,
    private val localDeliveryUnitRepository: LocalDeliveryUnitRepository,
    private val teamRepository: TeamRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()

        referenceDataRepository.save(ApprovedPremisesGenerator.DEFAULT.code)
        referenceDataRepository.save(ApprovedPremisesGenerator.NO_STAFF.code)
        referenceDataRepository.save(StaffGenerator.STAFF_GRADE)
        probationAreaRepository.save(ProbationAreaGenerator.DEFAULT)
        probationAreaRepository.save(ProbationAreaGenerator.WITHOUT_PDU)
        probationDeliveryUnitRepository.save(ProbationAreaGenerator.PDU)
        localDeliveryUnitRepository.save(ProbationAreaGenerator.APPROVED_PREMISES_LDU_1)
        localDeliveryUnitRepository.save(ProbationAreaGenerator.APPROVED_PREMISES_LDU_2)
        localDeliveryUnitRepository.save(ProbationAreaGenerator.NON_APPROVED_PREMISES_LDU)
        teamRepository.save(TeamGenerator.APPROVED_PREMISES_TEAM_1)
        teamRepository.save(TeamGenerator.APPROVED_PREMISES_TEAM_2)
        teamRepository.save(TeamGenerator.APPROVED_PREMISES_TEAM_3)
        teamRepository.save(TeamGenerator.NON_APPROVED_PREMISES_TEAM)
        approvedPremisesRepository.save(ApprovedPremisesGenerator.DEFAULT)
        approvedPremisesRepository.save(ApprovedPremisesGenerator.NO_STAFF)

        staffRepository.save(StaffGenerator.generate("Key-worker (team 1)", listOf(TeamGenerator.APPROVED_PREMISES_TEAM_1), listOf(ApprovedPremisesGenerator.DEFAULT)))
        staffRepository.save(StaffGenerator.generate("Key-worker (team 2)", listOf(TeamGenerator.APPROVED_PREMISES_TEAM_2), listOf(ApprovedPremisesGenerator.DEFAULT)))
        staffRepository.save(StaffGenerator.generate("Key-worker (team 3)", listOf(TeamGenerator.APPROVED_PREMISES_TEAM_3), listOf(ApprovedPremisesGenerator.DEFAULT)))
        staffRepository.save(StaffGenerator.generate("Normal AP staff (not key-worker)", listOf(TeamGenerator.APPROVED_PREMISES_TEAM_3), emptyList()))
        staffRepository.save(StaffGenerator.generate("Normal staff (not AP-related, not key-worker)", listOf(TeamGenerator.NON_APPROVED_PREMISES_TEAM), emptyList()))
    }
}

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long>
interface ProbationAreaRepository : JpaRepository<ProbationArea, Long>
interface ProbationDeliveryUnitRepository : JpaRepository<ProbationDeliveryUnit, Long>
interface LocalDeliveryUnitRepository : JpaRepository<LocalDeliveryUnit, Long>
interface TeamRepository : JpaRepository<Team, Long>
