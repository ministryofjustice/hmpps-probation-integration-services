package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactTypeOutcome

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        loadProviders()
        loadTeams()
        loadStaff()
        loadUsers()
        loadProbationAreaUsers()
        loadPeople()
        loadReferenceData()
        loadUnpaidWorkData()
        loadLimitedAccessData()
    }

    fun loadUsers() {
        save(UserGenerator.DEFAULT_USER)
    }

    fun loadPeople() {
        save(PersonGenerator.DEFAULT_PERSON)
        save(PersonGenerator.SECOND_PERSON)
        save(PersonGenerator.EXCLUDED_PERSON)
        save(PersonGenerator.RESTRICTED_PERSON)
    }

    fun loadProviders() {
        save(ProviderGenerator.DEFAULT_PROVIDER)
        save(ProviderGenerator.SECOND_PROVIDER)
        save(ProviderGenerator.UNSELECTABLE_PROVIDER)
    }

    fun loadProbationAreaUsers() {
        save(ProbationAreaUserGenerator.DEFAULT_PROBATION_AREA_USER)
        save(ProbationAreaUserGenerator.SECOND_DEFAULT_PROBATION_AREA_USER)
        save(ProbationAreaUserGenerator.DEFAULT_USER_UNSELECTABLE_PROBATION_AREA)
    }

    fun loadTeams() {
        save(TeamGenerator.DEFAULT_UPW_TEAM)
        save(TeamGenerator.SECOND_UPW_TEAM)
        save(TeamGenerator.NON_UPW_TEAM)
        save(TeamGenerator.END_DATED_TEAM)
        save(TeamGenerator.OTHER_PROVIDER_TEAM)
    }

    fun loadStaff() {
        save(StaffGenerator.DEFAULT_STAFF)
        save(StaffGenerator.SECOND_STAFF)
        save(PersonGenerator.DEFAULT_PERSON_MANAGER)
    }

    fun loadReferenceData() {
        save(DatasetGenerator.UPW_PROJECT_TYPE_DATASET)
        save(DatasetGenerator.UPW_WORK_QUALITY_DATASET)
        save(DatasetGenerator.UPW_BEHAVIOUR_DATASET)
        save(DatasetGenerator.NON_WORKING_DAYS_DATASET)
        save(DatasetGenerator.UPW_FREQUENCY_DATASET)
        save(ReferenceDataGenerator.GROUP_PLACEMENT_PROJECT_TYPE)
        save(ReferenceDataGenerator.INDIVIDUAL_PLACEMENT_PROJECT_TYPE)
        save(ReferenceDataGenerator.INACTIVE_PROJECT_TYPE)
        save(ReferenceDataGenerator.UPW_APPOINTMENT_TYPE)
        save(ReferenceDataGenerator.REVIEW_ENFORCEMENT_STATUS_TYPE)
        save(ReferenceDataGenerator.ROM_ENFORCEMENT_ACTION)
        save(ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME)
        save(ReferenceDataGenerator.FAILED_TO_ATTEND_CONTACT_OUTCOME)
        save(
            ContactTypeOutcome(
                ReferenceDataGenerator.UPW_APPOINTMENT_TYPE,
                ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME
            )
        )
        save(ReferenceDataGenerator.EXCELLENT_WORK_QUALITY)
        save(ReferenceDataGenerator.UNSATISFACTORY_WORK_QUALITY)
        save(ReferenceDataGenerator.EXCELLENT_BEHAVIOUR)
        save(ReferenceDataGenerator.UNSATISFACTORY_BEHAVIOUR)
        save(ReferenceDataGenerator.UPW_RQMNT_MAIN_CATEGORY)
        save(ReferenceDataGenerator.DEFAULT_DISPOSAL_TYPE)
        save(ReferenceDataGenerator.NON_WORKING_DAY_CHRISTMAS)
        save(ReferenceDataGenerator.NON_WORKING_DAY_NEW_YEAR)
        save(ReferenceDataGenerator.UPW_DAY_MONDAY)
        save(ReferenceDataGenerator.UPW_FREQUENCY_WEEKLY)
    }

    fun loadUnpaidWorkData() {
        save(UPWGenerator.DEFAULT_ADDRESS)
        save(UPWGenerator.DEFAULT_OFFICE_LOCATION)
        save(UPWGenerator.DEFAULT_UPW_PROJECT)
        save(UPWGenerator.SECOND_UPW_PROJECT)
        save(UPWGenerator.DEFAULT_UPW_PROJECT_AVAILABILITY)
        save(UPWGenerator.SECOND_UPW_PROJECT_AVAILABILITY)
        save(UPWGenerator.DEFAULT_EVENT)
        save(UPWGenerator.SECOND_EVENT)
        save(UPWGenerator.DEFAULT_DISPOSAL)
        save(UPWGenerator.SECOND_DISPOSAL)
        save(UPWGenerator.DEFAULT_UPW_DETAILS)
        save(UPWGenerator.SECOND_UPW_DETAILS)
        save(UPWGenerator.THIRD_UPW_DETAILS)
        save(UPWGenerator.DEFAULT_UPW_ALLOCATION)
        save(UPWGenerator.DEFAULT_CONTACT)
        save(UPWGenerator.CONTACT_NO_ENFORCEMENT)
        save(UPWGenerator.DEFAULT_UPW_APPOINTMENT)
        save(UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT)
        save(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE)
        save(UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME)
        save(UPWGenerator.SECOND_UPW_APPOINTMENT_OUTCOME_NO_ENFORCEMENT)
        save(UPWGenerator.DEFAULT_RQMNT)
        save(UPWGenerator.SECOND_RQMNT)
        save(UPWGenerator.LAO_EXCLUDED_UPW_APPOINTMENT)
        save(UPWGenerator.LAO_RESTRICTED_UPW_APPOINTMENT)
        save(UPWGenerator.SECOND_UPW_DETAILS_ADJUSTMENT_NEGATIVE)
        save(UPWGenerator.SECOND_UPW_DETAILS_ADJUSTMENT_POSITIVE)
    }

    fun loadLimitedAccessData() {
        save(LimitedAccessGenerator.FULL_ACCESS_USER)
        save(LimitedAccessGenerator.LIMITED_ACCESS_USER)
        save(LimitedAccessGenerator.EXCLUDED_CASE)
        save(LimitedAccessGenerator.RESTRICTED_CASE)
        save(
            LimitedAccessGenerator.generateExclusion(
                LimitedAccessGenerator.EXCLUDED_CASE
            )
        )
        save(
            LimitedAccessGenerator.generateRestriction(
                LimitedAccessGenerator.RESTRICTED_CASE
            )
        )
    }
}
