package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.MainOffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.repository.DatasetRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.MainOffenceRepository
import uk.gov.justice.digital.hmpps.data.repository.OffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.TeamRepository
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val userRepository: UserRepository,
    private val personRepository: PersonRepository,
    private val datasetRepository: DatasetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val offenceRepository: OffenceRepository,
    private val eventRepository: EventRepository,
    private val disposalRepository: DisposalRepository,
    private val disposalTypeRepository: DisposalTypeRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val contactTypeRepository: ContactTypeRepository

) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        datasetRepository.saveAll(
            listOf(
                DatasetGenerator.GENDER,
                DatasetGenerator.TIER_CHANGE_REASON,
                DatasetGenerator.TIER
            )
        )

        referenceDataRepository.saveAll(
            listOf(
                ReferenceDataGenerator.GENDER_MALE,
                ReferenceDataGenerator.TIER_CHANGE_REASON_OGRS,
                ReferenceDataGenerator.TIER_NA
            )
        )

        contactTypeRepository.save(ContactTypeGenerator.DEFAULT)
        staffRepository.save(StaffGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)
        offenceRepository.save(OffenceGenerator.DEFAULT)
        personRepository.save(PersonGenerator.DEFAULT)
        personManagerRepository.save(PersonManagerGenerator.DEFAULT)
        eventRepository.save(EventGenerator.DEFAULT)
        disposalTypeRepository.save(DisposalTypeGenerator.DEFAULT)
        disposalRepository.save(DisposalGenerator.DEFAULT)
        mainOffenceRepository.save(MainOffenceGenerator.DEFAULT)
        personManagerRepository.save(PersonManagerGenerator.DEFAULT)
    }
}
