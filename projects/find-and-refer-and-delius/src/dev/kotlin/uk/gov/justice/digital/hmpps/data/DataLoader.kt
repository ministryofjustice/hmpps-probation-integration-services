package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            LimitedAccessUserGenerator.EXCLUSION_USER,
            LimitedAccessUserGenerator.RESTRICTION_USER,
            LimitedAccessUserGenerator.RESTRICTION_AND_EXCLUSION_USER
        )
        saveAll(
            PersonGenerator.GENDER_MALE,
            PersonGenerator.ETHNICITY,
            PersonGenerator.PERSON_1,
            PersonGenerator.PERSON_2,
            PersonGenerator.EXCLUSION,
            PersonGenerator.RESTRICTION,
            PersonGenerator.RESTRICTION_EXCLUSION,
        )
        saveAll(
            LimitedAccessGenerator.EXCLUSION,
            LimitedAccessGenerator.RESTRICTION,
            LimitedAccessGenerator.BOTH_EXCLUSION,
            LimitedAccessGenerator.BOTH_RESTRICTION,
        )
        saveAll(
            PersonManagerGenerator.DEFAULT_PROVIDER,
            PersonManagerGenerator.PROVIDER_1,
            PersonManagerGenerator.PROVIDER_2
        )
        saveAll(
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
        saveAll(
            OfficeLocationGenerator.LOCATION_1,
            OfficeLocationGenerator.LOCATION_2,
            OfficeLocationGenerator.TEAM_OFFICE_1,
            OfficeLocationGenerator.TEAM_OFFICE_2,
        )
        saveAll(
            EventGenerator.CUSTODIAL_STATUS,
            EventGenerator.EVENT,
            EventGenerator.DISPOSAL,
            EventGenerator.CUSTODY,
        )
        saveAll(
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
}
