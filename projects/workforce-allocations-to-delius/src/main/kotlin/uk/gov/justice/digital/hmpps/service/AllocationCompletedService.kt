package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.AllocationCompletedResponse
import uk.gov.justice.digital.hmpps.api.model.Event
import uk.gov.justice.digital.hmpps.api.model.InitialAppointment
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.toManager
import uk.gov.justice.digital.hmpps.api.model.toStaffMember
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.getByPersonCrnAndNumber
import uk.gov.justice.digital.hmpps.integrations.delius.event.getOrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrnAndSoftDeletedFalse
import uk.gov.justice.digital.hmpps.integrations.delius.person.getCaseType
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository

@Service
class AllocationCompletedService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val staffRepository: StaffRepository,
    private val ldapService: LdapService,
    private val contactRepository: ContactRepository,
    private val orderManagerRepository: OrderManagerRepository,
) {
    fun getDetails(
        crn: String,
        eventNumber: String,
        staffCode: String,
    ): AllocationCompletedResponse {
        val person = personRepository.getByCrnAndSoftDeletedFalse(crn)
        val event = eventRepository.getByPersonCrnAndNumber(crn, eventNumber)
        val staff = staffRepository.findStaffWithUserByCode(staffCode)
        val initialAppointmentData = contactRepository.getInitialAppointmentData(person.id, event.id)
        val emails = ldapService.findEmailsForStaffIn(listOfNotNull(staff, initialAppointmentData?.staff))
        val initialAppointment =
            initialAppointmentData?.let { ia ->
                InitialAppointment(ia.date, ia.staff.toStaffMember(ia.staff.user?.username?.let { emails[it] }))
            }
        return AllocationCompletedResponse(
            crn = crn,
            name = person.name(),
            event = Event(eventNumber),
            type = personRepository.getCaseType(crn),
            initialAppointment = initialAppointment,
            staff = staff?.toStaffMember(staff.user?.username?.let { emails[it] }),
        )
    }

    fun getAllocationManager(
        crn: String,
        eventNumber: String,
    ): Manager {
        val event = eventRepository.getByPersonCrnAndNumber(crn, eventNumber)
        return orderManagerRepository.getOrderManager(event.id).toManager()
    }
}
