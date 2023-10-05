package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
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
        entityManager.persist(ProviderGenerator.DEFAULT_PROVIDER)
        entityManager.persist(StaffGenerator.PDUHEAD)
        entityManager.persist(StaffGenerator.DEFAULT_PDUSTAFF_USER)
        entityManager.persist(ProviderGenerator.DEFAULT_BOROUGH)
        entityManager.persist(ProviderGenerator.DEFAULT_DISTRICT)
        entityManager.persist(ProviderGenerator.DEFAULT_TEAM)

        StaffGenerator.DEFAULT = StaffGenerator.generateStaff(
            StaffGenerator.DEFAULT.code,
            StaffGenerator.DEFAULT.forename,
            StaffGenerator.DEFAULT.surname,
            listOf(ProviderGenerator.DEFAULT_TEAM),
            StaffGenerator.DEFAULT.middleName,
            StaffGenerator.DEFAULT.user,
            StaffGenerator.DEFAULT.id
        )
        entityManager.persist(StaffGenerator.DEFAULT)

        entityManager.persist(StaffGenerator.DEFAULT_STAFF_USER)

        entityManager.persist(PersonGenerator.DEFAULT_PERSON)
        entityManager.persist(PersonGenerator.DEFAULT_CM)

        val person = PersonGenerator.generatePerson("N123456").also(entityManager::persist)
        PersonGenerator.generateManager(person).also(entityManager::persist)
        listOf(
            AddressGenerator.ADDRESS_STATUS_MAIN,
            AddressGenerator.ADDRESS_STATUS_PREVIOUS,
            AddressGenerator.ADDRESS_STATUS_OTHER,
            AddressGenerator.ADDRESS_MAIN,
            AddressGenerator.ADDRESS_PREVIOUS,
            AddressGenerator.ADDRESS_OTHER,
            AddressGenerator.ADDRESS_DELETED
        ).forEach(entityManager::persist)
    }
}
