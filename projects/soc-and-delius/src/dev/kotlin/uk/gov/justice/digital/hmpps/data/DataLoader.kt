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
            ProbationAreaGenerator.DEFAULT_PA,
            ProbationAreaGenerator.DEFAULT_BOROUGH,
            ProbationAreaGenerator.DEFAULT_LDU,
            ProbationAreaGenerator.DEFAULT_LDU2,
            ProbationAreaGenerator.NON_SELECTABLE_PA,
            ProbationAreaGenerator.NON_SELECTABLE_BOROUGH,
            ProbationAreaGenerator.NON_SELECTABLE_LDU,
            CourtAppearanceGenerator.DEFAULT_OUTCOME,
            CourtAppearanceGenerator.DEFAULT_CA_TYPE,
            CourtAppearanceGenerator.DEFAULT_COURT,
            CourtAppearanceGenerator.DEFAULT_PERSON,
            CourtAppearanceGenerator.DEFAULT_EVENT,
            CourtAppearanceGenerator.DEFAULT_CA,
            ConvictionEventGenerator.PERSON,
            ConvictionEventGenerator.ADDITIONAL_OFFENCE_TYPE,
            ConvictionEventGenerator.OFFENCE_MAIN_TYPE,
            ConvictionEventGenerator.DEFAULT_EVENT,
            ConvictionEventGenerator.INACTIVE_EVENT,
            ConvictionEventGenerator.MAIN_OFFENCE,
            ConvictionEventGenerator.OTHER_OFFENCE,
            ConvictionEventGenerator.DISPOSAL_TYPE,
            ConvictionEventGenerator.DISPOSAL,
            ConvictionEventGenerator.COURT_APPEARANCE,
            DetailsGenerator.INSTITUTION,
            DetailsGenerator.RELIGION,
            DetailsGenerator.NATIONALITY,
            DetailsGenerator.PERSON,
            DetailsGenerator.DEFAULT_PA,
            DetailsGenerator.DISTRICT,
            DetailsGenerator.TEAM,
            DetailsGenerator.STAFF,
            DetailsGenerator.PERSON_MANAGER,
            DetailsGenerator.RELEASE_TYPE,
            DetailsGenerator.RELEASE,
            DetailsGenerator.RECALL_REASON,
            DetailsGenerator.RECALL,
            NSIGenerator.BREACH_TYPE,
            NSIGenerator.RECALL_TYPE,
            NSIGenerator.BREACH_NSI,
            NSIGenerator.RECALL_NSI,
            ConvictionEventGenerator.PERSON_2,
            ConvictionEventGenerator.EVENT_2,
            ConvictionEventGenerator.MAIN_OFFENCE_2,
            ConvictionEventGenerator.OTHER_OFFENCE_2,
            ConvictionEventGenerator.DISPOSAL_2,
            KeyDateGenerator.CUSTODY_STATUS,
            KeyDateGenerator.SED_KEYDATE,
            KeyDateGenerator.CUSTODY,
            KeyDateGenerator.KEYDATE,
            KeyDateGenerator.CUSTODY_1,
            KeyDateGenerator.KEYDATE_1
        )

        em.createNativeQuery(
            """
             update event set offender_id = ${DetailsGenerator.PERSON.id} 
             where event_id = ${ConvictionEventGenerator.EVENT_2.id}
            """.trimMargin()
        )
            .executeUpdate()
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
}
