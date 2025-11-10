package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactTypeOutcome
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

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
        loadUsers()
        loadPeople()
        loadProviders()
        loadTeams()
        loadStaff()
        loadReferenceData()
        loadUnpaidWorkData()
    }

    fun loadUsers() {
        entityManager.persist(UserGenerator.DEFAULT_USER)
    }

    fun loadPeople() {
        entityManager.persist(PersonGenerator.DEFAULT_PERSON)
        entityManager.persist(PersonGenerator.SECOND_PERSON)
    }

    fun loadProviders() {
        entityManager.persist(ProviderGenerator.DEFAULT_PROVIDER)
        entityManager.persist(ProviderGenerator.SECOND_PROVIDER)
        entityManager.persist(ProviderGenerator.UNSELECTABLE_PROVIDER)

        entityManager.persist(ProviderGenerator.DEFAULT_PROBATION_AREA_USER)
        entityManager.persist(ProviderGenerator.SECOND_DEFAULT_PROBATION_AREA_USER)
        entityManager.persist(ProviderGenerator.DEFAULT_USER_UNSELECTABLE_PROBATION_AREA)
    }

    fun loadTeams() {
        entityManager.persist(TeamGenerator.DEFAULT_UPW_TEAM)
        entityManager.persist(TeamGenerator.SECOND_UPW_TEAM)
        entityManager.persist(TeamGenerator.NON_UPW_TEAM)
        entityManager.persist(TeamGenerator.END_DATED_TEAM)
        entityManager.persist(TeamGenerator.OTHER_PROVIDER_TEAM)
    }

    fun loadStaff() {
        entityManager.persist(StaffGenerator.DEFAULT_STAFF)
        entityManager.persist(StaffGenerator.SECOND_STAFF)
    }

    fun loadReferenceData() {
        entityManager.persist(DatasetGenerator.UPW_PROJECT_TYPE_DATASET)
        entityManager.persist(DatasetGenerator.UPW_WORK_QUALITY_DATASET)
        entityManager.persist(DatasetGenerator.UPW_BEHAVIOUR_DATASET)
        entityManager.persist(ReferenceDataGenerator.GROUP_PLACEMENT_PROJECT_TYPE)
        entityManager.persist(ReferenceDataGenerator.INDIVIDUAL_PLACEMENT_PROJECT_TYPE)
        entityManager.persist(ReferenceDataGenerator.INACTIVE_PROJECT_TYPE)
        entityManager.persist(ReferenceDataGenerator.DEFAULT_ENFORCEMENT_ACTION)
        entityManager.persist(ReferenceDataGenerator.UPW_APPOINTMENT_TYPE)
        entityManager.persist(ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME)
        entityManager.persist(ReferenceDataGenerator.FAILED_TO_ATTEND_CONTACT_OUTCOME)
        entityManager.persist(
            ContactTypeOutcome(
                ReferenceDataGenerator.UPW_APPOINTMENT_TYPE,
                ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME
            )
        )
        entityManager.persist(ReferenceDataGenerator.EXCELLENT_WORK_QUALITY)
        entityManager.persist(ReferenceDataGenerator.UNSATISFACTORY_WORK_QUALITY)
        entityManager.persist(ReferenceDataGenerator.EXCELLENT_BEHAVIOUR)
        entityManager.persist(ReferenceDataGenerator.UNSATISFACTORY_BEHAVIOUR)
        entityManager.persist(ReferenceDataGenerator.UPW_RQMNT_MAIN_CATEGORY)
        entityManager.persist(ReferenceDataGenerator.DEFAULT_DISPOSAL_TYPE)
    }

    fun loadUnpaidWorkData() {
        entityManager.persist(UPWGenerator.DEFAULT_ADDRESS)
        entityManager.persist(UPWGenerator.DEFAULT_OFFICE_LOCATION)
        entityManager.persist(UPWGenerator.DEFAULT_UPW_PROJECT)
        entityManager.persist(UPWGenerator.SECOND_UPW_PROJECT)
        entityManager.persist(UPWGenerator.DEFAULT_UPW_PROJECT_AVAILABILITY)
        entityManager.persist(UPWGenerator.SECOND_UPW_PROJECT_AVAILABILITY)
        entityManager.persist(UPWGenerator.DEFAULT_DISPOSAL)
        entityManager.persist(UPWGenerator.SECOND_DISPOSAL)
        entityManager.persist(UPWGenerator.DEFAULT_UPW_DETAILS)
        entityManager.persist(UPWGenerator.SECOND_UPW_DETAILS)
        entityManager.persist(UPWGenerator.THIRD_UPW_DETAILS)
        entityManager.persist(UPWGenerator.DEFAULT_CONTACT)
        entityManager.persist(UPWGenerator.CONTACT_NO_ENFORCEMENT)
        entityManager.persist(UPWGenerator.DEFAULT_UPW_APPOINTMENT)
        entityManager.persist(UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT)
        entityManager.persist(UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME)
        entityManager.persist(UPWGenerator.SECOND_UPW_APPOINTMENT_OUTCOME_NO_ENFORCEMENT)
        entityManager.persist(UPWGenerator.DEFAULT_RQMNT)
    }
}
