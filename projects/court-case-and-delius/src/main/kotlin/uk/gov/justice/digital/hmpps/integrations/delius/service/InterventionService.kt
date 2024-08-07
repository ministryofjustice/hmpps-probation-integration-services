package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByPersonAndEventNumber
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.getByNsiId
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.Nsi as NsiEntity
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiManager as NsiManagerEntity

@Service
class InterventionService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val nsiRepository: NsiRepository,
) {
    fun getNsiByCodes(crn: String, convictionId: Long, nsiCodes: List<String>): NsiDetails {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.getByPersonAndEventNumber(person, convictionId)


        return NsiDetails(
            nsiRepository
                .findByPersonIdAndEventIdAndTypeCodeIn(person.id, event.id, nsiCodes)
                .map { it.toNsi() })
    }

    fun getNsiByNsiId(crn: String, convictionId: Long, nsiId: Long): Nsi {
        val person = personRepository.getPerson(crn)
        eventRepository.getByPersonAndEventNumber(person, convictionId)
        return nsiRepository.getByNsiId(nsiId).toNsi()
    }
}

fun NsiManagerEntity.toNsiManager(): NsiManager =
    NsiManager(
        probationArea.toProbationArea(false),
        team.toTeam(),
        staff.toStaffDetails(),
        startDate,
        endDate
    )

fun NsiEntity.toNsi(): Nsi =
    Nsi(
        id,
        KeyValue(type.code, type.description),
        subType?.keyValueOf(),
        outcome?.keyValueOf(),
        requirement?.toRequirementModel(),
        KeyValue(nsiStatus.code, nsiStatus.description),
        statusDate,
        actualStartDate,
        expectedStartDate,
        actualEndDate,
        expectedEndDate,
        referralDate,
        length,
        "Months",
        managers.map { it.toNsiManager() },
        notes,
        intendedProvider?.toProbationArea(false),
        active,
        softDeleted,
        externalReference,
        this.toRecallRejectedOrWithdrawn(),
        this.toOutcomeRecall()
    )

fun Staff.toStaffDetails(): StaffDetails = StaffDetails(
    user?.username,
    code,
    id,
    Human(getForenames(), surname),
    teams.map { it.toTeam() },
    probationArea.toProbationArea(false),
    grade?.keyValueOf()
)

private inline fun <reified T : Enum<T>> String.toEnumOrElse(default: T) =
    T::class.java.enumConstants.firstOrNull { it.name == this } ?: default

fun NsiEntity.toOutcomeRecall() = outcome?.code?.toEnumOrElse(OutcomeType.UNKNOWN)?.isOutcomeRecall

fun NsiEntity.toRecallRejectedOrWithdrawn() = nsiStatus.code.toEnumOrElse(Status.UNKNOWN).isRejectedOrWithdrawn?.let {
    it || (outcome?.code?.toEnumOrElse(OutcomeType.UNKNOWN)?.isOutcomeRejectedOrWithdrawn ?: false)
}

enum class OutcomeType(val code: String, val isOutcomeRejectedOrWithdrawn: Boolean?, val isOutcomeRecall: Boolean?) {
    REC01("Fixed Term Recall", true, false),
    REC02("Standard Term Recall", true, false),
    REC03("Recall Rejected by NPS", false, true),
    REC04("Recall Rejected by PPCS", false, true),
    REC05("Request Withdrawn by OM", false, true),
    UNKNOWN("No recall matching code", null, null)
}

enum class Status(val code: String, val isRejectedOrWithdrawn: Boolean?) {
    REC01("Recall Initiated", false),
    REC02("Part A Completed by NPS/CRC OM", false),
    REC03("Part A Countersigned by NPS/CRC Manager", false),
    REC04("NPS Recall Endorsed by Senior Manager", false),
    REC05("NPS Recall Rejected by Senior Manager", true),
    REC06("Recall Referred to NPS", false),
    REC07("PPCS Recall Decision Received", false),
    REC08("Recall Submitted to PPCS", false),
    REC09("Out-of-hours Recall Instigated", false),
    REC10("Request Withdrawn by OM", true),
    UNKNOWN("No recall matching code", null)
}




