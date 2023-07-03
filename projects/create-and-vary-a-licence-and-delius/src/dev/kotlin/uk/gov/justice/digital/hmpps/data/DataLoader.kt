package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.ZonedDateTime

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
        entityManager.persist(ProviderGenerator.DEFAULT_BOROUGH)
        entityManager.persist(ProviderGenerator.DEFAULT_DISTRICT)
        entityManager.persist(ProviderGenerator.DEFAULT_TEAM)

        entityManager.persist(StaffGenerator.DEFAULT)
        entityManager.persist(StaffGenerator.DEFAULT_STAFF_USER)

        entityManager.persist(PersonGenerator.DEFAULT_PERSON)
        entityManager.persist(PersonGenerator.DEFAULT_CM)
        entityManager.persist(PersonGenerator.DEFAULT_RO)

        val person = PersonGenerator.generatePerson("N123456")
        val cm = PersonGenerator.generateManager()
        entityManager.persist(person)
        entityManager.persist(cm)
        entityManager.persist(
            PersonGenerator.generateResponsibleOfficer(
                person,
                cm,
                endDate = ZonedDateTime.now().minusMinutes(20)
            )
        )
    }
}
