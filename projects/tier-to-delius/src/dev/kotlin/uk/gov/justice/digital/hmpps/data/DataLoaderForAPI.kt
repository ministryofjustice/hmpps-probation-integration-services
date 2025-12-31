package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.nsi.entity.NsiEvent
import java.time.LocalDate

@Component
class DataLoaderForAPI(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun setupData() {
        save(ReferenceDataSetGenerator.GENDER)
        save(ReferenceDataSetGenerator.REGISTER_LEVEL)
        save(ReferenceDataSetGenerator.REGISTER_TYPE_FLAG)
        save(ReferenceDataSetGenerator.RSR_TYPE)
        save(ReferenceDataGenerator.GENDER_MALE)
        save(ReferenceDataGenerator.TIER_ONE)
        save(ReferenceDataGenerator.LEVEL_ONE)
        save(ReferenceDataGenerator.FLAG)
        save(ReferenceDataGenerator.STATIC_RSR)
        save(ReferenceDataGenerator.DYNAMIC_RSR)
        save(RegisterTypeGenerator.DEFAULT)

        save(ReferenceDataSetGenerator.NSI_OUTCOME)
        saveAll(ReferenceDataGenerator.ENFORCEMENT_OUTCOMES)

        val case = save(CaseEntityGenerator.DEFAULT)
        val event = save(EventGenerator.DEFAULT)
        save(DisposalTypeGenerator.DEFAULT)
        saveAll(listOf(DisposalGenerator.DEFAULT))
        save(RequirementGenerator.RequirementMainCategoryGenerator.DEFAULT)
        save(RequirementGenerator.DEFAULT)
        save(OgrsAssessmentGenerator.DEFAULT)
        save(OasysAssessmentGenerator.DEFAULT)
        save(RegistrationGenerator.DEFAULT)
        saveAll(RsrScoreHistoryGenerator.HISTORY)
        save(
            NsiGenerator.generate(
                case.id,
                NsiEvent(event.id, event.softDeleted),
                LocalDate.now().minusDays(360),
                ReferenceDataGenerator.ENFORCEMENT_OUTCOMES.first()
            )
        )
    }

    override fun systemUser() = null
}
