package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.ProbationAreaUser
import uk.gov.justice.digital.hmpps.entity.ProbationAreaUserId
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

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
        with(entityManager) {
            persist(StaffGenerator.TEST_STAFF)
            persist(UserGenerator.TEST_USER)
            persist(ProviderGenerator.LONDON)
            persist(ProviderGenerator.WALES)
            persist(ProbationAreaUser(ProbationAreaUserId(UserGenerator.TEST_USER, ProviderGenerator.LONDON)))
            savePerson(PersonGenerator.PERSON1)
            savePerson(PersonGenerator.PERSON2)
            savePerson(PersonGenerator.PERSON3)
            savePerson(PersonGenerator.PERSON4)
            savePerson(PersonGenerator.PERSON5)
            savePerson(PersonGenerator.PERSON6)
        }
    }

    private fun savePerson(person: Person) {
        with(entityManager) {
            persist(person)
            persist(person.manager?.team?.localAdminUnit?.probationDeliveryUnit?.provider)
            persist(person.manager?.team?.localAdminUnit?.probationDeliveryUnit)
            persist(person.manager?.team?.localAdminUnit)
            persist(person.manager?.team)
            persist(person.manager)
            person.events.forEach { persist(it) }
        }
    }
}
