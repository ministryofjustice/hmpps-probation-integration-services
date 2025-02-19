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
class BreachNoticeLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        basicDetailsData()
        warningData()
        appointmentData()
        entityManager.persist(DocumentGenerator.DEFAULT_BREACH_NOTICE)
        lao()
    }

    private fun basicDetailsData() {
        entityManager.persistAll(
            PersonGenerator.DS_ADDRESS_TYPE,
            PersonGenerator.DEFAULT_ADDRESS_TYPE,
            TeamGenerator.DEFAULT_LOCATION,
            TeamGenerator.DEFAULT_TEAM,
            StaffGenerator.DEFAULT_STAFF,
            StaffGenerator.DEFAULT_SU,
            PersonGenerator.DEFAULT_PERSON,
            PersonGenerator.DEFAULT_ADDRESS,
            PersonGenerator.DEFAULT_PERSON_MANAGER,
        )
    }

    private fun warningData() {
        entityManager.persistAll(
            WarningGenerator.DS_BREACH_NOTICE_TYPE,
            *WarningGenerator.NOTICE_TYPES.toTypedArray(),
            WarningGenerator.DS_BREACH_REASON,
            *WarningGenerator.BREACH_REASONS.toTypedArray(),
            WarningGenerator.DS_BREACH_CONDITION_TYPE,
            *WarningGenerator.CONDITION_TYPES.toTypedArray(),
            WarningGenerator.DS_BREACH_SENTENCE_TYPE,
            *WarningGenerator.SENTENCE_TYPES.toTypedArray(),
            WarningGenerator.ENFORCEABLE_CONTACT_TYPE,
            WarningGenerator.ENFORCEABLE_CONTACT_OUTCOME,
            EventGenerator.DEFAULT_EVENT,
            EventGenerator.DEFAULT_DISPOSAL_TYPE,
            EventGenerator.DEFAULT_DISPOSAL,
            EventGenerator.DEFAULT_RQMNT_CATEGORY,
            EventGenerator.DS_REQUIREMENT_SUB_CATEOGORY,
            EventGenerator.DEFAULT_RQMNT_SUB_CATEGORY,
            EventGenerator.DEFAULT_RQMNT,
            WarningGenerator.DEFAULT_ENFORCEABLE_CONTACT,
        )
    }

    private fun appointmentData() {
        entityManager.persistAll(
            AppointmentGenerator.APPOINTMENT_CONTACT_TYPE,
            AppointmentGenerator.APPOINTMENT_OUTCOME,
            *AppointmentGenerator.OTHER_APPOINTMENTS.toTypedArray(),
            *AppointmentGenerator.FUTURE_APPOINTMENTS.toTypedArray(),
        )
    }

    private fun lao() {
        entityManager.persistAll(
            UserGenerator.LIMITED_ACCESS_USER,
            UserGenerator.NON_LAO_USER,
            PersonGenerator.EXCLUSION,
            PersonGenerator.RESTRICTION,
            PersonGenerator.RESTRICTION_EXCLUSION,
        )
        entityManager.flush()
        entityManager.persistAll(
            LimitedAccessGenerator.EXCLUSION,
            LimitedAccessGenerator.RESTRICTION,
            LimitedAccessGenerator.BOTH_EXCLUSION,
            LimitedAccessGenerator.BOTH_RESTRICTION
        )
    }

    private fun EntityManager.persistAll(vararg entities: Any) {
        entities.forEach { persist(it) }
    }
}
