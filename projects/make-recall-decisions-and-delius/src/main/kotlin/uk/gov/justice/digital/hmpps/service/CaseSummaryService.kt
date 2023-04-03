package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.LicenceConditions
import uk.gov.justice.digital.hmpps.api.model.MappaAndRoshHistory
import uk.gov.justice.digital.hmpps.api.model.Overview
import uk.gov.justice.digital.hmpps.api.model.PersonalDetails
import uk.gov.justice.digital.hmpps.api.model.PersonalDetailsOverview
import uk.gov.justice.digital.hmpps.api.model.dates
import uk.gov.justice.digital.hmpps.api.model.identifiers
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.toAddress
import uk.gov.justice.digital.hmpps.api.model.toConviction
import uk.gov.justice.digital.hmpps.api.model.toConvictionWithLicenceConditions
import uk.gov.justice.digital.hmpps.api.model.toManager
import uk.gov.justice.digital.hmpps.api.model.toMappa
import uk.gov.justice.digital.hmpps.api.model.toRosh
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryPersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryPersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryRegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryReleaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Person
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.findMainAddress
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.findMappa
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.findRoshHistory
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
    fun getPersonalDetailsOverview(person: Person) = PersonalDetailsOverview(
        name = person.name(),
        identifiers = person.identifiers(),
        dateOfBirth = person.dateOfBirth,
        gender = person.gender.description,
        ethnicity = person.ethnicity?.description,
        primaryLanguage = person.primaryLanguage?.description
    )

    fun getPersonalDetails(crn: String): PersonalDetails {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetailsOverview(person)
        val mainAddress = addressRepository.findMainAddress(person.id)
        val personManager = personManagerRepository.findByPersonId(person.id)
        return PersonalDetails(
            personalDetails = personalDetails,
            mainAddress = mainAddress?.toAddress(),
            communityManager = personManager?.toManager()
        )
    }

    fun getOverview(crn: String): Overview {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetailsOverview(person)
        val registerFlags = registrationRepository.findActiveTypeDescriptionsByPersonId(person.id)
        val events = eventRepository.findByPersonId(person.id)
        val lastRelease = events.singleCustody()?.let { releaseRepository.findFirstByCustodyIdOrderByDateDesc(it.id) }
        return Overview(
            personalDetails = personalDetails,
            registerFlags = registerFlags,
            lastRelease = lastRelease?.dates(),
            activeConvictions = events.map { it.toConviction() }
        )
    }

    fun getMappaAndRoshHistory(crn: String): MappaAndRoshHistory {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetailsOverview(person)
        val mappa = registrationRepository.findMappa(person.id)?.toMappa()
        val roshHistory = registrationRepository.findRoshHistory(person.id).map { it.toRosh() }
        return MappaAndRoshHistory(
            personalDetails = personalDetails,
            mappa = mappa,
            roshHistory = roshHistory
        )
    }

    fun getLicenceConditions(crn: String): LicenceConditions {
        val person = personRepository.getPerson(crn)
        val personalDetails = getPersonalDetailsOverview(person)
        val events = eventRepository.findByPersonId(person.id)
        return LicenceConditions(
            personalDetails = personalDetails,
            activeConvictions = events.map { it.toConvictionWithLicenceConditions() }
        )
    }

    private fun List<Event>.singleCustody() = mapNotNull { it.disposal?.custody }.singleOrNull()
}
