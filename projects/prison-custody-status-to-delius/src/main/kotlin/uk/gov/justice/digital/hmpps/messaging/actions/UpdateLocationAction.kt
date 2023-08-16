package uk.gov.justice.digital.hmpps.messaging.actions

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactDetail
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.getByNomisCdeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodyEventType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.messaging.ActionResult
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementAction
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementContext
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties

@Component
class UpdateLocationAction(
    private val institutionRepository: InstitutionRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val custodyRepository: CustodyRepository,
    private val custodyHistoryRepository: CustodyHistoryRepository,
    private val prisonManagerService: PrisonManagerService,
    private val contactService: ContactService
) : PrisonerMovementAction {

    private val eotlLocationChangeContactNotes = """${System.lineSeparator()}
    |The date of the change to the custody location has been identified from the case being updated following a Temporary Absence Return in NOMIS.
    |The date may reflect an update after the date the actual change to location occurred.
    """.trimMargin()

    override val name: String = "UpdateLocation"

    override fun accept(context: PrisonerMovementContext): ActionResult {
        val (prisonerMovement, custody) = context
        if ((
            prisonerMovement is PrisonerMovement.Received ||
                prisonerMovement.isHospitalRelease() || prisonerMovement.isIrcRelease()
            ) && prisonerMovement.prisonId != null && custody.institution?.nomisCdeCode == prisonerMovement.prisonId
        ) {
            return ActionResult.Ignored("PrisonerLocationCorrect", prisonerMovement.telemetryProperties())
        }

        val institution = when (prisonerMovement) {
            is PrisonerMovement.Received -> institutionRepository.getByNomisCdeCode(prisonerMovement.prisonId)
            is PrisonerMovement.Released -> if (prisonerMovement.isHospitalRelease()) {
                prisonerMovement.prisonId?.let { institutionRepository.findByNomisCdeCode(it) }
                    ?: run { institutionRepository.getByCode(InstitutionCode.OTHER_SECURE_UNIT.code) }
            } else if (prisonerMovement.isIrcRelease()) {
                if (custody.institution?.irc == true) {
                    custody.institution!!
                } else {
                    institutionRepository.getByCode(InstitutionCode.OTHER_IRC.code)
                }
            } else {
                institutionRepository.getByCode(InstitutionCode.IN_COMMUNITY.code)
            }
        }

        return custody.updateLocationAt(institution, prisonerMovement.occurredAt) {
            referenceDataRepository.getCustodyEventType(CustodyEventTypeCode.LOCATION_CHANGE.code)
        }?.let { history ->
            custodyRepository.save(custody)
            custodyHistoryRepository.save(history)
            institution.probationArea?.let {
                prisonManagerService.allocateToProbationArea(
                    custody.disposal,
                    it,
                    prisonerMovement.occurredAt
                )
            }
            createLocationChangeContact(prisonerMovement, custody)

            ActionResult.Success(ActionResult.Type.LocationUpdated, prisonerMovement.telemetryProperties())
        } ?: ActionResult.Ignored("PrisonerLocationCorrect", prisonerMovement.telemetryProperties())
    }

    private fun createLocationChangeContact(prisonerMovement: PrisonerMovement, custody: Custody) {
        if (prisonerMovement is PrisonerMovement.Received ||
            prisonerMovement.isHospitalRelease() || prisonerMovement.isIrcRelease()
        ) {
            val notes = """
            |Custodial Status: ${custody.status.description}
            |Custodial Establishment: ${custody.institution!!.description}
            |Location Change Date: ${DeliusDateTimeFormatter.format(custody.locationChangeDate!!)}
            |-------------------------------
            """.trimMargin() +
                if (prisonerMovement.type == PrisonerMovement.Type.TEMPORARY_ABSENCE_RETURN) eotlLocationChangeContactNotes else ""
            contactService.createContact(
                ContactDetail(ContactType.Code.CHANGE_OF_INSTITUTION, custody.locationChangeDate!!, notes),
                custody.disposal.event.person,
                event = custody.disposal.event,
                manager = custody.disposal.event.manager()
            )
        }
    }
}
