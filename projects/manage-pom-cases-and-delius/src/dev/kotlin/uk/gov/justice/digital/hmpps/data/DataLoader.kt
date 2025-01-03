package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataSet
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val referenceDataSetRepository: ReferenceDataSetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val institutionRepository: InstitutionRepository,
    private val probationAreaRepository: ProbationAreaRepository,
    private val districtRepository: DistrictRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val disposalRepository: DisposalRepository,
    private val custodyRepository: CustodyRepository,
    private val caseAllocationRepository: CaseAllocationRepository,
    private val registrationRepository: RegistrationRepository,
    private val keyDateRepository: KeyDateRepository,
    private val contactTypeRepository: ContactTypeRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        institutionRepository.save(ProviderGenerator.DEFAULT_PROVIDER.institution!!)
        probationAreaRepository.save(ProviderGenerator.DEFAULT_PROVIDER)
        referenceDataSetRepository.save(ReferenceDataGenerator.KEY_DATE_TYPE_DATASET)
        referenceDataSetRepository.save(ReferenceDataGenerator.POM_ALLOCATION_DATASET)
        referenceDataRepository.saveAll(ReferenceDataGenerator.ALL)
        registerTypeRepository.saveAll(
            listOf(
                RegistrationGenerator.TYPE_MAPPA,
                RegistrationGenerator.TYPE_OTH,
                RegistrationGenerator.TYPE_DASO
            )
        )
        contactTypeRepository.saveAll(
            ContactType.Code.entries.map {
                ContactType(
                    it.value,
                    IdGenerator.getAndIncrement()
                )
            }
        )

        districtRepository.save(ProviderGenerator.DEFAULT_DISTRICT)
        teamRepository.saveAll(PersonManagerGenerator.ALL.map { it.team } + ProviderGenerator.POM_TEAM + ProviderGenerator.UNALLOCATED_TEAM)

        staffRepository.save(ProviderGenerator.generateStaff("Test", "Test", "Test"))

        personRepository.saveAll(
            listOf(
                PersonGenerator.DEFAULT,
                PersonGenerator.HANDOVER,
                PersonGenerator.NO_MAPPA,
                PersonGenerator.CREATE_HANDOVER_AND_START,
                PersonGenerator.UPDATE_HANDOVER_AND_START,
                PersonGenerator.CREATE_SENTENCE_CHANGED,
                PersonGenerator.PERSON_NOT_FOUND,
                PersonGenerator.PERSON_MULTIPLE_CUSTODIAL
            )
        )

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

        val sentenceChangedHandoverEvent =
            eventRepository.save(EventGenerator.generateEvent(PersonGenerator.CREATE_SENTENCE_CHANGED.id))
        val sentenceChangedHandoverDisposal =
            disposalRepository.save(EventGenerator.generateDisposal(sentenceChangedHandoverEvent))
        custodyRepository.save(EventGenerator.generateCustody(sentenceChangedHandoverDisposal))

        val notFoundSentenceChangedHandoverEvent =
            eventRepository.save(EventGenerator.generateEvent(PersonGenerator.PERSON_NOT_FOUND.id))
        val notFoundSentenceChangedHandoverDisposal =
            disposalRepository.save(EventGenerator.generateDisposal(notFoundSentenceChangedHandoverEvent))
        custodyRepository.save(EventGenerator.generateCustody(notFoundSentenceChangedHandoverDisposal))

        //Multiple custodial
        val multipleHandoverEvent1 =
            eventRepository.save(EventGenerator.generateEvent(PersonGenerator.PERSON_MULTIPLE_CUSTODIAL.id))
        val multipleHandoverEvent2 =
            eventRepository.save(EventGenerator.generateEvent(PersonGenerator.PERSON_MULTIPLE_CUSTODIAL.id))
        val multipleHandoverDisposal1 =
            disposalRepository.save(EventGenerator.generateDisposal(multipleHandoverEvent1))
        val multipleHandoverDisposal2 =
            disposalRepository.save(EventGenerator.generateDisposal(multipleHandoverEvent2))
        custodyRepository.save(EventGenerator.generateCustody(multipleHandoverDisposal1))
        custodyRepository.save(EventGenerator.generateCustody(multipleHandoverDisposal2))

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
interface DistrictRepository : JpaRepository<District, Long>
interface EventRepository : JpaRepository<Event, Long>
interface DisposalRepository : JpaRepository<Disposal, Long>
interface RegisterTypeRepository : JpaRepository<RegisterType, Long>
interface InstitutionRepository : JpaRepository<Institution, Long>
interface PersonManagerRepository : JpaRepository<PersonManager, Long>
