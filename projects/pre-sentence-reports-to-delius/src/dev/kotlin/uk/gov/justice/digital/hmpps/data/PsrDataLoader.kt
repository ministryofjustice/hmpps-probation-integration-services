package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtReportGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.audit.entity.UserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReportRepository
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.Event
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class PsrDataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val providerRepository: ProviderRepository,
    private val userRepository: UserRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository,
    private val documentRepository: DocumentRepository,
    private val courtReportRepository: CourtReportRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        userRepository.save(UserGenerator.DOCUMENT_USER)
        providerRepository.save(Provider(IdGenerator.getAndIncrement(), "N00", "NPS London"))

        businessInteractionRepository.saveAll(
            listOf(BusinessInteractionGenerator.UPLOAD_DOCUMENT)
        )

        personRepository.save(PersonGenerator.DEFAULT)
        eventRepository.save(CourtReportGenerator.DEFAULT_EVENT)
        courtAppearanceRepository.save(CourtReportGenerator.DEFAULT_CA)
        val cr = CourtReportGenerator.DEFAULT
        courtReportRepository.save(cr)
        documentRepository.save(DocumentGenerator.DEFAULT)
    }
}

interface EventRepository : JpaRepository<Event, Long>
interface CourtAppearanceRepository : JpaRepository<CourtAppearance, Long>
