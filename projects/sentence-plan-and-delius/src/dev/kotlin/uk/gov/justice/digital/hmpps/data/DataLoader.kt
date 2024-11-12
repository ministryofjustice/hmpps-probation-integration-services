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
            ReferenceDataGenerator.TIER_1,
            ReferenceDataGenerator.TC_STATUS_CUSTODY,
            ReferenceDataGenerator.TC_STATUS_NO_CUSTODY,
            ProviderGenerator.DEFAULT_AREA,
            ProviderGenerator.DEFAULT_TEAM,
            ProviderGenerator.DEFAULT_STAFF,
            PersonGenerator.DEFAULT,
            PersonManagerGenerator.DEFAULT,
            EventGenerator.DISPOSAL_TYPE,
            EventGenerator.REQUIREMENT_CAT_F,
            EventGenerator.REQUIREMENT_CAT_W,
            EventGenerator.DEFAULT_EVENT,
            EventGenerator.DEFAULT_DISPOSAL,
            EventGenerator.DEFAULT_CUSTODY,
            PersonGenerator.NON_CUSTODIAL,
            PersonManagerGenerator.NON_CUSTODIAL_MANAGER,
            EventGenerator.NON_CUSTODIAL_EVENT,
            EventGenerator.NON_CUSTODIAL_DISPOSAL,
            EventGenerator.NON_CUSTODIAL_CUSTODY,
            EventGenerator.FIRST_APPT_CT,
            EventGenerator.FIRST_APPT_CONTACT,
            EventGenerator.REQUIREMENT_1,
            EventGenerator.REQUIREMENT_2,
            EventGenerator.UPW_DETAILS_1,
            EventGenerator.UPW_DETAILS_2,
            EventGenerator.UPW_APPOINTMENT_1,
            EventGenerator.UPW_APPOINTMENT_2,
            EventGenerator.RAR_CONTACT_1,
            EventGenerator.RAR_CONTACT_2
        )
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
}
