package uk.gov.justice.digital.hmpps.integrations.delius.custody

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.custody.history.CustodyHistory
import uk.gov.justice.digital.hmpps.integrations.delius.custody.history.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.manager.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodialStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodyEventType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import java.time.ZonedDateTime

@Service
class CustodyService(
    private val referenceDataRepository: ReferenceDataRepository,
    private val custodyRepository: CustodyRepository,
    private val custodyHistoryRepository: CustodyHistoryRepository,
    private val institutionRepository: InstitutionRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository
) {
    fun updateStatus(custody: Custody, status: CustodialStatusCode, date: ZonedDateTime, detail: String) {
        custody.status = referenceDataRepository.getCustodialStatus(status.code)
        custody.statusChangeDate = date
        custodyRepository.save(custody)
        custodyHistoryRepository.save(
            CustodyHistory(
                date = date,
                type = referenceDataRepository.getCustodyEventType(CustodyEventTypeCode.STATUS_CHANGE.code),
                detail = detail,
                person = custody.disposal.event.person,
                custody = custody,
            )
        )
    }

    fun updateLocation(custody: Custody, institutionCode: String, date: ZonedDateTime, orderManager: OrderManager? = null) {
        custody.institution = institutionRepository.getByCode(institutionCode)
        custody.locationChangeDate = date
        custodyRepository.save(custody)
        custodyHistoryRepository.save(
            CustodyHistory(
                date = date,
                type = referenceDataRepository.getCustodyEventType(CustodyEventTypeCode.LOCATION_CHANGE.code),
                detail = custody.institution.description,
                person = custody.disposal.event.person,
                custody = custody,
            )
        )

        if (orderManager != null) {
            val person = custody.disposal.event.person
            contactRepository.save(
                Contact(
                    type = contactTypeRepository.getByCode(ContactTypeCode.CHANGE_OF_INSTITUTION.code),
                    date = date,
                    person = person,
                    notes =
                    "Custodial Status: ${custody.status.description}\n" +
                        "Custodial Establishment: ${custody.institution.description}\n" +
                        "Location Change Date: ${DeliusDateTimeFormatter.format(date)}\n" +
                        "-------------------------------",
                    staffId = orderManager.staffId,
                    teamId = orderManager.teamId,
                    createdDatetime = ZonedDateTime.now(),
                    alert = false,
                )
            )
        }
    }
}
