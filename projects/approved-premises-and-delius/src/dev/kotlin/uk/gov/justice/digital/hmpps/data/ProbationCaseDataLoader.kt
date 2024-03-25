package uk.gov.justice.digital.hmpps.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.PersonalCircumstanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.PersonalCircumstanceSubType
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.PersonalCircumstanceType
import uk.gov.justice.digital.hmpps.integrations.delius.person.Ldu
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.MainOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import java.time.LocalDate

@Component
class ProbationCaseDataLoader(
    private val probationAreaRepository: ProbationAreaRepository,
    private val lduRepository: LduRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val probationCaseRepository: ProbationCaseRepository,
    private val registrationRepository: RegistrationRepository,
    private val eventRepository: EventRepository,
    private val offenceRepository: OffenceRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val additionalOffenceRepository: AdditionalOffenceRepository,
    private val personalCircumstanceTypeRepository: PersonalCircumstanceTypeRepository,
    private val personalCircumstanceSubTypeRepository: PersonalCircumstanceSubTypeRepository,
    private val personalCircumstanceRepository: PersonalCircumstanceRepository
) {
    fun loadData() {
        offenceRepository.saveAll(listOf(OffenceGenerator.OFFENCE_ONE, OffenceGenerator.OFFENCE_TWO))
        probationAreaRepository.save(ProbationCaseGenerator.COM_PROVIDER)
        lduRepository.save(ProbationCaseGenerator.COM_LDU)
        teamRepository.save(ProbationCaseGenerator.COM_TEAM.asTeam())
        staffRepository.save(ProbationCaseGenerator.COM_UNALLOCATED)
        probationCaseRepository.save(ProbationCaseGenerator.CASE_COMPLEX)
        probationCaseRepository.save(ProbationCaseGenerator.CASE_SIMPLE)
        probationCaseRepository.save(ProbationCaseGenerator.CASE_X320741)

        personManagerRepository.saveAll(
            listOf(
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_COMPLEX).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_SIMPLE).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_X320741).asPersonManager()
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

        val event = PersonGenerator.generateEvent(
            "1",
            ProbationCaseGenerator.CASE_COMPLEX.id
        ).apply(eventRepository::save)

        mainOffenceRepository.save(
            OffenceGenerator.generateMainOffence(
                event,
                OffenceGenerator.OFFENCE_ONE,
                LocalDate.now().minusDays(7)
            )
        )

        additionalOffenceRepository.save(
            OffenceGenerator.generateAdditionalOffence(
                event,
                OffenceGenerator.OFFENCE_TWO,
                LocalDate.now().minusDays(5)
            )
        )

        personalCircumstanceTypeRepository.saveAll(PersonalCircumstanceGenerator.PC_TYPES)
        personalCircumstanceSubTypeRepository.saveAll(PersonalCircumstanceGenerator.PC_SUB_TYPES)
        personalCircumstanceRepository.save(PersonalCircumstanceGenerator.generate(
            ProbationCaseGenerator.CASE_COMPLEX.id,
            PersonalCircumstanceGenerator.PC_TYPES.first { it.code == PersonalCircumstanceType.Code.VETERAN.value },
            PersonalCircumstanceGenerator.PC_SUB_TYPES.first { it.description == PersonalCircumstanceType.Code.VETERAN.value + "SUB" }
        ))
    }
}

interface LduRepository : JpaRepository<Ldu, Long>
interface OffenceRepository : JpaRepository<Offence, Long>
interface AdditionalOffenceRepository : JpaRepository<AdditionalOffence, Long>

interface PersonalCircumstanceTypeRepository : JpaRepository<PersonalCircumstanceType, Long>
interface PersonalCircumstanceSubTypeRepository : JpaRepository<PersonalCircumstanceSubType, Long>
