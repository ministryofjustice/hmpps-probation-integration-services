package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.ZonedDateTime

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager,
    private val businessInteractionRepository: BusinessInteractionRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
        BusinessInteractionCode.entries
            .map { BusinessInteraction(IdGenerator.getAndIncrement(), it.code, ZonedDateTime.now()) }
            .forEach { businessInteractionRepository.save(it) }
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        entityManager.persist(PersonGenerator.DS_ADDRESS_STATUS)
        entityManager.persist(PersonGenerator.DEFAULT_ADDRESS_STATUS)
        entityManager.persist(PersonGenerator.DEFAULT_PERSON)
        entityManager.persist(PersonGenerator.DEFAULT_ADDRESS)

        entityManager.persist(ProviderGenerator.N00)
        entityManager.persist(StaffGenerator.DEFAULT)
        entityManager.persist(UserGenerator.LIMITED_ACCESS_USER)
        entityManager.persist(UserGenerator.NON_LAO_USER)
        entityManager.persist(UserGenerator.DEFAULT)
        entityManager.persist(PersonGenerator.RESTRICTION)
        entityManager.persist(PersonGenerator.EXCLUSION)
        entityManager.persist(PersonGenerator.RESTRICTION_EXCLUSION)
        entityManager.flush()
        entityManager.persist(LimitedAccessGenerator.RESTRICTION)
        entityManager.persist(LimitedAccessGenerator.EXCLUSION)
        entityManager.persist(LimitedAccessGenerator.BOTH_RESTRICTION)
        entityManager.persist(LimitedAccessGenerator.BOTH_EXCLUSION)

        entityManager.persist(ReferenceDataGenerator.REGISTER_TYPE_FLAG_DATASET)
        entityManager.persist(ReferenceDataGenerator.SAFEGUARDING_FLAG)
        entityManager.persist(ReferenceDataGenerator.INFORMATION_FLAG)
        entityManager.persist(ReferenceDataGenerator.REGISTER_LEVEL_DATASET)
        entityManager.persist(ReferenceDataGenerator.HIGH_RISK_REGISTER_LEVEL)
        entityManager.persist(ReferenceDataGenerator.APPOINTMENT_CONTACT_TYPE)
        entityManager.persist(ReferenceDataGenerator.APPOINTMENT_OUTCOME)
        entityManager.persist(RegistrationGenerator.SUICIDE_SELF_HARM_RISK_TYPE)
        entityManager.persist(RegistrationGenerator.CONTACT_SUSPENDED_TYPE)
        entityManager.flush()
        entityManager.persist(RegistrationGenerator.SUICIDE_SELF_HARM_REGISTRATION)
        entityManager.persist(PersonGenerator.PERSON_NO_REGISTRATIONS)
        entityManager.persist(OfficeLocationGenerator.DEFAULT)
        entityManager.persist(DocumentGenerator.DEFAULT_SUICIDE_RISK_FORM)
        entityManager.persist(DocumentGenerator.DELETED_SUICIDE_RISK_FORM)
        entityManager.persist(ContactGenerator.DEFAULT_CONTACT)
    }
}
