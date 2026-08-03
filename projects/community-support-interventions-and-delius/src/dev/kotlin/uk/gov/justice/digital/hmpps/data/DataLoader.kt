package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.DisabilityGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(ReferenceDataGenerator.LANGUAGE_ENGLISH)
        save(ReferenceDataGenerator.DISABILITY_TYPE_VISUAL)
        save(PersonGenerator.PERSON1)
        save(PersonGenerator.PERSON2)
        save(RegistrationGenerator.HOIE_TYPE)
        save(RegistrationGenerator.HOME_OFFICE_INTEREST)
        save(PersonalCircumstanceGenerator.TYPE_EMPLOYMENT)
        save(PersonalCircumstanceGenerator.SUB_TYPE_FULL_TIME)
        save(PersonalCircumstanceGenerator.CIRCUMSTANCE)
        save(DisabilityGenerator.DISABILITY)
        save(NsiGenerator.OPD_TYPE)
        save(NsiGenerator.OPD_STATUS)
        save(NsiGenerator.OPD_NSI)
    }
}
