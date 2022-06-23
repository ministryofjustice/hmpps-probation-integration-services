package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteNomisTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.repository.CaseNoteTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val userRepository: UserRepository,
    private val caseNoteTypeRepository: CaseNoteTypeRepository,
    private val caseNoteNomisTypeRepository: CaseNoteNomisTypeRepository,
    private val probationAreaRepository: ProbationAreaRepository,
    private val institutionRepository: InstitutionRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val offenderRepository: OffenderRepository,
    private val caseNoteRepository: CaseNoteRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        caseNoteTypeRepository.save(CaseNoteNomisTypeGenerator.DEFAULT.type)
        caseNoteNomisTypeRepository.save(CaseNoteNomisTypeGenerator.DEFAULT)
        institutionRepository.save(ProbationAreaGenerator.DEFAULT.institution!!)
        probationAreaRepository.save(ProbationAreaGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)
        staffRepository.save(StaffGenerator.DEFAULT)
        offenderRepository.save(OffenderGenerator.DEFAULT)
        caseNoteRepository.save(CaseNoteGenerator.EXISTING)
    }
}