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
    private val entityManager: EntityManager,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        entityManager.persistAll(
            LimitedAccessUserGenerator.EXCLUSION_USER,
            LimitedAccessUserGenerator.RESTRICTION_USER,
            LimitedAccessUserGenerator.RESTRICTION_AND_EXCLUSION_USER
        )
        entityManager.persistAll(
            PersonGenerator.GENDER_MALE,
            PersonGenerator.ETHNICITY,
            PersonGenerator.PERSON_1,
            PersonGenerator.PERSON_2,
            PersonGenerator.EXCLUSION,
            PersonGenerator.RESTRICTION,
            PersonGenerator.RESTRICTION_EXCLUSION,
        )
        entityManager.flush()
        entityManager.persistAll(
            LimitedAccessGenerator.EXCLUSION,
            LimitedAccessGenerator.RESTRICTION,
            LimitedAccessGenerator.BOTH_EXCLUSION,
            LimitedAccessGenerator.BOTH_RESTRICTION,
        )
        entityManager.persistAll(
            PersonManagerGenerator.DEFAULT_PROVIDER,
            PersonManagerGenerator.PROVIDER_1,
            PersonManagerGenerator.PROVIDER_2
        )
        entityManager.flush()
        entityManager.persistAll(
            PersonManagerGenerator.DEFAULT_BOROUGH,
            PersonManagerGenerator.BOROUGH_1,
            PersonManagerGenerator.BOROUGH_2,
            PersonManagerGenerator.DEFAULT_DISTRICT,
            PersonManagerGenerator.DISTRICT_1,
            PersonManagerGenerator.DISTRICT_2,
            PersonManagerGenerator.DEFAULT_TEAM,
            PersonManagerGenerator.TEAM_1,
            PersonManagerGenerator.TEAM_2,
            PersonManagerGenerator.PERSON_MANAGER,
        )
        entityManager.flush()
        entityManager.persistAll(
            OfficeLocationGenerator.LOCATION_1,
            OfficeLocationGenerator.LOCATION_2,
            OfficeLocationGenerator.TEAM_OFFICE_1,
            OfficeLocationGenerator.TEAM_OFFICE_2,
        )
        entityManager.persistAll(
            EventGenerator.CUSTODIAL_STATUS,
            EventGenerator.EVENT,
            EventGenerator.DISPOSAL,
            EventGenerator.CUSTODY,
        )
        entityManager.persistAll(
            RequirementGenerator.RMC38,
            RequirementGenerator.RMC_7,
            RequirementGenerator.RMC_OTHER,
            RequirementGenerator.SUB_CAT,
            RequirementGenerator.TERMINATION_DETAILS,
            RequirementGenerator.AMC_RMC38,
            RequirementGenerator.AMC_7,
            RequirementGenerator.ACC_PROG_1,
            RequirementGenerator.ACC_PROG_2,
            RequirementGenerator.ACC_PROG_3,
            RequirementGenerator.ACC_PROG_4,
            RequirementGenerator.ACC_PROG_5,
            RequirementGenerator.ACC_PROG_6,
        )
    }

    private fun EntityManager.persistAll(vararg entities: Any) {
        entities.forEach { persist(it) }
    }
}
