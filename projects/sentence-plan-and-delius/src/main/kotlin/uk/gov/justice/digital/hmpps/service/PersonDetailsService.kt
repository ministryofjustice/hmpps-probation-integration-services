package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.service.entity.*

@Service
class PersonDetailsService(
    val personRepository: PersonRepository,
    val eventRepository: EventRepository,
    val disposalRepository: DisposalRepository,
    val requirementRepository: RequirementRepository,
    val contactRepository: ContactRepository,
    val upwAppointmentRepository: UpwAppointmentRepository
) {
    fun getCaseDetails(crn: String): CaseDetails {
        val personEntity = personRepository.getPerson(crn)
        val manager = personEntity.manager

        val sentences = disposalRepository.findActiveSentences(crn).map { disposal ->
            val rarDays = requirementRepository.getRar(disposal.id)
            val upwHoursOrdered = requirementRepository.sumTotalUnpaidWorkHoursByDisposal(disposal.id)
            val upwMinutesCompleted = upwAppointmentRepository.calculateUnpaidTimeWorked(disposal.id)
            Sentence(
                description = disposal.type.description,
                startDate = disposal.startDate,
                endDate = disposal.expectedEndDate(),
                programmeRequirement = false, //ToDo
                unpaidWorkHoursOrdered = upwHoursOrdered,
                unpaidWorkMinutesCompleted = upwMinutesCompleted,
                rarDaysCompleted = rarDays.completed,
                rarDaysOrdered = rarDays.totalDays
            )
        }

        return CaseDetails(
            personEntity.name(),
            personEntity.crn,
            personEntity.tier?.description,
            personEntity.dateOfBirth,
            personEntity.nomisId,
            manager.team.probationArea.description,
            Manager(
                Name(manager.staff.forename, manager.staff.middleName, manager.staff.surname),
                manager.staff.isUnallocated()
            ),
            eventRepository.isInCustody(crn),
            sentences
        )
    }

    fun getFirstAppointmentDate(crn: String): FirstAppointment {
        val dateTime = contactRepository.getFirstAppointmentDate(crn)
        return FirstAppointment(dateTime?.atZone(EuropeLondon))
    }
}
