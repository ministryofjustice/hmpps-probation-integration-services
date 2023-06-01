package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.CaseAllocationGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataSet
import uk.gov.justice.digital.hmpps.user.UserRepository
import java.time.LocalDate

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val userRepository: UserRepository,
    private val staffUserRepository: StaffUserRepository,
    private val referenceDataSetRepository: ReferenceDataSetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val lduRepository: LocalDeliveryUnitRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val eventRepository: EventRepository,
    private val disposalRepository: DisposalRepository,
    private val custodyRepository: CustodyRepository,
    private val caseAllocationRepository: CaseAllocationRepository,
    private val registrationRepository: RegistrationRepository,
    private val keyDateRepository: KeyDateRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        referenceDataSetRepository.save(ReferenceDataGenerator.KEY_DATE_TYPE_DATASET)
        referenceDataRepository.saveAll(ReferenceDataGenerator.ALL)
        registerTypeRepository.saveAll(
            listOf(
                RegistrationGenerator.TYPE_MAPPA,
                RegistrationGenerator.TYPE_OTH,
                RegistrationGenerator.TYPE_DASO
            )
        )
        lduRepository.save(ProviderGenerator.DEFAULT_LDU)
        teamRepository.saveAll(PersonManagerGenerator.ALL.map { it.team })
        staffRepository.saveAll(PersonManagerGenerator.ALL.map { it.staff })
        staffUserRepository.save(UserGenerator.DEFAULT_STAFF_USER)

        personRepository.saveAll(
            listOf(
                PersonGenerator.DEFAULT,
                PersonGenerator.HANDOVER,
                PersonGenerator.CREATE_HANDOVER_AND_START,
                PersonGenerator.UPDATE_HANDOVER_AND_START
            )
        )
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

        val handoverEvent = eventRepository.save(EventGenerator.generateEvent(PersonGenerator.HANDOVER.id))
        val handoverDisposal = disposalRepository.save(EventGenerator.generateDisposal(handoverEvent))
        custodyRepository.save(EventGenerator.generateCustody(handoverDisposal))

        val bothEvent = eventRepository.save(EventGenerator.generateEvent(PersonGenerator.CREATE_HANDOVER_AND_START.id))
        val bothDisposal = disposalRepository.save(EventGenerator.generateDisposal(bothEvent))
        custodyRepository.save(EventGenerator.generateCustody(bothDisposal))

        val handoverStartEvent =
            eventRepository.save(EventGenerator.generateEvent(PersonGenerator.UPDATE_HANDOVER_AND_START.id))
        val handoverStartDisposal = disposalRepository.save(EventGenerator.generateDisposal(handoverStartEvent))
        val handoverStartCustody = custodyRepository.save(EventGenerator.generateCustody(handoverStartDisposal))
        keyDateRepository.saveAll(
            listOf(
                EventGenerator.generateKeyDate(
                    handoverStartCustody,
                    ReferenceDataGenerator.KEY_DATE_HANDOVER_TYPE,
                    LocalDate.of(2023, 5, 2)
                ),
                EventGenerator.generateKeyDate(
                    handoverStartCustody,
                    ReferenceDataGenerator.KEY_DATE_HANDOVER_START_DATE_TYPE,
                    LocalDate.of(2023, 5, 1)
                )
            )
        )
    }
}

interface ReferenceDataSetRepository : JpaRepository<ReferenceDataSet, Long>
interface StaffUserRepository : JpaRepository<StaffUser, Long>
interface LocalDeliveryUnitRepository : JpaRepository<LocalDeliveryUnit, Long>
interface TeamRepository : JpaRepository<Team, Long>
interface StaffRepository : JpaRepository<Staff, Long>
interface PersonManagerRepository : JpaRepository<PersonManager, Long>
interface EventRepository : JpaRepository<Event, Long>
interface DisposalRepository : JpaRepository<Disposal, Long>
interface RegisterTypeRepository : JpaRepository<RegisterType, Long>
