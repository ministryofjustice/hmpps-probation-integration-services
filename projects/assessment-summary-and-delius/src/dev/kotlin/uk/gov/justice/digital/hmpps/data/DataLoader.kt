package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
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
        saveAll(*ReferenceDataGenerator.BUSINESS_INTERACTIONS.toTypedArray())
        saveAll(*ReferenceDataGenerator.COURTS.toTypedArray())
        saveAll(*ReferenceDataGenerator.OFFENCES.toTypedArray())
        saveAll(ReferenceDataGenerator.FLAG_DATASET, ReferenceDataGenerator.DEFAULT_FLAG)
        saveAll(*ContactGenerator.TYPES.values.toTypedArray())
        saveAll(*RegistrationGenerator.TYPES.values.toTypedArray())
        saveAll(*ReferenceDataGenerator.REQ_MAIN_CATS.toTypedArray())
        saveAll(
            ReferenceDataGenerator.DOMAIN_EVENT_TYPE_DATASET,
            *ReferenceDataGenerator.DOMAIN_EVENT_TYPES.toTypedArray()
        )

        PersonGenerator.NO_RISK.withEvent().withRisk(Risk.M, Risk.L)
        PersonGenerator.LOW_RISK
            .withEvent()
            .withRisk(Risk.H)
            .withAssessment("10096930")
            .withAccreditedProgramRequirement()
        PersonGenerator.MEDIUM_RISK.withEvent().withRisk(Risk.M)
        PersonGenerator.HIGH_RISK.withEvent().withRisk(Risk.L, Risk.H)
        PersonGenerator.VERY_HIGH_RISK.withEvent().withRisk(Risk.L, Risk.M, Risk.H)
    }

    private fun Person.withEvent(): Person {
        val personManager = PersonGenerator.generateManager(this)
        val event = PersonGenerator.generateEvent(this)
        saveAll(this, personManager, event)
        this.set(Person::manager, personManager)
        return this
    }

    private fun Person.withAssessment(oasysId: String): Person {
        val contact =
            entityManager.merge(
                ContactGenerator.generateContact(
                    this,
                    ContactGenerator.TYPES[ContactType.Code.OASYS_ASSESSMENT.value]!!
                )
            )
        val assessment = AssessmentGenerator.generate(this, contact, LocalDate.parse("2013-06-07"), oasysId = oasysId)
        entityManager.merge(assessment)
        return this
    }

    private fun Person.withRisk(vararg risks: Risk): Person {
        risks.forEach {
            val type = RegistrationGenerator.TYPES[it.code]
            val contact = entityManager.merge(ContactGenerator.generateContact(this, type!!.registrationContactType!!))
            val registration = RegistrationGenerator.generate(this.id, LocalDate.parse("2023-06-14"), type, contact)
            val reviewContact = entityManager.merge(ContactGenerator.generateContact(this, type.reviewContactType!!))
            highestRiskColour = type.colour
            saveAll(this, registration.withReview(reviewContact))
        }
        return this
    }

    private fun Person.withAccreditedProgramRequirement(): Person {
        saveAll(PersonGenerator.generateRequirement(this))
        return this
    }

    private fun saveAll(vararg entities: Any) = entities.forEach(entityManager::merge)
}
