package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator
import uk.gov.justice.digital.hmpps.data.generator.asPersonManager
import uk.gov.justice.digital.hmpps.data.generator.asTeam
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository

@Component
class ProbationCaseDataLoader(
    private val probationAreaRepository: ProbationAreaRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val probationCaseRepository: ProbationCaseRepository
) {
    fun loadData() {
        probationAreaRepository.save(ProbationCaseGenerator.COM_PROVIDER)
        teamRepository.save(ProbationCaseGenerator.COM_TEAM.asTeam())
        staffRepository.save(ProbationCaseGenerator.COM_UNALLOCATED)
        probationCaseRepository.save(ProbationCaseGenerator.CASE_COMPLEX)
        probationCaseRepository.save(ProbationCaseGenerator.CASE_SIMPLE)

        personManagerRepository.saveAll(
            listOf(
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_COMPLEX).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_SIMPLE).asPersonManager()
            )
        )
    }
}
