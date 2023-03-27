package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Overview
import uk.gov.justice.digital.hmpps.api.model.PersonalDetails
import uk.gov.justice.digital.hmpps.api.model.dates
import uk.gov.justice.digital.hmpps.api.model.identifiers
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.toAddress
import uk.gov.justice.digital.hmpps.api.model.toConviction
import uk.gov.justice.digital.hmpps.api.model.toManager
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryPersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryPersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryRegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryReleaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Person
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.findMainAddress
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.getPerson

@Service
class CaseSummaryService(
    private val personRepository: CaseSummaryPersonRepository,
    private val addressRepository: CaseSummaryAddressRepository,
    private val personManagerRepository: CaseSummaryPersonManagerRepository,
    private val registrationRepository: CaseSummaryRegistrationRepository,
    private val releaseRepository: CaseSummaryReleaseRepository,
    private val eventRepository: CaseSummaryEventRepository
) {
    fun getPersonalDetails(crn: String) = getPersonalDetails(personRepository.getPerson(crn))

    fun getPersonalDetails(person: Person): PersonalDetails {
        val mainAddress = addressRepository.findMainAddress(person.id)
        return PersonalDetails(
            name = person.name(),
            identifiers = person.identifiers(),
            dateOfBirth = person.dateOfBirth,
            gender = person.gender.description,
            ethnicity = person.ethnicity?.description,
            primaryLanguage = person.primaryLanguage?.description,
            mainAddress = mainAddress?.toAddress()
        )
    }

    fun getOverview(crn: String): Overview {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetails(person)
        val personManager = personManagerRepository.findByPersonId(person.id)
        val registerFlags = registrationRepository.findTypeDescriptionsByPersonId(person.id)
        val events = eventRepository.findByPersonId(person.id)
        val lastRelease = releaseRepository.findFirstByCustodyIdOrderByDateDesc(events.singleCustody(crn).id)
        return Overview(
            personalDetails = personalDetails,
            communityManager = personManager?.toManager(),
            registerFlags = registerFlags,
            lastRelease = lastRelease?.dates(),
            activeConvictions = events.map { it.toConviction() }
        )
    }

    private fun List<Event>.singleCustody(crn: String): Custody {
        val custodies = mapNotNull { it.disposal?.custody }
        if (custodies.size > 1) throw IllegalStateException("Multiple active custodial events for $crn")
        if (custodies.isEmpty()) throw IllegalStateException("No active custodial events for $crn")
        return custodies.single()
    }
}
