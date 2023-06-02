package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.DeliveryUnit
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.LocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.PduRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.RequirementMainCategory
import uk.gov.justice.digital.hmpps.user.UserRepository
import java.time.ZonedDateTime

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val userRepository: UserRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val datasetRepository: DatasetRepository,
    private val disposalTypeRepository: DisposalTypeRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiOutcomeRepository: NsiOutcomeRepository,
    private val mainCatRepository: MainCatRepository,
    private val providerRepository: ProviderRepository,
    private val boroughRepository: BoroughRepository,
    private val districtRepository: DistrictRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val staffUserRepository: StaffUserRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val prisonManagerRepository: PrisonManagerRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val eventRepository: EventRepository,
    private val disposalRepository: DisposalRepository,
    private val contactRepository: ContactRepository,
    private val nsiRepository: NsiRepository,
    private val nsiManagerRepository: NsiManagerRepository,
    private val requirementRepository: RequirementRepository,
    private val locationRepository: LocationRepository,
    private val pduRepository: PduRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        businessInteractionRepository.saveAll(
            listOf(
                BusinessInteraction(
                    IdGenerator.getAndIncrement(),
                    BusinessInteractionCode.MANAGE_NSI.code,
                    ZonedDateTime.now()
                ),
                BusinessInteraction(
                    IdGenerator.getAndIncrement(),
                    BusinessInteractionCode.ADD_CONTACT.code,
                    ZonedDateTime.now()
                ),
                BusinessInteraction(
                    IdGenerator.getAndIncrement(),
                    BusinessInteractionCode.UPDATE_CONTACT.code,
                    ZonedDateTime.now()
                )
            )
        )
        disposalTypeRepository.save(SentenceGenerator.DEFAULT_DISPOSAL_TYPE)
        contactTypeRepository.saveAll(ContactGenerator.TYPES.values)
        contactOutcomeRepository.saveAll(ContactGenerator.OUTCOMES.values)
        enforcementActionRepository.save(ContactGenerator.ENFORCEMENT_ACTION)
        nsiTypeRepository.saveAll(NsiGenerator.TYPES.values)
        nsiStatusRepository.saveAll(listOf(NsiGenerator.INPROG_STATUS, NsiGenerator.COMP_STATUS))

        datasetRepository.save(NsiGenerator.NSI_OUTCOME_DS)
        nsiOutcomeRepository.saveAll(NsiGenerator.OUTCOMES.values)

        mainCatRepository.save(SentenceGenerator.MAIN_CAT_F)

        val provider = providerRepository.saveAndFlush(ProviderGenerator.INTENDED_PROVIDER)
        pduRepository.saveAll(
            listOf(
                ProviderGenerator.PROBATION_BOROUGH.let {
                    DeliveryUnit(
                        it.code,
                        it.description,
                        provider,
                        true,
                        it.id
                    )
                },
                ProviderGenerator.PRISON_BOROUGH.let {
                    DeliveryUnit(
                        it.code,
                        it.description,
                        provider,
                        false,
                        it.id
                    )
                }
            )
        )
        districtRepository.saveAll(listOf(ProviderGenerator.PROBATION_DISTRICT, ProviderGenerator.PRISON_DISTRICT))

        val pdus = pduRepository.findAll().forEach { println("${it.code}, ${it.description}, ${it.id}") }

        teamRepository.saveAll(
            listOf(
                ProviderGenerator.INTENDED_TEAM,
                ProviderGenerator.PROBATION_TEAM,
                ProviderGenerator.PRISON_TEAM
            )
        )
        staffRepository.saveAll(
            listOf(
                ProviderGenerator.INTENDED_STAFF,
                ProviderGenerator.JOHN_SMITH,
                ProviderGenerator.PRISON_MANAGER
            )
        )

        locationRepository.save(ProviderGenerator.DEFAULT_LOCATION)

        personRepository.saveAll(
            listOf(
                PersonGenerator.DEFAULT,
                PersonGenerator.SENTENCED_WITHOUT_NSI,
                PersonGenerator.COMMUNITY_RESPONSIBLE,
                PersonGenerator.COMMUNITY_NOT_RESPONSIBLE,
                PersonGenerator.NO_APPOINTMENTS
            )
        )

        val roCom = PersonGenerator.generatePersonManager(
            PersonGenerator.COMMUNITY_RESPONSIBLE,
            ProviderGenerator.JOHN_SMITH,
            ProviderGenerator.PROBATION_TEAM
        )
        personManagerRepository.saveAll(
            listOf(
                roCom,
                PersonGenerator.generatePersonManager(
                    PersonGenerator.COMMUNITY_NOT_RESPONSIBLE,
                    ProviderGenerator.JOHN_SMITH,
                    ProviderGenerator.PROBATION_TEAM
                )
            )
        )

        val pom = prisonManagerRepository.save(
            PersonGenerator.generatePrisonManager(
                PersonGenerator.COMMUNITY_NOT_RESPONSIBLE,
                ProviderGenerator.PRISON_MANAGER,
                ProviderGenerator.PRISON_TEAM
            )
        )

        responsibleOfficerRepository.save(PersonGenerator.generateResponsibleOfficer(roCom))
        responsibleOfficerRepository.save(PersonGenerator.generateResponsibleOfficer(null, pom))

        staffUserRepository.save(ProviderGenerator.JOHN_SMITH_USER)

        personRepository.saveAll(listOf(PersonGenerator.DEFAULT, PersonGenerator.SENTENCED_WITHOUT_NSI))

        eventRepository.saveAll(listOf(SentenceGenerator.EVENT_WITHOUT_NSI, SentenceGenerator.EVENT_WITH_NSI))
        disposalRepository.saveAll(listOf(SentenceGenerator.SENTENCE_WITHOUT_NSI, SentenceGenerator.SENTENCE_WITH_NSI))
        requirementRepository.save(SentenceGenerator.generateRequirement(SentenceGenerator.SENTENCE_WITHOUT_NSI))

        val rfn = requirementRepository.save(SentenceGenerator.generateRequirement(SentenceGenerator.SENTENCE_WITH_NSI))
        val nsi = NsiGenerator.END_PREMATURELY
        NsiGenerator.END_PREMATURELY = nsiRepository.save(
            NsiGenerator.generate(
                nsi.type,
                externalReference = nsi.externalReference,
                eventId = nsi.eventId,
                requirementId = rfn.id,
                rarCount = nsi.rarCount
            )
        )

        val nsiNa = NsiGenerator.NO_APPOINTMENTS
        NsiGenerator.NO_APPOINTMENTS = nsiRepository.save(
            NsiGenerator.generate(
                nsiNa.type,
                nsiNa.person,
                externalReference = nsiNa.externalReference,
                eventId = nsiNa.eventId,
                rarCount = nsiNa.rarCount
            )
        )

        nsiManagerRepository.saveAll(
            listOf(
                NsiGenerator.generateManager(NsiGenerator.END_PREMATURELY),
                NsiGenerator.generateManager(NsiGenerator.NO_APPOINTMENTS)
            )
        )

        val crsA = ContactGenerator.CRSAPT_NON_COMPLIANT
        ContactGenerator.CRSAPT_NON_COMPLIANT = contactRepository.save(
            ContactGenerator.generate(
                crsA.type,
                date = crsA.date,
                notes = crsA.notes,
                nsi = NsiGenerator.END_PREMATURELY,
                rarActivity = crsA.rarActivity
            )
        )

        val crsB = ContactGenerator.CRSAPT_COMPLIANT
        ContactGenerator.CRSAPT_COMPLIANT = contactRepository.save(
            ContactGenerator.generate(
                crsB.type,
                date = crsB.date,
                notes = crsB.notes,
                nsi = NsiGenerator.END_PREMATURELY,
                rarActivity = crsB.rarActivity,
                externalReference = crsB.externalReference
            )
        )

        val crsC = ContactGenerator.CRSAPT_NOT_ATTENDED
        ContactGenerator.CRSAPT_NOT_ATTENDED = contactRepository.save(
            ContactGenerator.generate(
                crsC.type,
                date = crsC.date,
                notes = crsC.notes,
                nsi = NsiGenerator.END_PREMATURELY,
                rarActivity = crsC.rarActivity
            )
        )

        personRepository.save(PersonGenerator.FUZZY_SEARCH)
        NsiGenerator.FUZZY_SEARCH = nsiRepository.save(NsiGenerator.FUZZY_SEARCH)
        nsiManagerRepository.save(NsiGenerator.generateManager(NsiGenerator.FUZZY_SEARCH))

        NsiGenerator.TERMINATED = nsiRepository.save(NsiGenerator.TERMINATED)
    }
}

interface DatasetRepository : JpaRepository<Dataset, Long>
interface DistrictRepository : JpaRepository<District, Long>
interface BoroughRepository : JpaRepository<Borough, Long>
interface MainCatRepository : JpaRepository<RequirementMainCategory, Long>
interface DisposalTypeRepository : JpaRepository<DisposalType, Long>
interface StaffUserRepository : JpaRepository<StaffUser, Long>
interface ResponsibleOfficerRepository : JpaRepository<ResponsibleOfficer, Long>
