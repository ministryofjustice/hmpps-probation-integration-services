package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateExclusion
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateRestriction
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Disability
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonDetailRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.*
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.RequirementMainCategory
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate
import java.time.ZonedDateTime

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val datasetRepository: DatasetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val disposalTypeRepository: DisposalTypeRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val mainCatRepository: MainCatRepository,
    private val providerRepository: ProviderRepository,
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
    private val pduRepository: PduRepository,
    private val restrictionRepository: RestrictionRepository,
    private val exclusionRepository: ExclusionRepository,
    private val personDetailRepository: PersonDetailRepository,
    private val personAddressRepository: PersonAddressRepository,
    private val disabilityRepository: DisabilityRepository,
    private val offenceRepository: OffenceRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val teamOfficeLinkRepository: TeamOfficeLinkRepository,
    private val entityManagerDataLoader: EntityManagerDataLoader
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
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

        datasetRepository.saveAll(ReferenceDataGenerator.allDatasets() + NsiGenerator.NSI_OUTCOME_DS)
        referenceDataRepository.saveAll(ReferenceDataGenerator.allReferenceData() + NsiGenerator.OUTCOMES.values + NsiGenerator.WITHDRAWN_OUTCOMES.values)

        mainCatRepository.save(SentenceGenerator.MAIN_CAT_F)

        providerRepository.save(ProviderGenerator.NON_CRS_PROVIDER)
        providerRepository.save(ProviderGenerator.INACTIVE_PROVIDER)
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

        locationRepository.saveAll(ProviderGenerator.LOCATIONS + ProviderGenerator.DEFAULT_LOCATION)

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
        val nsi = NsiGenerator.WITHDRAWN
        NsiGenerator.WITHDRAWN = nsiRepository.save(
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
                NsiGenerator.generateManager(NsiGenerator.WITHDRAWN),
                NsiGenerator.generateManager(NsiGenerator.NO_APPOINTMENTS)
            )
        )

        val crsA = ContactGenerator.CRSAPT_NON_COMPLIANT
        ContactGenerator.CRSAPT_NON_COMPLIANT = contactRepository.save(
            ContactGenerator.generate(
                crsA.type,
                date = crsA.date,
                notes = crsA.notes,
                nsi = NsiGenerator.WITHDRAWN,
                rarActivity = crsA.rarActivity
            )
        )

        val crsB = ContactGenerator.CRSAPT_COMPLIANT
        ContactGenerator.CRSAPT_COMPLIANT = contactRepository.save(
            ContactGenerator.generate(
                crsB.type,
                date = crsB.date,
                notes = crsB.notes,
                nsi = NsiGenerator.WITHDRAWN,
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
                nsi = NsiGenerator.WITHDRAWN,
                rarActivity = crsC.rarActivity
            )
        )

        val crsD = ContactGenerator.CRSAPT_NO_SESSION
        ContactGenerator.CRSAPT_NO_SESSION = contactRepository.save(
            ContactGenerator.generate(
                crsD.type,
                date = crsD.date,
                notes = crsD.notes,
                nsi = NsiGenerator.WITHDRAWN,
                rarActivity = crsD.rarActivity,
                externalReference = crsD.externalReference
            )
        )

        personRepository.save(PersonGenerator.FUZZY_SEARCH)
        entityManagerDataLoader.loadData()
        nsiManagerRepository.save(NsiGenerator.generateManager(entityManagerDataLoader.NSI_FUZZY_SEARCH!!))

        auditUserRepository.save(UserGenerator.LIMITED_ACCESS_USER)
        personRepository.saveAll(
            listOf(
                PersonGenerator.EXCLUSION,
                PersonGenerator.RESTRICTION,
                PersonGenerator.RESTRICTION_EXCLUSION
            )
        )

        exclusionRepository.save(LimitedAccessGenerator.EXCLUSION)
        restrictionRepository.save(LimitedAccessGenerator.RESTRICTION)
        exclusionRepository.save(generateExclusion(person = PersonGenerator.RESTRICTION_EXCLUSION))
        restrictionRepository.save(generateRestriction(person = PersonGenerator.RESTRICTION_EXCLUSION))

        personDetailRepository.saveAll(listOf(CaseDetailsGenerator.MINIMAL_PERSON, CaseDetailsGenerator.FULL_PERSON))
        personAddressRepository.saveAll(
            listOf(
                CaseDetailsGenerator.generateAddress(
                    ReferenceDataGenerator.ADDRESS_MAIN,
                    buildingName = "Some Building",
                    streetName = "Some Street",
                    postcode = "SB1 1SS"
                ),
                CaseDetailsGenerator.generateAddress(
                    ReferenceDataGenerator.ADDRESS_OTHER,
                    buildingName = "No Such Place",
                    postcode = "NS1 1SP"
                )
            )
        )
        disabilityRepository.saveAll(
            listOf(
                CaseDetailsGenerator.generateDisability(
                    ReferenceDataGenerator.DISABILITY1,
                    notes = "Some notes about the disability"
                ),
                CaseDetailsGenerator.generateDisability(ReferenceDataGenerator.DISABILITY2, softDeleted = true),
                CaseDetailsGenerator.generateDisability(
                    ReferenceDataGenerator.DISABILITY2,
                    endDate = LocalDate.now().minusDays(1)
                )
            )
        )

        offenceRepository.save(SentenceGenerator.OFFENCE)
        eventRepository.save(SentenceGenerator.FULL_DETAIL_EVENT)
        mainOffenceRepository.save(SentenceGenerator.FULL_DETAIL_MAIN_OFFENCE)
        disposalRepository.save(SentenceGenerator.FULL_DETAIL_SENTENCE)

        teamOfficeLinkRepository.saveAll(
            listOf(
                ProviderGenerator.linkTeamAndOfficeLocation(
                    ProviderGenerator.INTENDED_TEAM, ProviderGenerator.DEFAULT_LOCATION
                ),
                ProviderGenerator.linkTeamAndOfficeLocation(
                    ProviderGenerator.PROBATION_TEAM, ProviderGenerator.DEFAULT_LOCATION
                )
            )
        )
    }
}

interface DatasetRepository : JpaRepository<Dataset, Long>
interface DistrictRepository : JpaRepository<District, Long>
interface MainCatRepository : JpaRepository<RequirementMainCategory, Long>
interface DisposalTypeRepository : JpaRepository<DisposalType, Long>
interface StaffUserRepository : JpaRepository<StaffUser, Long>
interface ResponsibleOfficerRepository : JpaRepository<ResponsibleOfficer, Long>
interface RestrictionRepository : JpaRepository<Restriction, Long>
interface ExclusionRepository : JpaRepository<Exclusion, Long>
interface DisabilityRepository : JpaRepository<Disability, Long>
interface OffenceRepository : JpaRepository<Offence, Long>
interface MainOffenceRepository : JpaRepository<MainOffence, Long>

interface TeamOfficeLinkRepository : JpaRepository<TeamOfficeLink, TeamOfficeLinkId>
