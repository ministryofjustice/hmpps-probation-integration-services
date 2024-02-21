package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val em: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        em.saveAll(
            DatasetGenerator.ADDRESS_STATUS,
            DatasetGenerator.ADDRESS_TYPE,
            AddressRDGenerator.CAS3_ADDRESS_TYPE,
            AddressRDGenerator.MAIN_ADDRESS_STATUS,
            AddressRDGenerator.PREV_ADDRESS_STATUS,
            BusinessInteractionGenerator.UPDATE_CONTACT,
            ContactTypeGenerator.EARS_CONTACT_TYPE,
            ContactTypeGenerator.EACA_CONTACT_TYPE,
            ContactTypeGenerator.EACO_CONTACT_TYPE,
            ContactTypeGenerator.EABP_CONTACT_TYPE,
            ContactTypeGenerator.EAAR_CONTACT_TYPE,
            ContactTypeGenerator.EADP_CONTACT_TYPE,
            PersonGenerator.PERSON_CRN,
            PersonGenerator.generatePersonManager(PersonGenerator.PERSON_CRN)
        )
        saveProviderDetails()
    }

    private fun saveProviderDetails() {
        em.saveAll(
            ProviderGenerator.DEFAULT_PROVIDER,
            ProviderGenerator.DEFAULT_TEAM,
            ProviderGenerator.DEFAULT_STAFF,
        )
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
}