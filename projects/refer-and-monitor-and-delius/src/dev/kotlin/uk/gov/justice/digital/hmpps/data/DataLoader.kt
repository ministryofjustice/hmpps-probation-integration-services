package uk.gov.justice.digital.hmpps.data

import IdGenerator
import UserGenerator
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.ResponsibleOfficer
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
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.RequirementMainCategory
import uk.gov.justice.digital.hmpps.user.UserRepository
import java.time.ZonedDateTime

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val userRepository: UserRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val datasetRepository: DatasetRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiOutcomeRepository: NsiOutcomeRepository,
    private val mainCatRepository: MainCatRepository,
    private val providerRepository: ProviderRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val staffUserRepository: StaffUserRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val contactRepository: ContactRepository,
    private val nsiRepository: NsiRepository,
    private val nsiManagerRepository: NsiManagerRepository,
    private val disposalRepository: DisposalRepository,
    private val requirementRepository: RequirementRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        businessInteractionRepository.save(
            BusinessInteraction(
                IdGenerator.getAndIncrement(),
                BusinessInteractionCode.MANAGE_NSI.code,
                ZonedDateTime.now()
            )
        )
        contactTypeRepository.saveAll(ContactGenerator.TYPES.values)
        contactOutcomeRepository.saveAll(ContactGenerator.OUTCOMES.values)
        enforcementActionRepository.saveAll(ContactGenerator.ENFORCEMENT_ACTIONS.values)
        nsiTypeRepository.saveAll(NsiGenerator.TYPES.values)
        nsiStatusRepository.saveAll(listOf(NsiGenerator.INPROG_STATUS, NsiGenerator.COMP_STATUS))

        datasetRepository.save(NsiGenerator.NSI_OUTCOME_DS)
        nsiOutcomeRepository.saveAll(NsiGenerator.OUTCOMES.values)

        mainCatRepository.save(SentenceGenerator.MAIN_CAT_F)

        providerRepository.save(ProviderGenerator.INTENDED_PROVIDER)
        teamRepository.save(ProviderGenerator.INTENDED_TEAM)
        staffRepository.saveAll(listOf(ProviderGenerator.INTENDED_STAFF, ProviderGenerator.JOHN_SMITH))

        personRepository.saveAll(
            listOf(
                PersonGenerator.DEFAULT,
                PersonGenerator.SENTENCED_WITHOUT_NSI,
                PersonGenerator.COMMUNITY_RESPONSIBLE,
                PersonGenerator.COMMUNITY_NOT_RESPONSIBLE
            )
        )

        val roCom = PersonGenerator.generatePersonManager(
            PersonGenerator.COMMUNITY_RESPONSIBLE,
            ProviderGenerator.JOHN_SMITH
        )
        personManagerRepository.saveAll(
            listOf(
                roCom,
                PersonGenerator.generatePersonManager(
                    PersonGenerator.COMMUNITY_NOT_RESPONSIBLE,
                    ProviderGenerator.JOHN_SMITH
                )
            )
        )

        responsibleOfficerRepository.save(PersonGenerator.generateResponsibleOfficer(roCom))

        staffUserRepository.save(ProviderGenerator.JOHN_SMITH_USER)

        contactRepository.save(ContactGenerator.CRSAPT)

        nsiRepository.save(NsiGenerator.END_PREMATURELY)
        nsiManagerRepository.save(NsiGenerator.generateManager(NsiGenerator.END_PREMATURELY))

        disposalRepository.save(SentenceGenerator.SENTENCE_WITHOUT_NSI)
        requirementRepository.save(SentenceGenerator.generateRequirement(SentenceGenerator.SENTENCE_WITHOUT_NSI))

        personRepository.save(PersonGenerator.FUZZY_SEARCH)
        nsiRepository.save(NsiGenerator.FUZZY_SEARCH)
        nsiManagerRepository.save(NsiGenerator.generateManager(NsiGenerator.FUZZY_SEARCH))
    }
}

interface DatasetRepository : JpaRepository<Dataset, Long>
interface MainCatRepository : JpaRepository<RequirementMainCategory, Long>

interface StaffUserRepository : JpaRepository<StaffUser, Long>
interface ResponsibleOfficerRepository : JpaRepository<ResponsibleOfficer, Long>
