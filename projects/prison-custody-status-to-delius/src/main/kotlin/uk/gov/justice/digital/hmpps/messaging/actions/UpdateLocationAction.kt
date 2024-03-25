package uk.gov.justice.digital.hmpps.messaging.actions

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactDetail
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.canBeReleased
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.getByNomisCdeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodyEventType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.messaging.*

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
        if (prisonerMovement is PrisonerMovement.Received && custody.institution?.nomisCdeCode == prisonerMovement.toPrisonId) {
            return ActionResult.Ignored("PrisonerLocationCorrect", prisonerMovement.telemetryProperties())
        }

        val institution = when (prisonerMovement) {
            is PrisonerMovement.Received -> institutionRepository.getByNomisCdeCode(prisonerMovement.toPrisonId)
            is PrisonerMovement.Released -> prisonerMovement.releaseLocation(custody)
        }

        // do this before updating location so that we have access to previous institution
        val pomInstitutionOverride: Institution? = when (prisonerMovement) {
            is PrisonerMovement.Released -> institutionRepository.getByNomisCdeCode(prisonerMovement.fromPrisonId)
            else -> null
        }

        return institution?.let { inst ->
            custody.updateLocationAt(inst, prisonerMovement.occurredAt) {
                referenceDataRepository.getCustodyEventType(CustodyEventTypeCode.LOCATION_CHANGE.code)
            }?.let { history ->
                custodyRepository.save(custody)
                custodyHistoryRepository.save(history)
                (pomInstitutionOverride ?: inst).probationArea?.let {
                    prisonManagerService.allocateToProbationArea(
                        custody.disposal,
                        it,
                        prisonerMovement.occurredAt
                    )
                }
                createLocationChangeContact(prisonerMovement, custody)

                ActionResult.Success(ActionResult.Type.LocationUpdated, prisonerMovement.telemetryProperties())
            }
        } ?: ActionResult.Ignored("PrisonerLocationCorrect", prisonerMovement.telemetryProperties())
    }

    private fun PrisonerMovement.releaseLocation(custody: Custody) =
        when {
            isHospitalRelease() -> if (custody.institution?.secureHospital == true) {
                custody.institution!!
            } else {
                institutionRepository.getByCode(InstitutionCode.OTHER_SECURE_UNIT.code)
            }

            isIrcRelease() -> if (custody.institution?.irc == true) {
                custody.institution!!
            } else {
                institutionRepository.getByCode(InstitutionCode.OTHER_IRC.code)
            }

            isAbsconded() -> institutionRepository.getByCode(InstitutionCode.UNLAWFULLY_AT_LARGE.code)

            custody.canBeReleased() -> institutionRepository.getByCode(InstitutionCode.IN_COMMUNITY.code)

            else -> custody.institution
        }

    private fun createLocationChangeContact(prisonerMovement: PrisonerMovement, custody: Custody) {
        if (prisonerMovement is PrisonerMovement.Received ||
            prisonerMovement.isHospitalRelease() || prisonerMovement.isIrcRelease() || prisonerMovement.isAbsconded()
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
