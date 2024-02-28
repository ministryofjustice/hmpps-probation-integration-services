package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.overview.*
import uk.gov.justice.digital.hmpps.api.model.overview.Disability
import uk.gov.justice.digital.hmpps.api.model.overview.Offence
import uk.gov.justice.digital.hmpps.api.model.overview.PersonalCircumstance
import uk.gov.justice.digital.hmpps.api.model.overview.Provision
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*

@Service
class OverviewService(
    private val personRepository: PersonOverviewRepository,
    private val contactRepository: ContactRepository,
    private val requirementRepository: RequirementRepository,
    private val eventRepository: EventRepository
) {

    @Transactional
    fun getOverview(crn: String): Overview {
        val person = personRepository.getPerson(crn)
        val personalDetails = person.toPersonalDetails()
        val schedule = Schedule(contactRepository.findFirstAppointment(person.id).firstOrNull()?.toNextAppointment())
        val events = eventRepository.findByPersonId(person.id)
        val activeEvents = events.filter { it.active }
        val sentences = activeEvents.map { it.toSentence() }
        val activeEventsBreached = activeEvents.count { it.inBreach }
        val previousOrders = events.count { !it.active && it.disposal != null }
        val previousOrdersBreached = events.count { !it.active && it.disposal != null && it.inBreach }

        return Overview(
            personalDetails = personalDetails,
            schedule = schedule,
            previousOrders = PreviousOrders(previousOrdersBreached, previousOrders),
            sentences = sentences.mapNotNull { it },
            activity = null, //ToDo
            compliance = Compliance(currentBreaches = activeEventsBreached, failureToComplyInLast12Months = 0), //ToDo
        )
    }

    private fun getRar(disposalId: Long): Rar {
        val rarDays = requirementRepository.getRarDays(disposalId)
        val scheduledDays = rarDays.find { it.type == "SCHEDULED" }?.days ?: 0
        val completedDays = rarDays.find { it.type == "COMPLETED" }?.days ?: 0
        return Rar(completed = completedDays, scheduled = scheduledDays)
    }

    fun Disposal.toOrder() = Order(description = type.description, startDate = date, endDate = expectedEndDate())
    fun Event.toSentence() = mainOffence?.offence?.let { offence ->
        Sentence(
            mainOffence = offence.toOffence(),
            additionalOffences = additionalOffences.map { it.offence.toOffence() },
            order = disposal?.toOrder(),
            rar = disposal?.let { getRar(it.id) })
    }

    fun Person.toPersonalDetails() = PersonalDetails(
        name = name(),
        mobileNumber = mobileNumber,
        telephoneNumber = telephoneNumber,
        preferredGender = gender.description,
        preferredName = preferredName,
        personalCircumstances = personalCircumstances.map { it.toPersonalCircumstance() },
        disabilities = disabilities.map { it.toDisability() },
        provisions = provisions.map { it.toProvision() },
    )

    fun uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Offence.toOffence() =
        Offence(code = code, description = description)

    fun uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonalCircumstance.toPersonalCircumstance() =
        PersonalCircumstance(type = type.description, subType = subType.description)

    fun uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Disability.toDisability() =
        Disability(description = type.description)

    fun uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Provision.toProvision() =
        Provision(description = type.description)

    fun Contact.toNextAppointment() = NextAppointment(description = type.description, date = date.toLocalDateTime())
}
