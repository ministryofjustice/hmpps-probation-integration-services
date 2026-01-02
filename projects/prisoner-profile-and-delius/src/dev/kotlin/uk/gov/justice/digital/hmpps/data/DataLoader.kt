package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.ALLOCATED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.JAMES_BROWN
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.STAFF
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.UNALLOCATED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.UNALLOCATED_STAFF
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.generateCommunityManager
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            TEAM,
            UNALLOCATED_STAFF,
            STAFF,
            JAMES_BROWN,
            UNALLOCATED_PERSON,
            ALLOCATED_PERSON,
            generateCommunityManager(
                ALLOCATED_PERSON,
                STAFF
            ),
            generateCommunityManager(
                UNALLOCATED_PERSON,
                UNALLOCATED_STAFF
            )
        )
        saveAll(documentData())
    }

    fun documentData() = listOfNotNull(
        PersonGenerator.DEFAULT,
        EventGenerator.EVENT.mainOffence.offence,
        EventGenerator.EVENT,
        EventGenerator.EVENT.mainOffence,
        EventGenerator.UNSENTENCED_EVENT.mainOffence.offence,
        EventGenerator.UNSENTENCED_EVENT,
        EventGenerator.UNSENTENCED_EVENT.mainOffence,
        EventGenerator.DISPOSAL.lengthUnits,
        EventGenerator.DISPOSAL.type,
        EventGenerator.DISPOSAL,
        EventGenerator.INSTITUTION,
        EventGenerator.CUSTODY,
        EventGenerator.COURT,
        EventGenerator.COURT_APPEARANCE,
        EventGenerator.UNSENTENCED_COURT_APPEARANCE.outcome,
        EventGenerator.UNSENTENCED_COURT_APPEARANCE,
        EventGenerator.COURT_REPORT_TYPE,
        EventGenerator.COURT_REPORT,
        EventGenerator.INSTITUTIONAL_REPORT_TYPE,
        EventGenerator.INSTITUTIONAL_REPORT,
        EventGenerator.CONTACT_TYPE,
        EventGenerator.CONTACT,
        EventGenerator.NSI_TYPE,
        EventGenerator.NSI,
        DocumentGenerator.OFFENDER,
        DocumentGenerator.PREVIOUS_CONVICTIONS,
        DocumentGenerator.EVENT,
        DocumentGenerator.CPS_PACK,
        DocumentGenerator.ADDRESSASSESSMENT,
        DocumentGenerator.PERSONALCONTACT,
        DocumentGenerator.PERSONAL_CIRCUMSTANCE,
        DocumentGenerator.COURT_REPORT,
        DocumentGenerator.INSTITUTIONAL_REPORT,
        DocumentGenerator.OFFENDER_CONTACT,
        DocumentGenerator.EVENT_CONTACT,
        DocumentGenerator.OFFENDER_NSI,
        DocumentGenerator.EVENT_NSI
    )
}
