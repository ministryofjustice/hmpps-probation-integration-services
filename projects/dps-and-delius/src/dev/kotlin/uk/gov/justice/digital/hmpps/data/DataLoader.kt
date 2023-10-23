package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        entityManager.persist(PersonGenerator.DEFAULT)
        entityManager.persist(EventGenerator.EVENT.mainOffence.offence)
        entityManager.persist(EventGenerator.EVENT)
        entityManager.persist(EventGenerator.EVENT.mainOffence)
        entityManager.persist(EventGenerator.DISPOSAL.lengthUnits)
        entityManager.persist(EventGenerator.DISPOSAL.type)
        entityManager.persist(EventGenerator.DISPOSAL)
        entityManager.persist(EventGenerator.INSTITUTION)
        entityManager.persist(EventGenerator.CUSTODY)
        entityManager.persist(EventGenerator.COURT)
        entityManager.persist(EventGenerator.COURT_APPEARANCE.outcome)
        entityManager.persist(EventGenerator.COURT_APPEARANCE)
        entityManager.persist(EventGenerator.COURT_REPORT_TYPE)
        entityManager.persist(EventGenerator.COURT_REPORT)
        entityManager.persist(EventGenerator.INSTITUTIONAL_REPORT_TYPE)
        entityManager.persist(EventGenerator.INSTITUTIONAL_REPORT)
        entityManager.persist(EventGenerator.CONTACT_TYPE)
        entityManager.persist(EventGenerator.CONTACT)
        entityManager.persist(EventGenerator.NSI_TYPE)
        entityManager.persist(EventGenerator.NSI)
        entityManager.persist(DocumentGenerator.OFFENDER)
        entityManager.persist(DocumentGenerator.PREVIOUS_CONVICTIONS)
        entityManager.persist(DocumentGenerator.EVENT)
        entityManager.persist(DocumentGenerator.CPS_PACK)
        entityManager.persist(DocumentGenerator.ADDRESSASSESSMENT)
        entityManager.persist(DocumentGenerator.PERSONALCONTACT)
        entityManager.persist(DocumentGenerator.PERSONAL_CIRCUMSTANCE)
        entityManager.persist(DocumentGenerator.COURT_REPORT)
        entityManager.persist(DocumentGenerator.INSTITUTIONAL_REPORT)
        entityManager.persist(DocumentGenerator.OFFENDER_CONTACT)
        entityManager.persist(DocumentGenerator.EVENT_CONTACT)
        entityManager.persist(DocumentGenerator.OFFENDER_NSI)
        entityManager.persist(DocumentGenerator.EVENT_NSI)
    }
}
