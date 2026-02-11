package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.AdditionalOffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.MainOffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonAddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            PersonGenerator.DEFAULT_PERSON,
            ReferenceDataGenerator.MR_TITLE,
            ReferenceDataGenerator.LENGTH_UNITS_MONTHS,
            ReferenceDataGenerator.LENGTH_UNITS_DAYS,
            ReferenceDataGenerator.DEFAULT_OUTCOME,
            ReferenceDataGenerator.DEFAULT_REQUIREMENT_SUBTYPE,
            ReferenceDataGenerator.SENTENCE_APPEARANCE_TYPE,
            PersonAddressGenerator.DEFAULT_PERSON_MAIN_ADDRESS,
            EventGenerator.DEFAULT_EVENT,
            EventGenerator.MISSING_MAIN_OFFENCE_EVENT,
            EventGenerator.MISSING_COURT_APPEARANCE_EVENT,
            EventGenerator.MISSING_DISPOSAL_EVENT,
            DocumentGenerator.DEFAULT_DOCUMENT,
            DocumentGenerator.MISSING_MAIN_OFFENCE_DOCUMENT,
            DocumentGenerator.MISSING_COURT_APPEARANCE_DOCUMENT,
            DocumentGenerator.MISSING_DISPOSAL_DOCUMENT,
            MainOffenceGenerator.DEFAULT_MAIN_OFFENCE,
            MainOffenceGenerator.MISSING_COURT_APPEARANCE_MAIN_OFFENCE,
            MainOffenceGenerator.MISSING_DISPOSAL_MAIN_OFFENCE,
            DisposalGenerator.DEFAULT_DISPOSAL,
            CourtAppearanceGenerator.DEFAULT_COURT_APPEARANCE,
            CourtAppearanceGenerator.MISSING_DISPOSAL_COURT_APPEARANCE,
            RequirementGenerator.DEFAULT_REQUIREMENT,
            AdditionalOffenceGenerator.DEFAULT_ADDITIONAL_OFFENCE
        )
    }
}
