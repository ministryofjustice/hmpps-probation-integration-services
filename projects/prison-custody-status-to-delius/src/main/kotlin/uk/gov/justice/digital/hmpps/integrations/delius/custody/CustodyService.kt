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
import uk.gov.justice.digital.hmpps.integrations.delius.custody.keydate.entity.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.custody.keydate.entity.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.manager.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.isEotl
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodialStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodyEventType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getKeyDateType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.Release
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
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val keyDateRepository: KeyDateRepository,
    private val prisonManagerService: PrisonManagerService
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
                detail = custody.institution.description,
                person = custody.disposal.event.person,
                custody = custody
            )
        )

        if (orderManager != null) {
            val person = custody.disposal.event.person
            val notes = "Custodial Status: ${custody.status.description}\n" +
                "Custodial Establishment: ${custody.institution.description}\n" +
                "Location Change Date: ${DeliusDateTimeFormatter.format(date)}\n" +
                "-------------------------------" +
                if (recallReason.isEotl()) EOTL_LOCATION_CHANGE_CONTACT_NOTES else ""
            contactRepository.save(
                Contact(
                    type = contactTypeRepository.getByCode(ContactTypeCode.CHANGE_OF_INSTITUTION.code),
                    date = date,
                    person = person,
                    notes = notes,
                    staffId = orderManager.staffId,
                    teamId = orderManager.teamId,
                    createdDatetime = ZonedDateTime.now(),
                    alert = false
                )
            )
        }
    }

    fun allocatePrisonManager(
        latestRelease: Release?,
        toInstitution: Institution,
        custody: Custody,
        allocationDateTime: ZonedDateTime
    ) {
        // allocate a prison manager if institution has changed and institution is linked to a provider
        if ((
            (latestRelease != null && toInstitution.id != latestRelease.institutionId) ||
                (latestRelease == null && toInstitution.id != custody.institution.id)
            ) &&
            toInstitution.probationArea != null
        ) {
            prisonManagerService.allocateToProbationArea(
                custody.disposal,
                toInstitution.probationArea,
                allocationDateTime
            )
        }
    }

    fun findAutoConditionalReleaseDate(custodyId: Long): KeyDate? =
        keyDateRepository.findByCustodyIdAndTypeCode(custodyId, KeyDate.TypeCode.AUTO_CONDITIONAL_RELEASE_DATE.value)

    fun addRotlEndDate(acrDate: KeyDate) {
        val type = referenceDataRepository.getKeyDateType(KeyDate.TypeCode.ROTL_END_DATE.value)
        keyDateRepository.save(KeyDate(acrDate.custodyId, type, acrDate.date.minusDays(1)))
    }
}
