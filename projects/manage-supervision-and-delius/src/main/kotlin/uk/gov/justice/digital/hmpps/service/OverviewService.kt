package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.overview.*
import uk.gov.justice.digital.hmpps.api.model.overview.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.getAllBreaches
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
    private val nsiRepository: NsiRepository,
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
        val allBreaches = nsiRepository.getAllBreaches(person.id)
        val previousOrders = events.filter { !it.active && it.disposal != null }
        val previousOrdersBreached = allBreaches.filter { it -> it.eventId in previousOrders.map { it.id } }.size
        val compliance = toSentenceCompliance(previousAppointments.map { it.toActivity() }, allBreaches)
        val registrations = registrationRepository.findByPersonId(person.id)


        return Overview(
            appointmentsWithoutOutcome = previousAppointmentNoOutcome,
            absencesWithoutEvidence = absentWithoutEvidence,
            personalDetails = personalDetails,
            schedule = schedule,
            previousOrders = PreviousOrders(previousOrdersBreached, previousOrders.size),
            sentences = sentences.mapNotNull { it },
            activity = toSentenceActivityCounts(previousAppointments.map { it.toActivity() }),
            compliance = compliance,
            registrations = registrations.map { it.type.description }
        )
    }

    fun Disposal.toOrder() =
        Order(description = type.description, length = length, startDate = date, endDate = expectedEndDate())

    fun Event.toSentence() = mainOffence?.offence?.let { offence ->
        Sentence(
            mainOffence = offence.toOffence(),
            eventNumber = eventNumber,
            additionalOffences = additionalOffences.map { it.offence.toOffence() },
            order = disposal?.toOrder(),
            rar = disposal?.let { requirementRepository.getRar(it.id) })
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
