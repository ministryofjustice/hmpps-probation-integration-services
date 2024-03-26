package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.overview.*
import uk.gov.justice.digital.hmpps.api.model.overview.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Disability
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonalCircumstance
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Provision

@Service
class OverviewService(
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository,
    private val requirementRepository: RequirementRepository,
    private val registrationRepository: RegistrationRepository,
    private val provisionRepository: ProvisionRepository,
    private val disabilityRepository: DisabilityRepository,
    private val personalCircumstanceRepository: PersonCircumstanceRepository,
    private val eventRepository: EventRepository
) {

    @Transactional
    fun getOverview(crn: String): Overview {
        val person = personRepository.getPerson(crn)
        val provisions = provisionRepository.findByPersonId(person.id)
        val personalCircumstances = personalCircumstanceRepository.findCurrentCircumstances(person.id)
        val disabilities = disabilityRepository.findByPersonId(person.id)
        val personalDetails = person.toPersonalDetails(personalCircumstances, disabilities, provisions)
        val previousAppointments = contactRepository.getPreviousAppointments(person.id)
        val previousAppointmentNoOutcome =
            previousAppointments.filter { it.attended != false && it.outcome == null }.size
        val absentWithoutEvidence = previousAppointments.filter { it.attended == false && it.outcome == null }.size
        val schedule = Schedule(contactRepository.firstAppointment(person.id)?.toNextAppointment())
        val events = eventRepository.findByPersonId(person.id)
        val activeEvents = events.filter { it.active }
        val sentences = activeEvents.map { it.toSentence() }
        val activeEventsBreached = activeEvents.count { it.inBreach }
        val previousOrders = events.count { !it.active && it.disposal != null }
        val previousOrdersBreached = events.count { !it.active && it.disposal != null && it.inBreach }
        val registrations = registrationRepository.findByPersonId(person.id)


        return Overview(
            appointmentsWithoutOutcome = previousAppointmentNoOutcome,
            absencesWithoutEvidence = absentWithoutEvidence,
            personalDetails = personalDetails,
            schedule = schedule,
            previousOrders = PreviousOrders(previousOrdersBreached, previousOrders),
            sentences = sentences.mapNotNull { it },
            activity = null, //ToDo
            compliance = Compliance(currentBreaches = activeEventsBreached, failureToComplyInLast12Months = 0), //ToDo
            registrations = registrations.map { it.type.description }
        )
    }

    private fun getRar(disposalId: Long): Rar {
        val rarDays = requirementRepository.getRarDays(disposalId)
        val scheduledDays = rarDays.find { it.type == "SCHEDULED" }?.days ?: 0
        val completedDays = rarDays.find { it.type == "COMPLETED" }?.days ?: 0
        return Rar(completed = completedDays, scheduled = scheduledDays)
    }

    fun Disposal.toOrder() = Order(description = type.description, length = length, startDate = date, endDate = expectedEndDate())
    fun Event.toSentence() = mainOffence?.offence?.let { offence ->
        Sentence(
            mainOffence = offence.toOffence(),
            eventNumber = eventNumber,
            additionalOffences = additionalOffences.map { it.offence.toOffence() },
            order = disposal?.toOrder(),
            rar = disposal?.let { getRar(it.id) })
    }

    fun Person.toPersonalDetails(
        personalCircumstances: List<PersonalCircumstance>,
        disabilities: List<Disability>,
        provisions: List<Provision>
    ) = PersonalDetails(
        name = name(),
        mobileNumber = mobileNumber,
        telephoneNumber = telephoneNumber,
        preferredGender = gender.description,
        preferredName = preferredName,
        personalCircumstances = personalCircumstances.map { it.toPersonalCircumstance() },
        disabilities = disabilities.map { it.toDisability() },
        dateOfBirth = dateOfBirth,
        provisions = provisions.map { it.toProvision() },
    )

    fun uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Offence.toOffence() =
        Offence(code = code, description = description)

    fun PersonalCircumstance.toPersonalCircumstance() =
        uk.gov.justice.digital.hmpps.api.model.overview.PersonalCircumstance(
            type = type.description,
            subType = subType.description
        )

    fun Disability.toDisability() =
        uk.gov.justice.digital.hmpps.api.model.overview.Disability(description = type.description)

    fun Provision.toProvision() =
        uk.gov.justice.digital.hmpps.api.model.overview.Provision(description = type.description)

    fun Contact.toNextAppointment() =
        NextAppointment(description = type.description, date = startDateTime())
}
