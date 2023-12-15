package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.service.Risk
import uk.gov.justice.digital.hmpps.set
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate

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
        saveAll(*ReferenceDataGenerator.COURTS.toTypedArray())
        saveAll(*ReferenceDataGenerator.OFFENCES.toTypedArray())
        saveAll(ReferenceDataGenerator.FLAG_DATASET, ReferenceDataGenerator.DEFAULT_FLAG)
        saveAll(*ContactGenerator.TYPES.values.toTypedArray())
        saveAll(*RegistrationGenerator.TYPES.values.toTypedArray())

        personWithEvent(PersonGenerator.NO_RISK).withRisk(Risk.M, Risk.L)
        personWithEvent(PersonGenerator.LOW_RISK).withRisk(Risk.H)
        personWithEvent(PersonGenerator.MEDIUM_RISK).withRisk(Risk.M)
        personWithEvent(PersonGenerator.HIGH_RISK).withRisk(Risk.L, Risk.H)
        personWithEvent(PersonGenerator.VERY_HIGH_RISK).withRisk(Risk.L, Risk.M, Risk.H)
    }

    private fun personWithEvent(person: Person): Person {
        val personManager = PersonGenerator.generateManager(person)
        val event = PersonGenerator.generateEvent(person)
        saveAll(person, personManager, event)
        person.set(Person::manager, personManager)
        return person
    }

    private fun Person.withRisk(vararg risks: Risk) {
        risks.forEach {
            val type = RegistrationGenerator.TYPES[it.code]
            val contact = ContactGenerator.generateContact(this, type!!.registrationContactType!!)
            val registration = RegistrationGenerator.generate(this.id, LocalDate.parse("2023-06-14"), type, contact)
            val reviewContact = ContactGenerator.generateContact(this, type.reviewContactType!!)
            saveAll(contact, registration.withReview(reviewContact))
        }
    }

    private fun saveAll(vararg entities: Any) = entities.forEach(entityManager::merge)
}
