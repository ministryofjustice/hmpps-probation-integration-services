package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.ZonedDateTime

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        entityManager.saveAll(
            BusinessInteraction(
                IdGenerator.getAndIncrement(),
                BusinessInteractionCode.ADD_CONTACT.code,
                ZonedDateTime.now()
            ),
            BusinessInteraction(
                IdGenerator.getAndIncrement(),
                BusinessInteractionCode.UPDATE_CONTACT.code,
                ZonedDateTime.now()
            ),
            ContactTypeGenerator.CT_ESPCHI,
            ProviderGenerator.DEFAULT_PROVIDER,
            ProviderGenerator.DEFAULT_PDU,
            ProviderGenerator.DEFAULT_LDU,
            ProviderGenerator.DEFAULT_TEAM,
            ProviderGenerator.DEFAULT_STAFF,
            ProviderGenerator.DEFAULT_STAFF_USER,
            PersonGenerator.DEFAULT_PERSON,
            PersonGenerator.DEFAULT_COM,
            PersonGenerator.PREVIOUS_EVENT,
            PersonGenerator.DEFAULT_EVENT,
            PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1,
            PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_2,
            PersonGenerator.generatePersonManager(PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1),
            PersonGenerator.generatePersonManager(PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_2),
            ContactGenerator.CONTACT_TO_REVIEW,
            ContactGenerator.CONTACT_TO_UPDATE
        )
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
}
