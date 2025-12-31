package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator.generatePersonManager
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager, private val em: EntityManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            ProbationAreaGenerator.DEFAULT_PA,
            ProbationAreaGenerator.DEFAULT_BOROUGH,
            ProbationAreaGenerator.DEFAULT_LDU,
            ProbationAreaGenerator.DEFAULT_LDU2,
            ProbationAreaGenerator.NON_SELECTABLE_PA,
            ProbationAreaGenerator.NON_SELECTABLE_BOROUGH,
            ProbationAreaGenerator.NON_SELECTABLE_LDU,
            CourtAppearanceGenerator.DEFAULT_OUTCOME,
            CourtAppearanceGenerator.DEFAULT_CA_TYPE,
            CourtAppearanceGenerator.DEFAULT_COURT,
            CourtAppearanceGenerator.DEFAULT_PERSON,
            CourtAppearanceGenerator.DEFAULT_EVENT,
            CourtAppearanceGenerator.DEFAULT_CA,
            CourtAppearanceGenerator.PERSON_2,
            CourtAppearanceGenerator.EVENT_2,
            CourtAppearanceGenerator.CA_2,
            CourtAppearanceGenerator.CA_3,
            ConvictionEventGenerator.PERSON,
            ConvictionEventGenerator.ADDITIONAL_OFFENCE_TYPE,
            ConvictionEventGenerator.OFFENCE_MAIN_TYPE,
            ConvictionEventGenerator.DEFAULT_EVENT,
            ConvictionEventGenerator.INACTIVE_EVENT,
            ConvictionEventGenerator.MAIN_OFFENCE,
            ConvictionEventGenerator.OTHER_OFFENCE,
            ConvictionEventGenerator.DISPOSAL_TYPE,
            ConvictionEventGenerator.DISPOSAL,
            ConvictionEventGenerator.COURT_APPEARANCE,
            DetailsGenerator.INSTITUTION,
            DetailsGenerator.RELIGION,
            DetailsGenerator.NATIONALITY,
            DetailsGenerator.MALE,
            DetailsGenerator.FEMALE,
            DetailsGenerator.PERSON,
            DetailsGenerator.ALIAS_1,
            DetailsGenerator.ALIAS_2,
            DetailsGenerator.DEFAULT_PA,
            DetailsGenerator.DISTRICT,
            DetailsGenerator.TEAM,
            DetailsGenerator.STAFF,
            DetailsGenerator.PERSON_MANAGER,
            DetailsGenerator.RELEASE_TYPE,
            DetailsGenerator.RELEASE,
            DetailsGenerator.RECALL_REASON,
            DetailsGenerator.RECALL,
            NSIGenerator.BREACH_TYPE,
            NSIGenerator.RECALL_TYPE,
            NSIGenerator.BREACH_NSI,
            NSIGenerator.RECALL_NSI,
            ConvictionEventGenerator.PERSON_2,
            ConvictionEventGenerator.EVENT_2,
            ConvictionEventGenerator.MAIN_OFFENCE_2,
            ConvictionEventGenerator.OTHER_OFFENCE_2,
            ConvictionEventGenerator.DISPOSAL_2,
            KeyDateGenerator.CUSTODY_STATUS,
            KeyDateGenerator.SED_KEYDATE,
            KeyDateGenerator.CUSTODY,
            KeyDateGenerator.KEYDATE,
            KeyDateGenerator.CUSTODY_1,
            KeyDateGenerator.KEYDATE_1,
            SearchGenerator.JOHN_DOE,
            generatePersonManager(SearchGenerator.JOHN_DOE),
            SearchGenerator.JOHN_SMITH_1,
            generatePersonManager(SearchGenerator.JOHN_SMITH_1),
            SearchGenerator.JOHN_SMITH_1_ALIAS,
            SearchGenerator.JOHN_SMITH_2,
            generatePersonManager(SearchGenerator.JOHN_SMITH_2),
            ManagerGenerator.PERSON,
            ManagerGenerator.PERSON_2,
            ManagerGenerator.PROBATION_AREA_1,
            ManagerGenerator.PROBATION_AREA_2,
            ManagerGenerator.PROBATION_AREA_3,
            ManagerGenerator.PDU_1,
            ManagerGenerator.PDU_2,
            ManagerGenerator.PDU_3,
            ManagerGenerator.LAU_1,
            ManagerGenerator.LAU_2,
            ManagerGenerator.LAU_3,
            ManagerGenerator.TEAM_1,
            ManagerGenerator.TEAM_2,
            ManagerGenerator.TEAM_3,
            *ManagerGenerator.PERSON_MANAGERS.toTypedArray(),
        )

        em.createNativeQuery(
            """
             update event set offender_id = ${DetailsGenerator.PERSON.id} 
             where event_id = ${ConvictionEventGenerator.EVENT_2.id}
            """.trimMargin()
        )
            .executeUpdate()
    }
}
