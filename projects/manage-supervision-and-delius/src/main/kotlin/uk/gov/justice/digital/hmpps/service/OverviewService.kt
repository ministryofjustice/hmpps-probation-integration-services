package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.api.model.overview.NextAppointment
import uk.gov.justice.digital.hmpps.api.model.overview.Overview
import uk.gov.justice.digital.hmpps.api.model.overview.PersonalDetails
import uk.gov.justice.digital.hmpps.api.model.overview.Schedule
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.*

@Service
class OverviewService(
    private val personRepository: CaseSummaryPersonRepository
) {

    fun getOverview(crn: String): Overview {
        val person = personRepository.getPerson(crn)
        val personalDetails = person.toPersonalDetails()
        val nextAppointment = if (person.nextAppointment.isSingle()) person.nextAppointment.first().toNextAppointment() else null
        val schedule = Schedule(nextAppointment)

        return Overview(
            personalDetails = personalDetails,
            schedule = schedule,
            activity = null, //ToDo
            compliance = null, //ToDo
            previousOrders = null, //ToDo
            sentences = null,
        )
    }


    fun PersonalCircumstance.toPersonalCircumstance() = uk.gov.justice.digital.hmpps.api.model.overview.PersonalCircumstance(type = type.description, subType = subType.description)
    fun Disability.toDisability() = uk.gov.justice.digital.hmpps.api.model.overview.Disability(description = type.description)
    fun Provision.toProvision() = uk.gov.justice.digital.hmpps.api.model.overview.Provision(description = type.description)
    fun Person.toPersonalDetails() = PersonalDetails(
        name = name(),
        mobileNumber = mobileNumber,
        telephoneNumber = telephoneNumber,
        preferredGender = gender.description,
        preferredName = preferredName,
        personalCircumstances = personalCircumstances.map{it.toPersonalCircumstance()},
        disabilities = disabilities.map{it.toDisability()},
        provisions = provisions.map{it.toProvision()},
    )
    fun Contact.toNextAppointment() = NextAppointment(description = type.description, date = date)
    fun <T> Collection<T>.isSingle(): Boolean = size == 1
}
