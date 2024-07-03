package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByPersonAndEventNumber
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiRepository
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
            length?.let { "Months" },
            managers.map { it.toNsiManager() },
            notes,
            intendedProvider?.toProbationArea(true),
            active,
            softDeleted,
            externalReference
        )

    fun NsiManagerEntity.toNsiManager(): NsiManager =
        NsiManager(
            probationArea.toProbationArea(false),
            team.toTeam(),
            staff.toStaffDetails(),
            startDate,
            endDate
        )
}

fun Staff.toStaffDetails(): StaffDetails = StaffDetails(
    user?.username,
    code,
    id,
    Human(getForenames(), surname),
    teams.map { it.toTeam() },
    probationArea.toProbationArea(false),
    grade?.keyValueOf()
)


