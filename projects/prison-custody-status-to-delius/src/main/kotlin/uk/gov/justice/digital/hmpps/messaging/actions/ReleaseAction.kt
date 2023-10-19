package uk.gov.justice.digital.hmpps.messaging.actions

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactDetail
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.canBeReleased
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host.entity.HostRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getReleaseType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.Release
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.ReleaseRepository
import uk.gov.justice.digital.hmpps.messaging.ActionResult
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementAction
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementContext
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class ReleaseAction(
    private val referenceDataRepository: ReferenceDataRepository,
    private val institutionRepository: InstitutionRepository,
    private val hostRepository: HostRepository,
    private val releaseRepository: ReleaseRepository,
    private val contactService: ContactService,
    private val eventService: EventService
) : PrisonerMovementAction {
    override val name: String
        get() = "Release"

    override fun accept(context: PrisonerMovementContext): ActionResult {
        val (prisonerMovement, custody) = context
        checkPreConditions(prisonerMovement, custody)
        val releaseType = referenceDataRepository.getReleaseType(prisonerMovement.releaseType().code)
        val releasedFrom = prisonerMovement.prisonId?.let { institutionRepository.findByNomisCdeCode(it) }
            ?: custody.institution ?: institutionRepository.getByCode(InstitutionCode.UNKNOWN.code)
        return release(prisonerMovement, releaseType, custody, releasedFrom)
    }

    private fun checkPreConditions(prisonerMovement: PrisonerMovement, custody: Custody) {
        if (prisonerMovement.occurredBefore(custody.disposal.date, custody.mostRecentRelease()?.recall?.date)) {
            throw IgnorableMessageException("InvalidReleaseDate", prisonerMovement.telemetryProperties())
        }
    }

    private fun release(
        prisonerMovement: PrisonerMovement,
        type: ReferenceData,
        custody: Custody,
        institution: Institution
    ): ActionResult {
        if (custody.canBeReleased()) {
            val releaseDate = prisonerMovement.occurredAt.truncatedTo(ChronoUnit.DAYS)
            releaseRepository.save(
                Release(
                    date = releaseDate,
                    type = type,
                    person = custody.disposal.event.person,
                    custody = custody,
                    institutionId = institution.id,
                    probationAreaId = hostRepository.findLeadHostProviderIdByInstitutionId(
                        institution.id.institutionId,
                        prisonerMovement.occurredAt
                    )
                )
            )
            val event = custody.disposal.event
            contactService.createContact(
                ContactDetail(
                    ContactType.Code.RELEASE_FROM_CUSTODY,
                    prisonerMovement.occurredAt,
                    "Release Type: ${type.description}"
                ),
                event.person,
                event,
                event.manager()
            )
            eventService.updateReleaseDateAndIapsFlag(event, releaseDate)
            return ActionResult.Success(ActionResult.Type.Released, prisonerMovement.telemetryProperties())
        }
        return ActionResult.Ignored(
            "UnableToRelease",
            prisonerMovement.telemetryProperties() + ("currentStatus" to custody.status.code)
        )
    }
}

private fun PrisonerMovement.occurredBefore(sentenceDate: ZonedDateTime, recalledDateTime: ZonedDateTime?): Boolean {
    return occurredAt.isBefore(sentenceDate) || recalledDateTime?.let { occurredAt.isBefore(it) } ?: false
}

private fun PrisonerMovement.releaseType(): ReleaseTypeCode {
    if (type != PrisonerMovement.Type.RELEASED) {
        throw IgnorableMessageException("UnsupportedReleaseType")
    }
    return when (reason) {
        "ECSL" -> ReleaseTypeCode.END_CUSTODY_SUPERVISED_LICENCE
        else -> ReleaseTypeCode.ADULT_LICENCE
    }
}
