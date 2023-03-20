package uk.gov.justice.digital.hmpps.data

import UserGenerator
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Dataset
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val userRepository: UserRepository,
    private val datasetRepository: DatasetRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiOutcomeRepository: NsiOutcomeRepository,
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository,
    private val nsiRepository: NsiRepository,
    private val nsiManagerRepository: NsiManagerRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        contactTypeRepository.saveAll(ContactGenerator.TYPES.values)
        contactOutcomeRepository.saveAll(ContactGenerator.OUTCOMES.values)
        enforcementActionRepository.saveAll(ContactGenerator.ENFORCEMENT_ACTIONS.values)
        nsiTypeRepository.saveAll(NsiGenerator.TYPES.values)
        nsiStatusRepository.saveAll(listOf(NsiGenerator.INPROG_STATUS, NsiGenerator.COMP_STATUS))

        datasetRepository.save(NsiGenerator.NSI_OUTCOME_DS)
        nsiOutcomeRepository.saveAll(NsiGenerator.OUTCOMES.values)

        personRepository.save(PersonGenerator.DEFAULT)
        contactRepository.save(ContactGenerator.CRSAPT)

        nsiRepository.save(NsiGenerator.END_PREMATURELY)
        nsiManagerRepository.save(NsiGenerator.generateManager(NsiGenerator.END_PREMATURELY))
    }
}

interface DatasetRepository : JpaRepository<Dataset, Long>
