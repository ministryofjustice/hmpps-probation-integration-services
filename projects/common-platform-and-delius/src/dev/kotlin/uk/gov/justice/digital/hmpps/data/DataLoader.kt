package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(BusinessInteractionGenerator.INSERT_PERSON)
        save(BusinessInteractionGenerator.INSERT_ADDRESS)
        save(BusinessInteractionGenerator.INSERT_EVENT)
        save(BusinessInteractionGenerator.INSERT_COURT_APPEARANCE)
        save(DatasetGenerator.GENDER)
        save(DatasetGenerator.OM_ALLOCATION_REASON)
        save(DatasetGenerator.ADDRESS_STATUS)
        save(DatasetGenerator.ADDRESS_TYPE)
        save(DatasetGenerator.ORDER_ALLOCATION_REASON)
        save(DatasetGenerator.COURT_APPEARANCE_TYPE)
        save(DatasetGenerator.REMAND_STATUS)
        save(DatasetGenerator.PLEA)
        save(DatasetGenerator.COURT_APPEARANCE_OUTCOME)
        save(DatasetGenerator.NATIONALITY)
        save(DatasetGenerator.ETHNICITY)
        save(ReferenceDataGenerator.GENDER_MALE)
        save(ReferenceDataGenerator.GENDER_FEMALE)
        save(ReferenceDataGenerator.INITIAL_ALLOCATION)
        save(ReferenceDataGenerator.MAIN_ADDRESS_STATUS)
        save(ReferenceDataGenerator.AWAITING_ASSESSMENT)
        save(ReferenceDataGenerator.ORDER_MANAGER_INITIAL_ALLOCATION)
        save(ReferenceDataGenerator.GUILTY_PLEA)
        save(ReferenceDataGenerator.TRIAL_ADJOURNMENT_APPEARANCE_TYPE)
        save(ReferenceDataGenerator.REMANDED_IN_CUSTODY_OUTCOME)
        save(ReferenceDataGenerator.REMANDED_IN_CUSTODY_STATUS)
        save(ReferenceDataGenerator.BRITISH_NATIONALITY)
        save(ReferenceDataGenerator.FRENCH_NATIONALITY)
        save(ReferenceDataGenerator.WHITE_BRITISH_ETHNICITY)
        save(TransferReasonGenerator.CASE_ORDER)
        save(ProviderGenerator.DEFAULT)
        save(TeamGenerator.ALLOCATED)
        save(TeamGenerator.UNALLOCATED)
        save(StaffGenerator.UNALLOCATED)
        save(StaffGenerator.ALLOCATED)
        save(CourtGenerator.UNKNOWN_COURT_N07_PROVIDER)
        save(OffenceGenerator.DEFAULT)
        save(OffenceGenerator.SECOND_OFFENCE)
        save(OffenceGenerator.THIRD_OFFENCE)
        save(ContactTypeGenerator.EAPP)
        save(DetailedOffenceGenerator.DEFAULT)
        save(DetailedOffenceGenerator.SECOND_OFFENCE)
        save(DetailedOffenceGenerator.THIRD_OFFENCE)
    }
}
