package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.EXCLUDED_CASE
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.RESTRICTED_CASE
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Ldu
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCase
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.MainOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalcircumstance.PersonalCircumstanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalcircumstance.entity.PersonalCircumstanceSubType
import uk.gov.justice.digital.hmpps.integrations.delius.personalcircumstance.entity.PersonalCircumstanceType
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
    private val personalCircumstanceRepository: PersonalCircumstanceRepository,
    private val mutableLimitedAccessPersonRepository: MutableLimitedAccessPersonRepository,
    private val restrictionRepository: RestrictionRepository,
    private val exclusionRepository: ExclusionRepository
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
        probationCaseRepository.save(ProbationCaseGenerator.CASE_X320811)
        probationCaseRepository.save(ProbationCaseGenerator.CASE_LAO_EXCLUSION)
        probationCaseRepository.save(ProbationCaseGenerator.CASE_LAO_RESTRICTED)

        personManagerRepository.saveAll(
            listOf(
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_COMPLEX).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_SIMPLE).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_X320741).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_X320811).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_LAO_EXCLUSION).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_LAO_RESTRICTED).asPersonManager()
            )
        )

        registrationRepository.save(
            PersonGenerator.generateRegistration(
                ProbationCaseGenerator.CASE_COMPLEX.asPerson(),
                ReferenceDataGenerator.REGISTER_TYPES[RegisterType.Code.MAPPA.value]!!,
                LocalDate.now().minusDays(7),
                ReferenceDataGenerator.NON_MAPPA_CATEGORY,
                ReferenceDataGenerator.REGISTER_LEVELS["M2"]
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

        listOf(
            DataLoaderCaseAndEventAndOffences(ProbationCaseGenerator.CASE_COMPLEX,        eventId = 100001L, mainOffence = Pair(200001L, LocalDate.parse("2024-10-11")), additionalOffence = Pair(300001L, LocalDate.parse("2024-10-21"))),
            DataLoaderCaseAndEventAndOffences(ProbationCaseGenerator.CASE_X320741,        eventId = 100002L, mainOffence = Pair(200002L, LocalDate.parse("2024-10-12")), additionalOffence = Pair(300002L, LocalDate.parse("2024-10-22"))),
            DataLoaderCaseAndEventAndOffences(ProbationCaseGenerator.CASE_LAO_RESTRICTED, eventId = 100003L, mainOffence = Pair(200003L, LocalDate.parse("2024-10-13")), additionalOffence = Pair(300003L, LocalDate.parse("2024-10-23"))),
            DataLoaderCaseAndEventAndOffences(ProbationCaseGenerator.CASE_LAO_EXCLUSION,  eventId = 100004L, mainOffence = Pair(200004L, LocalDate.parse("2024-10-14")), additionalOffence = Pair(300004L, LocalDate.parse("2024-10-24"))),
        ).forEach {
            generateEventAndAddOffences(
                it.probationCase,
                it.eventId,
                it.mainOffence,
                it.additionalOffence
            )
        }

        personalCircumstanceTypeRepository.saveAll(PersonalCircumstanceGenerator.PC_TYPES)
        personalCircumstanceSubTypeRepository.saveAll(PersonalCircumstanceGenerator.PC_SUB_TYPES)
        personalCircumstanceRepository.save(
            PersonalCircumstanceGenerator.generate(
                ProbationCaseGenerator.CASE_COMPLEX.id,
                PersonalCircumstanceGenerator.PC_TYPES.first { it.code == PersonalCircumstanceType.Code.VETERAN.value },
                PersonalCircumstanceGenerator.PC_SUB_TYPES.first { it.description == PersonalCircumstanceType.Code.VETERAN.value + "SUB" }
            ))

        mutableLimitedAccessPersonRepository.save(RESTRICTED_CASE)
        mutableLimitedAccessPersonRepository.save(EXCLUDED_CASE)
        restrictionRepository.save(LimitedAccessGenerator.generateRestriction(RESTRICTED_CASE.toLimitedAccessPerson()))
        exclusionRepository.save(LimitedAccessGenerator.generateExclusion(EXCLUDED_CASE.toLimitedAccessPerson()))
    }

    private fun generateEventAndAddOffences(
        probationCase: ProbationCase,
        eventId: Long,
        mainOffence: Pair<Long, LocalDate>,
        additionalOffence: Pair<Long, LocalDate>,
    ) {
        val event = PersonGenerator.generateEvent(
            "1",
            probationCase.id,
            id = eventId
        ).apply(eventRepository::save)

        mainOffenceRepository.save(
            OffenceGenerator.generateMainOffence(
                event,
                OffenceGenerator.OFFENCE_ONE,
                id = mainOffence.first,
                date = mainOffence.second
            )
        )

        additionalOffenceRepository.save(
            OffenceGenerator.generateAdditionalOffence(
                event,
                OffenceGenerator.OFFENCE_TWO,
                id = additionalOffence.first,
                date = additionalOffence.second
            )
        )
    }
}

@Entity
@Table(name = "offender")
class MutableLimitedAccessPerson(
    @Column(columnDefinition = "char(7)")
    val crn: String,
    val exclusionMessage: String?,
    val restrictionMessage: String?,
    @Id
    @Column(name = "offender_id")
    val id: Long,
) {
    fun toLimitedAccessPerson() = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)
}

data class DataLoaderCaseAndEventAndOffences(
    val probationCase: ProbationCase,
    val eventId: Long,
    val mainOffence: Pair<Long, LocalDate>,
    val additionalOffence: Pair<Long, LocalDate>
)

interface LduRepository : JpaRepository<Ldu, Long>
interface OffenceRepository : JpaRepository<Offence, Long>
interface AdditionalOffenceRepository : JpaRepository<AdditionalOffence, Long>

interface PersonalCircumstanceTypeRepository : JpaRepository<PersonalCircumstanceType, Long>
interface PersonalCircumstanceSubTypeRepository : JpaRepository<PersonalCircumstanceSubType, Long>

interface MutableLimitedAccessPersonRepository : JpaRepository<MutableLimitedAccessPerson, Long>
interface RestrictionRepository : JpaRepository<Restriction, Long>
interface ExclusionRepository : JpaRepository<Exclusion, Long>
