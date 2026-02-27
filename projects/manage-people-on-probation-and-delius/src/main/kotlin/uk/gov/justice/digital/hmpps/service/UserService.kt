package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.response.Homepage
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val contactRepository: ContactRepository
) {
    fun getHomepage(username: String): Homepage {
        val user = userRepository.findByUsername(username).orNotFoundBy("username", username)
        val staff = user.staff ?: return Homepage()
        val upcoming = contactRepository.findNextFiveAppointments(staff.id)
        val requiringOutcome = contactRepository.findLastFiveAppointmentsRequiringAnOutcome(staff.id)
        return Homepage(
            upcomingAppointments = upcoming.content.map { it.toSummary() },
            appointmentsRequiringOutcome = requiringOutcome.content.map { it.toSummary() },
            appointmentsRequiringOutcomeCount = requiringOutcome.totalElements.toInt()
        )
    }

    private fun Contact.toSummary() = Homepage.AppointmentSummary(
        id = id,
        crn = person.crn,
        name = person.name(),
        type = type.description,
        startDateTime = startDateTime(),
        endDateTime = endDateTime(),
        location = location?.description,
        deliusManaged = type.isDeliusManaged() || outcome?.complied == false || requirement?.isRar() == true,
    )
}
