package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.repository.PersonManagerRepository
import uk.gov.justice.digital.hmpps.data.repository.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.data.repository.ReferenceDataSetRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.user.AuditUser
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    @Value("\${delius.db.username}") private val deliusDbUsername: String,
    private val auditUserRepository: AuditUserRepository,
    private val probationAreaRepository: ProbationAreaRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val referenceDataSetRepository: ReferenceDataSetRepository,
    private val contactTypeRepository: ContactTypeRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(AuditUser(IdGenerator.getAndIncrement(), deliusDbUsername))
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        probationAreaRepository.save(ProbationAreaGenerator.DEFAULT)
        staffRepository.save(StaffGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)

        val person = PersonGenerator.generate("A000001")
        person.managers.clear()
        personRepository.save(person)
        personManagerRepository.save(PersonManagerGenerator.generate(person))

        referenceDataSetRepository.save(ReferenceDataSetGenerator.TIER)
        referenceDataSetRepository.save(ReferenceDataSetGenerator.TIER_CHANGE_REASON)
        referenceDataRepository.save(ReferenceDataGenerator.generate("UD0", ReferenceDataSetGenerator.TIER))
        referenceDataRepository.save(ReferenceDataGenerator.generate("UD2", ReferenceDataSetGenerator.TIER))
        referenceDataRepository.save(ReferenceDataGenerator.generate("UC2", ReferenceDataSetGenerator.TIER))
        referenceDataRepository.save(
            ReferenceDataGenerator.generate(
                "ATS",
                ReferenceDataSetGenerator.TIER_CHANGE_REASON
            )
        )
        contactTypeRepository.save(ContactTypeGenerator.TIER_UPDATE)
    }
}
