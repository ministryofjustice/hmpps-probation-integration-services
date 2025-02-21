package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.repository.*
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
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
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
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
            )
        )

        contactTypeRepository.save(ContactTypeGenerator.DEFAULT)
        staffRepository.save(StaffGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)
        offenceRepository.save(OffenceGenerator.DEFAULT)
        personRepository.saveAll(listOf(PersonGenerator.DEFAULT, PersonGenerator.NULL_EVENT_PROCESSING))
        personManagerRepository.save(PersonManagerGenerator.DEFAULT)
        eventRepository.saveAll(
            listOf(EventGenerator.DEFAULT, EventGenerator.NEP_1, EventGenerator.NEP_2, EventGenerator.NEP_3)
        )
        disposalTypeRepository.save(DisposalTypeGenerator.DEFAULT)
        disposalRepository.saveAll(
            listOf(DisposalGenerator.DEFAULT, DisposalGenerator.NEP_DISPOSAL_2, DisposalGenerator.NEP_DISPOSAL_3)
        )
        mainOffenceRepository.save(MainOffenceGenerator.DEFAULT)
        personManagerRepository.saveAll(
            listOf(
                PersonManagerGenerator.DEFAULT,
                PersonManagerGenerator.generate(PersonGenerator.NULL_EVENT_PROCESSING)
            )
        )
    }
}
