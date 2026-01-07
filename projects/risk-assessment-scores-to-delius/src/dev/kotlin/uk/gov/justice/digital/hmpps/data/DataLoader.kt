package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateAdditionalIdentifier
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            listOf(
                DatasetGenerator.GENDER,
                DatasetGenerator.ADDITIONAL_IDENTIFIER_TYPE
            )
        )

        saveAll(
            listOf(
                ReferenceDataGenerator.GENDER_MALE,
                ReferenceDataGenerator.MERGED_TO_CRN
            )
        )

        save(ContactTypeGenerator.DEFAULT)
        save(StaffGenerator.DEFAULT)
        save(TeamGenerator.DEFAULT)
        save(OffenceGenerator.DEFAULT)
        saveAll(
            listOf(
                PersonGenerator.DEFAULT,
                PersonGenerator.NULL_EVENT_PROCESSING,
                PersonGenerator.MERGED_FROM,
                PersonGenerator.MERGED_TO,
            )
        )
        saveAll(
            listOf(
                EventGenerator.DEFAULT,
                EventGenerator.NEP_1,
                EventGenerator.NEP_2,
                EventGenerator.NEP_3,
                EventGenerator.MERGED_TO,
            )
        )
        save(DisposalTypeGenerator.DEFAULT)
        saveAll(
            listOf(DisposalGenerator.DEFAULT, DisposalGenerator.NEP_DISPOSAL_2, DisposalGenerator.NEP_DISPOSAL_3)
        )
        save(MainOffenceGenerator.DEFAULT)
        saveAll(
            listOf(
                PersonManagerGenerator.DEFAULT,
                PersonManagerGenerator.generate(PersonGenerator.NULL_EVENT_PROCESSING),
                PersonManagerGenerator.generate(PersonGenerator.MERGED_TO),
            )
        )
        save(
            generateAdditionalIdentifier(
                PersonGenerator.MERGED_FROM.id,
                ReferenceDataGenerator.MERGED_TO_CRN,
                PersonGenerator.MERGED_TO.crn
            )
        )
    }
}
