package uk.gov.justice.digital.hmpps.integrations.delius.custody

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactDetail
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistory
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.isEotl
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodialStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodyEventType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import java.time.ZonedDateTime

val EOTL_LOCATION_CHANGE_CONTACT_NOTES = """${System.lineSeparator()}
    |The date of the change to the custody location has been identified from the case being updated following a Temporary Absence Return in NOMIS.
    |The date may reflect an update after the date the actual change to location occurred.
""".trimMargin()

@Service
class CustodyService(
    private val referenceDataRepository: ReferenceDataRepository,
    private val custodyRepository: CustodyRepository,
    private val custodyHistoryRepository: CustodyHistoryRepository,
    private val prisonManagerService: PrisonManagerService,
    private val contactService: ContactService
) {
    fun updateStatus(custody: Custody, status: CustodialStatusCode, date: ZonedDateTime, detail: String) {
        if (custody.status.code != status.code) {
            custody.status = referenceDataRepository.getCustodialStatus(status.code)
            custody.statusChangeDate = date
            custodyRepository.save(custody)
            custodyHistoryRepository.save(
                CustodyHistory(
                    date = date,
                    type = referenceDataRepository.getCustodyEventType(CustodyEventTypeCode.STATUS_CHANGE.code),
                    detail = detail,
                    person = custody.disposal.event.person,
                    custody = custody
                )
            )
        }
    }

    fun updateLocation(
        custody: Custody,
        institution: Institution,
        date: ZonedDateTime,
        orderManager: OrderManager? = null,
        recallReason: RecallReason? = null
    ) {
        custody.institution = institution
        custody.locationChangeDate = date
        custodyRepository.save(custody)
        custodyHistoryRepository.save(
            CustodyHistory(
                date = date,
                type = referenceDataRepository.getCustodyEventType(CustodyEventTypeCode.LOCATION_CHANGE.code),
                detail = custody.institution!!.description,
                person = custody.disposal.event.person,
                custody = custody
            )
        )
        if (orderManager != null) {
            val person = custody.disposal.event.person
            val notes = "Custodial Status: ${custody.status.description}\n" +
                "Custodial Establishment: ${custody.institution!!.description}\n" +
                "Location Change Date: ${DeliusDateTimeFormatter.format(date)}\n" +
                "-------------------------------" +
                if (recallReason.isEotl()) EOTL_LOCATION_CHANGE_CONTACT_NOTES else ""
            contactService.createContact(
                ContactDetail(ContactType.Code.CHANGE_OF_INSTITUTION, date, notes),
                person,
                event = custody.disposal.event,
                manager = orderManager
            )
        }
    }

    fun allocatePrisonManager(
        toInstitution: Institution,
        custody: Custody,
        allocationDateTime: ZonedDateTime
    ) {
        // allocate a prison manager if institution has changed and institution is linked to a provider
        if (toInstitution.id != custody.institution?.id && toInstitution.probationArea != null) {
            prisonManagerService.allocateToProbationArea(
                custody.disposal,
                toInstitution.probationArea,
                allocationDateTime
            )
        }
    }
}
