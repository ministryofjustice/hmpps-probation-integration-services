package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
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

        entityManager.persistAll(
            ContactGenerator.DEFAULT_PROVIDER,
            ContactGenerator.DEFAULT_BOROUGH,
            ContactGenerator.DEFAULT_DISTRICT,
            ContactGenerator.DEFAULT_STAFF,
            ContactGenerator.LOCATION_BRK_1
        )

        entityManager.persist(PersonGenerator.OVERVIEW.gender)
        entityManager.persist(UserGenerator.USER)
        PersonGenerator.DISABILITIES.forEach { entityManager.persist(it.type) }
        PersonGenerator.PROVISIONS.forEach { entityManager.persist(it.type) }
        PersonGenerator.PERSONAL_CIRCUMSTANCES.forEach {
            entityManager.persist(it.type)
            entityManager.persist(it.subType)
        }

        entityManager.persistCollection(PersonGenerator.DISABILITIES)
        entityManager.persistCollection(PersonGenerator.PROVISIONS)
        entityManager.persistCollection(PersonGenerator.PERSONAL_CIRCUMSTANCES)
        entityManager.persist(PersonGenerator.OVERVIEW)

        entityManager.persist(CourtGenerator.BHAM)
        entityManager.persist(PersonGenerator.EVENT_1)
        entityManager.persist(PersonGenerator.EVENT_2)
        entityManager.persist(PersonGenerator.INACTIVE_EVENT_1)
        entityManager.persist(PersonGenerator.INACTIVE_EVENT_2)

        entityManager.persist(AdditionalSentenceGenerator.REF_DISQ)
        entityManager.persist(AdditionalSentenceGenerator.REF_FINE)
        entityManager.persist(
            AdditionalSentenceGenerator.generateSentence(
                3,
                null,
                null,
                PersonGenerator.EVENT_1,
                AdditionalSentenceGenerator.REF_DISQ
            )
        )
        entityManager.persist(CourtGenerator.DEFAULT)
        entityManager.persist(CourtAppearanceGenerator.generate())

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
            ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT,
            ContactGenerator.CONTACT_DOCUMENT_1,
            ContactGenerator.CONTACT_DOCUMENT_2,
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
            PersonGenerator.REGISTER_TYPE_1,
            PersonGenerator.REGISTER_TYPE_2,
            PersonGenerator.REGISRATION_1,
            PersonGenerator.REGISRATION_2
        )

        personalDetailsData()
    }

    fun personalDetailsData() {
        entityManager.persistAll(
            PersonDetailsGenerator.GENDER_FEMALE,
            PersonDetailsGenerator.RELIGION_DEFAULT,
            PersonDetailsGenerator.SEXUAL_ORIENTATION,
            PersonDetailsGenerator.LANGUAGE_RD,
            PersonDetailsGenerator.GENDER_IDENTITY_RD,
            PersonDetailsGenerator.PERSONAL_DETAILS,
            PersonDetailsGenerator.DISABILITY_1_RD,
            PersonDetailsGenerator.DISABILITY_2_RD,
            PersonDetailsGenerator.PERSONAL_CIRCUMSTANCE_1_RD,
            PersonDetailsGenerator.PERSONAL_CIRCUMSTANCE_SUBTYPE_1,
            PersonDetailsGenerator.PERSONAL_CIRCUMSTANCE_2_RD,
            PersonDetailsGenerator.PERSONAL_CIRCUMSTANCE_SUBTYPE_2,
            PersonDetailsGenerator.PROVISION_1_RD,
            PersonDetailsGenerator.PROVISION_2_RD,
            PersonDetailsGenerator.DISABILITY_1,
            PersonDetailsGenerator.DISABILITY_2,
            PersonDetailsGenerator.PROVISION_1,
            PersonDetailsGenerator.PROVISION_2,
            PersonDetailsGenerator.PERSONAL_CIRC_1,
            PersonDetailsGenerator.PERSONAL_CIRC_2,
            PersonDetailsGenerator.PERSONAL_CIRC_PREV,
            PersonDetailsGenerator.RELATIONSHIP_TYPE,
            PersonDetailsGenerator.CONTACT_ADDRESS,
            PersonDetailsGenerator.PERSONAL_CONTACT_1,
            PersonDetailsGenerator.PERSON_ADDRESS_STATUS_1,
            PersonDetailsGenerator.PERSON_ADDRESS_TYPE_1,
            PersonDetailsGenerator.PERSON_ADDRESS_1,
            PersonDetailsGenerator.PERSON_ADDRESS_STATUS_2,
            PersonDetailsGenerator.PERSON_ADDRESS_TYPE_2,
            PersonDetailsGenerator.PERSON_ADDRESS_2,
            PersonDetailsGenerator.NULL_ADDRESS,
            PersonDetailsGenerator.PREVIOUS_ADDRESS,
            PersonDetailsGenerator.DOCUMENT_1,
            PersonDetailsGenerator.DOCUMENT_2,
            PersonDetailsGenerator.ALIAS_1,
            PersonDetailsGenerator.ALIAS_2
        )
    }

    private fun EntityManager.persistAll(vararg entities: Any) {
        entities.forEach { persist(it) }
    }

    private fun EntityManager.persistCollection(entities: Collection<Any>) {
        entities.forEach { persist(it) }
    }
}
