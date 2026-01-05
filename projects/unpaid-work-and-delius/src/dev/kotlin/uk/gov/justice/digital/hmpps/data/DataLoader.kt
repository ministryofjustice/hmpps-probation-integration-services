package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateExclusion
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateRestriction
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(UserGenerator.LIMITED_ACCESS_USER)
        saveAll(
            listOf(
                DatasetGenerator.GENDER,
                DatasetGenerator.ETHNICITY,
                DatasetGenerator.DISABILITY,
                DatasetGenerator.DISABILITY_CONDITION,
                DatasetGenerator.LANGUAGE,
                DatasetGenerator.REGISTER_LEVEL,
                DatasetGenerator.REGISTER_CATEGORY,
                DatasetGenerator.DISABILITY_PROVISION,
                DatasetGenerator.DISABILITY_PROVISION_CATEGORY,
                DatasetGenerator.RELATIONSHIP,
                DatasetGenerator.ADDRESS_STATUS
            )
        )
        saveAll(
            listOf(BusinessInteractionGenerator.UPLOAD_DOCUMENT)
        )

        saveAll(
            listOf(
                ReferenceDataGenerator.GENDER_MALE,
                ReferenceDataGenerator.ETHNICITY_INDIAN,
                ReferenceDataGenerator.DISABILITY_HEARING,
                ReferenceDataGenerator.DISABILITY_HEARING_CONDITION,
                ReferenceDataGenerator.LANGUAGE_ENGLISH,
                ReferenceDataGenerator.MAPPA_LEVEL_1,
                ReferenceDataGenerator.MAPPA_CATEGORY_2,
                ReferenceDataGenerator.HEARING_PROVISION,
                ReferenceDataGenerator.HEARING_PROVISION_CATEGORY,
                ReferenceDataGenerator.DOCTOR_RELATIONSHIP,
                ReferenceDataGenerator.MAIN_ADDRESS

            )
        )
        save(ContactTypeGenerator.DEFAULT)
        save(StaffGenerator.DEFAULT)
        save(TeamGenerator.DEFAULT)
        save(OffenceGenerator.DEFAULT)
        save(PersonalCircumstanceTypeGenerator.DEFAULT)
        save(PersonalCircumstanceSubTypeGenerator.DEFAULT)
        save(CaseGenerator.DEFAULT)
        save(CaseGenerator.EXCLUSION)
        save(CaseGenerator.RESTRICTION)
        save(CaseGenerator.RESTRICTION_EXCLUSION)
        save(AliasGenerator.DEFAULT)
        save(PersonalCircumstanceGenerator.DEFAULT)
        save(AddressGenerator.DEFAULT)
        save(PersonalContactGenerator.DEFAULT)
        save(CaseAddressGenerator.DEFAULT)
        save(DisabilityGenerator.DEFAULT)
        save(ProvisionGenerator.DEFAULT)
        save(RegisterTypeGenerator.DEFAULT)
        save(RegistrationGenerator.DEFAULT)
        save(EventGenerator.DEFAULT)
        save(DisposalGenerator.DEFAULT)
        save(MainOffenceGenerator.DEFAULT)
        save(PersonManagerGenerator.DEFAULT)

        save(LimitedAccessGenerator.EXCLUSION)
        save(LimitedAccessGenerator.RESTRICTION)
        save(generateExclusion(person = CaseGenerator.RESTRICTION_EXCLUSION))
        save(generateRestriction(person = CaseGenerator.RESTRICTION_EXCLUSION))
    }
}
