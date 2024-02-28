package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val entityManager: EntityManager,
    private val auditUserRepository: AuditUserRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {

        entityManager.persist(PersonGenerator.OVERVIEW.gender)
        PersonGenerator.OVERVIEW.disabilities.forEach { entityManager.persist(it.type) }
        PersonGenerator.OVERVIEW.provisions.forEach { entityManager.persist(it.type) }
        PersonGenerator.OVERVIEW.personalCircumstances.forEach {
            entityManager.persist(it.type)
            entityManager.persist(it.subType)
        }

        entityManager.persistCollection(PersonGenerator.OVERVIEW.disabilities)
        entityManager.persistCollection(PersonGenerator.OVERVIEW.provisions)
        entityManager.persistCollection(PersonGenerator.OVERVIEW.personalCircumstances)
        entityManager.persist(PersonGenerator.OVERVIEW)

        entityManager.persist(PersonGenerator.EVENT_1)
        entityManager.persist(PersonGenerator.EVENT_2)
        entityManager.persist(PersonGenerator.INACTIVE_EVENT_1)
        entityManager.persist(PersonGenerator.INACTIVE_EVENT_2)

        entityManager.persistAll(
            PersonGenerator.DEFAULT_DISPOSAL_TYPE,
            PersonGenerator.ACTIVE_ORDER,
            PersonGenerator.INACTIVE_ORDER_1,
            PersonGenerator.INACTIVE_ORDER_2,
            ContactGenerator.APPT_CT_1,
            ContactGenerator.OTHER_CT,
            ContactGenerator.APPT_CT_2,
            ContactGenerator.APPT_CT_3,
            ContactGenerator.PREVIOUS_APPT_CONTACT,
            ContactGenerator.FIRST_NON_APPT_CONTACT,
            ContactGenerator.NEXT_APPT_CONTACT,
            ContactGenerator.FIRST_APPT_CONTACT,
            PersonGenerator.OFFENCE_1,
            PersonGenerator.MAIN_OFFENCE_1,
            PersonGenerator.OFFENCE_2,
            PersonGenerator.MAIN_OFFENCE_2,
            PersonGenerator.ADD_OFF_1,
            PersonGenerator.ADDITIONAL_OFFENCE_1,
            PersonGenerator.ADD_OFF_2,
            PersonGenerator.ADDITIONAL_OFFENCE_2,
            PersonGenerator.MAIN_CAT_F,
            PersonGenerator.REQUIREMENT,
            PersonGenerator.REQUIREMENT_CONTACT_1,
            PersonGenerator.REQUIREMENT_CONTACT_2,
        )
    }

    private fun EntityManager.persistAll(vararg entities: Any) {
        entities.forEach { persist(it) }
    }

    private fun EntityManager.persistCollection(entities: Collection<Any>) {
        entities.forEach { persist(it) }
    }
}
