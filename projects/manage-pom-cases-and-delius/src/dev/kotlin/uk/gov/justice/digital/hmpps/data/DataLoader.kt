package uk.gov.justice.digital.hmpps.data

import UserGenerator
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.CaseAllocationGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import uk.gov.justice.digital.hmpps.user.UserRepository
import java.time.LocalDate

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val userRepository: UserRepository,
    private val staffUserRepository: StaffUserRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val lduRepository: LocalDeliveryUnitRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val eventRepository: EventRepository,
    private val disposalRepository: DisposalRepository,
    private val caseAllocationRepository: CaseAllocationRepository,
    private val registrationRepository: RegistrationRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        referenceDataRepository.saveAll(ReferenceDataGenerator.ALL)
        registerTypeRepository.saveAll(listOf(RegistrationGenerator.TYPE_MAPPA, RegistrationGenerator.TYPE_OTH))
        lduRepository.save(ProviderGenerator.DEFAULT_LDU)
        teamRepository.saveAll(PersonManagerGenerator.ALL.map { it.team })
        staffRepository.saveAll(PersonManagerGenerator.ALL.map { it.staff })
        staffUserRepository.save(UserGenerator.DEFAULT_STAFF_USER)

        personRepository.save(PersonGenerator.DEFAULT)
        personManagerRepository.saveAll(PersonManagerGenerator.ALL)

        eventRepository.saveAll(CaseAllocationGenerator.ALL.map { it.event })
        disposalRepository.saveAll(CaseAllocationGenerator.ALL.map { it.event.disposal })
        caseAllocationRepository.saveAll(CaseAllocationGenerator.ALL)

        registrationRepository.saveAll(
            listOf(
                RegistrationGenerator.generate(
                    RegistrationGenerator.TYPE_MAPPA,
                    ReferenceDataGenerator.LEVEL_M2,
                    LocalDate.now().minusDays(3)
                ),
                RegistrationGenerator.generate(
                    RegistrationGenerator.TYPE_MAPPA,
                    ReferenceDataGenerator.LEVEL_M1,
                    LocalDate.now().minusDays(1),
                    softDeleted = true
                ),
                RegistrationGenerator.generate(
                    RegistrationGenerator.TYPE_MAPPA,
                    ReferenceDataGenerator.LEVEL_M3,
                    LocalDate.now().minusDays(2),
                    deRegistered = true
                ),
                RegistrationGenerator.generate(
                    RegistrationGenerator.TYPE_OTH,
                    ReferenceDataGenerator.LEVEL_M1,
                    LocalDate.now().minusDays(1)
                )
            )
        )
    }
}

interface StaffUserRepository : JpaRepository<StaffUser, Long>
interface ReferenceDataRepository : JpaRepository<ReferenceData, Long>
interface LocalDeliveryUnitRepository : JpaRepository<LocalDeliveryUnit, Long>
interface TeamRepository : JpaRepository<Team, Long>
interface StaffRepository : JpaRepository<Staff, Long>
interface PersonManagerRepository : JpaRepository<PersonManager, Long>
interface EventRepository : JpaRepository<Event, Long>
interface DisposalRepository : JpaRepository<Disposal, Long>
interface RegisterTypeRepository : JpaRepository<RegisterType, Long>
