package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.asPerson
import uk.gov.justice.digital.hmpps.data.generator.asPersonManager
import uk.gov.justice.digital.hmpps.data.generator.asTeam
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import java.time.LocalDate

@Component
class ProbationCaseDataLoader(
    private val probationAreaRepository: ProbationAreaRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val probationCaseRepository: ProbationCaseRepository,
    private val registrationRepository: RegistrationRepository
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

        registrationRepository.save(
            PersonGenerator.generateRegistration(
                ProbationCaseGenerator.CASE_COMPLEX.asPerson(),
                ReferenceDataGenerator.REGISTER_TYPES[RegisterType.Code.MAPPA.value]!!,
                LocalDate.now().minusDays(7),
                ReferenceDataGenerator.REGISTER_CATEGORIES["M3"],
                ReferenceDataGenerator.REGISTER_LEVELS["M2"]
            )
        )

        registrationRepository.save(
            PersonGenerator.generateRegistration(
                ProbationCaseGenerator.CASE_COMPLEX.asPerson(),
                ReferenceDataGenerator.REGISTER_TYPES[RegisterType.Code.SEX_OFFENCE.value]!!,
                LocalDate.now().minusDays(7)
            )
        )
    }
}
