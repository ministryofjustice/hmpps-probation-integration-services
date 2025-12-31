package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
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
        save(PersonGenerator.DEFAULT)
        save(EventGenerator.EVENT)
        save(EventGenerator.UNSENTENCED_EVENT)
        save(EventGenerator.DISPOSAL)
        save(EventGenerator.INSTITUTION)
        save(EventGenerator.CUSTODY)
        save(EventGenerator.COURT)
        save(EventGenerator.COURT_APPEARANCE)
        save(EventGenerator.UNSENTENCED_COURT_APPEARANCE)
        save(EventGenerator.COURT_REPORT_TYPE)
        save(EventGenerator.COURT_REPORT)
        save(EventGenerator.INSTITUTIONAL_REPORT_TYPE)
        save(EventGenerator.INSTITUTIONAL_REPORT)
        save(EventGenerator.CONTACT_TYPE)
        save(EventGenerator.CONTACT)
        save(EventGenerator.NSI_TYPE)
        save(EventGenerator.NSI)
        save(DocumentGenerator.OFFENDER)
        save(DocumentGenerator.PREVIOUS_CONVICTIONS)
        save(DocumentGenerator.EVENT)
        save(DocumentGenerator.CPS_PACK)
        save(DocumentGenerator.ADDRESSASSESSMENT)
        save(DocumentGenerator.PERSONALCONTACT)
        save(DocumentGenerator.PERSONAL_CIRCUMSTANCE)
        save(DocumentGenerator.COURT_REPORT)
        save(DocumentGenerator.INSTITUTIONAL_REPORT)
        save(DocumentGenerator.OFFENDER_CONTACT)
        save(DocumentGenerator.EVENT_CONTACT)
        save(DocumentGenerator.OFFENDER_NSI)
        save(DocumentGenerator.EVENT_NSI)
    }
}
